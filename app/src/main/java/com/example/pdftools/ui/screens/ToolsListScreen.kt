package com.example.pdftools.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pdftools.R
import com.example.pdftools.data.PdfTool
import com.example.pdftools.data.ToolCategory
import com.example.pdftools.ui.components.CategorySection
import com.example.pdftools.ui.components.ShimmerToolCardGrid
import com.example.pdftools.ui.components.staggeredFadeIn
import com.example.pdftools.ui.viewmodels.HomeViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsListScreen(
    onToolClick: (PdfTool) -> Unit,
    modifier: Modifier = Modifier,
    scrollState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
    viewModel: HomeViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val focusRequester = remember { FocusRequester() }
    var isSearchFocused by remember { mutableStateOf(false) }
    var showSkeleton by remember { mutableStateOf(true) }

    val filteredTools = remember(searchQuery) {
        viewModel.getFilteredTools(searchQuery)
    }

    LaunchedEffect(searchQuery) {
        showSkeleton = true
        delay(120)
        showSkeleton = false
    }

    // Parallax translation for title based on list scroll offset
    val titleTranslationY = remember {
        derivedStateOf {
            if (scrollState.firstVisibleItemIndex == 0) {
                (scrollState.firstVisibleItemScrollOffset / 4f).coerceAtMost(30f)
            } else {
                30f
            }
        }
    }

    // Spring animations for search field on focus
    val searchPaddingBottom by animateDpAsState(
        targetValue = if (isSearchFocused) 12.dp else 4.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "searchPaddingBottom"
    )

    val searchScale by animateFloatAsState(
        targetValue = if (isSearchFocused) 1.02f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "searchScale"
    )

    val searchBorderGlowColor by animateColorAsState(
        targetValue = if (isSearchFocused) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        },
        animationSpec = tween(durationMillis = 250),
        label = "searchBorderGlowColor"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ToolsTopAppBar(
                titleTranslationY = titleTranslationY.value,
                onSearchClick = {
                    focusRequester.requestFocus()
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Permanent Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = searchPaddingBottom)
                        .scale(searchScale)
                        .focusRequester(focusRequester)
                        .onFocusChanged { isSearchFocused = it.isFocused },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search_for_tools),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(R.string.search_tools),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.clear_search),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        focusedBorderColor = searchBorderGlowColor,
                        unfocusedBorderColor = searchBorderGlowColor
                    )
                )
            }

            val categories = ToolCategory.entries.filter { filteredTools.containsKey(it) }
            if (showSkeleton) {
                item {
                    ShimmerToolCardGrid(
                        columns = 2,
                        rows = 3
                    )
                }
            } else {
                itemsIndexed(
                    items = categories,
                    key = { _, category -> category.name }
                ) { index, category ->
                    CategorySection(
                        category = category,
                        tools = filteredTools[category] ?: emptyList(),
                        onToolClick = onToolClick,
                        modifier = Modifier.staggeredFadeIn(index)
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolsTopAppBar(
    titleTranslationY: Float,
    onSearchClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "All Tools",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.graphicsLayer {
                    translationY = -titleTranslationY
                    alpha = (1f - (titleTranslationY / 60f)).coerceIn(0.2f, 1f)
                    val scaleVal = (1f - (titleTranslationY / 120f)).coerceIn(0.85f, 1f)
                    scaleX = scaleVal
                    scaleY = scaleVal
                }
            )
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.search_tools),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}
