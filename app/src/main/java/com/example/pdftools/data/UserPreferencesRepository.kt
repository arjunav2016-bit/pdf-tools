package com.example.pdftools.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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
    val defaultSaveLocation: SaveLocation = SaveLocation.INTERNAL,
    val onboardingCompleted: Boolean = false,
    val savedSignatures: Set<String> = emptySet(),
    val ocrLanguage: String = "latin"
)

@Singleton
class UserPreferencesRepository internal constructor(
    private val dataStore: DataStore<Preferences>
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this(context.userPreferencesStore)

    private object Keys {
        val themeMode = stringPreferencesKey("theme_mode")
        val compressionQuality = intPreferencesKey("compression_quality")
        val exportDpi = intPreferencesKey("export_dpi")
        val defaultSaveLocation = stringPreferencesKey("default_save_location")
        val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
        val savedSignatures = stringSetPreferencesKey("saved_signatures")
        val ocrLanguage = stringPreferencesKey("ocr_language")
    }

    val preferences: Flow<UserPreferences> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map(::mapPreferences)

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[Keys.themeMode] = themeMode.name
        }
    }

    suspend fun updateCompressionQuality(quality: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.compressionQuality] = quality.coerceIn(30, 100)
        }
    }

    suspend fun updateExportDpi(dpi: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.exportDpi] = dpi.coerceIn(72, 300)
        }
    }

    suspend fun updateDefaultSaveLocation(saveLocation: SaveLocation) {
        dataStore.edit { preferences ->
            preferences[Keys.defaultSaveLocation] = saveLocation.name
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.onboardingCompleted] = completed
        }
    }

    suspend fun addSavedSignature(path: String) {
        dataStore.edit { preferences ->
            val current = preferences[Keys.savedSignatures] ?: emptySet()
            preferences[Keys.savedSignatures] = current + path
        }
    }

    suspend fun removeSavedSignature(path: String) {
        dataStore.edit { preferences ->
            val current = preferences[Keys.savedSignatures] ?: emptySet()
            preferences[Keys.savedSignatures] = current - path
        }
    }

    suspend fun updateOcrLanguage(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[Keys.ocrLanguage] = languageCode
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
                .toEnumOrDefault(SaveLocation.INTERNAL),
            onboardingCompleted = preferences[Keys.onboardingCompleted] ?: false,
            savedSignatures = preferences[Keys.savedSignatures] ?: emptySet(),
            ocrLanguage = preferences[Keys.ocrLanguage] ?: "latin"
        )
    }
}

private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(default: T): T {
    return this?.let { stored ->
        enumValues<T>().firstOrNull { it.name == stored }
    } ?: default
}
