package com.example.pdftools.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.pdftools.R
import com.example.pdftools.data.PdfTool

enum class AppTab(val title: String) {
    HOME("Home"),
    RECENT("Recent"),
    FAVORITES("Favorites")
}

@Composable
fun MainScreen(
    onToolClick: (PdfTool) -> Unit,
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentTab by rememberSaveable { mutableStateOf(AppTab.HOME) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == AppTab.HOME,
                    onClick = { currentTab = AppTab.HOME },
                    label = { Text(stringResource(R.string.home)) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = stringResource(R.string.home)
                        )
                    }
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.RECENT,
                    onClick = { currentTab = AppTab.RECENT },
                    label = { Text(stringResource(R.string.recent)) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.RECENT) Icons.Filled.History else Icons.Outlined.History,
                            contentDescription = stringResource(R.string.recent)
                        )
                    }
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.FAVORITES,
                    onClick = { currentTab = AppTab.FAVORITES },
                    label = { Text(stringResource(R.string.favorites)) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.FAVORITES) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = stringResource(R.string.favorites)
                        )
                    }
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = {
                fadeIn().togetherWith(fadeOut())
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "TabTransition"
        ) { tab ->
            when (tab) {
                AppTab.HOME -> HomeScreen(
                    onToolClick = onToolClick,
                    onSettingsClick = onSettingsClick,
                    modifier = Modifier.fillMaxSize()
                )
                AppTab.FAVORITES -> FavoritesScreen(onToolClick = onToolClick, modifier = Modifier.fillMaxSize())
                AppTab.RECENT -> RecentScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
