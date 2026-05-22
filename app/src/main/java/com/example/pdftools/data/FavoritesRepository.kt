package com.example.pdftools.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "pdf_tools_favorites"
        private const val KEY_FAVORITES = "favorites"
    }
    
    private val favorites = mutableStateListOf<String>()

    init {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
        favorites.clear()
        favorites.addAll(set)
    }

    fun getFavorites(): SnapshotStateList<String> = favorites

    fun isFavorite(toolId: String): Boolean = favorites.contains(toolId)

    fun toggleFavorite(toolId: String) {
        if (favorites.contains(toolId)) {
            favorites.remove(toolId)
        } else {
            favorites.add(toolId)
        }
        save()
    }

    private fun save() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_FAVORITES, favorites.toSet()).apply()
    }
}
