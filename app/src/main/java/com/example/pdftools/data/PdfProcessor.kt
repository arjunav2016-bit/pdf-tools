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
import com.tom_roush.pdfbox.rendering.PDFRenderer
import com.tom_roush.pdfbox.rendering.ImageType
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
}
