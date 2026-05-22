package com.example.pdftools.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdftools.data.SaveLocation
import com.example.pdftools.data.ThemeMode
import com.example.pdftools.data.UserPreferences
import com.example.pdftools.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    val preferences: StateFlow<UserPreferences> = userPreferencesRepository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = UserPreferences()
    )

    private val _cacheSizeBytes = MutableStateFlow(0L)
    val cacheSizeBytes: StateFlow<Long> = _cacheSizeBytes.asStateFlow()

    init {
        refreshCacheSize()
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.updateThemeMode(themeMode)
        }
    }

    fun updateCompressionQuality(quality: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updateCompressionQuality(quality)
        }
    }

    fun updateExportDpi(dpi: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updateExportDpi(dpi)
        }
    }

    fun updateDefaultSaveLocation(saveLocation: SaveLocation) {
        viewModelScope.launch {
            userPreferencesRepository.updateDefaultSaveLocation(saveLocation)
        }
    }

    fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            context.cacheDir.listFiles()?.forEach(File::deleteRecursively)
            _cacheSizeBytes.value = calculateCacheSize()
        }
    }

    fun refreshCacheSize() {
        viewModelScope.launch(Dispatchers.IO) {
            _cacheSizeBytes.value = calculateCacheSize()
        }
    }

    private suspend fun calculateCacheSize(): Long = withContext(Dispatchers.IO) {
        context.cacheDir.walkTopDown()
            .filter(File::isFile)
            .sumOf(File::length)
    }
}
