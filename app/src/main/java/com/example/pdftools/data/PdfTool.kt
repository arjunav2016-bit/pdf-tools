package com.example.pdftools.data

import androidx.compose.ui.graphics.vector.ImageVector

data class PdfTool(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val category: ToolCategory,
    val description: String,
    val isImplemented: Boolean = false
)
