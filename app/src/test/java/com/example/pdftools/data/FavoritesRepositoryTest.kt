package com.example.pdftools.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.pdftools.data.db.PdfToolsDatabase
import com.example.pdftools.data.db.FavoriteDao
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
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
class FavoritesRepositoryTest {

    private lateinit var context: Context
    private lateinit var database: PdfToolsDatabase
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var repository: FavoritesRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, PdfToolsDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .build()
        favoriteDao = database.favoriteDao()
        
        // Clear any leftover preferences from previous tests
        context.getSharedPreferences("pdf_tools_favorites", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testToggleFavorite() = runTest {
        repository = FavoritesRepository(context, favoriteDao, this)

        // Initial state should be empty
        assertTrue(repository.favorites.value.isEmpty())

        // Toggle on
        repository.toggleFavorite("merge_pdf")
        
        // Wait for flow to emit the new favorite
        withTimeout(2000) {
            repository.favorites.filter { it.contains("merge_pdf") }.first()
        }
        
        assertTrue(repository.isFavorite("merge_pdf"))

        // Toggle off
        repository.toggleFavorite("merge_pdf")

        // Wait for flow to emit empty
        withTimeout(2000) {
            repository.favorites.filter { it.isEmpty() }.first()
        }

        assertFalse(repository.isFavorite("merge_pdf"))

        coroutineContext.cancelChildren()
    }

    @Test
    fun testSharedPreferencesMigration() = runTest {
        // Pre-populate SharedPreferences
        val prefs = context.getSharedPreferences("pdf_tools_favorites", Context.MODE_PRIVATE)
        prefs.edit().putStringSet("favorites", setOf("merge_pdf", "split_pdf")).commit()

        // Initialize repository, which triggers migration in init block
        repository = FavoritesRepository(context, favoriteDao, this)

        // Wait for flow to emit both migrated items
        withTimeout(2000) {
            repository.favorites.filter { it.contains("merge_pdf") && it.contains("split_pdf") }.first()
        }

        assertTrue(repository.isFavorite("merge_pdf"))
        assertTrue(repository.isFavorite("split_pdf"))

        // Verify shared preferences were cleared
        assertFalse(prefs.contains("favorites"))

        coroutineContext.cancelChildren()
    }
}
