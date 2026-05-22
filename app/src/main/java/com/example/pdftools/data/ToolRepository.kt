package com.example.pdftools.data

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.PresentToAll
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.pdftools.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    val allTools: List<PdfTool> by lazy {
        listOf(
            // ── ORGANIZE_PDF ──────────────────────────────────────────────
            PdfTool(
                id = "merge_pdf",
                nameResId = R.string.tool_merge_pdf_name,
                icon = Icons.Filled.ArrowUpward,
                category = ToolCategory.ORGANIZE_PDF,
                descriptionResId = R.string.tool_merge_pdf_desc,
                name = context.getString(R.string.tool_merge_pdf_name),
                description = context.getString(R.string.tool_merge_pdf_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "split_pdf",
                nameResId = R.string.tool_split_pdf_name,
                icon = Icons.Filled.CallSplit,
                category = ToolCategory.ORGANIZE_PDF,
                descriptionResId = R.string.tool_split_pdf_desc,
                name = context.getString(R.string.tool_split_pdf_name),
                description = context.getString(R.string.tool_split_pdf_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "remove_pages",
                nameResId = R.string.tool_remove_pages_name,
                icon = Icons.Filled.Delete,
                category = ToolCategory.ORGANIZE_PDF,
                descriptionResId = R.string.tool_remove_pages_desc,
                name = context.getString(R.string.tool_remove_pages_name),
                description = context.getString(R.string.tool_remove_pages_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "extract_pages",
                nameResId = R.string.tool_extract_pages_name,
                icon = Icons.Filled.InsertDriveFile,
                category = ToolCategory.ORGANIZE_PDF,
                descriptionResId = R.string.tool_extract_pages_desc,
                name = context.getString(R.string.tool_extract_pages_name),
                description = context.getString(R.string.tool_extract_pages_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "organize_pdf",
                nameResId = R.string.tool_organize_pdf_name,
                icon = Icons.Filled.SwapVert,
                category = ToolCategory.ORGANIZE_PDF,
                descriptionResId = R.string.tool_organize_pdf_desc,
                name = context.getString(R.string.tool_organize_pdf_name),
                description = context.getString(R.string.tool_organize_pdf_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "scan_to_pdf",
                nameResId = R.string.tool_scan_to_pdf_name,
                icon = Icons.Filled.DocumentScanner,
                category = ToolCategory.ORGANIZE_PDF,
                descriptionResId = R.string.tool_scan_to_pdf_desc,
                name = context.getString(R.string.tool_scan_to_pdf_name),
                description = context.getString(R.string.tool_scan_to_pdf_desc),
                isImplemented = true
            ),

            // ── OPTIMIZE_PDF ──────────────────────────────────────────────
            PdfTool(
                id = "compress_pdf",
                nameResId = R.string.tool_compress_pdf_name,
                icon = Icons.Filled.Compress,
                category = ToolCategory.OPTIMIZE_PDF,
                descriptionResId = R.string.tool_compress_pdf_desc,
                name = context.getString(R.string.tool_compress_pdf_name),
                description = context.getString(R.string.tool_compress_pdf_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "repair_pdf",
                nameResId = R.string.tool_repair_pdf_name,
                icon = Icons.Filled.Build,
                category = ToolCategory.OPTIMIZE_PDF,
                descriptionResId = R.string.tool_repair_pdf_desc,
                name = context.getString(R.string.tool_repair_pdf_name),
                description = context.getString(R.string.tool_repair_pdf_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "ocr_pdf",
                nameResId = R.string.tool_ocr_pdf_name,
                icon = Icons.Filled.TextFields,
                category = ToolCategory.OPTIMIZE_PDF,
                descriptionResId = R.string.tool_ocr_pdf_desc,
                name = context.getString(R.string.tool_ocr_pdf_name),
                description = context.getString(R.string.tool_ocr_pdf_desc),
                isImplemented = true
            ),

            // ── CONVERT_TO_PDF ────────────────────────────────────────────
            PdfTool(
                id = "jpg_to_pdf",
                nameResId = R.string.tool_jpg_to_pdf_name,
                icon = Icons.Filled.Image,
                category = ToolCategory.CONVERT_TO_PDF,
                descriptionResId = R.string.tool_jpg_to_pdf_desc,
                name = context.getString(R.string.tool_jpg_to_pdf_name),
                description = context.getString(R.string.tool_jpg_to_pdf_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "word_to_pdf",
                nameResId = R.string.tool_word_to_pdf_name,
                icon = Icons.Filled.Description,
                category = ToolCategory.CONVERT_TO_PDF,
                descriptionResId = R.string.tool_word_to_pdf_desc,
                name = context.getString(R.string.tool_word_to_pdf_name),
                description = context.getString(R.string.tool_word_to_pdf_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "ppt_to_pdf",
                nameResId = R.string.tool_ppt_to_pdf_name,
                icon = Icons.Filled.Slideshow,
                category = ToolCategory.CONVERT_TO_PDF,
                descriptionResId = R.string.tool_ppt_to_pdf_desc,
                name = context.getString(R.string.tool_ppt_to_pdf_name),
                description = context.getString(R.string.tool_ppt_to_pdf_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "excel_to_pdf",
                nameResId = R.string.tool_excel_to_pdf_name,
                icon = Icons.Filled.TableChart,
                category = ToolCategory.CONVERT_TO_PDF,
                descriptionResId = R.string.tool_excel_to_pdf_desc,
                name = context.getString(R.string.tool_excel_to_pdf_name),
                description = context.getString(R.string.tool_excel_to_pdf_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "html_to_pdf",
                nameResId = R.string.tool_html_to_pdf_name,
                icon = Icons.Filled.Code,
                category = ToolCategory.CONVERT_TO_PDF,
                descriptionResId = R.string.tool_html_to_pdf_desc,
                name = context.getString(R.string.tool_html_to_pdf_name),
                description = context.getString(R.string.tool_html_to_pdf_desc),
                isImplemented = true
            ),

            // ── CONVERT_FROM_PDF ──────────────────────────────────────────
            PdfTool(
                id = "pdf_to_jpg",
                nameResId = R.string.tool_pdf_to_jpg_name,
                icon = Icons.Filled.PhotoLibrary,
                category = ToolCategory.CONVERT_FROM_PDF,
                descriptionResId = R.string.tool_pdf_to_jpg_desc,
                name = context.getString(R.string.tool_pdf_to_jpg_name),
                description = context.getString(R.string.tool_pdf_to_jpg_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "pdf_to_word",
                nameResId = R.string.tool_pdf_to_word_name,
                icon = Icons.Filled.Article,
                category = ToolCategory.CONVERT_FROM_PDF,
                descriptionResId = R.string.tool_pdf_to_word_desc,
                name = context.getString(R.string.tool_pdf_to_word_name),
                description = context.getString(R.string.tool_pdf_to_word_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "pdf_to_ppt",
                nameResId = R.string.tool_pdf_to_ppt_name,
                icon = Icons.Filled.PresentToAll,
                category = ToolCategory.CONVERT_FROM_PDF,
                descriptionResId = R.string.tool_pdf_to_ppt_desc,
                name = context.getString(R.string.tool_pdf_to_ppt_name),
                description = context.getString(R.string.tool_pdf_to_ppt_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "pdf_to_excel",
                nameResId = R.string.tool_pdf_to_excel_name,
                icon = Icons.Filled.GridOn,
                category = ToolCategory.CONVERT_FROM_PDF,
                descriptionResId = R.string.tool_pdf_to_excel_desc,
                name = context.getString(R.string.tool_pdf_to_excel_name),
                description = context.getString(R.string.tool_pdf_to_excel_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "pdf_to_pdfa",
                nameResId = R.string.tool_pdf_to_pdfa_name,
                icon = Icons.Filled.Verified,
                category = ToolCategory.CONVERT_FROM_PDF,
                descriptionResId = R.string.tool_pdf_to_pdfa_desc,
                name = context.getString(R.string.tool_pdf_to_pdfa_name),
                description = context.getString(R.string.tool_pdf_to_pdfa_desc),
                isImplemented = true
            ),

            // ── EDIT_PDF ──────────────────────────────────────────────────
            PdfTool(
                id = "rotate_pdf",
                nameResId = R.string.tool_rotate_pdf_name,
                icon = Icons.Filled.RotateRight,
                category = ToolCategory.EDIT_PDF,
                descriptionResId = R.string.tool_rotate_pdf_desc,
                name = context.getString(R.string.tool_rotate_pdf_name),
                description = context.getString(R.string.tool_rotate_pdf_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "add_page_numbers",
                nameResId = R.string.tool_add_page_numbers_name,
                icon = Icons.Filled.Pin,
                category = ToolCategory.EDIT_PDF,
                descriptionResId = R.string.tool_add_page_numbers_desc,
                name = context.getString(R.string.tool_add_page_numbers_name),
                description = context.getString(R.string.tool_add_page_numbers_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "add_watermark",
                nameResId = R.string.tool_add_watermark_name,
                icon = Icons.Filled.BrandingWatermark,
                category = ToolCategory.EDIT_PDF,
                descriptionResId = R.string.tool_add_watermark_desc,
                name = context.getString(R.string.tool_add_watermark_name),
                description = context.getString(R.string.tool_add_watermark_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "crop_pdf",
                nameResId = R.string.tool_crop_pdf_name,
                icon = Icons.Filled.Crop,
                category = ToolCategory.EDIT_PDF,
                descriptionResId = R.string.tool_crop_pdf_desc,
                name = context.getString(R.string.tool_crop_pdf_name),
                description = context.getString(R.string.tool_crop_pdf_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "edit_pdf",
                nameResId = R.string.tool_edit_pdf_name,
                icon = Icons.Filled.Edit,
                category = ToolCategory.EDIT_PDF,
                descriptionResId = R.string.tool_edit_pdf_desc,
                name = context.getString(R.string.tool_edit_pdf_name),
                description = context.getString(R.string.tool_edit_pdf_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "pdf_forms",
                nameResId = R.string.tool_pdf_forms_name,
                icon = Icons.Filled.ListAlt,
                category = ToolCategory.EDIT_PDF,
                descriptionResId = R.string.tool_pdf_forms_desc,
                name = context.getString(R.string.tool_pdf_forms_name),
                description = context.getString(R.string.tool_pdf_forms_desc),
                isImplemented = true
            ),

            // ── PDF_SECURITY ──────────────────────────────────────────────
            PdfTool(
                id = "unlock_pdf",
                nameResId = R.string.tool_unlock_pdf_name,
                icon = Icons.Filled.LockOpen,
                category = ToolCategory.PDF_SECURITY,
                descriptionResId = R.string.tool_unlock_pdf_desc,
                name = context.getString(R.string.tool_unlock_pdf_name),
                description = context.getString(R.string.tool_unlock_pdf_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "protect_pdf",
                nameResId = R.string.tool_protect_pdf_name,
                icon = Icons.Filled.Lock,
                category = ToolCategory.PDF_SECURITY,
                descriptionResId = R.string.tool_protect_pdf_desc,
                name = context.getString(R.string.tool_protect_pdf_name),
                description = context.getString(R.string.tool_protect_pdf_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "sign_pdf",
                nameResId = R.string.tool_sign_pdf_name,
                icon = Icons.Filled.Draw,
                category = ToolCategory.PDF_SECURITY,
                descriptionResId = R.string.tool_sign_pdf_desc,
                name = context.getString(R.string.tool_sign_pdf_name),
                description = context.getString(R.string.tool_sign_pdf_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "redact_pdf",
                nameResId = R.string.tool_redact_pdf_name,
                icon = Icons.Filled.VisibilityOff,
                category = ToolCategory.PDF_SECURITY,
                descriptionResId = R.string.tool_redact_pdf_desc,
                name = context.getString(R.string.tool_redact_pdf_name),
                description = context.getString(R.string.tool_redact_pdf_desc),
                isImplemented = true
            ),
            PdfTool(
                id = "compare_pdf",
                nameResId = R.string.tool_compare_pdf_name,
                icon = Icons.Filled.Compare,
                category = ToolCategory.PDF_SECURITY,
                descriptionResId = R.string.tool_compare_pdf_desc,
                name = context.getString(R.string.tool_compare_pdf_name),
                description = context.getString(R.string.tool_compare_pdf_desc),
                isImplemented = true
            )
        )
    }

    val toolsByCategory: Map<ToolCategory, List<PdfTool>> by lazy {
        allTools.groupBy { it.category }
    }

    fun getToolById(id: String): PdfTool? = allTools.find { it.id == id }
}
