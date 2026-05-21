package com.example.pdftools.data

import androidx.compose.ui.graphics.Color

enum class ToolCategory(
    val displayName: String,
    val accentColor: Color,
    val containerColor: Color,
    val darkAccentColor: Color,
    val darkContainerColor: Color
) {
    ORGANIZE_PDF(
        displayName = "Organize PDF",
        accentColor = Color(0xFFE74C3C),
        containerColor = Color(0xFFFDEDEB),
        darkAccentColor = Color(0xFFFF6B6B),
        darkContainerColor = Color(0xFF3D1F1F)
    ),
    OPTIMIZE_PDF(
        displayName = "Optimize PDF",
        accentColor = Color(0xFF27AE60),
        containerColor = Color(0xFFE8F8EF),
        darkAccentColor = Color(0xFF4CD97B),
        darkContainerColor = Color(0xFF1A3D25)
    ),
    CONVERT_TO_PDF(
        displayName = "Convert to PDF",
        accentColor = Color(0xFFF39C12),
        containerColor = Color(0xFFFEF5E7),
        darkAccentColor = Color(0xFFFFBB33),
        darkContainerColor = Color(0xFF3D2F0F)
    ),
    CONVERT_FROM_PDF(
        displayName = "Convert from PDF",
        accentColor = Color(0xFF2980B9),
        containerColor = Color(0xFFEAF2F8),
        darkAccentColor = Color(0xFF5DADE2),
        darkContainerColor = Color(0xFF1A2D3D)
    ),
    EDIT_PDF(
        displayName = "Edit PDF",
        accentColor = Color(0xFF8E44AD),
        containerColor = Color(0xFFF4ECF7),
        darkAccentColor = Color(0xFFBB6BD9),
        darkContainerColor = Color(0xFF2D1A3D)
    ),
    PDF_SECURITY(
        displayName = "PDF Security",
        accentColor = Color(0xFF16A085),
        containerColor = Color(0xFFE8F6F3),
        darkAccentColor = Color(0xFF1ABC9C),
        darkContainerColor = Color(0xFF0F3D33)
    )
}
