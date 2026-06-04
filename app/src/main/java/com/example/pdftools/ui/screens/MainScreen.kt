package com.example.pdftools.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.unit.dp
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
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pdftools.R
import com.example.pdftools.data.PdfTool
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTab(val title: String) {
    HOME("Home"),
    RECENT("Recent"),
    FAVORITES("Favorites")
}

class MainScreenViewModel : ViewModel() {
    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }
}

@Composable
fun MainScreen(
    onToolClick: (PdfTool) -> Unit,
    onSettingsClick: () -> Unit = {},
    viewModel: MainScreenViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val homeLazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val recentLazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val favoritesLazyGridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    val isWideScreen = configuration.screenWidthDp >= 600

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (!isWideScreen) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentTab == AppTab.HOME,
                        onClick = { viewModel.selectTab(AppTab.HOME) },
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
                        onClick = { viewModel.selectTab(AppTab.RECENT) },
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
                        onClick = { viewModel.selectTab(AppTab.FAVORITES) },
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
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isWideScreen) {
                NavigationRail(
                    header = {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                ) {
                    NavigationRailItem(
                        selected = currentTab == AppTab.HOME,
                        onClick = { viewModel.selectTab(AppTab.HOME) },
                        label = { Text(stringResource(R.string.home)) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = stringResource(R.string.home)
                            )
                        }
                    )
                    NavigationRailItem(
                        selected = currentTab == AppTab.RECENT,
                        onClick = { viewModel.selectTab(AppTab.RECENT) },
                        label = { Text(stringResource(R.string.recent)) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppTab.RECENT) Icons.Filled.History else Icons.Outlined.History,
                                contentDescription = stringResource(R.string.recent)
                            )
                        }
                    )
                    NavigationRailItem(
                        selected = currentTab == AppTab.FAVORITES,
                        onClick = { viewModel.selectTab(AppTab.FAVORITES) },
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

            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn().togetherWith(fadeOut())
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    AppTab.HOME -> HomeScreen(
                        onToolClick = onToolClick,
                        onSettingsClick = onSettingsClick,
                        scrollState = homeLazyListState,
                        modifier = Modifier.fillMaxSize()
                    )
                    AppTab.FAVORITES -> FavoritesScreen(
                        onToolClick = onToolClick,
                        gridState = favoritesLazyGridState,
                        modifier = Modifier.fillMaxSize()
                    )
                    AppTab.RECENT -> RecentScreen(
                        scrollState = recentLazyListState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
