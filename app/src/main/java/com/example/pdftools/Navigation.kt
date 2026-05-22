package com.example.pdftools

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.pdftools.ui.screens.ComingSoonScreen
import com.example.pdftools.ui.screens.MainScreen
import com.example.pdftools.ui.screens.SettingsScreen
import com.example.pdftools.ui.screens.ToolScreen
import com.example.pdftools.ui.screens.OnboardingScreen
import com.example.pdftools.ui.viewmodels.NavigationViewModel

@Composable
fun MainNavigation() {
    val navigationViewModel: NavigationViewModel = hiltViewModel()
    val preferences by navigationViewModel.preferences.collectAsState(initial = null)

    if (preferences == null) {
        return
    }

    val startDestination = remember(preferences != null) {
        if (preferences!!.onboardingCompleted) Main else Onboarding
    }

    val backStack = rememberNavBackStack(startDestination)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Onboarding> {
                OnboardingScreen(
                    onFinished = {
                        navigationViewModel.setOnboardingCompleted()
                        backStack.clear()
                        backStack.add(Main)
                    },
                    modifier = Modifier.safeDrawingPadding()
                )
            }

            entry<Main> {
                MainScreen(
                    onToolClick = { tool ->
                        backStack.add(ToolDetail(toolId = tool.id))
                    },
                    onSettingsClick = { backStack.add(Settings) },
                    modifier = Modifier.safeDrawingPadding()
                )
            }

            entry<Settings> {
                SettingsScreen(
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.safeDrawingPadding()
                )
            }

            entry<ToolDetail> { key ->
                val tool = navigationViewModel.getToolById(key.toolId)
                if (tool != null) {
                    if (tool.isImplemented) {
                        ToolScreen(
                            tool = tool,
                            onBack = { backStack.removeLastOrNull() },
                            modifier = Modifier.safeDrawingPadding()
                        )
                    } else {
                        ComingSoonScreen(
                            tool = tool,
                            onBack = { backStack.removeLastOrNull() },
                            modifier = Modifier.safeDrawingPadding()
                        )
                    }
                }
            }
        }
    )
}
