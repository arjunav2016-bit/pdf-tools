package com.example.pdftools.ui.main

import com.example.pdftools.data.ToolRepository
import com.example.pdftools.ui.screens.AppTab
import com.example.pdftools.ui.screens.MainScreenViewModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock

class MainScreenViewModelTest {
    private val toolRepository: ToolRepository = mock()

    @Test
    fun startsOnHomeTab() {
        val viewModel = MainScreenViewModel(toolRepository)

        assertEquals(AppTab.HOME, viewModel.currentTab.value)
    }

    @Test
    fun selectingTabUpdatesNavigationState() {
        val viewModel = MainScreenViewModel(toolRepository)

        viewModel.selectTab(AppTab.FILES)
        assertEquals(AppTab.FILES, viewModel.currentTab.value)

        viewModel.selectTab(AppTab.TOOLS)
        assertEquals(AppTab.TOOLS, viewModel.currentTab.value)

        viewModel.selectTab(AppTab.SETTINGS)
        assertEquals(AppTab.SETTINGS, viewModel.currentTab.value)
    }
}
