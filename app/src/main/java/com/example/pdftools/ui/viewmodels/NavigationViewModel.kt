package com.example.pdftools.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdftools.data.PdfTool
import com.example.pdftools.data.ToolRepository
import com.example.pdftools.data.UserPreferences
import com.example.pdftools.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val toolRepository: ToolRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    val preferences: Flow<UserPreferences> = userPreferencesRepository.preferences

    fun getToolById(id: String): PdfTool? = toolRepository.getToolById(id)

    fun setOnboardingCompleted() {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingCompleted(true)
        }
    }
}
