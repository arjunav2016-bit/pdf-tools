package com.example.pdftools.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.rendering.ImageType
import com.tom_roush.pdfbox.rendering.PDFRenderer
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

@Singleton
class PdfPreviewRepository @Inject constructor() {
    private val bitmapCache = object : LruCache<String, Bitmap>(16 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    private val pageCountCache = ConcurrentHashMap<String, Int>()
    private val pageSizeCache = ConcurrentHashMap<String, Pair<Float, Float>>()

    private fun getFileSize(context: Context, uri: Uri): Long {
        var size = 0L
        try {
            if (uri.scheme == "content") {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                        if (index >= 0) {
                            size = cursor.getLong(index)
                        }
                    }
                }
            }
            if (size == 0L) {
                val path = uri.path
                if (path != null) {
                    val file = File(path)
                    if (file.exists()) {
                        size = file.length()
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore query failures, default to 0
        }
        return size
    }

    private fun getCacheKey(context: Context, uri: Uri): String {
        val size = getFileSize(context, uri)
        return "$uri#$size"
    }

    private fun getCacheFileName(cacheKey: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(cacheKey.toByteArray(Charsets.UTF_8))
            digest.joinToString("") { "%02x".format(it) } + ".jpg"
        } catch (e: Exception) {
            cacheKey.replace(Regex("[^a-zA-Z0-9_]"), "_") + ".jpg"
        }
    }

    suspend fun getPageCount(context: Context, uri: Uri): Int {
        val cacheKey = getCacheKey(context, uri)
        pageCountCache[cacheKey]?.let { return it }

        val count = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { input ->
                PDDocument.load(input).use { doc -> doc.numberOfPages }
            } ?: throw IllegalArgumentException("Unable to open PDF preview.")
        }

        pageCountCache[cacheKey] = count
        return count
    }

    suspend fun renderPage(context: Context, uri: Uri, pageIndex: Int, width: Int): Bitmap {
        val targetWidth = width.coerceAtLeast(1)
        val fileKey = getCacheKey(context, uri)
        val cacheKey = "$fileKey#$pageIndex@$targetWidth"

        synchronized(bitmapCache) {
            bitmapCache.get(cacheKey)?.let { return it }
        }

        val thumbnailDir = File(context.cacheDir, "pdf_thumbnails")
        val cacheFileName = getCacheFileName(cacheKey)
        val cacheFile = File(thumbnailDir, cacheFileName)

        if (cacheFile.exists()) {
            val bitmap = withContext(Dispatchers.IO) {
                runCatching {
                    BitmapFactory.decodeFile(cacheFile.absolutePath)
                }.getOrNull()
            }
            if (bitmap != null) {
                synchronized(bitmapCache) {
                    bitmapCache.put(cacheKey, bitmap)
                }
                return bitmap
            }
        }

        return withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { input ->
                PDDocument.load(input).use { doc ->
                    require(pageIndex in 0 until doc.numberOfPages) {
                        "Page ${pageIndex + 1} is outside this PDF."
                    }
                    val pageWidth = doc.getPage(pageIndex).cropBox.width.coerceAtLeast(1f)
                    val scale = targetWidth / pageWidth
                    val bitmap = PDFRenderer(doc).renderImage(pageIndex, scale, ImageType.ARGB)

                    runCatching {
                        if (!thumbnailDir.exists()) {
                            thumbnailDir.mkdirs()
                        }
                        FileOutputStream(cacheFile).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                        }
                    }

                    synchronized(bitmapCache) {
                        bitmapCache.put(cacheKey, bitmap)
                    }
                    bitmap
                }
            } ?: throw IllegalArgumentException("Unable to render PDF preview.")
        }
    }

    suspend fun getPageSize(context: Context, uri: Uri, pageIndex: Int): Pair<Float, Float> {
        val fileKey = getCacheKey(context, uri)
        val cacheKey = "$fileKey#$pageIndex"
        pageSizeCache[cacheKey]?.let { return it }

        val size = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { input ->
                PDDocument.load(input).use { doc ->
                    if (pageIndex in 0 until doc.numberOfPages) {
                        val box = doc.getPage(pageIndex).cropBox ?: doc.getPage(pageIndex).mediaBox
                        box.width to box.height
                    } else {
                        0f to 0f
                    }
                }
            } ?: (0f to 0f)
        }

        if (size.first > 0 && size.second > 0) {
            pageSizeCache[cacheKey] = size
        }
        return size
    }

    fun evictAll() {
        synchronized(bitmapCache) {
            bitmapCache.evictAll()
        }
        pageCountCache.clear()
        pageSizeCache.clear()
    }
}
