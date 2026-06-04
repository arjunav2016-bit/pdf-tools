package com.example.pdftools.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pdftools.R
import com.example.pdftools.data.PdfTool
import com.example.pdftools.data.ToolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

enum class AppTab(val title: String) {
    HOME("Home"),
    FILES("Files"),
    TOOLS("Tools"),
    SETTINGS("Settings")
}

@Stable
@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val toolRepository: ToolRepository
) : ViewModel() {
    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun getScanTool(): PdfTool? = toolRepository.getToolById("scan_to_pdf")
}

@Composable
fun MainScreen(
    onToolClick: (PdfTool) -> Unit,
    onSettingsClick: () -> Unit = {},
    viewModel: MainScreenViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val isWideScreen by remember(screenWidthDp) {
        derivedStateOf { screenWidthDp >= 600 }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (!isWideScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(tab) },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        AppTab.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                                        AppTab.FILES -> if (isSelected) Icons.Filled.Folder else Icons.Outlined.Folder
                                        AppTab.TOOLS -> if (isSelected) Icons.Filled.GridView else Icons.Outlined.GridView
                                        AppTab.SETTINGS -> if (isSelected) Icons.Filled.Settings else Icons.Outlined.Settings
                                    },
                                    contentDescription = tab.title
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (!isWideScreen && currentTab == AppTab.HOME) {
                FloatingActionButton(
                    onClick = {
                        val scanTool = viewModel.getScanTool()
                        if (scanTool != null) {
                            onToolClick(scanTool)
                        }
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Document",
                        modifier = Modifier.size(24.dp)
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
                    AppTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(tab) },
                            label = { Text(tab.title) },
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        AppTab.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                                        AppTab.FILES -> if (isSelected) Icons.Filled.Folder else Icons.Outlined.Folder
                                        AppTab.TOOLS -> if (isSelected) Icons.Filled.GridView else Icons.Outlined.GridView
                                        AppTab.SETTINGS -> if (isSelected) Icons.Filled.Settings else Icons.Outlined.Settings
                                    },
                                    contentDescription = tab.title
                                )
                            },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(durationMillis = 250)) +
                            slideInVertically(
                                animationSpec = tween(durationMillis = 250),
                                initialOffsetY = { it / 20 }
                            )).togetherWith(
                        fadeOut(animationSpec = tween(durationMillis = 250)) +
                                slideOutVertically(
                                    animationSpec = tween(durationMillis = 250),
                                    targetOffsetY = { -it / 20 }
                                )
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                label = "TabTransition"
            ) { tab ->
                val tabModifier = Modifier.fillMaxSize()

                when (tab) {
                    AppTab.HOME -> HomeScreen(
                        onToolClick = onToolClick,
                        onViewAllClick = { viewModel.selectTab(AppTab.FILES) },
                        onSettingsClick = { viewModel.selectTab(AppTab.SETTINGS) },
                        modifier = tabModifier
                    )
                    AppTab.FILES -> RecentScreen(
                        modifier = tabModifier
                    )
                    AppTab.TOOLS -> ToolsListScreen(
                        onToolClick = onToolClick,
                        modifier = tabModifier
                    )
                    AppTab.SETTINGS -> SettingsScreen(
                        onBack = { viewModel.selectTab(AppTab.HOME) },
                        modifier = tabModifier
                    )
                }
            }
        }
    }
}
