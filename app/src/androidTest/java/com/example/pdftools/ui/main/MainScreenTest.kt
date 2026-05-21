package com.example.pdftools.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.example.pdftools.ui.screens.MainScreen

/** UI tests for [com.example.pdftools.ui.screens.MainScreen]. */
class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent {
      MainScreen(onToolClick = {})
    }
  }

  @Test
  fun testHomeTab_exists() {
    composeTestRule.onNodeWithText("Home").assertExists()
    composeTestRule.onNodeWithText("Recent").assertExists()
    composeTestRule.onNodeWithText("Favorites").assertExists()
  }
}
