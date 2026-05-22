package com.example.pdftools

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey

@Serializable data object Settings : NavKey

@Serializable data object Onboarding : NavKey

@Serializable data class ToolDetail(val toolId: String) : NavKey
