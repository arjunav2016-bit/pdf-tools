package com.example.pdftools.data

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import java.io.File
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
