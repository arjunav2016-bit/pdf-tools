package com.example.pdftools.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.pdftools.data.PdfTool
import java.io.File
import java.util.Locale

/**
 * Helper functions for ToolScreen file output handling.
 */

internal fun openOutputUris(context: Context, tool: PdfTool, uris: List<Uri>) {
    try {
        val targetUri = uris.firstOrNull() ?: return
        val file = File(targetUri.path ?: "")
        if (!file.exists()) {
            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
            return
        }
        val contentUri = FileProvider.getUriForFile(context, "com.example.pdftools.fileprovider", file)
        val mimeType = getOutputMimeType(tool, targetUri)
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Open File"))
    } catch (e: Exception) {
        Toast.makeText(context, "Open failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

internal fun shareOutputUris(context: Context, tool: PdfTool, uris: List<Uri>) {
    try {
        if (uris.isEmpty()) return
        
        if (uris.size == 1) {
            val uri = uris.first()
            val file = File(uri.path ?: "")
            val contentUri = FileProvider.getUriForFile(context, "com.example.pdftools.fileprovider", file)
            val mimeType = getOutputMimeType(tool, uri)
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share File"))
        } else {
            // Share multiple files
            val arrayList = ArrayList<Uri>()
            uris.forEach { uri ->
                val file = File(uri.path ?: "")
                arrayList.add(FileProvider.getUriForFile(context, "com.example.pdftools.fileprovider", file))
            }
            
            val mimeType = if (uris.all { getOutputMimeType(tool, it).startsWith("image/") }) {
                getOutputMimeType(tool, uris.first())
            } else {
                "application/octet-stream"
            }

            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = mimeType
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayList)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Files"))
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Share failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

internal fun saveOutputUrisToDownloads(context: Context, tool: PdfTool, uris: List<Uri>) {
    try {
        val resolver = context.contentResolver
        var count = 0
        
        uris.forEachIndexed { index, uri ->
            val file = File(uri.path ?: "")
            val extension = getOutputExtension(tool, uri)
            val mimeType = getOutputMimeType(tool, uri)
            val displayTitle = if (tool.id == "pdf_to_jpg") "Page_${index + 1}_${System.currentTimeMillis()}.$extension" else "${tool.name.replace(" ", "_")}_${System.currentTimeMillis()}.$extension"
            
            file.inputStream().use { inputStream ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, displayTitle)
                        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/PDFTools")
                    }
                    val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
                    val destUri = resolver.insert(collection, contentValues) ?: throw Exception("Failed to create Downloads entry")
                    
                    resolver.openOutputStream(destUri)?.use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    count++
                } else {
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val pdfToolsDir = File(downloadsDir, "PDFTools")
                    pdfToolsDir.mkdirs()
                    val destFile = File(pdfToolsDir, displayTitle)
                    
                    destFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    count++
                }
            }
        }
        
        Toast.makeText(context, "Successfully exported $count file(s) to Downloads/PDFTools", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Export failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

internal fun getOutputExtension(tool: PdfTool, uri: Uri): String {
    val pathExt = uri.path?.substringAfterLast('.', missingDelimiterValue = "")?.lowercase(Locale.getDefault())
    if (!pathExt.isNullOrEmpty()) return pathExt

    return when (tool.id) {
        "pdf_to_jpg" -> "jpg"
        "pdf_to_word" -> "docx"
        "pdf_to_ppt" -> "pptx"
        "pdf_to_excel" -> "xlsx"
        "ocr_pdf", "compare_pdf" -> "txt"
        else -> "pdf"
    }
}

internal fun getOutputMimeType(tool: PdfTool, uri: Uri): String {
    return when (getOutputExtension(tool, uri)) {
        "txt" -> "text/plain"
        "jpg", "jpeg" -> "image/jpeg"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        "pdf" -> "application/pdf"
        else -> "application/octet-stream"
    }
}

internal fun getActionButtonText(toolId: String): String {
    return when (toolId) {
        "merge_pdf" -> "Merge PDFs"
        "compress_pdf" -> "Compress PDF"
        "jpg_to_pdf" -> "Convert to PDF"
        "pdf_to_jpg" -> "Convert to JPG"
        "split_pdf" -> "Split PDF"
        "remove_pages" -> "Remove Pages"
        "protect_pdf" -> "Protect PDF"
        "unlock_pdf" -> "Unlock PDF"
        "extract_pages" -> "Extract Pages"
        "rotate_pdf" -> "Rotate PDF"
        "add_watermark" -> "Add Watermark"
        "add_page_numbers" -> "Add Page Numbers"
        "crop_pdf" -> "Crop PDF"
        "organize_pdf" -> "Organize PDF"
        "repair_pdf" -> "Repair PDF"
        "pdf_to_pdfa" -> "Convert to PDF/A"
        "sign_pdf" -> "Sign PDF"
        "redact_pdf" -> "Redact PDF"
        "pdf_forms" -> "Fill PDF Forms"
        "scan_to_pdf" -> "Assemble Scan"
        "ocr_pdf" -> "Extract Text"
        "compare_pdf" -> "Compare PDFs"
        "edit_pdf" -> "Save Annotations"
        "html_to_pdf" -> "Convert to PDF"
        "word_to_pdf" -> "Convert to PDF"
        "ppt_to_pdf" -> "Convert to PDF"
        "excel_to_pdf" -> "Convert to PDF"
        "pdf_to_word" -> "Convert to Word"
        "pdf_to_ppt" -> "Convert to PowerPoint"
        "pdf_to_excel" -> "Convert to Excel"
        else -> "Process"
    }
}
