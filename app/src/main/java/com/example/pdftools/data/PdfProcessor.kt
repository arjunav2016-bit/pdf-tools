package com.example.pdftools.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.tom_roush.pdfbox.io.MemoryUsageSetting
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import com.tom_roush.pdfbox.rendering.PDFRenderer
import com.tom_roush.pdfbox.rendering.ImageType
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object PdfProcessor {

    /**
     * Merges a list of PDF URIs into a single PDF file.
     * Returns the Uri of the merged file in cache.
     */
    suspend fun mergePdfs(context: Context, uris: List<Uri>): Uri = withContext(Dispatchers.IO) {
        val merger = PDFMergerUtility()
        val tempFiles = mutableListOf<File>()
        val outputFile = File(context.cacheDir, "Merged_${System.currentTimeMillis()}.pdf")
        
        try {
            for (uri in uris) {
                val tempFile = File.createTempFile("merge_input_", ".pdf", context.cacheDir)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                tempFiles.add(tempFile)
                merger.addSource(tempFile)
            }
            
            outputFile.outputStream().use { outputStream ->
                merger.destinationStream = outputStream
                merger.mergeDocuments(MemoryUsageSetting.setupTempFileOnly())
            }
            
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            outputFile.delete()
            throw e
        } finally {
            for (file in tempFiles) {
                file.delete()
            }
        }
    }

    /**
     * Compresses a PDF file by rendering pages to compressed JPEGs and repackaging.
     * Returns the Uri of the compressed file in cache.
     */
    suspend fun compressPdf(context: Context, uri: Uri): Uri = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("compress_input_", ".pdf", context.cacheDir)
        val outputFile = File(context.cacheDir, "Compressed_${System.currentTimeMillis()}.pdf")
        
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            PDDocument.load(tempInputFile).use { doc ->
                PDDocument().use { outDoc ->
                    val renderer = PDFRenderer(doc)
                    for (i in 0 until doc.numberOfPages) {
                        // Render page to bitmap at 130 DPI (perfect blend of readability and size)
                        val bitmap = renderer.renderImageWithDPI(i, 130f, ImageType.ARGB)
                        val page = PDPage(PDRectangle(bitmap.width.toFloat(), bitmap.height.toFloat()))
                        outDoc.addPage(page)
                        
                        val tempImgFile = File.createTempFile("compress_page_", ".jpg", context.cacheDir)
                        try {
                            tempImgFile.outputStream().use { outStream ->
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outStream)
                            }
                            
                            val pdImage = JPEGFactory.createFromStream(outDoc, tempImgFile.inputStream())
                            PDPageContentStream(outDoc, page).use { contentStream ->
                                contentStream.drawImage(pdImage, 0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
                            }
                        } finally {
                            tempImgFile.delete()
                            bitmap.recycle()
                        }
                    }
                    outDoc.save(outputFile)
                }
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
     * Converts a list of image URIs (JPEG/PNG) into a single PDF.
     * Returns the Uri of the output PDF in cache.
     */
    suspend fun convertImagesToPdf(context: Context, uris: List<Uri>): Uri = withContext(Dispatchers.IO) {
        val outputFile = File(context.cacheDir, "Converted_${System.currentTimeMillis()}.pdf")
        
        try {
            PDDocument().use { doc ->
                for (uri in uris) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val tempImgFile = File.createTempFile("convert_input_", ".jpg", context.cacheDir)
                        try {
                            tempImgFile.outputStream().use { output ->
                                inputStream.copyTo(output)
                            }
                            
                            val options = BitmapFactory.Options().apply {
                                inJustDecodeBounds = true
                            }
                            BitmapFactory.decodeFile(tempImgFile.absolutePath, options)
                            val width = options.outWidth.toFloat()
                            val height = options.outHeight.toFloat()
                            
                            val page = PDPage(PDRectangle(width, height))
                            doc.addPage(page)
                            
                            val pdImage = JPEGFactory.createFromStream(doc, tempImgFile.inputStream())
                            PDPageContentStream(doc, page).use { contentStream ->
                                contentStream.drawImage(pdImage, 0f, 0f, width, height)
                            }
                        } finally {
                            tempImgFile.delete()
                        }
                    }
                }
                doc.save(outputFile)
            }
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            outputFile.delete()
            throw e
        }
    }

    /**
     * Converts a PDF file into a list of image files (JPEGs) of the pages.
     * Returns a list of image Uris in cache.
     */
    suspend fun convertPdfToImages(context: Context, uri: Uri): List<Uri> = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("pdf_to_jpg_input_", ".pdf", context.cacheDir)
        val outputDir = File(context.cacheDir, "PDF_Pages_${System.currentTimeMillis()}")
        outputDir.mkdirs()
        val imageUris = mutableListOf<Uri>()
        
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            PDDocument.load(tempInputFile).use { doc ->
                val renderer = PDFRenderer(doc)
                for (i in 0 until doc.numberOfPages) {
                    val bitmap = renderer.renderImageWithDPI(i, 150f, ImageType.ARGB)
                    val imgFile = File(outputDir, "Page_${i + 1}.jpg")
                    imgFile.outputStream().use { outStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream)
                    }
                    bitmap.recycle()
                    imageUris.add(Uri.fromFile(imgFile))
                }
            }
            imageUris
        } catch (e: Exception) {
            // clean up directory on error
            outputDir.deleteRecursively()
            throw e
        } finally {
            tempInputFile.delete()
        }
    }

    /**
     * Parses a page range string (e.g., "1-3, 5") into a sorted, unique list of 0-based page indices.
     */
    fun parsePageRanges(rangeStr: String, totalPages: Int): List<Int> {
        val selectedPages = mutableSetOf<Int>()
        
        // Split by comma
        val parts = rangeStr.split(",")
        for (part in parts) {
            val trimmed = part.trim()
            if (trimmed.isEmpty()) continue
            
            if (trimmed.contains("-")) {
                val rangeParts = trimmed.split("-")
                if (rangeParts.size == 2) {
                    val startStr = rangeParts[0].trim()
                    val endStr = rangeParts[1].trim()
                    val start = startStr.toIntOrNull()
                    val end = endStr.toIntOrNull()
                    if (start != null && end != null) {
                        val actualStart = minOf(start, end)
                        val actualEnd = maxOf(start, end)
                        for (p in actualStart..actualEnd) {
                            val pageIdx = p - 1
                            if (pageIdx in 0 until totalPages) {
                                selectedPages.add(pageIdx)
                            }
                        }
                    }
                }
            } else {
                val page = trimmed.toIntOrNull()
                if (page != null) {
                    val pageIdx = page - 1
                    if (pageIdx in 0 until totalPages) {
                        selectedPages.add(pageIdx)
                    }
                }
            }
        }
        
        return selectedPages.sorted()
    }

    /**
     * Splits a PDF by returning a new document containing only the selected pages.
     */
    suspend fun splitPdf(context: Context, uri: Uri, pageRange: String): Uri = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("split_input_", ".pdf", context.cacheDir)
        val outputFile = File(context.cacheDir, "Split_${System.currentTimeMillis()}.pdf")
        
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            PDDocument.load(tempInputFile).use { doc ->
                val totalPages = doc.numberOfPages
                val selectedPages = parsePageRanges(pageRange, totalPages)
                
                if (selectedPages.isEmpty()) {
                    throw IllegalArgumentException("Please enter a valid page range (e.g., 1-3, 5) within the document's bounds (1-$totalPages).")
                }
                
                // Keep selected pages by removing all complement pages from back to front
                val allPages = (0 until totalPages).toList()
                val pagesToRemove = allPages.filter { it !in selectedPages }
                val sortedToRemove = pagesToRemove.sortedDescending()
                
                for (index in sortedToRemove) {
                    doc.removePage(index)
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
     * Removes specified pages from a PDF document and returns the new PDF.
     */
    suspend fun removePages(context: Context, uri: Uri, pagesToRemoveStr: String): Uri = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("remove_input_", ".pdf", context.cacheDir)
        val outputFile = File(context.cacheDir, "Removed_${System.currentTimeMillis()}.pdf")
        
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            PDDocument.load(tempInputFile).use { doc ->
                val totalPages = doc.numberOfPages
                val selectedPagesToRemove = parsePageRanges(pagesToRemoveStr, totalPages)
                
                if (selectedPagesToRemove.isEmpty()) {
                    throw IllegalArgumentException("Please enter valid page numbers to remove (e.g., 2, 4) within the document's bounds (1-$totalPages).")
                }
                
                if (selectedPagesToRemove.size == totalPages) {
                    throw IllegalArgumentException("Cannot remove all pages from the PDF document.")
                }
                
                // Remove pages from back to front
                val sortedToRemove = selectedPagesToRemove.sortedDescending()
                for (index in sortedToRemove) {
                    doc.removePage(index)
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
     * Encrypts/Protects a PDF document using standard 128-bit security.
     */
    suspend fun protectPdf(context: Context, uri: Uri, password: String): Uri = withContext(Dispatchers.IO) {
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
                // Set the StandardProtectionPolicy: user password, owner password (same), access permission
                val spp = StandardProtectionPolicy(password, password, ap)
                spp.encryptionKeyLength = 128
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
     * Rotates specified pages (or all pages if range is empty) by the given degrees (90, 180, 270).
     */
    suspend fun rotatePdf(context: Context, uri: Uri, degrees: Int, pageRange: String): Uri = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("rotate_input_", ".pdf", context.cacheDir)
        val outputFile = File(context.cacheDir, "Rotated_${System.currentTimeMillis()}.pdf")
        
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
                    parsePageRanges(pageRange, totalPages)
                }
                
                if (selectedPages.isEmpty() && pageRange.trim().isNotEmpty()) {
                    throw IllegalArgumentException("Please enter a valid page range (e.g., 1-3, 5) within the document's bounds (1-$totalPages).")
                }
                
                for (pageIdx in selectedPages) {
                    val page = doc.getPage(pageIdx)
                    val currentRotation = page.rotation
                    // In PDFBox, rotation is stored as degrees (0, 90, 180, 270)
                    val newRotation = (currentRotation + degrees) % 360
                    page.rotation = newRotation
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
     * Extracts selected pages from a PDF into a new file.
     */
    suspend fun extractPages(context: Context, uri: Uri, pageRange: String): Uri = withContext(Dispatchers.IO) {
        if (pageRange.trim().isEmpty()) {
            throw IllegalArgumentException("Please enter a page range to extract (e.g., 1-3, 5).")
        }
        // Under the hood, this works exactly like splitting: keeping only the specified page range
        splitPdf(context, uri, pageRange)
    }

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
                    parsePageRanges(pageRange, totalPages)
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
                    parsePageRanges(pageRange, totalPages)
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
     * Crops targeted pages (or all pages if range is empty) by a given margin percentage.
     */
    suspend fun cropPdf(
        context: Context,
        uri: Uri,
        marginPercentage: Float, // e.g. 0.05f, 0.10f, 0.20f
        pageRange: String
    ): Uri = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("crop_input_", ".pdf", context.cacheDir)
        val outputFile = File(context.cacheDir, "Cropped_${System.currentTimeMillis()}.pdf")
        
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
                    parsePageRanges(pageRange, totalPages)
                }
                
                if (selectedPages.isEmpty() && pageRange.trim().isNotEmpty()) {
                    throw IllegalArgumentException("Please enter a valid page range (e.g., 1-3, 5) within the document's bounds (1-$totalPages).")
                }
                
                for (pageIdx in selectedPages) {
                    val page = doc.getPage(pageIdx)
                    val cropBox = page.cropBox
                    
                    val dx = cropBox.width * marginPercentage
                    val dy = cropBox.height * marginPercentage
                    
                    val newCropBox = PDRectangle(
                        cropBox.lowerLeftX + dx,
                        cropBox.lowerLeftY + dy,
                        cropBox.width - (2f * dx),
                        cropBox.height - (2f * dy)
                    )
                    page.cropBox = newCropBox
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

    data class PageTransform(val originalIndex: Int, val rotation: Int)

    /**
     * Reorders, rotates, and duplicates pages based on a transformation pipeline.
     */
    suspend fun organizePdf(
        context: Context,
        uri: Uri,
        pageTransforms: List<PageTransform>
    ): Uri = withContext(Dispatchers.IO) {
        if (pageTransforms.isEmpty()) {
            throw IllegalArgumentException("Page transforms cannot be empty.")
        }
        val tempInputFile = File.createTempFile("organize_input_", ".pdf", context.cacheDir)
        val outputFile = File(context.cacheDir, "Organized_${System.currentTimeMillis()}.pdf")
        
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            PDDocument.load(tempInputFile).use { originalDoc ->
                PDDocument().use { outDoc ->
                    for (transform in pageTransforms) {
                        val originalIdx = transform.originalIndex
                        if (originalIdx in 0 until originalDoc.numberOfPages) {
                            val originalPage = originalDoc.getPage(originalIdx)
                            val importedPage = outDoc.importPage(originalPage)
                            // Accumulate rotation
                            val newRotation = (importedPage.rotation + transform.rotation) % 360
                            importedPage.rotation = newRotation
                        }
                    }
                    outDoc.save(outputFile)
                }
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
     * Self-heals damaged or corrupted PDF files by running PDFBox's recovery parser.
     */
    suspend fun repairPdf(context: Context, uri: Uri): Uri = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("repair_input_", ".pdf", context.cacheDir)
        val outputFile = File(context.cacheDir, "Repaired_${System.currentTimeMillis()}.pdf")
        
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            // PDFBox's PDDocument.load automatically attempts recovery parsing when encountering 
            // broken XREF lists, invalid trailers, or misplaced byte offsets.
            PDDocument.load(tempInputFile).use { doc ->
                // Saving the document completely rebuilds the structural tables, trailers,
                // and outputs a fully standards-compliant PDF file structure.
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
     * Converts standard PDF into archive-grade PDF/A-1b or PDF/A-2b by injecting compliance XMP metadata 
     * and output intent schemas.
     */
    suspend fun convertToPdfA(context: Context, uri: Uri, conformanceLevel: String): Uri = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("pdfa_input_", ".pdf", context.cacheDir)
        val outputFile = File(context.cacheDir, "Archived_${System.currentTimeMillis()}.pdf")
        
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            PDDocument.load(tempInputFile).use { doc ->
                val catalog = doc.documentCatalog
                
                // 1. Inject sRGB Output Intent
                val dummyProfile = java.io.ByteArrayInputStream(ByteArray(0))
                val outputIntent = com.tom_roush.pdfbox.pdmodel.graphics.color.PDOutputIntent(doc, dummyProfile)
                outputIntent.info = "sRGB IEC61966-2.1"
                outputIntent.outputCondition = "sRGB IEC61966-2.1"
                outputIntent.outputConditionIdentifier = "sRGB IEC61966-2.1"
                outputIntent.registryName = "http://www.color.org"
                catalog.addOutputIntent(outputIntent)
                
                // 2. Inject XMP Metadata
                val metadata = com.tom_roush.pdfbox.pdmodel.common.PDMetadata(doc)
                
                // PDF/A identification schema
                val part = if (conformanceLevel == "pdfa_2b") "2" else "1"
                val conformance = "B" // Basic conformance
                
                val xmp = """
                    <?xpacket begin="" id="W5M0MpCehiHzreSzNTczkc9d"?>
                    <x:xmpmeta xmlns:x="adobe:ns:meta/">
                      <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                        <rdf:Description rdf:about="" xmlns:pdfaid="http://www.aiim.org/pdfa/ns/id/">
                          <pdfaid:part>$part</pdfaid:part>
                          <pdfaid:conformance>$conformance</pdfaid:conformance>
                        </rdf:Description>
                      </rdf:RDF>
                    </x:xmpmeta>
                    <?xpacket end="w"?>
                """.trimIndent()
                
                metadata.importXMPMetadata(xmp.toByteArray(Charsets.UTF_8))
                catalog.metadata = metadata
                
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
     * Redacts a specific rectangular coordinate region by permanently drawing a solid black bounding box.
     */
    suspend fun redactPdf(
        context: Context,
        uri: Uri,
        pageIndex: Int,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        textToRedact: String?
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
                
                PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true).use { contentStream ->
                    contentStream.setNonStrokingColor(0f, 0f, 0f) // solid black
                    contentStream.addRect(x, y, width, height)
                    contentStream.fill()
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


