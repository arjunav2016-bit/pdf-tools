package com.example.pdftools.data

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class PdfTool(
    val id: String,
    @StringRes val nameResId: Int,
    val icon: ImageVector,
    val category: ToolCategory,
    @StringRes val descriptionResId: Int,
    val name: String,
    val description: String,
    val isImplemented: Boolean = false
)
