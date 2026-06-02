package com.example.pdftools.data

import android.content.Context
import android.net.Uri
import com.example.pdftools.data.processors.OrganizeProcessor
import com.example.pdftools.data.processors.OptimizeProcessor
import com.example.pdftools.data.processors.ConvertProcessor
import com.example.pdftools.data.processors.EditProcessor
import com.example.pdftools.data.processors.SecurityProcessor

import com.example.pdftools.utils.PageRangeUtils
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Facade that delegates all PDF processing operations to category-specific processors.
 * 
 * This class maintains the same public API as before for backward compatibility.
 * All actual logic lives in the sub-processor objects under `data/processors/`.
 */
@Singleton
class PdfProcessor @Inject constructor(
    private val organizeProcessor: OrganizeProcessor,
    private val optimizeProcessor: OptimizeProcessor,
    private val convertProcessor: ConvertProcessor,
    private val editProcessor: EditProcessor,
    private val securityProcessor: SecurityProcessor
) {

    // Re-export PageTransform so callers can still reference PdfProcessor.PageTransform
    data class PageTransform(val originalIndex: Int, val rotation: Int)

    // ===== Organize Operations (OrganizeProcessor) =====

    fun parsePageRanges(rangeStr: String, totalPages: Int): List<Int> =
        PageRangeUtils.parsePageRanges(rangeStr, totalPages)

    suspend fun mergePdfs(
        context: Context,
        uris: List<Uri>,
        onProgress: ((Float) -> Unit)? = null
    ): Uri = organizeProcessor.mergePdfs(context, uris, onProgress)

    suspend fun splitPdf(context: Context, uri: Uri, pageRange: String): Uri =
        organizeProcessor.splitPdf(context, uri, pageRange)

    suspend fun removePages(context: Context, uri: Uri, pagesToRemoveStr: String): Uri =
        organizeProcessor.removePages(context, uri, pagesToRemoveStr)

    suspend fun extractPages(context: Context, uri: Uri, pageRange: String): Uri =
        organizeProcessor.extractPages(context, uri, pageRange)

    suspend fun rotatePdf(context: Context, uri: Uri, degrees: Int, pageRange: String): Uri =
        organizeProcessor.rotatePdf(context, uri, degrees, pageRange)

    suspend fun organizePdf(
        context: Context,
        uri: Uri,
        pageTransforms: List<PageTransform>
    ): Uri = organizeProcessor.organizePdf(
        context, uri,
        pageTransforms.map { OrganizeProcessor.PageTransform(it.originalIndex, it.rotation) }
    )

    // ===== Optimize Operations (OptimizeProcessor) =====

    suspend fun compressPdf(
        context: Context,
        uri: Uri,
        quality: Int = 70,
        onProgress: ((Float) -> Unit)? = null
    ): Uri = optimizeProcessor.compressPdf(context, uri, quality, onProgress)

    suspend fun repairPdf(context: Context, uri: Uri): Uri =
        optimizeProcessor.repairPdf(context, uri)

    suspend fun cropPdf(
        context: Context,
        uri: Uri,
        marginPercentage: Float,
        pageRange: String,
        useAbsoluteCrop: Boolean = false,
        leftMm: Float = 0f,
        topMm: Float = 0f,
        widthMm: Float = 0f,
        heightMm: Float = 0f,
        applyToAllPages: Boolean = true,
        currentPageIndex: Int = 0
    ): Uri = optimizeProcessor.cropPdf(
        context, uri, marginPercentage, pageRange,
        useAbsoluteCrop, leftMm, topMm, widthMm, heightMm,
        applyToAllPages, currentPageIndex
    )

    suspend fun convertToPdfA(
        context: Context,
        uri: Uri,
        conformanceLevel: String,
        embedFonts: Boolean = true,
        removeTransparencies: Boolean = false,
        convertSrgb: Boolean = true,
        title: String = "",
        author: String = "",
        subject: String = ""
    ): Uri = optimizeProcessor.convertToPdfA(
        context, uri, conformanceLevel, embedFonts, removeTransparencies, convertSrgb, title, author, subject
    )

    // ===== Convert Operations (ConvertProcessor) =====

    suspend fun convertImagesToPdf(
        context: Context,
        uris: List<Uri>,
        pageSize: String = "auto",
        orientation: String = "portrait",
        margin: Float = 0f,
        maxSizeMb: Int? = null,
        onProgress: ((Float) -> Unit)? = null
    ): Uri = convertProcessor.convertImagesToPdf(context, uris, pageSize, orientation, margin, maxSizeMb, onProgress)

    suspend fun convertPdfToImages(
        context: Context,
        uri: Uri,
        dpi: Int = 150,
        format: String = "jpg",
        quality: Int = 85,
        pageSelection: String = "all",
        customPageRange: String = "",
        onProgress: ((Float) -> Unit)? = null
    ): List<Uri> = convertProcessor.convertPdfToImages(context, uri, dpi, format, quality, pageSelection, customPageRange, onProgress)

    suspend fun scanToPdf(
        context: Context,
        imageUris: List<Uri>,
        rotations: List<Int>,
        filter: String,
        pageSize: String = "auto",
        quality: Int = 85,
        onProgress: ((Float) -> Unit)? = null
    ): Uri = convertProcessor.scanToPdf(context, imageUris, rotations, filter, pageSize, quality, onProgress)

    suspend fun convertHtmlToPdf(
        context: Context,
        htmlContent: String,
        inputType: String = "html",
        url: String = "",
        loadJs: Boolean = true,
        loadBackgroundGraphics: Boolean = true,
        pageScale: Float = 1.0f,
        captureArea: String = "whole_page",
        selectedAreaSelector: String = ""
    ): Uri = convertProcessor.convertHtmlToPdf(
        context, htmlContent, inputType, url, loadJs,
        loadBackgroundGraphics, pageScale, captureArea, selectedAreaSelector
    )

    suspend fun convertWordToPdf(
        context: Context,
        uri: Uri,
        maintainLayout: Boolean = true,
        imageQuality: String = "medium",
        runOcr: Boolean = false
    ): Uri = convertProcessor.convertWordToPdf(context, uri, maintainLayout, imageQuality, runOcr)

    suspend fun convertPptToPdf(
        context: Context,
        uri: Uri,
        slideRange: String = "all",
        customRange: String = "",
        selectedSlides: Set<Int> = emptySet(),
        slidesPerPage: Int = 1,
        includeNotes: Boolean = false,
        quality: String = "medium",
        onProgress: ((Float) -> Unit)? = null
    ): Uri = convertProcessor.convertPptToPdf(
        context, uri, slideRange, customRange, selectedSlides,
        slidesPerPage, includeNotes, quality, onProgress
    )

    suspend fun getSlideCount(context: Context, uri: Uri): Int =
        convertProcessor.getSlideCount(context, uri)

    suspend fun getSlidePreviewTexts(context: Context, uri: Uri): List<String> =
        convertProcessor.getSlidePreviewTexts(context, uri)

    suspend fun convertExcelToPdf(
        context: Context,
        uri: Uri,
        convertMode: String = "all_sheets",
        scalingMode: String = "fit_columns",
        showGridlines: Boolean = true
    ): Uri = convertProcessor.convertExcelToPdf(context, uri, convertMode, scalingMode, showGridlines)


    suspend fun convertPdfToWord(context: Context, uri: Uri): Uri =
        convertProcessor.convertPdfToWord(context, uri)

    suspend fun convertPdfToPpt(
        context: Context,
        uri: Uri,
        slidesPerPage: Int = 1,
        includeNotes: Boolean = false,
        runOcr: Boolean = true,
        exportFormat: String = "pptx"
    ): Uri =
        convertProcessor.convertPdfToPpt(context, uri, slidesPerPage, includeNotes, runOcr, exportFormat)


    suspend fun convertPdfToExcel(context: Context, uri: Uri): Uri =
        convertProcessor.convertPdfToExcel(context, uri)

    // ===== Edit Operations (EditProcessor) =====

    suspend fun addWatermark(
        context: Context, uri: Uri, text: String, colorHex: String,
        fontSize: Float, rotation: Float, opacity: Float, pageRange: String,
        isImage: Boolean = false, imageUri: Uri? = null, position: String = "center"
    ): Uri = editProcessor.addWatermark(
        context, uri, text, colorHex, fontSize, rotation, opacity, pageRange,
        isImage, imageUri, position
    )

    suspend fun addPageNumbers(
        context: Context, uri: Uri, format: String, position: String,
        fontSize: Float, pageRange: String = "", colorHex: String = "#80488D",
        rangeType: String = "all", startFromPage: Int = 1, startingNumber: Int = 1
    ): Uri = editProcessor.addPageNumbers(
        context, uri, format, position, fontSize, pageRange,
        colorHex, rangeType, startFromPage, startingNumber
    )

    suspend fun signPdf(
        context: Context, uri: Uri, signatureUri: Uri,
        pageIndex: Int, x: Float, y: Float, width: Float, height: Float
    ): Uri = editProcessor.signPdf(context, uri, signatureUri, pageIndex, x, y, width, height)

    suspend fun editPdf(
        context: Context, uri: Uri,
        textAnnotations: List<TextAnnotation>, imageAnnotations: List<ImageAnnotation>
    ): Uri = editProcessor.editPdf(context, uri, textAnnotations, imageAnnotations)

    suspend fun getFormFields(context: Context, uri: Uri): List<FormFieldInfo> =
        editProcessor.getFormFields(context, uri)

    suspend fun fillPdfFields(context: Context, uri: Uri, fieldValues: Map<String, String>): Uri =
        editProcessor.fillPdfFields(context, uri, fieldValues)

    suspend fun ocrPdf(context: Context, uri: Uri): String =
        editProcessor.ocrPdf(context, uri)

    suspend fun comparePdf(context: Context, uriA: Uri, uriB: Uri): List<DiffLine> =
        editProcessor.comparePdf(context, uriA, uriB)

    // ===== Security Operations (SecurityProcessor) =====

    suspend fun protectPdf(
        context: Context,
        uri: Uri,
        password: String,
        securityTier: String = "standard",
        openPasswordEnabled: Boolean = true,
        restrictPermissionsEnabled: Boolean = false
    ): Uri =
        securityProcessor.protectPdf(context, uri, password, securityTier, openPasswordEnabled, restrictPermissionsEnabled)

    suspend fun unlockPdf(context: Context, uri: Uri, password: String): Uri =
        securityProcessor.unlockPdf(context, uri, password)

    suspend fun redactPdf(
        context: Context, uri: Uri, pageIndex: Int,
        x: Float, y: Float, width: Float, height: Float, textToRedact: String?,
        redactionStyle: String = "black",
        sanitizeMetadata: Boolean = true
    ): Uri = securityProcessor.redactPdf(
        context, uri, pageIndex, x, y, width, height, textToRedact, redactionStyle, sanitizeMetadata
    )
}

/**
 * Data model containing parsed form field meta specifications.
 */
data class FormFieldInfo(
    val name: String,
    val type: String, // "text", "checkbox", "choice", "signature", "other"
    val value: String,
    val options: List<String> = emptyList()
)

enum class DiffType { ADDED, DELETED, EQUAL }
data class DiffLine(val text: String, val type: DiffType)

data class TextAnnotation(
    val text: String,
    val x: Float,          // Relative (0.0 to 1.0)
    val y: Float,          // Relative (0.0 to 1.0)
    val colorHex: String,  // HEX string
    val fontSize: Float,
    val pageIndex: Int
)

data class ImageAnnotation(
    val imageUri: String,
    val x: Float,          // Relative (0.0 to 1.0)
    val y: Float,          // Relative (0.0 to 1.0)
    val width: Float,      // Relative (0.0 to 1.0)
    val height: Float,     // Relative (0.0 to 1.0)
    val pageIndex: Int
)
