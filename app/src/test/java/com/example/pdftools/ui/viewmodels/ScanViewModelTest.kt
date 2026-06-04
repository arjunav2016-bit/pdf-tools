package com.example.pdftools.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.pdftools.data.PdfProcessor
import com.example.pdftools.data.RecentFilesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ScanViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var context: Context
    private lateinit var pdfProcessor: PdfProcessor
    private lateinit var recentFilesRepository: RecentFilesRepository
    private lateinit var viewModel: ScanViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        pdfProcessor = mock()
        recentFilesRepository = mock()
        viewModel = ScanViewModel(pdfProcessor, recentFilesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addPagesGuardsAgainstProcessingState() = runTest {
        viewModel.addPages(listOf(Uri.parse("file://page1.jpg")))
        assertEquals(ScanFlowState.Review, viewModel.flowState.value)
        assertEquals(1, viewModel.scannedPages.value.size)

        val mockUri = Uri.parse("file://output.pdf")
        doReturn(mockUri).`when`(pdfProcessor).scanToPdf(
            any(), any(), any(), any(), any(), any(), any()
        )

        viewModel.generatePdf(context)
        
        assertTrue(viewModel.flowState.value is ScanFlowState.Processing)

        viewModel.addPages(listOf(Uri.parse("file://page2.jpg")))
        assertEquals(1, viewModel.scannedPages.value.size)
        
        advanceUntilIdle()
        assertTrue(viewModel.flowState.value is ScanFlowState.Success)
    }

    @Test
    fun cancelProcessingCancelsJobAndReturnsToReview() = runTest {
        viewModel.addPages(listOf(Uri.parse("file://page1.jpg")))
        
        val mockUri = Uri.parse("file://output.pdf")
        doReturn(mockUri).`when`(pdfProcessor).scanToPdf(
            any(), any(), any(), any(), any(), any(), any()
        )

        viewModel.generatePdf(context)
        assertTrue(viewModel.flowState.value is ScanFlowState.Processing)

        viewModel.cancelProcessing()
        advanceUntilIdle()

        assertEquals(ScanFlowState.Review, viewModel.flowState.value)
    }

    @Test
    fun dismissErrorWithPagesReturnsToReview() = runTest {
        viewModel.addPages(listOf(Uri.parse("file://page1.jpg")))
        
        doAnswer { throw RuntimeException("Simulated error") }.`when`(pdfProcessor).scanToPdf(
            any(), any(), any(), any(), any(), any(), any()
        )

        viewModel.generatePdf(context)
        advanceUntilIdle()

        assertTrue(viewModel.flowState.value is ScanFlowState.Error)
        assertEquals("Simulated error", (viewModel.flowState.value as ScanFlowState.Error).message)

        viewModel.dismissError()
        assertEquals(ScanFlowState.Review, viewModel.flowState.value)
    }

    @Test
    fun dismissErrorWithoutPagesReturnsToLauncher() = runTest {
        viewModel.dismissError()
        assertEquals(ScanFlowState.Launcher, viewModel.flowState.value)
    }
}

