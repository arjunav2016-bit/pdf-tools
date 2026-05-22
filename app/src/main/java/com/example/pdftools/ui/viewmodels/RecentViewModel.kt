package com.example.pdftools.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.pdftools.data.PdfTool
import com.example.pdftools.data.RecentFile
import com.example.pdftools.data.RecentFilesRepository
import com.example.pdftools.data.ToolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class RecentViewModel @Inject constructor(
    private val recentFilesRepository: RecentFilesRepository,
    private val toolRepository: ToolRepository
) : ViewModel() {
    val recents: StateFlow<List<RecentFile>>
        get() = recentFilesRepository.recents

    fun getToolById(id: String): PdfTool? = toolRepository.getToolById(id)

    fun clear() {
        recentFilesRepository.clear()
    }
}
