package com.example.pdftools.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdftools.data.PdfProcessor
import com.example.pdftools.data.FavoritesRepository
import com.example.pdftools.data.RecentFilesRepository
import com.example.pdftools.data.FormFieldInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ToolUiState {
    data object Idle : ToolUiState
    data object Processing : ToolUiState
    data class Success(val outputUris: List<Uri>) : ToolUiState
    data class Error(val message: String) : ToolUiState
}

@HiltViewModel
class ToolViewModel @Inject constructor(
    private val pdfProcessor: PdfProcessor,
    private val favoritesRepository: FavoritesRepository,
    private val recentFilesRepository: RecentFilesRepository
) : ViewModel() {

    // Core state
    private val _uiState = MutableStateFlow<ToolUiState>(ToolUiState.Idle)
    val uiState: StateFlow<ToolUiState> = _uiState.asStateFlow()

    private val _selectedFiles = MutableStateFlow<List<Uri>>(emptyList())
    val selectedFiles: StateFlow<List<Uri>> = _selectedFiles.asStateFlow()

    private val _outputUris = MutableStateFlow<List<Uri>>(emptyList())
    val outputUris: StateFlow<List<Uri>> = _outputUris.asStateFlow()

    // Per-tool config state
    val pageRangeConfig = MutableStateFlow(PageRangeConfig())
    val passwordConfig = MutableStateFlow(PasswordConfig())
    val watermarkConfig = MutableStateFlow(WatermarkConfig())
    val pageNumberConfig = MutableStateFlow(PageNumberConfig())
    val rotateConfig = MutableStateFlow(RotateConfig())
    val cropConfig = MutableStateFlow(CropConfig())
    val organizeConfig = MutableStateFlow(OrganizeConfig())
    val signConfig = MutableStateFlow(SignConfig())
    val redactConfig = MutableStateFlow(RedactConfig())
    val formConfig = MutableStateFlow(FormConfig())
    val scanConfig = MutableStateFlow(ScanConfig())
    val editConfig = MutableStateFlow(EditConfig())
    val htmlConfig = MutableStateFlow(HtmlConfig())
    val ocrConfig = MutableStateFlow(OcrConfig())
    val compareConfig = MutableStateFlow(CompareConfig())
    val pdfaConfig = MutableStateFlow(PdfaConfig())

    // Actions
    fun addFiles(uris: List<Uri>) {
        _selectedFiles.value = _selectedFiles.value + uris
    }

    fun removeFile(index: Int) {
        val current = _selectedFiles.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _selectedFiles.value = current
        }
    }

    fun reset() {
        _uiState.value = ToolUiState.Idle
        _selectedFiles.value = emptyList()
        _outputUris.value = emptyList()
        
        // Reset configs to defaults
        pageRangeConfig.value = PageRangeConfig()
        passwordConfig.value = PasswordConfig()
        watermarkConfig.value = WatermarkConfig()
        pageNumberConfig.value = PageNumberConfig()
        rotateConfig.value = RotateConfig()
        cropConfig.value = CropConfig()
        organizeConfig.value = OrganizeConfig()
        signConfig.value = SignConfig()
        redactConfig.value = RedactConfig()
        formConfig.value = FormConfig()
        scanConfig.value = ScanConfig()
        editConfig.value = EditConfig()
        htmlConfig.value = HtmlConfig()
        ocrConfig.value = OcrConfig()
        compareConfig.value = CompareConfig()
        pdfaConfig.value = PdfaConfig()
    }

    fun isFavorite(toolId: String): Boolean {
        return favoritesRepository.isFavorite(toolId)
    }

    fun toggleFavorite(toolId: String) {
        favoritesRepository.toggleFavorite(toolId)
    }

    suspend fun getFormFields(context: Context, uri: Uri): List<FormFieldInfo> {
        return pdfProcessor.getFormFields(context, uri)
    }

    fun process(toolId: String, context: Context) {
        val files = selectedFiles.value
        if (files.isEmpty() && toolId != "html_to_pdf") {
            _uiState.value = ToolUiState.Error("No files selected")
            return
        }

        _uiState.value = ToolUiState.Processing
        viewModelScope.launch {
            try {
                when (toolId) {
                    "merge_pdf" -> {
                        val result = pdfProcessor.mergePdfs(context, files)
                        onProcessingSuccess(toolId, context, result)
                    }
                    "compress_pdf" -> {
                        val result = pdfProcessor.compressPdf(context, files.first())
                        onProcessingSuccess(toolId, context, result)
                    }
                    "jpg_to_pdf" -> {
                        val result = pdfProcessor.convertImagesToPdf(context, files)
                        onProcessingSuccess(toolId, context, result)
                    }
                    "pdf_to_jpg" -> {
                        val results = pdfProcessor.convertPdfToImages(context, files.first())
                        onProcessingSuccessMultiple(toolId, context, results)
                    }
                    "split_pdf" -> {
                        val result = pdfProcessor.splitPdf(context, files.first(), pageRangeConfig.value.pageRange)
                        onProcessingSuccess(toolId, context, result)
                    }
                    "remove_pages" -> {
                        val result = pdfProcessor.removePages(context, files.first(), pageRangeConfig.value.pageRange)
                        onProcessingSuccess(toolId, context, result)
                    }
                    "protect_pdf" -> {
                        val result = pdfProcessor.protectPdf(context, files.first(), passwordConfig.value.password)
                        onProcessingSuccess(toolId, context, result)
                    }
                    "unlock_pdf" -> {
                        val result = pdfProcessor.unlockPdf(context, files.first(), passwordConfig.value.password)
                        onProcessingSuccess(toolId, context, result)
                    }
                    "extract_pages" -> {
                        val result = pdfProcessor.extractPages(context, files.first(), pageRangeConfig.value.pageRange)
                        onProcessingSuccess(toolId, context, result)
                    }
                    "rotate_pdf" -> {
                        val result = pdfProcessor.rotatePdf(context, files.first(), rotateConfig.value.degrees, rotateConfig.value.pageRange)
                        onProcessingSuccess(toolId, context, result)
                    }
                    "add_watermark" -> {
                        val c = watermarkConfig.value
                        val result = pdfProcessor.addWatermark(
                            context, files.first(), c.text, c.colorHex,
                            c.fontSize, c.rotation, c.opacity, c.pageRange
                        )
                        onProcessingSuccess(toolId, context, result)
                    }
                    "add_page_numbers" -> {
                        val c = pageNumberConfig.value
                        val result = pdfProcessor.addPageNumbers(
                            context, files.first(), c.format, c.position,
                            c.fontSize, c.pageRange
                        )
                        onProcessingSuccess(toolId, context, result)
                    }
                    "crop_pdf" -> {
                        val c = cropConfig.value
                        val result = pdfProcessor.cropPdf(context, files.first(), c.marginPercentage, c.pageRange)
                        onProcessingSuccess(toolId, context, result)
                    }
                    "organize_pdf" -> {
                        val c = organizeConfig.value
                        val result = pdfProcessor.organizePdf(context, files.first(), c.pageTransforms)
                        onProcessingSuccess(toolId, context, result)
                    }
                    "repair_pdf" -> {
                        val result = pdfProcessor.repairPdf(context, files.first())
                        onProcessingSuccess(toolId, context, result)
                    }
                    "pdf_to_pdfa" -> {
                        val c = pdfaConfig.value
                        val result = pdfProcessor.convertToPdfA(context, files.first(), c.conformanceLevel)
                        onProcessingSuccess(toolId, context, result)
                    }
                    "sign_pdf" -> {
                        val c = signConfig.value
                        val sigUri = c.signatureUri ?: throw IllegalArgumentException("No signature selected")
                        val result = pdfProcessor.signPdf(
                            context, files.first(), sigUri,
                            c.pageIndex, c.x, c.y, c.width, c.height
                        )
                        onProcessingSuccess(toolId, context, result)
                    }
                    "redact_pdf" -> {
                        val c = redactConfig.value
                        val textToRedact = if (c.textToRedact.isEmpty()) null else c.textToRedact
                        val result = pdfProcessor.redactPdf(
                            context, files.first(), c.pageIndex,
                            c.x, c.y, c.width, c.height, textToRedact
                        )
                        onProcessingSuccess(toolId, context, result)
                    }
                    "pdf_forms", "fill_pdf_fields" -> {
                        val c = formConfig.value
                        val result = pdfProcessor.fillPdfFields(context, files.first(), c.fieldValues)
                        onProcessingSuccess(toolId, context, result)
                    }
                    "scan_to_pdf" -> {
                        val c = scanConfig.value
                        val result = pdfProcessor.scanToPdf(context, files, c.rotations, c.filter)
                        onProcessingSuccess(toolId, context, result)
                    }
                    "ocr_pdf" -> {
                        val text = pdfProcessor.ocrPdf(context, files.first())
                        ocrConfig.value = OcrConfig(ocrResultText = text)
                        _outputUris.value = emptyList()
                        _uiState.value = ToolUiState.Success(emptyList())
                    }
                    "compare_pdf" -> {
                        val c = compareConfig.value
                        val fileB = c.fileBUri ?: throw IllegalArgumentException("No comparison file selected")
                        val diffs = pdfProcessor.comparePdf(context, files.first(), fileB)
                        compareConfig.value = c.copy(diffLines = diffs)
                        _outputUris.value = emptyList()
                        _uiState.value = ToolUiState.Success(emptyList())
                    }
                    "edit_pdf" -> {
                        val c = editConfig.value
                        val result = pdfProcessor.editPdf(context, files.first(), c.textAnnotations, c.imageAnnotations)
                        onProcessingSuccess(toolId, context, result)
                    }
                    "html_to_pdf" -> {
                        val c = htmlConfig.value
                        val result = pdfProcessor.convertHtmlToPdf(context, c.htmlContent)
                        onProcessingSuccess(toolId, context, result)
                    }
                    "word_to_pdf" -> {
                        val result = pdfProcessor.convertWordToPdf(context, files.first())
                        onProcessingSuccess(toolId, context, result)
                    }
                    "ppt_to_pdf" -> {
                        val result = pdfProcessor.convertPptToPdf(context, files.first())
                        onProcessingSuccess(toolId, context, result)
                    }
                    "excel_to_pdf" -> {
                        val result = pdfProcessor.convertExcelToPdf(context, files.first())
                        onProcessingSuccess(toolId, context, result)
                    }
                    "pdf_to_word" -> {
                        val result = pdfProcessor.convertPdfToWord(context, files.first())
                        onProcessingSuccess(toolId, context, result)
                    }
                    "pdf_to_ppt" -> {
                        val result = pdfProcessor.convertPdfToPpt(context, files.first())
                        onProcessingSuccess(toolId, context, result)
                    }
                    "pdf_to_excel" -> {
                        val result = pdfProcessor.convertPdfToExcel(context, files.first())
                        onProcessingSuccess(toolId, context, result)
                    }
                    else -> {
                        _uiState.value = ToolUiState.Error("Unknown tool: $toolId")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ToolUiState.Error(e.message ?: "An error occurred during processing")
            }
        }
    }

    private fun onProcessingSuccess(toolId: String, context: Context, uri: Uri) {
        val fileName = getFileName(context, uri)
        recentFilesRepository.addRecent(fileName, toolId, uri.toString())
        _outputUris.value = listOf(uri)
        _uiState.value = ToolUiState.Success(listOf(uri))
    }

    private fun onProcessingSuccessMultiple(toolId: String, context: Context, uris: List<Uri>) {
        if (uris.isNotEmpty()) {
            val fileName = getFileName(context, uris.first())
            recentFilesRepository.addRecent(fileName, toolId, uris.first().toString())
        }
        _outputUris.value = uris
        _uiState.value = ToolUiState.Success(uris)
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
        return result ?: "processed.pdf"
    }
}
