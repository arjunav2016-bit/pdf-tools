package com.example.pdftools.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.userPreferencesStore by preferencesDataStore(name = "user_preferences")

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class SaveLocation {
    INTERNAL,
    DOWNLOADS
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val compressionQuality: Int = 70,
    val exportDpi: Int = 150,
    val defaultSaveLocation: SaveLocation = SaveLocation.INTERNAL
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val themeMode = stringPreferencesKey("theme_mode")
        val compressionQuality = intPreferencesKey("compression_quality")
        val exportDpi = intPreferencesKey("export_dpi")
        val defaultSaveLocation = stringPreferencesKey("default_save_location")
    }

    val preferences: Flow<UserPreferences> = context.userPreferencesStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map(::mapPreferences)

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        context.userPreferencesStore.edit { preferences ->
            preferences[Keys.themeMode] = themeMode.name
        }
    }

    suspend fun updateCompressionQuality(quality: Int) {
        context.userPreferencesStore.edit { preferences ->
            preferences[Keys.compressionQuality] = quality.coerceIn(30, 100)
        }
    }

    suspend fun updateExportDpi(dpi: Int) {
        context.userPreferencesStore.edit { preferences ->
            preferences[Keys.exportDpi] = dpi.coerceIn(72, 300)
        }
    }

    suspend fun updateDefaultSaveLocation(saveLocation: SaveLocation) {
        context.userPreferencesStore.edit { preferences ->
            preferences[Keys.defaultSaveLocation] = saveLocation.name
        }
    }

    private fun mapPreferences(preferences: Preferences): UserPreferences {
        return UserPreferences(
            themeMode = preferences[Keys.themeMode].toEnumOrDefault(ThemeMode.SYSTEM),
            compressionQuality = preferences[Keys.compressionQuality]?.coerceIn(30, 100)
                ?: UserPreferences().compressionQuality,
            exportDpi = preferences[Keys.exportDpi]?.coerceIn(72, 300)
                ?: UserPreferences().exportDpi,
            defaultSaveLocation = preferences[Keys.defaultSaveLocation]
                .toEnumOrDefault(SaveLocation.INTERNAL)
        )
    }
}

private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(default: T): T {
    return this?.let { stored ->
        enumValues<T>().firstOrNull { it.name == stored }
    } ?: default
}
