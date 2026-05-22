package com.example.pdftools.data.processors

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.io.MemoryUsageSetting
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

import com.example.pdftools.utils.PageRangeUtils
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Handles PDF organization operations: merge, split, remove, extract, organize, rotate.
 */
@Singleton
class OrganizeProcessor @Inject constructor() {

    data class PageTransform(val originalIndex: Int, val rotation: Int)

    /**
     * Merges a list of PDF URIs into a single PDF file.
     * Returns the Uri of the merged file in cache.
     */
    suspend fun mergePdfs(
        context: Context,
        uris: List<Uri>,
        onProgress: ((Float) -> Unit)? = null
    ): Uri = withContext(Dispatchers.IO) {
        val merger = PDFMergerUtility()
        val tempFiles = mutableListOf<File>()
        val outputFile = File(context.cacheDir, "Merged_${System.currentTimeMillis()}.pdf")
        
        try {
            for ((index, uri) in uris.withIndex()) {
                currentCoroutineContext().ensureActive()
                val tempFile = File.createTempFile("merge_input_", ".pdf", context.cacheDir)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                tempFiles.add(tempFile)
                merger.addSource(tempFile)
                onProgress?.invoke((index + 1f) / (uris.size + 1f))
            }
            
            outputFile.outputStream().use { outputStream ->
                merger.destinationStream = outputStream
                merger.mergeDocuments(MemoryUsageSetting.setupTempFileOnly())
            }
            onProgress?.invoke(1f)
            
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
                val selectedPages = PageRangeUtils.parsePageRanges(pageRange, totalPages)
                
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
                val selectedPagesToRemove = PageRangeUtils.parsePageRanges(pagesToRemoveStr, totalPages)
                
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
                    PageRangeUtils.parsePageRanges(pageRange, totalPages)
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
}
