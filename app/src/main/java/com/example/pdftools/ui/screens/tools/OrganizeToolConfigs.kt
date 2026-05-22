package com.example.pdftools.ui.screens.tools

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.InsertDriveFile
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pdftools.data.PdfProcessor
import com.example.pdftools.data.PdfTool
import com.example.pdftools.ui.components.PageThumbnailGrid
import com.example.pdftools.ui.components.PdfPagePreview
import com.example.pdftools.ui.screens.OrganizePageItem
import com.example.pdftools.ui.viewmodels.OrganizeConfig
import com.example.pdftools.ui.viewmodels.ToolViewModel
import com.example.pdftools.utils.PageRangeUtils
import kotlin.math.abs

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
                "split_pdf" -> "Page Range to Keep"
                "extract_pages" -> "Pages to Extract"
                else -> "Pages to Remove"
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
                Text(if (showAdvancedRange) "Hide advanced page range" else "Advanced page range")
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
                        text = if (tool.id == "split_pdf" || tool.id == "extract_pages") "e.g., 1-3, 5" else "e.g., 2, 4",
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
                "split_pdf" -> "Enter comma-separated ranges/numbers (e.g. 1-3, 5 to keep pages 1, 2, 3, and 5)."
                "extract_pages" -> "Enter page numbers or ranges to extract into a new PDF (e.g. 1-3, 5)."
                else -> "Enter page numbers or ranges to remove (e.g. 2, 4 to delete pages 2 and 4)."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
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
            text = "Select Rotation Angle",
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
                val isSelected = config.degrees == angle
                val cardBg = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceContainerLow
                val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                
                Card(
                    onClick = { viewModel.rotateConfig.value = config.copy(degrees = angle) },
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
                                    90 -> "Right"
                                    180 -> "Upside Down"
                                    270 -> "Left"
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
            text = "Pages to Rotate (Optional)",
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
                    text = "e.g., 1-3, 5 (leave empty for all)",
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
            text = "Enter page numbers or ranges to rotate. If left blank, all pages will be rotated.",
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
            text = "Arrange & Transform Pages",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "You can reorder, rotate, duplicate, or delete individual pages below.",
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
                                    text = "Page ${globalIndex + 1}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "(Orig. ${pageItem.originalIndex + 1})",
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
                                        contentDescription = "Rotate 90 degrees",
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
                                        contentDescription = "Duplicate page",
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
                                            Toast.makeText(context, "Cannot delete all pages", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete page",
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
                                    Text("← Move", style = MaterialTheme.typography.bodySmall)
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
                                    Text("Move →", style = MaterialTheme.typography.bodySmall)
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
