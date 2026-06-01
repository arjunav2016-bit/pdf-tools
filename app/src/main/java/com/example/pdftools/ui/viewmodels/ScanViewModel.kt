package com.example.pdftools.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdftools.data.PdfProcessor
import com.example.pdftools.data.RecentFilesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Represents a single scanned page with its URI and per-page rotation.
 */
data class ScannedPage(
    val uri: Uri,
    val rotation: Int = 0
)

/**
 * Output settings for the generated PDF.
 */
data class ScanOutputSettings(
    val pageSize: String = "auto", // "auto", "a4", "letter"
    val quality: Int = 85,         // 50–100 (JPEG compression quality)
    val dpi: Int = 200             // 150, 200, or 300
)

/**
 * State machine for the scan flow.
 */
sealed interface ScanFlowState {
    data object Launcher : ScanFlowState
    data object Review : ScanFlowState
    data class Processing(
        val progress: Float? = null,
        val message: String = "Generating PDF…"
    ) : ScanFlowState
    data class Success(val outputUri: Uri) : ScanFlowState
    data class Error(val message: String) : ScanFlowState
}

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val pdfProcessor: PdfProcessor,
    private val recentFilesRepository: RecentFilesRepository
) : ViewModel() {

    // Flow state machine
    private val _flowState = MutableStateFlow<ScanFlowState>(ScanFlowState.Launcher)
    val flowState: StateFlow<ScanFlowState> = _flowState.asStateFlow()

    // Scanned pages
    private val _scannedPages = MutableStateFlow<List<ScannedPage>>(emptyList())
    val scannedPages: StateFlow<List<ScannedPage>> = _scannedPages.asStateFlow()

    // Global filter applied to all pages
    private val _filter = MutableStateFlow("color") // "color", "Grayscale", "B&W Binarization"
    val filter: StateFlow<String> = _filter.asStateFlow()

    // Output settings
    private val _outputSettings = MutableStateFlow(ScanOutputSettings())
    val outputSettings: StateFlow<ScanOutputSettings> = _outputSettings.asStateFlow()

    // Output URI after successful generation
    private val _outputUri = MutableStateFlow<Uri?>(null)
    val outputUri: StateFlow<Uri?> = _outputUri.asStateFlow()

    private var processingJob: Job? = null

    // --- Page Management ---

    fun addPages(uris: List<Uri>) {
        val newPages = uris.map { ScannedPage(uri = it) }
        _scannedPages.value = _scannedPages.value + newPages
        if (_scannedPages.value.isNotEmpty()) {
            _flowState.value = ScanFlowState.Review
        }
    }

    fun removePage(index: Int) {
        val current = _scannedPages.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _scannedPages.value = current
            if (current.isEmpty()) {
                _flowState.value = ScanFlowState.Launcher
            }
        }
    }

    fun movePage(fromIndex: Int, toIndex: Int) {
        val current = _scannedPages.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices && fromIndex != toIndex) {
            val page = current.removeAt(fromIndex)
            current.add(toIndex, page)
            _scannedPages.value = current
        }
    }

    fun rotatePage(index: Int) {
        val current = _scannedPages.value.toMutableList()
        if (index in current.indices) {
            val page = current[index]
            current[index] = page.copy(rotation = (page.rotation + 90) % 360)
            _scannedPages.value = current
        }
    }

    // --- Filter & Settings ---

    fun setFilter(newFilter: String) {
        _filter.value = newFilter
    }

    fun updatePageSize(pageSize: String) {
        _outputSettings.value = _outputSettings.value.copy(pageSize = pageSize)
    }

    fun updateQuality(quality: Int) {
        _outputSettings.value = _outputSettings.value.copy(quality = quality.coerceIn(50, 100))
    }

    fun updateDpi(dpi: Int) {
        _outputSettings.value = _outputSettings.value.copy(dpi = dpi)
    }

    // --- Navigation ---

    fun goToReview() {
        if (_scannedPages.value.isNotEmpty()) {
            _flowState.value = ScanFlowState.Review
        }
    }

    fun goToLauncher() {
        _flowState.value = ScanFlowState.Launcher
    }

    fun dismissError() {
        _flowState.value = if (_scannedPages.value.isNotEmpty()) {
            ScanFlowState.Review
        } else {
            ScanFlowState.Launcher
        }
    }

    // --- PDF Generation ---

    fun generatePdf(context: Context) {
        val pages = _scannedPages.value
        if (pages.isEmpty()) return

        _flowState.value = ScanFlowState.Processing(message = "Preparing scan…")
        processingJob = viewModelScope.launch {
            try {
                val uris = pages.map { it.uri }
                val rotations = pages.map { it.rotation }
                val settings = _outputSettings.value

                val result = pdfProcessor.scanToPdf(
                    context = context,
                    imageUris = uris,
                    rotations = rotations,
                    filter = _filter.value,
                    pageSize = settings.pageSize,
                    quality = settings.quality,
                    onProgress = { progress ->
                        val normalized = progress.coerceIn(0f, 1f)
                        _flowState.value = ScanFlowState.Processing(
                            progress = normalized,
                            message = "Generating PDF ${(normalized * 100).toInt()}%"
                        )
                    }
                )

                // Record in recent files
                val fileName = getFileName(context, result)
                recentFilesRepository.addRecent(fileName, "scan_to_pdf", result.toString())

                _outputUri.value = result
                _flowState.value = ScanFlowState.Success(result)
            } catch (e: CancellationException) {
                _flowState.value = ScanFlowState.Review
            } catch (e: Exception) {
                _flowState.value = ScanFlowState.Error(
                    e.message ?: "An error occurred while generating the PDF"
                )
            } finally {
                processingJob = null
            }
        }
    }

    fun cancelProcessing() {
        processingJob?.cancel()
        processingJob = null
        _flowState.value = ScanFlowState.Review
    }

    // --- Reset ---

    fun reset() {
        processingJob?.cancel()
        processingJob = null
        _flowState.value = ScanFlowState.Launcher
        _scannedPages.value = emptyList()
        _filter.value = "color"
        _outputSettings.value = ScanOutputSettings()
        _outputUri.value = null
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        result = cursor.getString(index)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "scanned.pdf"
    }
}
