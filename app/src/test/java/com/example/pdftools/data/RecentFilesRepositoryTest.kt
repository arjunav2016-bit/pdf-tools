package com.example.pdftools.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.pdftools.data.db.PdfToolsDatabase
import com.example.pdftools.data.db.RecentFileDao
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RecentFilesRepositoryTest {

    private lateinit var context: Context
    private lateinit var database: PdfToolsDatabase
    private lateinit var recentFileDao: RecentFileDao
    private lateinit var repository: RecentFilesRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, PdfToolsDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .build()
        recentFileDao = database.recentFileDao()

        // Clear any leftover preferences from previous tests
        context.getSharedPreferences("pdf_tools_recents", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testAddAndClearRecents() = runTest {
        repository = RecentFilesRepository(context, recentFileDao, this)

        // Clear prepopulated mock files first so the test starts in a clean state
        repository.clear()
        withTimeout(2000) {
            repository.recents.filter { it.isEmpty() }.first()
        }

        // Initial state should be empty
        assertTrue(repository.recents.value.isEmpty())

        // Add a recent file
        repository.addRecent(
            fileName = "sample.pdf",
            toolId = "merge_pdf",
            filePath = "content://media/external/file/1"
        )

        // Wait for flow to emit the new recent file
        withTimeout(2000) {
            repository.recents.filter { it.isNotEmpty() && it.first().fileName == "sample.pdf" }.first()
        }

        assertEquals(1, repository.recents.value.size)
        val recent = repository.recents.value.first()
        assertEquals("sample.pdf", recent.fileName)
        assertEquals("merge_pdf", recent.toolId)
        assertEquals("content://media/external/file/1", recent.filePath)

        // Clear
        repository.clear()

        // Wait for flow to emit empty list
        withTimeout(2000) {
            repository.recents.filter { it.isEmpty() }.first()
        }

        assertTrue(repository.recents.value.isEmpty())

        coroutineContext.cancelChildren()
    }

    @Test
    fun testSharedPreferencesMigration() = runTest {
        // Pre-populate SharedPreferences with JSON array
        val jsonArray = JSONArray().apply {
            put(JSONObject().apply {
                put("id", "101")
                put("fileName", "old_file.pdf")
                put("toolId", "split_pdf")
                put("filePath", "content://old/path")
                put("timestamp", 123456789L)
            })
        }

        val prefs = context.getSharedPreferences("pdf_tools_recents", Context.MODE_PRIVATE)
        prefs.edit().putString("recents", jsonArray.toString()).commit()

        // Initialize repository, which triggers migration in init block
        repository = RecentFilesRepository(context, recentFileDao, this)

        // Wait for flow to emit the migrated item
        withTimeout(2000) {
            repository.recents.filter { it.any { file -> file.id == "101" } }.first()
        }

        val recents = repository.recents.value
        assertEquals(1, recents.size)
        val migrated = recents.first()
        assertEquals("101", migrated.id)
        assertEquals("old_file.pdf", migrated.fileName)
        assertEquals("split_pdf", migrated.toolId)
        assertEquals("content://old/path", migrated.filePath)
        assertEquals(123456789L, migrated.timestamp)

        // Verify shared preferences were cleared
        assertFalse(prefs.contains("recents"))

        coroutineContext.cancelChildren()
    }
}
