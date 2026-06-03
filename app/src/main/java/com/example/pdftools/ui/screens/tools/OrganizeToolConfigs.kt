package com.example.pdftools.ui.screens.tools

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pdftools.R
import com.example.pdftools.data.PdfProcessor
import com.example.pdftools.data.PdfTool
import com.example.pdftools.ui.components.PageThumbnailGrid
import com.example.pdftools.ui.components.SplitPageThumbnailGrid
import com.example.pdftools.ui.components.RemovePageThumbnailGrid
import com.example.pdftools.ui.components.PdfPagePreview
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import com.example.pdftools.ui.screens.OrganizePageItem
import com.example.pdftools.ui.screens.getFileNameFromUri
import com.example.pdftools.ui.viewmodels.OrganizeConfig
import com.example.pdftools.ui.viewmodels.ToolViewModel
import com.example.pdftools.utils.PageRangeUtils
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageRangeToolConfig(
    viewModel: ToolViewModel,
    tool: PdfTool,
    accentColor: Color
) {
    val config by viewModel.pageRangeConfig.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val pageCount by viewModel.pageCount.collectAsState()
    val selectedFile = selectedFiles.firstOrNull()
    val context = LocalContext.current
    var showAdvancedRange by remember { mutableStateOf(false) }

    LaunchedEffect(selectedFile) {
        selectedFile?.let { viewModel.loadPageCount(context, it) }
    }

    LaunchedEffect(pageCount, config.pageRange) {
        val totalPages = pageCount ?: return@LaunchedEffect
        if (config.pageRange.isNotBlank() && config.selectedPages.isEmpty()) {
            viewModel.pageRangeConfig.value = config.copy(
                selectedPages = PageRangeUtils.parsePageRanges(config.pageRange, totalPages).toSet()
            )
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = when (tool.id) {
                "split_pdf" -> stringResource(R.string.tool_page_range_keep)
                "extract_pages" -> stringResource(R.string.tool_pages_extract)
                else -> stringResource(R.string.tool_pages_remove)
            },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (selectedFile != null && (pageCount ?: 0) > 0) {
            PageThumbnailGrid(
                uri = selectedFile,
                pageCount = pageCount ?: 0,
                selectedPages = config.selectedPages,
                onTogglePage = { pageIndex ->
                    val updatedPages = config.selectedPages.toMutableSet().apply {
                        if (!add(pageIndex)) {
                            remove(pageIndex)
                        }
                    }
                    viewModel.pageRangeConfig.value = config.copy(
                        selectedPages = updatedPages,
                        pageRange = PageRangeUtils.formatPageRanges(updatedPages)
                    )
                },
                accentColor = accentColor,
                loadThumbnail = { uri, pageIndex, width ->
                    viewModel.renderPage(context, uri, pageIndex, width)
                }
            )
            TextButton(onClick = { showAdvancedRange = !showAdvancedRange }) {
                Text(
                    if (showAdvancedRange) {
                        stringResource(R.string.tool_hide_advanced_page_range)
                    } else {
                        stringResource(R.string.tool_advanced_page_range)
                    }
                )
            }
        }

        if (selectedFile == null || pageCount == null || showAdvancedRange) {
            OutlinedTextField(
                value = config.pageRange,
                onValueChange = { pageRange ->
                    viewModel.pageRangeConfig.value = config.copy(
                        pageRange = pageRange,
                        selectedPages = pageCount?.let {
                            PageRangeUtils.parsePageRanges(pageRange, it).toSet()
                        } ?: emptySet()
                    )
                },
                placeholder = {
                    Text(
                        text = if (tool.id == "split_pdf" || tool.id == "extract_pages") {
                            stringResource(R.string.tool_page_range_keep_placeholder)
                        } else {
                            stringResource(R.string.tool_page_range_remove_placeholder)
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedLabelColor = accentColor,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text(
            text = when (tool.id) {
                "split_pdf" -> stringResource(R.string.tool_page_range_keep_help)
                "extract_pages" -> stringResource(R.string.tool_page_range_extract_help)
                else -> stringResource(R.string.tool_page_range_remove_help)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color
) {
    val config by viewModel.pageRangeConfig.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val pageCount by viewModel.pageCount.collectAsState()
    val selectedFile = selectedFiles.firstOrNull()
    val context = LocalContext.current

    LaunchedEffect(selectedFile) {
        selectedFile?.let { viewModel.loadPageCount(context, it) }
    }

    LaunchedEffect(pageCount, config.pageRange) {
        val totalPages = pageCount ?: return@LaunchedEffect
        if (config.pageRange.isNotBlank() && config.selectedPages.isEmpty()) {
            viewModel.pageRangeConfig.value = config.copy(
                selectedPages = PageRangeUtils.parsePageRanges(config.pageRange, totalPages).toSet()
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. CONFIGURATION SECTION ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                val totalCount = pageCount ?: 0
                if (totalCount > 0) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = accentColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (totalCount == 1) {
                                stringResource(R.string.tool_split_pages_total_single)
                            } else {
                                stringResource(R.string.tool_split_pages_total, totalCount)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }

            OutlinedTextField(
                value = config.pageRange,
                onValueChange = { pageRange ->
                    viewModel.pageRangeConfig.value = config.copy(
                        pageRange = pageRange,
                        selectedPages = pageCount?.let {
                            PageRangeUtils.parsePageRanges(pageRange, it).toSet()
                        } ?: emptySet()
                    )
                },
                label = { Text(stringResource(R.string.tool_split_page_range_label)) },
                placeholder = {
                    Text(
                        text = stringResource(R.string.tool_page_range_keep_placeholder),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        tint = accentColor
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.tool_split_info_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }

        // --- 2. PAGE PREVIEW SECTION ---
        val totalCount = pageCount ?: 0
        if (selectedFile != null && totalCount > 0) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.tool_split_page_preview),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val isAllSelected = config.selectedPages.size == totalCount
                    TextButton(
                        onClick = {
                            val updatedPages = if (isAllSelected) {
                                emptySet()
                            } else {
                                (0 until totalCount).toSet()
                            }
                            viewModel.pageRangeConfig.value = config.copy(
                                selectedPages = updatedPages,
                                pageRange = PageRangeUtils.formatPageRanges(updatedPages)
                            )
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = accentColor
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GridView,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isAllSelected) {
                                    stringResource(R.string.tool_split_deselect_all)
                                } else {
                                    stringResource(R.string.tool_split_select_all)
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                SplitPageThumbnailGrid(
                    uri = selectedFile,
                    pageCount = totalCount,
                    selectedPages = config.selectedPages,
                    onTogglePage = { pageIndex ->
                        val updatedPages = config.selectedPages.toMutableSet().apply {
                            if (!add(pageIndex)) {
                                remove(pageIndex)
                            }
                        }
                        viewModel.pageRangeConfig.value = config.copy(
                            selectedPages = updatedPages,
                            pageRange = PageRangeUtils.formatPageRanges(updatedPages)
                        )
                    },
                    accentColor = accentColor,
                    loadThumbnail = { uri, pageIndex, width ->
                        viewModel.renderPage(context, uri, pageIndex, width)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemovePagesToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color
) {
    val config by viewModel.pageRangeConfig.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val pageCount by viewModel.pageCount.collectAsState()
    val selectedFile = selectedFiles.firstOrNull()
    val context = LocalContext.current

    LaunchedEffect(selectedFile) {
        selectedFile?.let { viewModel.loadPageCount(context, it) }
    }

    LaunchedEffect(pageCount, config.pageRange) {
        val totalPages = pageCount ?: return@LaunchedEffect
        if (config.pageRange.isNotBlank() && config.selectedPages.isEmpty()) {
            viewModel.pageRangeConfig.value = config.copy(
                selectedPages = PageRangeUtils.parsePageRanges(config.pageRange, totalPages).toSet()
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. SELECTION SUMMARY BANNER CARD ---
        val totalCount = pageCount ?: 0
        if (selectedFile != null && totalCount > 0) {
            val selectedPagesCount = config.selectedPages.size
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)),
                colors = CardDefaults.cardColors(
                    containerColor = accentColor.copy(alpha = 0.08f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(accentColor, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            val fileName = getFileNameFromUri(context, selectedFile)
                            Text(
                                text = fileName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = when (selectedPagesCount) {
                                    0 -> stringResource(R.string.tool_remove_selected_subtitle_none)
                                    1 -> stringResource(R.string.tool_remove_selected_subtitle_single)
                                    else -> stringResource(R.string.tool_remove_selected_subtitle, selectedPagesCount)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val isAllSelected = selectedPagesCount == totalCount
                    TextButton(
                        onClick = {
                            val updatedPages = if (isAllSelected) {
                                emptySet()
                            } else {
                                (0 until totalCount).toSet()
                            }
                            viewModel.pageRangeConfig.value = config.copy(
                                selectedPages = updatedPages,
                                pageRange = PageRangeUtils.formatPageRanges(updatedPages)
                            )
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = accentColor
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isAllSelected) {
                                stringResource(R.string.tool_split_deselect_all)
                            } else {
                                stringResource(R.string.tool_split_select_all)
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- 2. CONFIGURATION SECTION ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Configuration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = config.pageRange,
                onValueChange = { pageRange ->
                    viewModel.pageRangeConfig.value = config.copy(
                        pageRange = pageRange,
                        selectedPages = pageCount?.let {
                            PageRangeUtils.parsePageRanges(pageRange, it).toSet()
                        } ?: emptySet()
                    )
                },
                label = { Text(stringResource(R.string.tool_split_page_range_label)) },
                placeholder = {
                    Text(
                        text = stringResource(R.string.tool_page_range_remove_placeholder),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        tint = accentColor
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.tool_split_info_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }

        // --- 3. PAGE PREVIEW SECTION ---
        if (selectedFile != null && totalCount > 0) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.tool_split_page_preview),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                RemovePageThumbnailGrid(
                    uri = selectedFile,
                    pageCount = totalCount,
                    selectedPages = config.selectedPages,
                    onTogglePage = { pageIndex ->
                        val updatedPages = config.selectedPages.toMutableSet().apply {
                            if (!add(pageIndex)) {
                                remove(pageIndex)
                            }
                        }
                        viewModel.pageRangeConfig.value = config.copy(
                            selectedPages = updatedPages,
                            pageRange = PageRangeUtils.formatPageRanges(updatedPages)
                        )
                    },
                    accentColor = accentColor,
                    loadThumbnail = { uri, pageIndex, width ->
                        viewModel.renderPage(context, uri, pageIndex, width)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RotateToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color
) {
    val config by viewModel.rotateConfig.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val pageCount by viewModel.pageCount.collectAsState()
    val selectedFile = selectedFiles.firstOrNull()
    val context = LocalContext.current

    LaunchedEffect(selectedFile) {
        selectedFile?.let { viewModel.loadPageCount(context, it) }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.tool_select_rotation_angle),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val angles = listOf(90, 180, 270)
            angles.forEach { angle ->
                val isSelected = config.previewRotation == angle
                val cardBg = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceContainerLow
                val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                
                Card(
                    onClick = { viewModel.rotateConfig.value = config.copy(previewRotation = angle) },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardBg,
                        contentColor = contentColor
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$angle°",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when (angle) {
                                    90 -> stringResource(R.string.tool_rotation_right)
                                    180 -> stringResource(R.string.tool_rotation_upside_down)
                                    270 -> stringResource(R.string.tool_rotation_left)
                                    else -> ""
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.tool_pages_rotate_optional),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (selectedFile != null && (pageCount ?: 0) > 0) {
            PageThumbnailGrid(
                uri = selectedFile,
                pageCount = pageCount ?: 0,
                selectedPages = config.selectedPages,
                onTogglePage = { pageIndex ->
                    val updatedPages = config.selectedPages.toMutableSet().apply {
                        if (!add(pageIndex)) {
                            remove(pageIndex)
                        }
                    }
                    viewModel.rotateConfig.value = config.copy(
                        selectedPages = updatedPages,
                        pageRange = PageRangeUtils.formatPageRanges(updatedPages)
                    )
                },
                accentColor = accentColor,
                loadThumbnail = { uri, pageIndex, width ->
                    viewModel.renderPage(context, uri, pageIndex, width)
                }
            )
        }
        OutlinedTextField(
            value = config.pageRange,
            onValueChange = { pageRange ->
                viewModel.rotateConfig.value = config.copy(
                    pageRange = pageRange,
                    selectedPages = pageCount?.let {
                        PageRangeUtils.parsePageRanges(pageRange, it).toSet()
                    } ?: emptySet()
                )
            },
            placeholder = {
                Text(
                    text = stringResource(R.string.tool_page_range_all_placeholder),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedLabelColor = accentColor,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stringResource(R.string.tool_rotate_page_range_help),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizeToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color
) {
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val pageCount by viewModel.pageCount.collectAsState()
    val context = LocalContext.current
    val density = LocalDensity.current
    val selectedFile = selectedFiles.firstOrNull()
    val organizePages = remember { mutableStateListOf<OrganizePageItem>() }
    val reorderThreshold = with(density) { 56.dp.toPx() }

    LaunchedEffect(selectedFile) {
        if (selectedFile == null) {
            organizePages.clear()
            viewModel.organizeConfig.value = OrganizeConfig(emptyList())
            return@LaunchedEffect
        }
        viewModel.loadPageCount(context, selectedFile)
    }

    LaunchedEffect(selectedFile, pageCount) {
        val totalPages = pageCount ?: return@LaunchedEffect
        if (selectedFile == null) {
            return@LaunchedEffect
        }
        organizePages.clear()
        val pages = (0 until totalPages).map { pageIndex ->
            OrganizePageItem(
                id = java.util.UUID.randomUUID().toString(),
                originalIndex = pageIndex,
                rotation = 0
            )
        }
        organizePages.addAll(pages)
        viewModel.organizeConfig.value = OrganizeConfig(
            pages.map { PdfProcessor.PageTransform(it.originalIndex, it.rotation) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.tool_arrange_transform_pages),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.tool_arrange_transform_help),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        val itemsPerRow = 2
        val chunkedPages = organizePages.chunked(itemsPerRow)
        
        chunkedPages.forEachIndexed { rowIndex, rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEachIndexed { colIndex, pageItem ->
                    val globalIndex = rowIndex * itemsPerRow + colIndex
                    var dragOffsetX by remember(pageItem.id) { mutableFloatStateOf(0f) }
                    var dragOffsetY by remember(pageItem.id) { mutableFloatStateOf(0f) }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 8.dp)
                            .graphicsLayer {
                                translationX = dragOffsetX
                                translationY = dragOffsetY
                            }
                            .pointerInput(pageItem.id, globalIndex) {
                                detectDragGestures(
                                    onDragCancel = {
                                        dragOffsetX = 0f
                                        dragOffsetY = 0f
                                    },
                                    onDragEnd = {
                                        val currentIndex = organizePages.indexOfFirst { it.id == pageItem.id }
                                        val horizontalDrag = abs(dragOffsetX) > abs(dragOffsetY)
                                        val shift = when {
                                            horizontalDrag && dragOffsetX > reorderThreshold -> 1
                                            horizontalDrag && dragOffsetX < -reorderThreshold -> -1
                                            !horizontalDrag && dragOffsetY > reorderThreshold -> itemsPerRow
                                            !horizontalDrag && dragOffsetY < -reorderThreshold -> -itemsPerRow
                                            else -> 0
                                        }
                                        val targetIndex = (currentIndex + shift)
                                            .coerceIn(0, organizePages.lastIndex)
                                        if (currentIndex >= 0 && targetIndex != currentIndex) {
                                            val movedPage = organizePages.removeAt(currentIndex)
                                            organizePages.add(targetIndex, movedPage)
                                            viewModel.organizeConfig.value = OrganizeConfig(
                                                organizePages.map {
                                                    PdfProcessor.PageTransform(it.originalIndex, it.rotation)
                                                }
                                            )
                                        }
                                        dragOffsetX = 0f
                                        dragOffsetY = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetX += dragAmount.x
                                        dragOffsetY += dragAmount.y
                                    }
                                )
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                selectedFile?.let { uri ->
                                    PdfPagePreview(
                                        uri = uri,
                                        pageIndex = pageItem.originalIndex,
                                        loadThumbnail = { pdfUri, pageIndex, width ->
                                            viewModel.renderPage(context, pdfUri, pageIndex, width)
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                if (pageItem.rotation != 0) {
                                    Text(
                                        text = "${pageItem.rotation}°",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .background(
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.tool_organized_page, globalIndex + 1),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.tool_original_page, pageItem.originalIndex + 1),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                IconButton(
                                    onClick = {
                                        val currentIdx = organizePages.indexOf(pageItem)
                                        if (currentIdx != -1) {
                                            organizePages[currentIdx] = pageItem.copy(
                                                rotation = (pageItem.rotation + 90) % 360
                                            )
                                            viewModel.organizeConfig.value = OrganizeConfig(
                                                organizePages.map { PdfProcessor.PageTransform(it.originalIndex, it.rotation) }
                                            )
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.RotateRight,
                                        contentDescription = stringResource(R.string.tool_rotate_90_degrees),
                                        modifier = Modifier.size(18.dp),
                                        tint = accentColor
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val currentIdx = organizePages.indexOf(pageItem)
                                        if (currentIdx != -1) {
                                            organizePages.add(
                                                currentIdx + 1,
                                                pageItem.copy(id = java.util.UUID.randomUUID().toString())
                                            )
                                            viewModel.organizeConfig.value = OrganizeConfig(
                                                organizePages.map { PdfProcessor.PageTransform(it.originalIndex, it.rotation) }
                                            )
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = stringResource(R.string.tool_duplicate_page),
                                        modifier = Modifier.size(18.dp),
                                        tint = accentColor
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        if (organizePages.size > 1) {
                                            organizePages.remove(pageItem)
                                            viewModel.organizeConfig.value = OrganizeConfig(
                                                organizePages.map { PdfProcessor.PageTransform(it.originalIndex, it.rotation) }
                                            )
                                        } else {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.tool_cannot_delete_all_pages),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = stringResource(R.string.tool_delete_page),
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(
                                    onClick = {
                                        val currentIdx = organizePages.indexOf(pageItem)
                                        if (currentIdx > 0) {
                                            organizePages.removeAt(currentIdx)
                                            organizePages.add(currentIdx - 1, pageItem)
                                            viewModel.organizeConfig.value = OrganizeConfig(
                                                organizePages.map { PdfProcessor.PageTransform(it.originalIndex, it.rotation) }
                                            )
                                        }
                                    },
                                    enabled = globalIndex > 0,
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.tool_move_previous),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                
                                TextButton(
                                    onClick = {
                                        val currentIdx = organizePages.indexOf(pageItem)
                                        if (currentIdx < organizePages.size - 1) {
                                            organizePages.removeAt(currentIdx)
                                            organizePages.add(currentIdx + 1, pageItem)
                                            viewModel.organizeConfig.value = OrganizeConfig(
                                                organizePages.map { PdfProcessor.PageTransform(it.originalIndex, it.rotation) }
                                            )
                                        }
                                    },
                                    enabled = globalIndex < organizePages.size - 1,
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.tool_move_next),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
                if (rowItems.size < itemsPerRow) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun MergeToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color
) {
    val context = LocalContext.current
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val fileMetadata = remember { mutableStateMapOf<Uri, Pair<Long, Int>>() }

    // Standalone file picker for adding more files
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty() && viewModel.currentToolId == "merge_pdf") {
            viewModel.addFiles(uris)
        }
    }

    // Resolve size and page count asynchronously
    LaunchedEffect(selectedFiles) {
        selectedFiles.forEach { uri ->
            if (!fileMetadata.containsKey(uri)) {
                val size = getFileSize(context, uri)
                val pageCount = viewModel.getPageCountSuspend(context, uri)
                fileMetadata[uri] = Pair(size, pageCount)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Add Files dashed card ──
        val dashedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRoundRect(
                        color = dashedBorderColor,
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(10.dp.toPx(), 6.dp.toPx()),
                                0f
                            )
                        ),
                        cornerRadius = CornerRadius(16.dp.toPx())
                    )
                }
                .clip(RoundedCornerShape(16.dp))
                .clickable { filePickerLauncher.launch(arrayOf("application/pdf")) }
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.tool_merge_add_files),
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.tool_merge_add_files),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.tool_merge_add_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── Instruction row with info icon ──
        if (selectedFiles.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.tool_merge_instruction),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── Selected files list ──
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            selectedFiles.forEachIndexed { index, uri ->
                var dragOffsetY by remember(uri) { mutableFloatStateOf(0f) }
                val (size, pageCount) = fileMetadata[uri] ?: Pair(0L, 0)
                val formattedSize = formatFileSize(size)
                val pageString = if (pageCount == 1) {
                    stringResource(R.string.tool_merge_metadata_pages_single)
                } else {
                    stringResource(R.string.tool_merge_metadata_pages, pageCount)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationY = dragOffsetY
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Drag handle (6-dot grid)
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Drag to reorder",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier
                                .size(24.dp)
                                .pointerInput(uri) {
                                    detectDragGestures(
                                        onDragCancel = { dragOffsetY = 0f },
                                        onDragEnd = {
                                            val currentIdx = selectedFiles.indexOf(uri)
                                            val heightPx = 76.dp.toPx()
                                            val shift = (dragOffsetY / heightPx).roundToInt()
                                            val targetIdx = (currentIdx + shift).coerceIn(0, selectedFiles.lastIndex)
                                            if (currentIdx >= 0 && targetIdx != currentIdx) {
                                                val list = selectedFiles.toMutableList()
                                                val item = list.removeAt(currentIdx)
                                                list.add(targetIdx, item)
                                                viewModel.updateSelectedFiles(list)
                                            }
                                            dragOffsetY = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffsetY += dragAmount.y
                                        }
                                    )
                                }
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // PDF document icon
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.InsertDriveFile,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // File name and metadata
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = getFileNameFromUri(context, uri),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$formattedSize  •  $pageString",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Close / Remove button
                        IconButton(
                            onClick = { viewModel.removeFile(index) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.remove_file),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getFileSize(context: Context, uri: Uri): Long {
    var size = 0L
    try {
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (index >= 0) {
                        size = cursor.getLong(index)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (size == 0L) {
            val path = uri.path
            if (path != null) {
                val file = File(path)
                if (file.exists()) {
                    size = file.length()
                }
            }
        }
    } catch (e: Exception) {
        // Fallback
    }
    return size
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = listOf("B", "KB", "MB", "GB")
    var digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    if (digitGroups >= units.size) digitGroups = units.size - 1
    return String.format(java.util.Locale.US, "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
