package com.example.pdftools.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.pdftools.MainDispatcherRule
import com.example.pdftools.data.FavoritesRepository
import com.example.pdftools.data.PdfPreviewRepository
import com.example.pdftools.data.PdfProcessor
import com.example.pdftools.data.RecentFilesRepository
import com.example.pdftools.data.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ToolViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private lateinit var context: Context
    private lateinit var pdfProcessor: PdfProcessor
    private lateinit var previewRepository: PdfPreviewRepository
    private lateinit var preferencesRepository: UserPreferencesRepository
    private lateinit var favoritesRepository: FavoritesRepository
    private lateinit var recentFilesRepository: RecentFilesRepository
    private lateinit var viewModel: ToolViewModel

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        pdfProcessor = mock()
        previewRepository = mock()
        preferencesRepository = mock()
        favoritesRepository = mock()
        recentFilesRepository = mock()
        viewModel = ToolViewModel(
            pdfProcessor,
            previewRepository,
            preferencesRepository,
            favoritesRepository,
            recentFilesRepository
        )
    }

    @Test
    fun addRemoveAndResetFilesUpdateState() {
        val first = Uri.parse("file:///tmp/first.pdf")
        val second = Uri.parse("file:///tmp/second.pdf")

        viewModel.addFiles(listOf(first, second))
        assertEquals(listOf(first, second), viewModel.selectedFiles.value)

        viewModel.removeFile(0)
        assertEquals(listOf(second), viewModel.selectedFiles.value)

        viewModel.watermarkConfig.value = WatermarkConfig(text = "Draft")
        viewModel.reset()

        assertEquals(emptyList<Uri>(), viewModel.selectedFiles.value)
        assertEquals(ToolUiState.Idle, viewModel.uiState.value)
        assertEquals(WatermarkConfig(), viewModel.watermarkConfig.value)
    }

    @Test
    fun processDispatchesAndReportsSuccess() = runTest {
        val input = Uri.parse("file:///tmp/input.pdf")
        val output = Uri.parse("file:///tmp/output.pdf")
        viewModel.addFiles(listOf(input))
        whenever(pdfProcessor.repairPdf(context, input)).thenReturn(output)

        viewModel.process("repair_pdf", context)
        assertTrue(viewModel.uiState.value is ToolUiState.Processing)

        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(ToolUiState.Success(listOf(output)), viewModel.uiState.value)
        assertEquals(listOf(output), viewModel.outputUris.value)
        verify(pdfProcessor).repairPdf(context, input)
        verify(recentFilesRepository).addRecent("output.pdf", "repair_pdf", output.toString())
    }

    @Test
    fun processWithoutFilesReportsError() {
        viewModel.process("repair_pdf", context)

        assertEquals(ToolUiState.Error("No files selected"), viewModel.uiState.value)
    }

    @Test
    fun cancelProcessingReturnsToIdleBeforeWorkRuns() {
        viewModel.addFiles(listOf(Uri.parse("file:///tmp/input.pdf")))

        viewModel.process("repair_pdf", context)
        assertTrue(viewModel.uiState.value is ToolUiState.Processing)

        viewModel.cancelProcessing()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(ToolUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun favoritesDelegateToRepository() {
        whenever(favoritesRepository.isFavorite("repair_pdf")).thenReturn(true)

        assertTrue(viewModel.isFavorite("repair_pdf"))
        viewModel.toggleFavorite("repair_pdf")

        verify(favoritesRepository).toggleFavorite("repair_pdf")
    }

    @Test
    fun compressConfigResetsToDefault() {
        viewModel.compressConfig.value = CompressConfig(tier = CompressTier.EXTREME)
        viewModel.reset()
        assertEquals(CompressConfig(), viewModel.compressConfig.value)
    }

    @Test
    fun processCompressPdfMapsQualityCorrectly() = runTest {
        val input = Uri.parse("file:///tmp/input.pdf")
        val output = Uri.parse("file:///tmp/output.pdf")
        viewModel.addFiles(listOf(input))

        whenever(pdfProcessor.compressPdf(
            context = org.mockito.kotlin.eq(context),
            uri = org.mockito.kotlin.eq(input),
            quality = org.mockito.kotlin.eq(40),
            onProgress = org.mockito.kotlin.anyOrNull()
        )).thenReturn(output)

        viewModel.compressConfig.value = CompressConfig(tier = CompressTier.EXTREME)
        viewModel.process("compress_pdf", context)

        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(ToolUiState.Success(listOf(output)), viewModel.uiState.value)
        verify(pdfProcessor).compressPdf(
            context = org.mockito.kotlin.eq(context),
            uri = org.mockito.kotlin.eq(input),
            quality = org.mockito.kotlin.eq(40),
            onProgress = org.mockito.kotlin.anyOrNull()
        )
    }

    @Test
    fun updateSelectedFilesModifiesList() {
        val first = Uri.parse("file:///tmp/first.pdf")
        val second = Uri.parse("file:///tmp/second.pdf")
        viewModel.addFiles(listOf(first, second))

        viewModel.updateSelectedFiles(listOf(second, first))

        assertEquals(listOf(second, first), viewModel.selectedFiles.value)
    }

    @Test
    fun getPageCountSuspendReturnsResolvedCount() = runTest {
        val input = Uri.parse("file:///tmp/input.pdf")
        whenever(previewRepository.getPageCount(context, input)).thenReturn(12)

        val count = viewModel.getPageCountSuspend(context, input)

        assertEquals(12, count)
        verify(previewRepository).getPageCount(context, input)
    }

    @Test
    fun pdfToImageConfigResetsToDefault() {
        viewModel.pdfToImageConfig.value = PdfToImageConfig(
            format = "png",
            quality = 90,
            dpi = 300,
            pageSelection = "custom",
            customPageRange = "1-2"
        )
        viewModel.reset()
        assertEquals(PdfToImageConfig(), viewModel.pdfToImageConfig.value)
    }

    @Test
    fun processPdfToJpgMapsParametersCorrectly() = runTest {
        val input = Uri.parse("file:///tmp/input.pdf")
        val outputs = listOf(Uri.parse("file:///tmp/output_1.jpg"), Uri.parse("file:///tmp/output_2.jpg"))
        viewModel.addFiles(listOf(input))

        whenever(pdfProcessor.convertPdfToImages(
            context = org.mockito.kotlin.eq(context),
            uri = org.mockito.kotlin.eq(input),
            dpi = org.mockito.kotlin.eq(300),
            format = org.mockito.kotlin.eq("png"),
            quality = org.mockito.kotlin.eq(95),
            pageSelection = org.mockito.kotlin.eq("custom"),
            customPageRange = org.mockito.kotlin.eq("1-3"),
            onProgress = org.mockito.kotlin.anyOrNull()
        )).thenReturn(outputs)

        viewModel.pdfToImageConfig.value = PdfToImageConfig(
            format = "png",
            quality = 95,
            dpi = 300,
            pageSelection = "custom",
            customPageRange = "1-3"
        )
        viewModel.process("pdf_to_jpg", context)

        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(ToolUiState.Success(outputs), viewModel.uiState.value)
        verify(pdfProcessor).convertPdfToImages(
            context = org.mockito.kotlin.eq(context),
            uri = org.mockito.kotlin.eq(input),
            dpi = org.mockito.kotlin.eq(300),
            format = org.mockito.kotlin.eq("png"),
            quality = org.mockito.kotlin.eq(95),
            pageSelection = org.mockito.kotlin.eq("custom"),
            customPageRange = org.mockito.kotlin.eq("1-3"),
            onProgress = org.mockito.kotlin.anyOrNull()
        )
    }

    @Test
    fun pdfToPptConfigResetsToDefault() {
        viewModel.pdfToPptConfig.value = PdfToPptConfig(
            slidesPerPage = 4,
            includeNotes = true,
            runOcr = false,
            exportFormat = "otp"
        )
        viewModel.reset()
        assertEquals(PdfToPptConfig(), viewModel.pdfToPptConfig.value)
    }

    @Test
    fun processPdfToPptMapsParametersCorrectly() = runTest {
        val input = Uri.parse("file:///tmp/input.pdf")
        val output = Uri.parse("file:///tmp/output.pptx")
        viewModel.addFiles(listOf(input))

        whenever(pdfProcessor.convertPdfToPpt(
            context = org.mockito.kotlin.eq(context),
            uri = org.mockito.kotlin.eq(input),
            slidesPerPage = org.mockito.kotlin.eq(4),
            includeNotes = org.mockito.kotlin.eq(true),
            runOcr = org.mockito.kotlin.eq(false),
            exportFormat = org.mockito.kotlin.eq("otp")
        )).thenReturn(output)

        viewModel.pdfToPptConfig.value = PdfToPptConfig(
            slidesPerPage = 4,
            includeNotes = true,
            runOcr = false,
            exportFormat = "otp"
        )
        viewModel.process("pdf_to_ppt", context)

        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(ToolUiState.Success(listOf(output)), viewModel.uiState.value)
        verify(pdfProcessor).convertPdfToPpt(
            context = org.mockito.kotlin.eq(context),
            uri = org.mockito.kotlin.eq(input),
            slidesPerPage = org.mockito.kotlin.eq(4),
            includeNotes = org.mockito.kotlin.eq(true),
            runOcr = org.mockito.kotlin.eq(false),
            exportFormat = org.mockito.kotlin.eq("otp")
        )
    }
}


