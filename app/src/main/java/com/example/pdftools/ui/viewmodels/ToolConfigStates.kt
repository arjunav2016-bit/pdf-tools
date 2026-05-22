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
    val htmlContent: String = "<html><body><h1>Offline PDF Document</h1><p>Created with Android PDF Tools</p></body></html>"
)

data class OcrConfig(
    val ocrResultText: String = ""
)

data class CompareConfig(
    val fileBUri: Uri? = null,
    val diffLines: List<DiffLine> = emptyList()
)

data class PdfaConfig(
    val conformanceLevel: String = "pdfa_1b"
)
