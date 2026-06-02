package com.example.pdftools.data.processors

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.rendering.ImageType
import com.tom_roush.pdfbox.rendering.PDFRenderer
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.text.TextPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles PDF security operations: protect, unlock, redact.
 */
@Singleton
class SecurityProcessor @Inject constructor() {
    private companion object {
        const val REDACTION_DPI = 300f
        const val REDACTION_PADDING_POINTS = 2f
    }

    /**
     * Encrypts/Protects a PDF document using standard 128-bit or strong 256-bit AES security.
     */
    suspend fun protectPdf(
        context: Context,
        uri: Uri,
        password: String,
        securityTier: String = "standard",
        openPasswordEnabled: Boolean = true,
        restrictPermissionsEnabled: Boolean = false
    ): Uri = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("protect_input_", ".pdf", context.cacheDir)
        val outputFile = File(context.cacheDir, "Protected_${System.currentTimeMillis()}.pdf")
        
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            PDDocument.load(tempInputFile).use { doc ->
                val ap = AccessPermission()
                if (restrictPermissionsEnabled) {
                    ap.setCanPrint(false)
                    ap.setCanModify(false)
                    ap.setCanExtractContent(false)
                    ap.setCanModifyAnnotations(false)
                    ap.setCanFillInForm(false)
                }
                
                val userPassword = if (openPasswordEnabled) password else ""
                val ownerPassword = if (restrictPermissionsEnabled) {
                    if (userPassword.isEmpty()) password else password + "_owner"
                } else {
                    password
                }
                
                val spp = StandardProtectionPolicy(ownerPassword, userPassword, ap)
                spp.encryptionKeyLength = if (securityTier == "strong") 256 else 128
                doc.protect(spp)
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
     * Decrypts/Unlocks a password-protected PDF document.
     */
    suspend fun unlockPdf(context: Context, uri: Uri, password: String): Uri = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("unlock_input_", ".pdf", context.cacheDir)
        val outputFile = File(context.cacheDir, "Unlocked_${System.currentTimeMillis()}.pdf")
        
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            // Attempt to load the document with the password.
            // If the password is wrong, this will throw an InvalidPasswordException (IOException).
            PDDocument.load(tempInputFile, password).use { doc ->
                if (doc.isEncrypted) {
                    doc.setAllSecurityToBeRemoved(true)
                }
                doc.save(outputFile)
            }
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            outputFile.delete()
            // Throw a cleaner message on password error
            if (e.message?.contains("password", ignoreCase = true) == true || 
                e.javaClass.simpleName.contains("Password", ignoreCase = true)) {
                throw IllegalArgumentException("Invalid password. Please enter the correct password.")
            }
            throw e
        } finally {
            tempInputFile.delete()
        }
    }

    /**
     * Redacts a target page by replacing its content stream with a rasterized page image.
     * Manual coordinates are always applied and matching text runs add extra redaction areas.
     */
    suspend fun redactPdf(
        context: Context,
        uri: Uri,
        pageIndex: Int,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        textToRedact: String?,
        redactionStyle: String = "black",
        sanitizeMetadata: Boolean = true
    ): Uri = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("redact_input_", ".pdf", context.cacheDir)
        val outputFile = File(context.cacheDir, "Redacted_${System.currentTimeMillis()}.pdf")
        
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

                val redactionAreas = buildList {
                    if (width > 0f && height > 0f) {
                        add(PDRectangle(x, y, width, height))
                    }
                    addAll(findTextRedactionAreas(doc, targetPageIndex, textToRedact))
                }
                require(redactionAreas.isNotEmpty()) {
                    "Provide a redaction box or text to redact."
                }

                val bitmap = PDFRenderer(doc).renderImageWithDPI(
                    targetPageIndex,
                    REDACTION_DPI,
                    ImageType.RGB
                )
                try {
                    paintRedactionAreas(bitmap, page.cropBox, redactionAreas, redactionStyle)
                    val redactedPageImage = JPEGFactory.createFromImage(doc, bitmap, 0.98f)

                    // Overwriting the content stream destroys selectable text on this page.
                    PDPageContentStream(
                        doc,
                        page,
                        PDPageContentStream.AppendMode.OVERWRITE,
                        true,
                        true
                    ).use { contentStream ->
                        contentStream.drawImage(
                            redactedPageImage,
                            page.cropBox.lowerLeftX,
                            page.cropBox.lowerLeftY,
                            page.cropBox.width,
                            page.cropBox.height
                        )
                    }
                } finally {
                    bitmap.recycle()
                }

                if (sanitizeMetadata) {
                    sanitizeDocumentMetadata(doc)
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

    private fun paintRedactionAreas(
        bitmap: Bitmap,
        cropBox: PDRectangle,
        areas: List<PDRectangle>,
        redactionStyle: String
    ) {
        val scaleX = bitmap.width / cropBox.width
        val scaleY = bitmap.height / cropBox.height
        val paint = Paint().apply {
            color = if (redactionStyle == "white") {
                android.graphics.Color.WHITE
            } else {
                android.graphics.Color.BLACK
            }
            style = Paint.Style.FILL
        }
        val patternPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        val canvas = Canvas(bitmap)

        areas.forEach { area ->
            val left = (area.lowerLeftX - cropBox.lowerLeftX) * scaleX
            val top = (cropBox.upperRightY - area.upperRightY) * scaleY
            val right = (area.upperRightX - cropBox.lowerLeftX) * scaleX
            val bottom = (cropBox.upperRightY - area.lowerLeftY) * scaleY
            val rect = RectF(left, top, right, bottom)
            canvas.drawRect(rect, paint)
            if (redactionStyle == "patterned") {
                var x = rect.left - rect.height()
                while (x < rect.right) {
                    canvas.drawLine(x, rect.bottom, x + rect.height(), rect.top, patternPaint)
                    x += 24f
                }
            }
        }
    }

    private fun sanitizeDocumentMetadata(doc: PDDocument) {
        doc.documentInformation = PDDocumentInformation()
        doc.documentCatalog.metadata = null
        doc.documentCatalog.acroForm = null
        for (page in doc.pages) {
            page.annotations.clear()
        }
    }

    private fun findTextRedactionAreas(
        doc: PDDocument,
        pageIndex: Int,
        textToRedact: String?
    ): List<PDRectangle> {
        val needle = textToRedact?.trim().orEmpty()
        if (needle.isEmpty()) {
            return emptyList()
        }

        val page = doc.getPage(pageIndex)
        val cropBox = page.cropBox
        val matches = mutableListOf<PDRectangle>()
        val stripper = object : PDFTextStripper() {
            override fun writeString(text: String, textPositions: MutableList<TextPosition>) {
                if (!text.contains(needle, ignoreCase = true) || textPositions.isEmpty()) {
                    return
                }

                val left = textPositions.minOf { it.xDirAdj } - REDACTION_PADDING_POINTS
                val right = textPositions.maxOf { it.xDirAdj + it.widthDirAdj } +
                    REDACTION_PADDING_POINTS
                val topFromTop = textPositions.minOf {
                    it.yDirAdj - it.heightDir
                } - REDACTION_PADDING_POINTS
                val bottomFromTop = textPositions.maxOf { it.yDirAdj } +
                    REDACTION_PADDING_POINTS
                val bottom = cropBox.upperRightY - bottomFromTop
                val top = cropBox.upperRightY - topFromTop
                val clampedLeft = left.coerceIn(cropBox.lowerLeftX, cropBox.upperRightX)
                val clampedRight = right.coerceIn(cropBox.lowerLeftX, cropBox.upperRightX)
                val clampedBottom = bottom.coerceIn(cropBox.lowerLeftY, cropBox.upperRightY)
                val clampedTop = top.coerceIn(cropBox.lowerLeftY, cropBox.upperRightY)

                if (clampedRight > clampedLeft && clampedTop > clampedBottom) {
                    matches += PDRectangle(
                        clampedLeft,
                        clampedBottom,
                        clampedRight - clampedLeft,
                        clampedTop - clampedBottom
                    )
                }
            }
        }
        stripper.startPage = pageIndex + 1
        stripper.endPage = pageIndex + 1
        stripper.getText(doc)
        return matches
    }
}
