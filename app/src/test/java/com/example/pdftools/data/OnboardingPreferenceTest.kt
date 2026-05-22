package com.example.pdftools.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingPreferenceTest {
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
    fun defaultOnboardingCompletedIsFalse() = runTest {
        val repository = createRepository(backgroundScope)
        assertFalse(repository.preferences.first().onboardingCompleted)
    }

    @Test
    fun setOnboardingCompletedTruePersists() = runTest {
        val repository = createRepository(backgroundScope)
        repository.setOnboardingCompleted(true)
        assertTrue(repository.preferences.first().onboardingCompleted)
    }

    @Test
    fun setOnboardingCompletedFalsePersists() = runTest {
        val repository = createRepository(backgroundScope)
        repository.setOnboardingCompleted(false)
        assertFalse(repository.preferences.first().onboardingCompleted)
    }
}
