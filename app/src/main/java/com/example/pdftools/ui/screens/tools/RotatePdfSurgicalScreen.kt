package com.example.pdftools.ui.screens.tools

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pdftools.data.PdfTool
import com.example.pdftools.ui.components.PdfPagePreview
import com.example.pdftools.ui.screens.SuccessCard
import com.example.pdftools.ui.viewmodels.ToolUiState
import com.example.pdftools.ui.viewmodels.ToolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RotatePdfSurgicalScreen(
    tool: PdfTool,
    viewModel: ToolViewModel,
    selectedFiles: List<Uri>,
    pageCount: Int?,
    isProcessing: Boolean,
    isComplete: Boolean,
    outputUris: List<Uri>,
    progress: Float?,
    innerPadding: PaddingValues,
    accentColor: Color,
    containerColor: Color
) {
    val context = LocalContext.current
    val selectedFile = selectedFiles.firstOrNull()
    val config by viewModel.rotateConfig.collectAsState()

    LaunchedEffect(selectedFile) {
        selectedFile?.let { viewModel.loadPageCount(context, it) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Rotate PDF",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.resetCurrentRun() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Settings action */ }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isComplete) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SuccessCard(
                        tool = tool,
                        outputUris = outputUris,
                        onClear = { viewModel.resetCurrentRun() },
                        accentColor = accentColor,
                        containerColor = containerColor
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Interactive Document Preview Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedFile != null) {
                            val isSideways = config.previewRotation % 180 == 90
                            val aspectRatio = if (isSideways) 1.414f else 0.707f

                            Box(
                                modifier = Modifier
                                    .aspectRatio(aspectRatio)
                                    .fillMaxHeight()
                                    .border(2.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                    .background(Color.White)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            rotationZ = config.previewRotation.toFloat()
                                        }
                                ) {
                                    PdfPagePreview(
                                        uri = selectedFile,
                                        pageIndex = config.currentPageIndex,
                                        loadThumbnail = { uri, idx, width ->
                                            viewModel.renderPage(context, uri, idx, width)
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                // Central overlay rotation watermark icon
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(60.dp)
                                        .background(Color.White.copy(alpha = 0.85f), CircleShape)
                                        .border(2.dp, accentColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RotateRight,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        } else {
                            CircularProgressIndicator(color = accentColor)
                        }
                    }

                    // 2. Page Navigation indicator
                    val totalPages = pageCount ?: 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh, shape = RoundedCornerShape(20.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (config.currentPageIndex > 0) {
                                        viewModel.rotateConfig.value = config.copy(
                                            currentPageIndex = config.currentPageIndex - 1
                                        )
                                    }
                                },
                                enabled = config.currentPageIndex > 0,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Previous Page",
                                    tint = if (config.currentPageIndex > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "Page ${config.currentPageIndex + 1} of $totalPages",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            IconButton(
                                onClick = {
                                    if (config.currentPageIndex < totalPages - 1) {
                                        viewModel.rotateConfig.value = config.copy(
                                            currentPageIndex = config.currentPageIndex + 1
                                        )
                                    }
                                },
                                enabled = config.currentPageIndex < totalPages - 1,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Next Page",
                                    tint = if (config.currentPageIndex < totalPages - 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // 3. Rotation Controls Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ROTATION CONTROLS",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Left Card Button
                            Card(
                                onClick = {
                                    viewModel.rotateConfig.value = config.copy(
                                        previewRotation = (config.previewRotation + 270) % 360
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(72.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RotateLeft,
                                        contentDescription = "Rotate Left",
                                        tint = accentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Left",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Right Card Button
                            Card(
                                onClick = {
                                    viewModel.rotateConfig.value = config.copy(
                                        previewRotation = (config.previewRotation + 90) % 360
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(72.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RotateRight,
                                        contentDescription = "Rotate Right",
                                        tint = accentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Right",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Flip Card Button
                            Card(
                                onClick = {
                                    viewModel.rotateConfig.value = config.copy(
                                        previewRotation = (config.previewRotation + 180) % 360
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(72.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Flip,
                                        contentDescription = "Flip 180 Degrees",
                                        tint = accentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Flip",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // 4. Apply To Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "APPLY TO",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Option Current Page
                        val isCurrent = config.applyTo == "current"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (isCurrent) accentColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isCurrent) accentColor else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.rotateConfig.value = config.copy(applyTo = "current")
                                }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RadioButton(
                                    selected = isCurrent,
                                    onClick = {
                                        viewModel.rotateConfig.value = config.copy(applyTo = "current")
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                                )
                                Column {
                                    Text(
                                        text = "Current Page",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Only rotate page ${config.currentPageIndex + 1}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Option All Pages
                        val isAll = config.applyTo == "all"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (isAll) accentColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isAll) accentColor else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.rotateConfig.value = config.copy(applyTo = "all")
                                }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RadioButton(
                                    selected = isAll,
                                    onClick = {
                                        viewModel.rotateConfig.value = config.copy(applyTo = "all")
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                                )
                                Column {
                                    Text(
                                        text = "All Pages",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Uniform rotation for $totalPages pages",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Sticky Button Panel
            if (!isComplete) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isBtnEnabled = config.previewRotation != 0
                    Button(
                        onClick = { viewModel.process(tool.id, context) },
                        enabled = isBtnEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Confirm Rotation",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "DPI: 300  |  SIZE: 8.5 x 11.0 in",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }

            // Progress dialogue
            if (isProcessing) {
                val uiStateVal = viewModel.uiState.collectAsState().value
                val statusMsg = (uiStateVal as? ToolUiState.Processing)?.statusMessage ?: "Rotating document pages..."
                AlertDialog(
                    onDismissRequest = {},
                    confirmButton = {},
                    dismissButton = {
                        OutlinedButton(onClick = viewModel::cancelProcessing) {
                            Text("Cancel")
                        }
                    },
                    title = {
                        Text(
                            text = "Rotating PDF...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (progress != null) {
                                LinearProgressIndicator(
                                    progress = { progress },
                                    color = accentColor,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                LinearProgressIndicator(
                                    color = accentColor,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Text(
                                text = statusMsg,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            }
        }
    }
}
