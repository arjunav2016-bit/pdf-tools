package com.example.pdftools.data.processors

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import com.tom_roush.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState
import com.tom_roush.pdfbox.rendering.PDFRenderer
import com.tom_roush.pdfbox.rendering.ImageType
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.example.pdftools.data.FormFieldInfo
import com.example.pdftools.data.TextAnnotation
import com.example.pdftools.data.ImageAnnotation
import com.example.pdftools.data.DiffLine
import com.example.pdftools.data.DiffType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

import com.example.pdftools.utils.PageRangeUtils
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles PDF editing operations: watermark, page numbers, sign, edit annotations, forms, OCR, compare.
 */
@Singleton
class EditProcessor @Inject constructor() {

    /**
     * Overlays a custom text watermark on specified pages (or all pages if range is empty) with custom styling.
     */
    suspend fun addWatermark(
        context: Context,
        uri: Uri,
        text: String,
        colorHex: String,
        fontSize: Float,
        rotation: Float,
        opacity: Float,
        pageRange: String
    ): Uri = withContext(Dispatchers.IO) {
        if (text.trim().isEmpty()) {
            throw IllegalArgumentException("Watermark text cannot be empty.")
        }
        val tempInputFile = File.createTempFile("watermark_input_", ".pdf", context.cacheDir)
        val outputFile = File(context.cacheDir, "Watermarked_${System.currentTimeMillis()}.pdf")
        
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            PDDocument.load(tempInputFile).use { doc ->
                val totalPages = doc.numberOfPages
                val selectedPages = if (pageRange.trim().isEmpty()) {
                    (0 until totalPages).toList()
                } else {
                    PageRangeUtils.parsePageRanges(pageRange, totalPages)
                }
                
                if (selectedPages.isEmpty() && pageRange.trim().isNotEmpty()) {
                    throw IllegalArgumentException("Please enter a valid page range (e.g., 1-3, 5) within the document's bounds (1-$totalPages).")
                }
                
                val parsedColor = try {
                    android.graphics.Color.parseColor(colorHex)
                } catch (e: Exception) {
                    android.graphics.Color.GRAY
                }
                val r = android.graphics.Color.red(parsedColor) / 255f
                val g = android.graphics.Color.green(parsedColor) / 255f
                val b = android.graphics.Color.blue(parsedColor) / 255f
                
                for (pageIdx in selectedPages) {
                    val page = doc.getPage(pageIdx)
                    val width = page.mediaBox.width
                    val height = page.mediaBox.height
                    
                    PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true).use { contentStream ->
                        // Apply translucent styling using extended graphics state
                        val extGS = PDExtendedGraphicsState().apply {
                            nonStrokingAlphaConstant = opacity
                        }
                        contentStream.setGraphicsStateParameters(extGS)
                        contentStream.setNonStrokingColor(r, g, b)
                        
                        val font = PDType1Font.HELVETICA_BOLD
                        contentStream.beginText()
                        contentStream.setFont(font, fontSize)
                        
                        // Calculate dimensions for text centering
                        val textWidth = font.getStringWidth(text) / 1000f * fontSize
                        val textHeight = fontSize
                        
                        // Set text rotation and translation around the center of the page
                        val rad = Math.toRadians(rotation.toDouble())
                        val cos = Math.cos(rad).toFloat()
                        val sin = Math.sin(rad).toFloat()
                        
                        // Page center
                        val cx = width / 2f
                        val cy = height / 2f
                        
                        // Transform origin to page center, then apply rotation, and shift back to align text center
                        contentStream.setTextMatrix(cos.toDouble(), sin.toDouble(), -sin.toDouble(), cos.toDouble(), cx.toDouble(), cy.toDouble())
                        contentStream.newLineAtOffset(-textWidth / 2f, -textHeight / 4f)
                        
                        contentStream.showText(text)
                        contentStream.endText()
                    }
                }
                doc.save(outputFile)
            }
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            outputFile.delete()
            throw e
        } finally {
            tempInputFile.delete()
        }
    }

    /**
     * Inserts page numbers at custom positions for specified pages (or all pages if range is empty).
     */
    suspend fun addPageNumbers(
        context: Context,
        uri: Uri,
        format: String, // "simple" (e.g. "1"), "prefixed" (e.g. "Page 1"), "detailed" (e.g. "Page 1 of 5")
        position: String, // "bottom_center", "bottom_right", "top_right"
        fontSize: Float,
        pageRange: String
    ): Uri = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("page_numbers_input_", ".pdf", context.cacheDir)
        val outputFile = File(context.cacheDir, "PageNumbered_${System.currentTimeMillis()}.pdf")
        
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            PDDocument.load(tempInputFile).use { doc ->
                val totalPages = doc.numberOfPages
                val selectedPages = if (pageRange.trim().isEmpty()) {
                    (0 until totalPages).toList()
                } else {
                    PageRangeUtils.parsePageRanges(pageRange, totalPages)
                }
                
                if (selectedPages.isEmpty() && pageRange.trim().isNotEmpty()) {
                    throw IllegalArgumentException("Please enter a valid page range (e.g., 1-3, 5) within the document's bounds (1-$totalPages).")
                }
                
                for (pageIdx in selectedPages) {
                    val page = doc.getPage(pageIdx)
                    val width = page.mediaBox.width
                    val height = page.mediaBox.height
                    
                    val text = when (format) {
                        "simple" -> "${pageIdx + 1}"
                        "prefixed" -> "Page ${pageIdx + 1}"
                        "detailed" -> "Page ${pageIdx + 1} of $totalPages"
                        else -> "${pageIdx + 1}"
                    }
                    
                    PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true).use { contentStream ->
                        // Render page number in a semi-translucent dark gray (#555555)
                        val extGS = PDExtendedGraphicsState().apply {
                            nonStrokingAlphaConstant = 0.8f
                        }
                        contentStream.setGraphicsStateParameters(extGS)
                        contentStream.setNonStrokingColor(0.33f, 0.33f, 0.33f)
                        
                        val font = PDType1Font.HELVETICA
                        contentStream.beginText()
                        contentStream.setFont(font, fontSize)
                        
                        val textWidth = font.getStringWidth(text) / 1000f * fontSize
                        
                        val x = when (position) {
                            "bottom_center" -> (width - textWidth) / 2f
                            "bottom_right" -> width - textWidth - 40f
                            "top_right" -> width - textWidth - 40f
                            else -> (width - textWidth) / 2f
                        }
                        
                        val y = when (position) {
                            "bottom_center", "bottom_right" -> 30f
                            "top_right" -> height - 40f
                            else -> 30f
                        }
                        
                        contentStream.newLineAtOffset(x, y)
                        contentStream.showText(text)
                        contentStream.endText()
                    }
                }
                doc.save(outputFile)
            }
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            outputFile.delete()
            throw e
        } finally {
            tempInputFile.delete()
        }
    }

    /**
     * Overlays a handwritten signature or image onto a target page at specified coordinates offline.
     */
    suspend fun signPdf(
        context: Context,
        uri: Uri,
        signatureUri: Uri,
        pageIndex: Int,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ): Uri = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("sign_input_", ".pdf", context.cacheDir)
        val outputFile = File(context.cacheDir, "Signed_${System.currentTimeMillis()}.pdf")
        
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            PDDocument.load(tempInputFile).use { doc ->
                val totalPages = doc.numberOfPages
                val targetPageIndex = if (pageIndex in 0 until totalPages) pageIndex else 0
                val page = doc.getPage(targetPageIndex)
                
                context.contentResolver.openInputStream(signatureUri)?.use { sigStream ->
                    val bitmap = BitmapFactory.decodeStream(sigStream)
                    if (bitmap != null) {
                        val pdImage = com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory.createFromImage(doc, bitmap)
                        PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true).use { contentStream ->
                            contentStream.drawImage(pdImage, x, y, width, height)
                        }
                        bitmap.recycle()
                    } else {
                        throw IllegalArgumentException("Invalid signature image file.")
                    }
                }
                
                doc.save(outputFile)
            }
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            outputFile.delete()
            throw e
        } finally {
            tempInputFile.delete()
        }
    }

    /**
     * Stams text and image annotations onto specific coordinates of custom pages.
     */
    suspend fun editPdf(
        context: Context,
        uri: Uri,
        textAnnotations: List<TextAnnotation>,
        imageAnnotations: List<ImageAnnotation>
    ): Uri = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("edit_input_", ".pdf", context.cacheDir)
        val outputFile = File(context.cacheDir, "Edited_${System.currentTimeMillis()}.pdf")
        
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            PDDocument.load(tempInputFile).use { doc ->
                val textByPage = textAnnotations.groupBy { it.pageIndex }
                val imageByPage = imageAnnotations.groupBy { it.pageIndex }
                val activePages = (textByPage.keys + imageByPage.keys).toSet()
                
                for (pageIdx in activePages) {
                    if (pageIdx < 0 || pageIdx >= doc.numberOfPages) continue
                    val page = doc.getPage(pageIdx)
                    val width = page.mediaBox.width
                    val height = page.mediaBox.height
                    
                    PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true).use { contentStream ->
                        // 1. Draw Image Stamps
                        imageByPage[pageIdx]?.forEach { imgAnn ->
                            try {
                                val imgUri = Uri.parse(imgAnn.imageUri)
                                context.contentResolver.openInputStream(imgUri)?.use { input ->
                                    val bitmap = BitmapFactory.decodeStream(input)
                                    if (bitmap != null) {
                                        val pdImage = LosslessFactory.createFromImage(doc, bitmap)
                                        val pdfX = imgAnn.x * width
                                        val pdfY = (1f - imgAnn.y) * height
                                        val pdfW = imgAnn.width * width
                                        val pdfH = imgAnn.height * height
                                        
                                        // drawImage uses bottom-left origin. Since relative coordinates map top-left,
                                        // we translate bottom-left to (pdfX, pdfY - pdfH).
                                        contentStream.drawImage(pdImage, pdfX, pdfY - pdfH, pdfW, pdfH)
                                        bitmap.recycle()
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        
                        // 2. Draw Text Boxes
                        textByPage[pageIdx]?.forEach { textAnn ->
                            try {
                                if (textAnn.text.isNotEmpty()) {
                                    val parsedColor = try {
                                        android.graphics.Color.parseColor(textAnn.colorHex)
                                    } catch (e: Exception) {
                                        android.graphics.Color.BLACK
                                    }
                                    val r = android.graphics.Color.red(parsedColor) / 255f
                                    val g = android.graphics.Color.green(parsedColor) / 255f
                                    val b = android.graphics.Color.blue(parsedColor) / 255f
                                    
                                    contentStream.setNonStrokingColor(r, g, b)
                                    contentStream.beginText()
                                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, textAnn.fontSize)
                                    
                                    val pdfX = textAnn.x * width
                                    val pdfY = (1f - textAnn.y) * height
                                    
                                    // Offset by font size to align correctly with top edge
                                    contentStream.newLineAtOffset(pdfX, pdfY - textAnn.fontSize)
                                    contentStream.showText(textAnn.text)
                                    contentStream.endText()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                
                doc.save(outputFile)
            }
            
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            outputFile.delete()
            throw e
        } finally {
            tempInputFile.delete()
        }
    }

    /**
     * Extracts dynamic form fields from standard PDF AcroForms.
     */
    suspend fun getFormFields(context: Context, uri: Uri): List<FormFieldInfo> = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("form_parse_", ".pdf", context.cacheDir)
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            PDDocument.load(tempInputFile).use { doc ->
                val acroForm = doc.documentCatalog.acroForm
                if (acroForm == null) {
                    emptyList()
                } else {
                    acroForm.fields.mapNotNull { field ->
                        val name = field.fullyQualifiedName ?: field.partialName ?: return@mapNotNull null
                        val type = when (field) {
                            is com.tom_roush.pdfbox.pdmodel.interactive.form.PDTextField -> "text"
                            is com.tom_roush.pdfbox.pdmodel.interactive.form.PDCheckBox -> "checkbox"
                            is com.tom_roush.pdfbox.pdmodel.interactive.form.PDChoice -> "choice"
                            is com.tom_roush.pdfbox.pdmodel.interactive.form.PDSignatureField -> "signature"
                            else -> "other"
                        }
                        
                        val value = field.valueAsString ?: ""
                        val options = if (field is com.tom_roush.pdfbox.pdmodel.interactive.form.PDChoice) {
                            field.optionsDisplayValues ?: field.optionsExportValues ?: emptyList()
                        } else {
                            emptyList()
                        }
                        
                        FormFieldInfo(name, type, value, options)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        } finally {
            tempInputFile.delete()
        }
    }

    /**
     * Fills dynamic fields inside interactive PDF AcroForms entirely offline.
     */
    suspend fun fillPdfFields(
        context: Context,
        uri: Uri,
        fieldValues: Map<String, String>
    ): Uri = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("form_fill_input_", ".pdf", context.cacheDir)
        val outputFile = File(context.cacheDir, "Filled_${System.currentTimeMillis()}.pdf")
        
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            PDDocument.load(tempInputFile).use { doc ->
                val acroForm = doc.documentCatalog.acroForm
                if (acroForm != null) {
                    acroForm.needAppearances = true
                    
                    for ((fieldName, value) in fieldValues) {
                        val field = acroForm.getField(fieldName)
                        if (field != null) {
                            try {
                                if (field is com.tom_roush.pdfbox.pdmodel.interactive.form.PDCheckBox) {
                                    if (value == "true" || value == "Yes" || value == "1") {
                                        field.check()
                                    } else {
                                        field.unCheck()
                                    }
                                } else {
                                    field.setValue(value)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                } else {
                    throw IllegalArgumentException("This PDF does not contain an interactive AcroForm.")
                }
                doc.save(outputFile)
            }
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            outputFile.delete()
            throw e
        } finally {
            tempInputFile.delete()
        }
    }

    /**
     * Extracts text from a PDF, falling back to on-device OCR using Google ML Kit if needed.
     */
    suspend fun ocrPdf(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("ocr_input_", ".pdf", context.cacheDir)
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            PDDocument.load(tempInputFile).use { doc ->
                val stripper = PDFTextStripper()
                val extracted = stripper.getText(doc) ?: ""
                if (extracted.trim().isNotEmpty()) {
                    return@withContext extracted
                }
                
                // Falling back to page-by-page ML Kit OCR
                val ocrText = StringBuilder()
                val renderer = PDFRenderer(doc)
                for (i in 0 until doc.numberOfPages) {
                    val bitmap = renderer.renderImageWithDPI(i, 150f, ImageType.ARGB)
                    try {
                        val image = com.google.mlkit.vision.common.InputImage.fromBitmap(bitmap, 0)
                        val recognizer = com.google.mlkit.vision.text.TextRecognition.getClient(
                            com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS
                        )
                        val result = com.google.android.gms.tasks.Tasks.await(recognizer.process(image))
                        ocrText.append(result.text).append("\n")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        ocrText.append("[Scanned Page ${i + 1} - Offline Text Recognition Fallback Result]\n")
                    } finally {
                        bitmap.recycle()
                    }
                }
                
                if (ocrText.trim().isNotEmpty()) {
                    ocrText.toString()
                } else {
                    "No text could be recognized in this document."
                }
            }
        } finally {
            tempInputFile.delete()
        }
    }

    /**
     * Compares two PDF documents line-by-line using standard LCS diff.
     */
    suspend fun comparePdf(context: Context, uriA: Uri, uriB: Uri): List<DiffLine> = withContext(Dispatchers.IO) {
        val textA = extractAllText(context, uriA)
        val textB = extractAllText(context, uriB)
        
        val linesA = textA.split(Regex("\\r?\\n")).map { it.trim() }.filter { it.isNotEmpty() }
        val linesB = textB.split(Regex("\\r?\\n")).map { it.trim() }.filter { it.isNotEmpty() }
        
        val m = linesA.size
        val n = linesB.size
        
        val dp = Array(m + 1) { IntArray(n + 1) }
        for (i in 1..m) {
            for (j in 1..n) {
                if (linesA[i - 1] == linesB[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1] + 1
                } else {
                    dp[i][j] = maxOf(dp[i - 1][j], dp[i][j - 1])
                }
            }
        }
        
        var i = m
        var j = n
        val diff = mutableListOf<DiffLine>()
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && linesA[i - 1] == linesB[j - 1]) {
                diff.add(DiffLine(linesA[i - 1], DiffType.EQUAL))
                i--
                j--
            } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
                diff.add(DiffLine(linesB[j - 1], DiffType.ADDED))
                j--
            } else if (i > 0) {
                diff.add(DiffLine(linesA[i - 1], DiffType.DELETED))
                i--
            }
        }
        diff.reverse()
        diff
    }

    private fun extractAllText(context: Context, uri: Uri): String {
        val tempFile = File.createTempFile("extract_text_", ".pdf", context.cacheDir)
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            PDDocument.load(tempFile).use { doc ->
                val stripper = PDFTextStripper()
                return stripper.getText(doc) ?: ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        } finally {
            tempFile.delete()
        }
    }
}
