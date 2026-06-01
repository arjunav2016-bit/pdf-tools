package com.example.pdftools.ui.viewmodels

import android.net.Uri
import com.example.pdftools.data.DiffLine
import com.example.pdftools.data.FormFieldInfo
import com.example.pdftools.data.ImageAnnotation
import com.example.pdftools.data.PdfProcessor
import com.example.pdftools.data.TextAnnotation

data class PageRangeConfig(
    val pageRange: String = "",
    val selectedPages: Set<Int> = emptySet()
)

data class PasswordConfig(
    val password: String = ""
)

data class WatermarkConfig(
    val text: String = "CONFIDENTIAL",
    val colorHex: String = "#7F8C8D",
    val fontSize: Float = 40f,
    val rotation: Float = 45f,
    val opacity: Float = 0.3f,
    val pageRange: String = ""
)

data class PageNumberConfig(
    val format: String = "detailed", // "simple", "detailed"
    val position: String = "bottom_right", // "bottom_left", etc.
    val fontSize: Float = 12f,
    val pageRange: String = ""
)

data class RotateConfig(
    val degrees: Int = 90,
    val pageRange: String = "",
    val selectedPages: Set<Int> = emptySet()
)

data class CropConfig(
    val marginPercentage: Float = 0.10f,
    val pageRange: String = "",
    val selectedPages: Set<Int> = emptySet()
)

data class OrganizeConfig(
    val pageTransforms: List<PdfProcessor.PageTransform> = emptyList()
)

data class SignConfig(
    val signatureUri: Uri? = null,
    val pageIndex: Int = 0,
    val x: Float = 100f,
    val y: Float = 100f,
    val width: Float = 150f,
    val height: Float = 60f
)

data class RedactConfig(
    val pageIndex: Int = 0,
    val x: Float = 50f,
    val y: Float = 50f,
    val width: Float = 200f,
    val height: Float = 100f,
    val textToRedact: String = ""
)

data class FormConfig(
    val fields: List<FormFieldInfo> = emptyList(),
    val fieldValues: Map<String, String> = emptyMap()
)

data class ScanConfig(
    val imageUris: List<Uri> = emptyList(),
    val rotations: List<Int> = emptyList(),
    val filter: String = "color"
)

data class EditConfig(
    val textAnnotations: List<TextAnnotation> = emptyList(),
    val imageAnnotations: List<ImageAnnotation> = emptyList()
)


data class HtmlConfig(
    val htmlContent: String = "<html><body><h1>Offline PDF Document</h1><p>Created with Android PDF Tools</p></body></html>",
    val inputType: String = "url", // "url", "html"
    val url: String = "https://www.google.com",
    val loadJs: Boolean = true,
    val loadBackgroundGraphics: Boolean = true,
    val pageScale: Float = 1.0f,
    val captureArea: String = "whole_page", // "whole_page", "selected_area"
    val selectedAreaSelector: String = ""
)

data class OcrConfig(
    val ocrResultText: String = ""
)

data class CompareConfig(
    val fileBUri: Uri? = null,
    val diffLines: List<DiffLine> = emptyList()
)

data class PdfaConfig(
    val conformanceLevel: String = "pdfa_2b",
    val embedFonts: Boolean = true,
    val removeTransparencies: Boolean = false,
    val convertSrgb: Boolean = true,
    val title: String = "",
    val author: String = "",
    val subject: String = ""
)

enum class CompressTier {
    BASIC,
    RECOMMENDED,
    EXTREME
}

data class CompressConfig(
    val tier: CompressTier = CompressTier.RECOMMENDED
)

data class JpgToPdfConfig(
    val pageSize: String = "auto",       // "auto", "a4", "letter"
    val orientation: String = "portrait", // "portrait", "landscape"
    val margin: Float = 0f,              // 0f (none), 12f (small), 24f (medium), 36f (large)
    val maxSizeMb: Int? = null           // null (unlimited), 1, 2, 5
)

data class WordToPdfConfig(
    val maintainLayout: Boolean = true,
    val imageQuality: String = "medium", // "low", "medium", "high"
    val runOcr: Boolean = false
)

data class PdfToWordConfig(
    val conversionMode: String = "standard", // "standard", "ocr"
    val keepOriginalLayout: Boolean = true
)

data class PptToPdfConfig(
    val slideRange: String = "all",          // "all" or "custom"
    val customRange: String = "",            // e.g. "1-5, 8"
    val selectedSlides: Set<Int> = emptySet(), // visual selection state (1-indexed)
    val slidesPerPage: Int = 1,              // 1, 2, or 4
    val includeNotes: Boolean = false,
    val quality: String = "medium"           // "low", "medium", "high"
)

data class ExcelToPdfConfig(
    val convertMode: String = "all_sheets", // "active_sheet", "all_sheets"
    val scalingMode: String = "fit_columns", // "no_scaling", "fit_columns", "fit_rows", "fit_all"
    val showGridlines: Boolean = true
)

data class PdfToImageConfig(
    val format: String = "jpg",        // "jpg", "png", "webp"
    val quality: Int = 85,             // 1-100 (only for jpg/webp, png is lossless)
    val dpi: Int = 150,                // 72, 150, 200, 300
    val pageSelection: String = "all", // "all", "custom"
    val customPageRange: String = ""   // e.g. "1-5, 8"
)

data class PdfToPptConfig(
    val slidesPerPage: Int = 1,
    val includeNotes: Boolean = false,
    val runOcr: Boolean = true,
    val exportFormat: String = "pptx"
)

