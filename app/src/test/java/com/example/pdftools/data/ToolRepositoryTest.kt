package com.example.pdftools.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ToolRepositoryTest {

    private lateinit var context: Context
    private lateinit var toolRepository: ToolRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        toolRepository = ToolRepository(context)
    }

    @Test
    fun testAllToolsNotEmpty() {
        val tools = toolRepository.allTools
        assertTrue(tools.isNotEmpty())
        // Verify we have a few expected key tools
        val ids = tools.map { it.id }
        assertTrue(ids.contains("merge_pdf"))
        assertTrue(ids.contains("split_pdf"))
        assertTrue(ids.contains("compress_pdf"))
        assertTrue(ids.contains("pdf_to_pdfa"))
        assertTrue(ids.contains("redact_pdf"))
    }

    @Test
    fun testGetToolById_validId() {
        val tool = toolRepository.getToolById("merge_pdf")
        assertNotNull(tool)
        assertEquals("merge_pdf", tool?.id)
        assertEquals(ToolCategory.ORGANIZE_PDF, tool?.category)
    }

    @Test
    fun testGetToolById_invalidId() {
        val tool = toolRepository.getToolById("non_existent_tool_id")
        assertNull(tool)
    }

    @Test
    fun testToolsByCategory() {
        val grouped = toolRepository.toolsByCategory
        assertTrue(grouped.isNotEmpty())
        
        // Verify specific categories are present and populated
        assertTrue(grouped.containsKey(ToolCategory.ORGANIZE_PDF))
        assertTrue(grouped.containsKey(ToolCategory.PDF_SECURITY))
        
        val organizeTools = grouped[ToolCategory.ORGANIZE_PDF]
        assertNotNull(organizeTools)
        assertTrue(organizeTools!!.any { it.id == "merge_pdf" })
    }

    @Test
    fun testToolStringsResolved() {
        val tool = toolRepository.getToolById("merge_pdf")
        assertNotNull(tool)
        // Verify names and descriptions are resolved (not empty/null)
        // Note: Under Robolectric, resources should resolve correctly
        assertTrue(tool!!.name.isNotEmpty())
        assertTrue(tool.description.isNotEmpty())
    }
}
