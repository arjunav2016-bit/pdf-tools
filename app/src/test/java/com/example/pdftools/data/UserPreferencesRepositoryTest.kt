package com.example.pdftools.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class UserPreferencesRepositoryTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var repository: UserPreferencesRepository

    @Before
    fun setUp() {
        dataStoreScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val dataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = { File(tempFolder.root, "user_preferences.preferences_pb") }
        )
        repository = UserPreferencesRepository(dataStore)
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
    }

    @Test
    fun freshStoreEmitsDefaults() = runTest {
        assertEquals(UserPreferences(), repository.preferences.first())
    }

    @Test
    fun themeModeUpdatePersistsAndRoundTrips() = runTest {
        repository.updateThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, repository.preferences.first().themeMode)
    }

    @Test
    fun compressionQualityUpdatePersists() = runTest {
        repository.updateCompressionQuality(92)

        assertEquals(92, repository.preferences.first().compressionQuality)
    }

    @Test
    fun compressionQualityUpdateClamps() = runTest {
        repository.updateCompressionQuality(5)

        assertEquals(30, repository.preferences.first().compressionQuality)
    }

    @Test
    fun exportDpiUpdatePersists() = runTest {
        repository.updateExportDpi(220)

        assertEquals(220, repository.preferences.first().exportDpi)
    }

    @Test
    fun exportDpiUpdateClamps() = runTest {
        repository.updateExportDpi(900)

        assertEquals(300, repository.preferences.first().exportDpi)
    }

    @Test
    fun saveLocationUpdatePersistsAndRoundTrips() = runTest {
        repository.updateDefaultSaveLocation(SaveLocation.DOWNLOADS)

        assertEquals(SaveLocation.DOWNLOADS, repository.preferences.first().defaultSaveLocation)
    }
}
