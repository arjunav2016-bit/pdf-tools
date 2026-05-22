package com.example.pdftools.data

import android.content.Context
import com.example.pdftools.data.db.FavoriteDao
import com.example.pdftools.data.db.FavoriteEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Singleton
class FavoritesRepository(
    private val context: Context,
    private val favoriteDao: FavoriteDao,
    private val coroutineScope: CoroutineScope
) {
    @Inject
    constructor(
        @ApplicationContext context: Context,
        favoriteDao: FavoriteDao
    ) : this(context, favoriteDao, CoroutineScope(SupervisorJob() + Dispatchers.IO))

    val favorites: StateFlow<List<String>> = favoriteDao.getAll()
        .map { list -> list.map { it.toolId } }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    init {
        // Silent, automatic migration from old SharedPreferences
        coroutineScope.launch {
            val prefs = context.getSharedPreferences("pdf_tools_favorites", Context.MODE_PRIVATE)
            if (prefs.contains("favorites")) {
                val set = prefs.getStringSet("favorites", null)
                if (set != null) {
                    set.forEach { toolId ->
                        favoriteDao.insert(FavoriteEntity(toolId))
                    }
                }
                // Clear old preferences so we only migrate once
                prefs.edit().clear().apply()
            }
        }
    }

    fun isFavorite(toolId: String): Boolean = favorites.value.contains(toolId)

    fun toggleFavorite(toolId: String) {
        coroutineScope.launch {
            if (isFavorite(toolId)) {
                favoriteDao.delete(FavoriteEntity(toolId))
            } else {
                favoriteDao.insert(FavoriteEntity(toolId))
            }
        }
    }
}
