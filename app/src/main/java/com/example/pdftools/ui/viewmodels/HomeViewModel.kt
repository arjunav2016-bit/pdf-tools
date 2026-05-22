package com.example.pdftools.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.pdftools.data.PdfTool
import com.example.pdftools.data.ToolCategory
import com.example.pdftools.data.ToolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val toolRepository: ToolRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allTools: List<PdfTool>
        get() = toolRepository.allTools

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getFilteredTools(query: String): Map<ToolCategory, List<PdfTool>> {
        return if (query.isBlank()) {
            toolRepository.toolsByCategory
        } else {
            toolRepository.allTools
                .filter { it.name.contains(query, ignoreCase = true) }
                .groupBy { it.category }
        }
    }
}
