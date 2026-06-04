package com.example.pdftools.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PdfPreviewRepositoryTest {
    private lateinit var context: Context
    private lateinit var repository: PdfPreviewRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        PDFBoxResourceLoader.init(context)
        repository = PdfPreviewRepository()
        // Ensure thumbnail cache is clean at start of each test
        File(context.cacheDir, "pdf_thumbnails").deleteRecursively()
    }

    @Test
    fun getPageCountReadsGeneratedPdf() = runTest {
        val pdf = createPdf(pageCount = 2)
        try {
            assertEquals(2, repository.getPageCount(context, Uri.fromFile(pdf)))
        } finally {
            pdf.delete()
        }
    }

    @Test
    fun renderPageReturnsSizedBitmapAndCachesIt() = runTest {
        val pdf = createPdf(pageCount = 1)
        try {
            val uri = Uri.fromFile(pdf)
            val first = repository.renderPage(context, uri, pageIndex = 0, width = 120)
            val second = repository.renderPage(context, uri, pageIndex = 0, width = 120)

            assertEquals(120, first.width)
            assertTrue(first.height > first.width)
            assertSame(first, second)
        } finally {
            pdf.delete()
        }
    }

    @Test
    fun renderPageRejectsOutOfRangePage() = runTest {
        val pdf = createPdf(pageCount = 1)
        try {
            repository.renderPage(context, Uri.fromFile(pdf), pageIndex = 4, width = 120)
            fail("Expected an out-of-range page to throw.")
        } catch (error: IllegalArgumentException) {
            assertTrue(error.message.orEmpty().contains("outside this PDF"))
        } finally {
            pdf.delete()
        }
    }

    @Test
    fun evictAllClearsCache() = runTest {
        val pdf = createPdf(pageCount = 1)
        try {
            val uri = Uri.fromFile(pdf)
            val first = repository.renderPage(context, uri, pageIndex = 0, width = 120)

            repository.evictAll()

            val second = repository.renderPage(context, uri, pageIndex = 0, width = 120)

            assertEquals(120, first.width)
            assertEquals(120, second.width)
            org.junit.Assert.assertNotSame(first, second)
        } finally {
            pdf.delete()
        }
    }

    @Test
    fun getPageCountAndPageSizeAreCached() = runTest {
        val pdf = createPdf(pageCount = 3)
        try {
            val uri = Uri.fromFile(pdf)
            val count1 = repository.getPageCount(context, uri)
            val size1 = repository.getPageSize(context, uri, 0)
            assertEquals(3, count1)
            assertEquals(200f to 400f, size1)

            // Modify the cached value in the private maps using reflection
            val pageCountCacheField = PdfPreviewRepository::class.java.getDeclaredField("pageCountCache")
            pageCountCacheField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val pageCountCache = pageCountCacheField.get(repository) as MutableMap<String, Int>
            val countKey = pageCountCache.keys.first { it.contains(uri.toString()) }
            pageCountCache[countKey] = 99

            val pageSizeCacheField = PdfPreviewRepository::class.java.getDeclaredField("pageSizeCache")
            pageSizeCacheField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val pageSizeCache = pageSizeCacheField.get(repository) as MutableMap<String, Pair<Float, Float>>
            val sizeKey = pageSizeCache.keys.first { it.contains(uri.toString()) }
            pageSizeCache[sizeKey] = 999f to 999f

            // Read again and verify it returns the modified fake values, proving cache hit
            val count2 = repository.getPageCount(context, uri)
            val size2 = repository.getPageSize(context, uri, 0)
            assertEquals(99, count2)
            assertEquals(999f to 999f, size2)
        } finally {
            if (pdf.exists()) pdf.delete()
        }
    }

    @Test
    fun cacheInvalidatedWhenFileModified() = runTest {
        val pdf = createPdf(pageCount = 2)
        try {
            val uri = Uri.fromFile(pdf)
            val count1 = repository.getPageCount(context, uri)
            assertEquals(2, count1)

            // Overwrite with a different page count (which changes file size)
            pdf.delete()
            PDDocument().use { doc ->
                repeat(4) {
                    doc.addPage(PDPage(PDRectangle(200f, 400f)))
                }
                doc.save(pdf)
            }

            val count2 = repository.getPageCount(context, uri)
            assertEquals(4, count2)
        } finally {
            if (pdf.exists()) pdf.delete()
        }
    }

    @Test
    fun evictAllClearsMetadataCache() = runTest {
        val pdf = createPdf(pageCount = 2)
        try {
            val uri = Uri.fromFile(pdf)
            repository.getPageCount(context, uri)

            repository.evictAll()

            pdf.delete()
            try {
                repository.getPageCount(context, uri)
                fail("Expected getPageCount to throw because file is deleted and cache is cleared.")
            } catch (e: Exception) {
                // Expected
            }
        } finally {
            if (pdf.exists()) pdf.delete()
        }
    }

    @Test
    fun diskCacheLoadedWhenMemoryEvicted() = runTest {
        val pdf = createPdf(pageCount = 1)
        try {
            val uri = Uri.fromFile(pdf)

            // 1. Render first time (generates in-memory and disk cache)
            val first = repository.renderPage(context, uri, pageIndex = 0, width = 120)
            assertTrue(first.width > 1)

            // Get cache file path
            val thumbnailDir = File(context.cacheDir, "pdf_thumbnails")
            val files = thumbnailDir.listFiles()?.map { it.name } ?: emptyList()
            assertTrue(files.isNotEmpty())
            val cacheFile = File(thumbnailDir, files.first())

            // 2. Evict memory cache
            repository.evictAll()

            // 3. Replace the disk cache file with a 1x1 dummy bitmap
            val dummyBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            FileOutputStream(cacheFile).use { out ->
                dummyBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            // 4. Render again and assert it returns the 1x1 dummy bitmap from disk
            val second = repository.renderPage(context, uri, pageIndex = 0, width = 120)
            assertEquals(1, second.width)
            assertEquals(1, second.height)
        } finally {
            if (pdf.exists()) pdf.delete()
            File(context.cacheDir, "pdf_thumbnails").deleteRecursively()
        }
    }

    private fun createPdf(pageCount: Int): File {
        val file = File(context.cacheDir, "preview_${System.nanoTime()}.pdf")
        PDDocument().use { doc ->
            repeat(pageCount) {
                doc.addPage(PDPage(PDRectangle(200f, 400f)))
            }
            doc.save(file)
        }
        return file
    }
}
