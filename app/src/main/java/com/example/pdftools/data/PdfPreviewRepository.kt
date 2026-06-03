package com.example.pdftools.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.LruCache
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.rendering.ImageType
import com.tom_roush.pdfbox.rendering.PDFRenderer
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class PdfPreviewRepository @Inject constructor() {
    private val bitmapCache = object : LruCache<String, Bitmap>(16 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    suspend fun getPageCount(context: Context, uri: Uri): Int = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            PDDocument.load(input).use { doc -> doc.numberOfPages }
        } ?: throw IllegalArgumentException("Unable to open PDF preview.")
    }

    suspend fun renderPage(context: Context, uri: Uri, pageIndex: Int, width: Int): Bitmap {
        val targetWidth = width.coerceAtLeast(1)
        val cacheKey = "$uri#$pageIndex@$targetWidth"
        synchronized(bitmapCache) {
            bitmapCache.get(cacheKey)?.let { return it }
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
                    synchronized(bitmapCache) {
                        bitmapCache.put(cacheKey, bitmap)
                    }
                    bitmap
                }
            } ?: throw IllegalArgumentException("Unable to render PDF preview.")
        }
    }

    suspend fun getPageSize(context: Context, uri: Uri, pageIndex: Int): Pair<Float, Float> = withContext(Dispatchers.IO) {
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

    fun evictAll() {
        synchronized(bitmapCache) {
            bitmapCache.evictAll()
        }
    }
}
