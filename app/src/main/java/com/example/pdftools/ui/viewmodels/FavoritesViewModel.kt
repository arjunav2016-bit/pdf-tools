package com.example.pdftools.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdftools.data.FavoritesRepository
import com.example.pdftools.data.PdfTool
import com.example.pdftools.data.ToolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
    private val toolRepository: ToolRepository
) : ViewModel() {
    val favoriteTools: StateFlow<List<PdfTool>> = favoritesRepository.favorites
        .map { favoriteIds ->
            favoriteIds.mapNotNull { toolRepository.getToolById(it) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
