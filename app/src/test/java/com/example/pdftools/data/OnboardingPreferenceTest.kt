package com.example.pdftools.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingPreferenceTest {

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
    fun defaultOnboardingCompletedIsFalse() = runBlocking {
        val repository = createRepository()
        assertFalse(repository.preferences.first().onboardingCompleted)
    }

    @Test
    fun setOnboardingCompletedTruePersists() = runBlocking {
        val repository = createRepository()
        repository.setOnboardingCompleted(true)
        assertTrue(repository.preferences.first().onboardingCompleted)
    }

    @Test
    fun setOnboardingCompletedFalsePersists() = runBlocking {
        val repository = createRepository()
        repository.setOnboardingCompleted(false)
        assertFalse(repository.preferences.first().onboardingCompleted)
    }
}
