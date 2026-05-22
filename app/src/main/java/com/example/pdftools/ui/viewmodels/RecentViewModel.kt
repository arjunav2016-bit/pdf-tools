package com.example.pdftools.ui.viewmodels

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.example.pdftools.data.RecentFile
import com.example.pdftools.data.RecentFilesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RecentViewModel @Inject constructor(
    private val recentFilesRepository: RecentFilesRepository
) : ViewModel() {
    val recents: SnapshotStateList<RecentFile>
        get() = recentFilesRepository.getRecents()

    fun clear() {
        recentFilesRepository.clear()
    }
}
