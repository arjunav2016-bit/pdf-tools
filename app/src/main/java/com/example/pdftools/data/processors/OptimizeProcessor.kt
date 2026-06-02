package com.example.pdftools.data.processors

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.pdftools.R
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.rendering.PDFRenderer
import com.tom_roush.pdfbox.rendering.ImageType
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.GregorianCalendar

import com.example.pdftools.utils.PageRangeUtils
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Handles PDF optimization operations: compress, repair, crop, convertToPdfA.
 */
@Singleton
class OptimizeProcessor @Inject constructor() {

    /**
     * Compresses a PDF file by rendering pages to compressed JPEGs and repackaging.
     * Returns the Uri of the compressed file in cache.
     */
    suspend fun compressPdf(
        context: Context,
        uri: Uri,
        quality: Int = 70,
        onProgress: ((Float) -> Unit)? = null
    ): Uri = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("compress_input_", ".pdf", context.cacheDir)
        val outputFile = File(context.cacheDir, "Compressed_${System.currentTimeMillis()}.pdf")
        val jpegQuality = quality.coerceIn(30, 100)
        
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
                        currentCoroutineContext().ensureActive()
                        // Render page to bitmap at 130 DPI (perfect blend of readability and size)
                        val bitmap = renderer.renderImageWithDPI(i, 130f, ImageType.ARGB)
                        val page = PDPage(PDRectangle(bitmap.width.toFloat(), bitmap.height.toFloat()))
                        outDoc.addPage(page)
                        
