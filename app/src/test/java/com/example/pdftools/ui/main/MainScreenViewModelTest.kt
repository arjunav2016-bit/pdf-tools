package com.example.pdftools.ui.main

import com.example.pdftools.ui.screens.AppTab
import com.example.pdftools.ui.screens.MainScreenViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class MainScreenViewModelTest {
    @Test
    fun startsOnHomeTab() {
        val viewModel = MainScreenViewModel()

        assertEquals(AppTab.HOME, viewModel.currentTab.value)
    }

    @Test
    fun selectingTabUpdatesNavigationState() {
        val viewModel = MainScreenViewModel()

        viewModel.selectTab(AppTab.RECENT)
        assertEquals(AppTab.RECENT, viewModel.currentTab.value)

        viewModel.selectTab(AppTab.FAVORITES)
        assertEquals(AppTab.FAVORITES, viewModel.currentTab.value)
    }
}
