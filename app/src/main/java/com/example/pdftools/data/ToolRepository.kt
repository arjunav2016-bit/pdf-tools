package com.example.pdftools.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.PresentToAll
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VisibilityOff

object ToolRepository {

    val allTools: List<PdfTool> = listOf(
        // ── ORGANIZE_PDF ──────────────────────────────────────────────
        PdfTool(
            id = "merge_pdf",
            name = "Merge PDF",
            icon = Icons.Filled.MergeType,
            category = ToolCategory.ORGANIZE_PDF,
            description = "Combine multiple PDF files into a single document.",
            isImplemented = true
        ),
        PdfTool(
            id = "split_pdf",
            name = "Split PDF",
            icon = Icons.Filled.Splitscreen,
            category = ToolCategory.ORGANIZE_PDF,
            description = "Split a PDF into multiple smaller documents by page ranges."
        ),
        PdfTool(
            id = "remove_pages",
            name = "Remove Pages",
            icon = Icons.Filled.RemoveCircleOutline,
            category = ToolCategory.ORGANIZE_PDF,
            description = "Delete specific pages from a PDF document."
        ),
        PdfTool(
            id = "extract_pages",
            name = "Extract Pages",
            icon = Icons.Filled.ContentCut,
            category = ToolCategory.ORGANIZE_PDF,
            description = "Extract selected pages from a PDF into a new file."
        ),
        PdfTool(
            id = "organize_pdf",
            name = "Organize PDF",
            icon = Icons.Filled.SwapVert,
            category = ToolCategory.ORGANIZE_PDF,
            description = "Reorder, rotate, and rearrange pages within a PDF."
        ),
        PdfTool(
            id = "scan_to_pdf",
            name = "Scan to PDF",
            icon = Icons.Filled.DocumentScanner,
            category = ToolCategory.ORGANIZE_PDF,
            description = "Scan physical documents using your camera and save them as PDF."
        ),

        // ── OPTIMIZE_PDF ──────────────────────────────────────────────
        PdfTool(
            id = "compress_pdf",
            name = "Compress PDF",
            icon = Icons.Filled.Compress,
            category = ToolCategory.OPTIMIZE_PDF,
            description = "Reduce the file size of a PDF while preserving quality.",
            isImplemented = true
        ),
        PdfTool(
            id = "repair_pdf",
            name = "Repair PDF",
            icon = Icons.Filled.Build,
            category = ToolCategory.OPTIMIZE_PDF,
            description = "Fix corrupted or damaged PDF files and recover content."
        ),
        PdfTool(
            id = "ocr_pdf",
            name = "OCR PDF",
            icon = Icons.Filled.TextFields,
            category = ToolCategory.OPTIMIZE_PDF,
            description = "Recognize and extract text from scanned PDF documents."
        ),

        // ── CONVERT_TO_PDF ────────────────────────────────────────────
        PdfTool(
            id = "jpg_to_pdf",
            name = "JPG to PDF",
            icon = Icons.Filled.Image,
            category = ToolCategory.CONVERT_TO_PDF,
            description = "Convert JPG images into a PDF document.",
            isImplemented = true
        ),
        PdfTool(
            id = "word_to_pdf",
            name = "WORD to PDF",
            icon = Icons.Filled.Description,
            category = ToolCategory.CONVERT_TO_PDF,
            description = "Convert Microsoft Word documents into PDF format."
        ),
        PdfTool(
            id = "ppt_to_pdf",
            name = "POWERPOINT to PDF",
            icon = Icons.Filled.Slideshow,
            category = ToolCategory.CONVERT_TO_PDF,
            description = "Convert PowerPoint presentations into PDF format."
        ),
        PdfTool(
            id = "excel_to_pdf",
            name = "EXCEL to PDF",
            icon = Icons.Filled.TableChart,
            category = ToolCategory.CONVERT_TO_PDF,
            description = "Convert Excel spreadsheets into PDF format."
        ),
        PdfTool(
            id = "html_to_pdf",
            name = "HTML to PDF",
            icon = Icons.Filled.Code,
            category = ToolCategory.CONVERT_TO_PDF,
            description = "Convert web pages and HTML content into PDF documents."
        ),

        // ── CONVERT_FROM_PDF ──────────────────────────────────────────
        PdfTool(
            id = "pdf_to_jpg",
            name = "PDF to JPG",
            icon = Icons.Filled.PhotoLibrary,
            category = ToolCategory.CONVERT_FROM_PDF,
            description = "Convert PDF pages into high-quality JPG images.",
            isImplemented = true
        ),
        PdfTool(
            id = "pdf_to_word",
            name = "PDF to WORD",
            icon = Icons.Filled.Article,
            category = ToolCategory.CONVERT_FROM_PDF,
            description = "Convert PDF documents into editable Word files."
        ),
        PdfTool(
            id = "pdf_to_ppt",
            name = "PDF to POWERPOINT",
            icon = Icons.Filled.PresentToAll,
            category = ToolCategory.CONVERT_FROM_PDF,
            description = "Convert PDF files into editable PowerPoint presentations."
        ),
        PdfTool(
            id = "pdf_to_excel",
            name = "PDF to EXCEL",
            icon = Icons.Filled.GridOn,
            category = ToolCategory.CONVERT_FROM_PDF,
            description = "Extract tables from PDF and convert them into Excel spreadsheets."
        ),
        PdfTool(
            id = "pdf_to_pdfa",
            name = "PDF to PDF/A",
            icon = Icons.Filled.Verified,
            category = ToolCategory.CONVERT_FROM_PDF,
            description = "Convert PDF files into PDF/A format for long-term archival."
        ),

        // ── EDIT_PDF ──────────────────────────────────────────────────
        PdfTool(
            id = "rotate_pdf",
            name = "Rotate PDF",
            icon = Icons.Filled.RotateRight,
            category = ToolCategory.EDIT_PDF,
            description = "Rotate PDF pages by 90, 180, or 270 degrees."
        ),
        PdfTool(
            id = "add_page_numbers",
            name = "Add Page Numbers",
            icon = Icons.Filled.Pin,
            category = ToolCategory.EDIT_PDF,
            description = "Insert page numbers at custom positions in your PDF."
        ),
        PdfTool(
            id = "add_watermark",
            name = "Add Watermark",
            icon = Icons.Filled.BrandingWatermark,
            category = ToolCategory.EDIT_PDF,
            description = "Overlay text or image watermarks onto PDF pages."
        ),
        PdfTool(
            id = "crop_pdf",
            name = "Crop PDF",
            icon = Icons.Filled.Crop,
            category = ToolCategory.EDIT_PDF,
            description = "Trim margins and resize the visible area of PDF pages."
        ),
        PdfTool(
            id = "edit_pdf",
            name = "Edit PDF",
            icon = Icons.Filled.Edit,
            category = ToolCategory.EDIT_PDF,
            description = "Modify text, images, and annotations directly in a PDF."
        ),
        PdfTool(
            id = "pdf_forms",
            name = "PDF Forms",
            icon = Icons.Filled.ListAlt,
            category = ToolCategory.EDIT_PDF,
            description = "Create and fill interactive form fields in PDF documents."
        ),

        // ── PDF_SECURITY ──────────────────────────────────────────────
        PdfTool(
            id = "unlock_pdf",
            name = "Unlock PDF",
            icon = Icons.Filled.LockOpen,
            category = ToolCategory.PDF_SECURITY,
            description = "Remove password protection and restrictions from a PDF."
        ),
        PdfTool(
            id = "protect_pdf",
            name = "Protect PDF",
            icon = Icons.Filled.Lock,
            category = ToolCategory.PDF_SECURITY,
            description = "Add password encryption and permissions to secure a PDF."
        ),
        PdfTool(
            id = "sign_pdf",
            name = "Sign PDF",
            icon = Icons.Filled.Draw,
            category = ToolCategory.PDF_SECURITY,
            description = "Add digital or handwritten signatures to a PDF document."
        ),
        PdfTool(
            id = "redact_pdf",
            name = "Redact PDF",
            icon = Icons.Filled.VisibilityOff,
            category = ToolCategory.PDF_SECURITY,
            description = "Permanently remove sensitive information from a PDF."
        ),
        PdfTool(
            id = "compare_pdf",
            name = "Compare PDF",
            icon = Icons.Filled.Compare,
            category = ToolCategory.PDF_SECURITY,
            description = "Highlight differences between two PDF documents side by side."
        )
    )

    val toolsByCategory: Map<ToolCategory, List<PdfTool>> =
        allTools.groupBy { it.category }

    fun getToolById(id: String): PdfTool? = allTools.find { it.id == id }
}
