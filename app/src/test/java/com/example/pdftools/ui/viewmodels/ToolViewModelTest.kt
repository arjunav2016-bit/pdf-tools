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
}