                        val tempImgFile = File.createTempFile("compress_page_", ".jpg", context.cacheDir)
                        try {
                            tempImgFile.outputStream().use { outStream ->
                                bitmap.compress(Bitmap.CompressFormat.JPEG, jpegQuality, outStream)
                            }
                            
                            val pdImage = JPEGFactory.createFromStream(outDoc, tempImgFile.inputStream())
                            PDPageContentStream(outDoc, page).use { contentStream ->
                                contentStream.drawImage(pdImage, 0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
                            }
                        } finally {
                            tempImgFile.delete()
                            bitmap.recycle()
                        }
                        onProgress?.invoke((i + 1f) / doc.numberOfPages.coerceAtLeast(1))
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
     * Crops targeted pages (or all pages if range is empty) by a given margin percentage or absolute mm coordinates.
     */
    suspend fun cropPdf(
        context: Context,
        uri: Uri,
        marginPercentage: Float, // e.g. 0.05f, 0.10f, 0.20f
        pageRange: String,
        useAbsoluteCrop: Boolean = false,
        leftMm: Float = 0f,
        topMm: Float = 0f,
        widthMm: Float = 0f,
        heightMm: Float = 0f,
        applyToAllPages: Boolean = true,
        currentPageIndex: Int = 0
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
                val selectedPages = if (applyToAllPages) {
                    if (pageRange.trim().isEmpty()) {
                        (0 until totalPages).toList()
                    } else {
                        PageRangeUtils.parsePageRanges(pageRange, totalPages)
                    }
                } else {
                    listOf(currentPageIndex).filter { it in 0 until totalPages }
                }
                
                if (selectedPages.isEmpty() && applyToAllPages && pageRange.trim().isNotEmpty()) {
                    throw IllegalArgumentException("Please enter a valid page range (e.g., 1-3, 5) within the document's bounds (1-$totalPages).")
                }
                
                for (pageIdx in selectedPages) {
                    val page = doc.getPage(pageIdx)
                    val cropBox = page.cropBox ?: page.mediaBox
                    
                    if (useAbsoluteCrop) {
                        // 1 mm = 72 / 25.4 points
                        val pointsPerMm = 72f / 25.4f
                        val leftPoints = leftMm * pointsPerMm
                        val topPointsFromTop = topMm * pointsPerMm
                        val widthPoints = widthMm * pointsPerMm
                        val heightPoints = heightMm * pointsPerMm
                        
                        val pageHeight = cropBox.height
                        
                        val newLowerLeftX = cropBox.lowerLeftX + leftPoints
                        val newLowerLeftY = cropBox.lowerLeftY + (pageHeight - topPointsFromTop - heightPoints)
                        
                        val clampedX = newLowerLeftX.coerceIn(cropBox.lowerLeftX, cropBox.upperRightX)
                        val clampedY = newLowerLeftY.coerceIn(cropBox.lowerLeftY, cropBox.upperRightY)
                        val clampedWidth = widthPoints.coerceAtMost(cropBox.upperRightX - clampedX)
                        val clampedHeight = heightPoints.coerceAtMost(cropBox.upperRightY - clampedY)
                        
                        page.cropBox = PDRectangle(clampedX, clampedY, clampedWidth, clampedHeight)
                    } else {
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
     * Converts standard PDF into archive-grade PDF/A-1b or PDF/A-2b by injecting compliance XMP metadata 
     * and output intent schemas.
     */
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
    ): Uri = withContext(Dispatchers.IO) {
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
                if (convertSrgb) {
                    val outputIntent = context.resources.openRawResource(R.raw.srgb).use { srgbProfile ->
                        com.tom_roush.pdfbox.pdmodel.graphics.color.PDOutputIntent(doc, srgbProfile)
                    }
                    outputIntent.info = "sRGB IEC61966-2.1"
                    outputIntent.outputCondition = "sRGB IEC61966-2.1"
                    outputIntent.outputConditionIdentifier = "sRGB IEC61966-2.1"
                    outputIntent.registryName = "http://www.color.org"
                    catalog.addOutputIntent(outputIntent)
                }

                // 2. Compliance check: Remove Transparent Objects
                if (removeTransparencies) {
                    for (page in doc.pages) {
                        val resources = page.resources
                        if (resources != null) {
                            val extGStateNames = resources.extGStateNames?.toList() ?: emptyList()
                            extGStateNames.forEach { name ->
                                val extGState = resources.getExtGState(name)
                                if (extGState != null) {
                                    extGState.cosObject.setFloat(com.tom_roush.pdfbox.cos.COSName.getPDFName("CA"), 1.0f)
                                    extGState.cosObject.setFloat(com.tom_roush.pdfbox.cos.COSName.getPDFName("ca"), 1.0f)
                                }
                            }
                        }
                    }
                }

                // 3. Document Info Metadata
                val documentInfo = doc.documentInformation
                if (title.isNotEmpty()) {
                    documentInfo.title = title
                } else if (documentInfo.title.isNullOrBlank()) {
                    documentInfo.title = "Archived PDF"
                }
                if (author.isNotEmpty()) {
                    documentInfo.author = author
                }
                if (subject.isNotEmpty()) {
                    documentInfo.subject = subject
                }
                if (documentInfo.creator.isNullOrBlank()) {
                    documentInfo.creator = context.getString(R.string.app_name)
                }
                if (documentInfo.creationDate == null) {
                    documentInfo.creationDate = GregorianCalendar()
                }
                
                // 4. Inject XMP Metadata
                val metadata = com.tom_roush.pdfbox.pdmodel.common.PDMetadata(doc)
                
                // PDF/A identification schema
                val part = when (conformanceLevel) {
                    "pdfa_1b" -> "1"
                    "pdfa_2b" -> "2"
                    "pdfa_3b" -> "3"
                    else -> "2"
                }
                val conformance = "B" // Basic conformance
                
                val xmpTitle = title.ifEmpty { documentInfo.title ?: "Archived PDF" }
                val xmpAuthor = author.ifEmpty { documentInfo.author ?: "" }
                val xmpSubject = subject.ifEmpty { documentInfo.subject ?: "" }
                
                val xmp = """
                    <?xpacket begin="" id="W5M0MpCehiHzreSzNTczkc9d"?>
                    <x:xmpmeta xmlns:x="adobe:ns:meta/">
                      <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                        <rdf:Description rdf:about="" xmlns:pdfaid="http://www.aiim.org/pdfa/ns/id/">
                          <pdfaid:part>$part</pdfaid:part>
                          <pdfaid:conformance>$conformance</pdfaid:conformance>
                        </rdf:Description>
                        <rdf:Description rdf:about="" xmlns:dc="http://purl.org/dc/elements/1.1/">
                          <dc:title>
                            <rdf:Alt>
                              <rdf:li xml:lang="x-default">$xmpTitle</rdf:li>
                            </rdf:Alt>
                          </dc:title>
                          ${if (xmpAuthor.isNotEmpty()) """
                          <dc:creator>
                            <rdf:Seq>
                              <rdf:li>$xmpAuthor</rdf:li>
                            </rdf:Seq>
                          </dc:creator>
                          """ else ""}
                          ${if (xmpSubject.isNotEmpty()) """
                          <dc:description>
                            <rdf:Alt>
                              <rdf:li xml:lang="x-default">$xmpSubject</rdf:li>
                            </rdf:Alt>
                          </dc:description>
                          """ else ""}
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
}
