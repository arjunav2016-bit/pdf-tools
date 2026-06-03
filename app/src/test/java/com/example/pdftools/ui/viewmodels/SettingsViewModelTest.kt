package com.example.pdftools.ui.viewmodels

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.example.pdftools.data.PdfPreviewRepository
import com.example.pdftools.data.UserPreferencesRepository
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var context: Context
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var pdfPreviewRepository: PdfPreviewRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()

        val systemTempDir = File(System.getProperty("java.io.tmpdir") ?: ".")
        val testDir = File(systemTempDir, "datastore_test_${java.util.UUID.randomUUID()}")
        testDir.mkdirs()
        val file = File(testDir, "user_preferences.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = { file }
        )
        userPreferencesRepository = UserPreferencesRepository(dataStore)
        pdfPreviewRepository = mock()

        viewModel = SettingsViewModel(userPreferencesRepository, pdfPreviewRepository, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun clearCacheDeletesFilesAndEvictsPreviewCache() = runTest {
        val cacheFile = File(context.cacheDir, "dummy.txt")
        cacheFile.writeText("hello cache")
        assertTrue(cacheFile.exists())

        viewModel.refreshCacheSize()
        advanceUntilIdle()

        viewModel.clearCache()
        advanceUntilIdle()

        val result = viewModel.clearCacheResult.first()
        assertTrue(result.isSuccess)
        verify(pdfPreviewRepository).evictAll()

        org.junit.Assert.assertFalse(cacheFile.exists())
        assertEquals(0L, viewModel.cacheSizeBytes.value)
    }
}
