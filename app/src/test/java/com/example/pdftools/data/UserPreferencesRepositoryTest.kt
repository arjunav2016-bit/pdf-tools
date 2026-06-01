package com.example.pdftools.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class UserPreferencesRepositoryTest {

    private fun createRepository(): UserPreferencesRepository {
        val systemTempDir = File(System.getProperty("java.io.tmpdir") ?: ".")
        val testDir = File(systemTempDir, "datastore_test_${java.util.UUID.randomUUID()}")
        testDir.mkdirs()
        val file = File(testDir, "user_preferences.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = { file }
        )
        return UserPreferencesRepository(dataStore)
    }

    @Test
    fun freshStoreEmitsDefaults() = runBlocking {
        val repository = createRepository()
        assertEquals(UserPreferences(), repository.preferences.first())
    }

    @Test
    fun themeModeUpdatePersistsAndRoundTrips() = runBlocking {
        val repository = createRepository()
        repository.updateThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, repository.preferences.first().themeMode)
    }

    @Test
    fun compressionQualityUpdatePersists() = runBlocking {
        val repository = createRepository()
        repository.updateCompressionQuality(92)
        assertEquals(92, repository.preferences.first().compressionQuality)
    }

    @Test
    fun compressionQualityUpdateClamps() = runBlocking {
        val repository = createRepository()
        repository.updateCompressionQuality(5)
        assertEquals(30, repository.preferences.first().compressionQuality)
    }

    @Test
    fun exportDpiUpdatePersists() = runBlocking {
        val repository = createRepository()
        repository.updateExportDpi(220)
        assertEquals(220, repository.preferences.first().exportDpi)
    }

    @Test
    fun exportDpiUpdateClamps() = runBlocking {
        val repository = createRepository()
        repository.updateExportDpi(900)
        assertEquals(300, repository.preferences.first().exportDpi)
    }

    @Test
    fun saveLocationUpdatePersistsAndRoundTrips() = runBlocking {
        val repository = createRepository()
        repository.updateDefaultSaveLocation(SaveLocation.DOWNLOADS)
        assertEquals(SaveLocation.DOWNLOADS, repository.preferences.first().defaultSaveLocation)
    }
}
