package com.example.pdftools.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class UserPreferencesRepositoryTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createRepository(scope: kotlinx.coroutines.CoroutineScope): UserPreferencesRepository {
        val file = File(
            tempFolder.root,
            "user_preferences_${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}.preferences_pb"
        )
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file }
        )
        return UserPreferencesRepository(dataStore)
    }

    @Test
    fun freshStoreEmitsDefaults() = runTest {
        val repository = createRepository(backgroundScope)
        assertEquals(UserPreferences(), repository.preferences.first())
    }

    @Test
    fun themeModeUpdatePersistsAndRoundTrips() = runTest {
        val repository = createRepository(backgroundScope)
        repository.updateThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, repository.preferences.first().themeMode)
    }

    @Test
    fun compressionQualityUpdatePersists() = runTest {
        val repository = createRepository(backgroundScope)
        repository.updateCompressionQuality(92)
        assertEquals(92, repository.preferences.first().compressionQuality)
    }

    @Test
    fun compressionQualityUpdateClamps() = runTest {
        val repository = createRepository(backgroundScope)
        repository.updateCompressionQuality(5)
        assertEquals(30, repository.preferences.first().compressionQuality)
    }

    @Test
    fun exportDpiUpdatePersists() = runTest {
        val repository = createRepository(backgroundScope)
        repository.updateExportDpi(220)
        assertEquals(220, repository.preferences.first().exportDpi)
    }

    @Test
    fun exportDpiUpdateClamps() = runTest {
        val repository = createRepository(backgroundScope)
        repository.updateExportDpi(900)
        assertEquals(300, repository.preferences.first().exportDpi)
    }

    @Test
    fun saveLocationUpdatePersistsAndRoundTrips() = runTest {
        val repository = createRepository(backgroundScope)
        repository.updateDefaultSaveLocation(SaveLocation.DOWNLOADS)
        assertEquals(SaveLocation.DOWNLOADS, repository.preferences.first().defaultSaveLocation)
    }
}
