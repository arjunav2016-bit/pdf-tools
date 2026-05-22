package com.example.pdftools.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.LruCache
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
        context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
            PdfRenderer(descriptor).use { renderer -> renderer.pageCount }
        } ?: throw IllegalArgumentException("Unable to open PDF preview.")
    }

    suspend fun renderPage(context: Context, uri: Uri, pageIndex: Int, width: Int): Bitmap {
        val targetWidth = width.coerceAtLeast(1)
        val cacheKey = "$uri#$pageIndex@$targetWidth"
        synchronized(bitmapCache) {
            bitmapCache.get(cacheKey)?.let { return it }
        }

        return withContext(Dispatchers.IO) {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    require(pageIndex in 0 until renderer.pageCount) {
                        "Page ${pageIndex + 1} is outside this PDF."
                    }
                    renderer.openPage(pageIndex).use { page ->
                        val height = (targetWidth * (page.height.toFloat() / page.width))
                            .toInt()
                            .coerceAtLeast(1)
                        val bitmap = Bitmap.createBitmap(
                            targetWidth,
                            height,
                            Bitmap.Config.ARGB_8888
                        )
                        bitmap.eraseColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        synchronized(bitmapCache) {
                            bitmapCache.put(cacheKey, bitmap)
                        }
                        bitmap
                    }
                }
            } ?: throw IllegalArgumentException("Unable to render PDF preview.")
        }
    }
}
