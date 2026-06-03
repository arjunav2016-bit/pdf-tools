package com.example.pdftools.ui.screens.tools

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.pdftools.R
import com.example.pdftools.data.PdfTool
import com.example.pdftools.ui.components.PdfPagePreview
import com.example.pdftools.ui.screens.SuccessCard
import com.example.pdftools.ui.viewmodels.ToolUiState
import com.example.pdftools.ui.viewmodels.ToolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropPdfSurgicalScreen(
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
    val config by viewModel.cropConfig.collectAsState()
    val currentConfig by rememberUpdatedState(config)

    // Query actual page size from PDFBox to properly map MM coordinates to PDF points
    val pageSize by produceState<Pair<Float, Float>>(
        initialValue = 0f to 0f,
        key1 = selectedFile,
        key2 = config.currentPageIndex
    ) {
        value = selectedFile?.let { uri ->
            runCatching {
                viewModel.getPageSize(context, uri, config.currentPageIndex)
            }.getOrDefault(0f to 0f)
        } ?: (0f to 0f)
    }

    LaunchedEffect(selectedFile) {
        selectedFile?.let { viewModel.loadPageCount(context, it) }
    }

    // Initialize dimensions when page size is fetched
    LaunchedEffect(pageSize) {
        if (pageSize.first > 0 && pageSize.second > 0) {
            val widthMm = pageSize.first / 2.83464567f
            val heightMm = pageSize.second / 2.83464567f
            if (!config.useAbsoluteCrop && (config.widthMm == 0f || config.heightMm == 0f)) {
                viewModel.cropConfig.value = config.copy(
                    leftMm = 0f,
                    topMm = 0f,
                    widthMm = widthMm,
                    heightMm = heightMm
                )
            }
        }
    }

    var leftInput by remember { mutableStateOf("") }
    var topInput by remember { mutableStateOf("") }
    var widthInput by remember { mutableStateOf("") }
    var heightInput by remember { mutableStateOf("") }

    // Bidirectional sync: config change (drags/presets) updates inputs
    LaunchedEffect(config.leftMm, config.topMm, config.widthMm, config.heightMm) {
        leftInput = String.format(java.util.Locale.US, "%.1f", config.leftMm)
        topInput = String.format(java.util.Locale.US, "%.1f", config.topMm)
        widthInput = String.format(java.util.Locale.US, "%.1f", config.widthMm)
        heightInput = String.format(java.util.Locale.US, "%.1f", config.heightMm)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Crop PDF",
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
                    TextButton(
                        onClick = { viewModel.process(tool.id, context) },
                        enabled = pageSize.first > 0 && config.widthMm > 0 && config.heightMm > 0
                    ) {
                        Text(
                            text = "Apply",
                            color = if (pageSize.first > 0 && config.widthMm > 0 && config.heightMm > 0) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold
                        )
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
                        .padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Workspace / Interactive Document Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedFile != null && pageSize.first > 0 && pageSize.second > 0) {
                            val pageRatio = pageSize.first / pageSize.second
                            var containerSize by remember { mutableStateOf(IntSize.Zero) }

                            Box(
                                modifier = Modifier
                                    .aspectRatio(pageRatio)
                                    .fillMaxHeight()
                                    .onGloballyPositioned { coordinates ->
                                        containerSize = coordinates.size
                                    }
                            ) {
                                val W = containerSize.width.toFloat()
                                val H = containerSize.height.toFloat()

                                PdfPagePreview(
                                    uri = selectedFile,
                                    pageIndex = config.currentPageIndex,
                                    loadThumbnail = { uri, idx, width ->
                                        viewModel.renderPage(context, uri, idx, width)
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )

                                if (W > 0 && H > 0) {
                                    val pageWidthMm = pageSize.first / 2.83464567f
                                    val pageHeightMm = pageSize.second / 2.83464567f

                                    val cropLeftPx = (config.leftMm / pageWidthMm) * W
                                    val cropTopPx = (config.topMm / pageHeightMm) * H
                                    val cropWidthPx = (config.widthMm / pageWidthMm) * W
                                    val cropHeightPx = (config.heightMm / pageHeightMm) * H

                                    // Outer dim overlay + Inner dashed crop frame
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        // Draw top block
                                        drawRect(
                                            color = Color.Black.copy(alpha = 0.45f),
                                            topLeft = Offset(0f, 0f),
                                            size = Size(size.width, cropTopPx)
                                        )
                                        // Draw bottom block
                                        drawRect(
                                            color = Color.Black.copy(alpha = 0.45f),
                                            topLeft = Offset(0f, cropTopPx + cropHeightPx),
                                            size = Size(size.width, size.height - (cropTopPx + cropHeightPx))
                                        )
                                        // Draw left block
                                        drawRect(
                                            color = Color.Black.copy(alpha = 0.45f),
                                            topLeft = Offset(0f, cropTopPx),
                                            size = Size(cropLeftPx, cropHeightPx)
                                        )
                                        // Draw right block
                                        drawRect(
                                            color = Color.Black.copy(alpha = 0.45f),
                                            topLeft = Offset(cropLeftPx + cropWidthPx, cropTopPx),
                                            size = Size(size.width - (cropLeftPx + cropWidthPx), cropHeightPx)
                                        )

                                        // Draw dashed crop frame border
                                        drawRect(
                                            color = accentColor,
                                            topLeft = Offset(cropLeftPx, cropTopPx),
                                            size = Size(cropWidthPx, cropHeightPx),
                                            style = Stroke(
                                                width = 2.dp.toPx(),
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                                            )
                                        )
                                    }

                                    // Interactive drag handles at the 4 corners
                                    val density = LocalDensity.current
                                    val targetSize = 36.dp
                                    val hVis = 14.dp

                                    // Top-Left Corner
                                    Box(
                                        modifier = Modifier
                                            .offset(
                                                x = with(density) { (cropLeftPx - 18f).toDp() },
                                                y = with(density) { (cropTopPx - 18f).toDp() }
                                            )
                                            .size(targetSize)
                                            .pointerInput(pageSize) {
                                                detectDragGestures { change, dragAmount ->
                                                    change.consume()
                                                    val deltaX = (dragAmount.x / W) * pageWidthMm
                                                    val deltaY = (dragAmount.y / H) * pageHeightMm
                                                    val newL = (currentConfig.leftMm + deltaX).coerceIn(0f, currentConfig.leftMm + currentConfig.widthMm - 10f)
                                                    val newT = (currentConfig.topMm + deltaY).coerceIn(0f, currentConfig.topMm + currentConfig.heightMm - 10f)
                                                    viewModel.cropConfig.value = currentConfig.copy(
                                                        leftMm = newL,
                                                        topMm = newT,
                                                        widthMm = currentConfig.leftMm + currentConfig.widthMm - newL,
                                                        heightMm = currentConfig.topMm + currentConfig.heightMm - newT,
                                                        useAbsoluteCrop = true
                                                    )
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(hVis)
                                                .background(Color.White, shape = RoundedCornerShape(2.dp))
                                                .border(2.5.dp, accentColor, shape = RoundedCornerShape(2.dp))
                                        )
                                    }

                                    // Top-Right Corner
                                    Box(
                                        modifier = Modifier
                                            .offset(
                                                x = with(density) { (cropLeftPx + cropWidthPx - 18f).toDp() },
                                                y = with(density) { (cropTopPx - 18f).toDp() }
                                            )
                                            .size(targetSize)
                                            .pointerInput(pageSize) {
                                                detectDragGestures { change, dragAmount ->
                                                    change.consume()
                                                    val deltaX = (dragAmount.x / W) * pageWidthMm
                                                    val deltaY = (dragAmount.y / H) * pageHeightMm
                                                    val newW = (currentConfig.widthMm + deltaX).coerceIn(10f, pageWidthMm - currentConfig.leftMm)
                                                    val newT = (currentConfig.topMm + deltaY).coerceIn(0f, currentConfig.topMm + currentConfig.heightMm - 10f)
                                                    viewModel.cropConfig.value = currentConfig.copy(
                                                        topMm = newT,
                                                        widthMm = newW,
                                                        heightMm = currentConfig.topMm + currentConfig.heightMm - newT,
                                                        useAbsoluteCrop = true
                                                    )
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(hVis)
                                                .background(Color.White, shape = RoundedCornerShape(2.dp))
                                                .border(2.5.dp, accentColor, shape = RoundedCornerShape(2.dp))
                                        )
                                    }

                                    // Bottom-Left Corner
                                    Box(
                                        modifier = Modifier
                                            .offset(
                                                x = with(density) { (cropLeftPx - 18f).toDp() },
                                                y = with(density) { (cropTopPx + cropHeightPx - 18f).toDp() }
                                            )
                                            .size(targetSize)
                                            .pointerInput(pageSize) {
                                                detectDragGestures { change, dragAmount ->
                                                    change.consume()
                                                    val deltaX = (dragAmount.x / W) * pageWidthMm
                                                    val deltaY = (dragAmount.y / H) * pageHeightMm
                                                    val newL = (currentConfig.leftMm + deltaX).coerceIn(0f, currentConfig.leftMm + currentConfig.widthMm - 10f)
                                                    val newH = (currentConfig.heightMm + deltaY).coerceIn(10f, pageHeightMm - currentConfig.topMm)
                                                    viewModel.cropConfig.value = currentConfig.copy(
                                                        leftMm = newL,
                                                        widthMm = currentConfig.leftMm + currentConfig.widthMm - newL,
                                                        heightMm = newH,
                                                        useAbsoluteCrop = true
                                                    )
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(hVis)
                                                .background(Color.White, shape = RoundedCornerShape(2.dp))
                                                .border(2.5.dp, accentColor, shape = RoundedCornerShape(2.dp))
                                        )
                                    }

                                    // Bottom-Right Corner
                                    Box(
                                        modifier = Modifier
                                            .offset(
                                                x = with(density) { (cropLeftPx + cropWidthPx - 18f).toDp() },
                                                y = with(density) { (cropTopPx + cropHeightPx - 18f).toDp() }
                                            )
                                            .size(targetSize)
                                            .pointerInput(pageSize) {
                                                detectDragGestures { change, dragAmount ->
                                                    change.consume()
                                                    val deltaX = (dragAmount.x / W) * pageWidthMm
                                                    val deltaY = (dragAmount.y / H) * pageHeightMm
                                                    val newW = (currentConfig.widthMm + deltaX).coerceIn(10f, pageWidthMm - currentConfig.leftMm)
                                                    val newH = (currentConfig.heightMm + deltaY).coerceIn(10f, pageHeightMm - currentConfig.topMm)
                                                    viewModel.cropConfig.value = currentConfig.copy(
                                                        widthMm = newW,
                                                        heightMm = newH,
                                                        useAbsoluteCrop = true
                                                    )
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(hVis)
                                                .background(Color.White, shape = RoundedCornerShape(2.dp))
                                                .border(2.5.dp, accentColor, shape = RoundedCornerShape(2.dp))
                                        )
                                    }
                                }
                            }
                        } else {
                            CircularProgressIndicator(color = accentColor)
                        }

                        // Floating Page Indicator Pill
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                                .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (config.currentPageIndex > 0) {
                                        viewModel.cropConfig.value = config.copy(currentPageIndex = config.currentPageIndex - 1)
                                    }
                                },
                                enabled = config.currentPageIndex > 0,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Previous Page",
                                    tint = if (config.currentPageIndex > 0) Color.White else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Page ${config.currentPageIndex + 1} of ${pageCount ?: 1}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = {
                                    if (config.currentPageIndex < (pageCount ?: 1) - 1) {
                                        viewModel.cropConfig.value = config.copy(currentPageIndex = config.currentPageIndex + 1)
                                    }
                                },
                                enabled = config.currentPageIndex < (pageCount ?: 1) - 1,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Next Page",
                                    tint = if (config.currentPageIndex < (pageCount ?: 1) - 1) Color.White else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // 2. Presets Panel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Presets",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val presetW = pageSize.first / 2.83464567f
                            val presetH = pageSize.second / 2.83464567f

                            val presets = listOf("Auto-Crop", "A4", "Letter", "Square", "Custom")

                            presets.forEach { preset ->
                                val isSelected = when (preset) {
                                    "Auto-Crop" -> config.leftMm == 15f && config.topMm == 15f && config.widthMm == (presetW - 30f) && config.heightMm == (presetH - 30f)
                                    "A4" -> {
                                        val scale = minOf(presetW / 210f, presetH / 297f).coerceAtMost(1f)
                                        val expectedW = 210f * scale
                                        val expectedH = 297f * scale
                                        Math.abs(config.widthMm - expectedW) < 0.5f && Math.abs(config.heightMm - expectedH) < 0.5f
                                    }
                                    "Letter" -> {
                                        val scale = minOf(presetW / 215.9f, presetH / 279.4f).coerceAtMost(1f)
                                        val expectedW = 215.9f * scale
                                        val expectedH = 279.4f * scale
                                        Math.abs(config.widthMm - expectedW) < 0.5f && Math.abs(config.heightMm - expectedH) < 0.5f
                                    }
                                    "Square" -> {
                                        val side = minOf(presetW, presetH)
                                        Math.abs(config.widthMm - side) < 0.5f && Math.abs(config.heightMm - side) < 0.5f
                                    }
                                    else -> !config.useAbsoluteCrop || (
                                        !(config.leftMm == 15f && config.topMm == 15f && config.widthMm == (presetW - 30f) && config.heightMm == (presetH - 30f)) &&
                                        !run {
                                            val scaleA4 = minOf(presetW / 210f, presetH / 297f).coerceAtMost(1f)
                                            Math.abs(config.widthMm - 210f * scaleA4) < 0.5f
                                        } &&
                                        !run {
                                            val scaleLet = minOf(presetW / 215.9f, presetH / 279.4f).coerceAtMost(1f)
                                            Math.abs(config.widthMm - 215.9f * scaleLet) < 0.5f
                                        } &&
                                        Math.abs(config.widthMm - config.heightMm) > 0.5f
                                    )
                                }

                                val containerColor = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceContainerHigh
                                val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

                                AssistChip(
                                    onClick = {
                                        if (presetW > 0 && presetH > 0) {
                                            when (preset) {
                                                "Auto-Crop" -> {
                                                    viewModel.cropConfig.value = config.copy(
                                                        leftMm = 15f,
                                                        topMm = 15f,
                                                        widthMm = presetW - 30f,
                                                        heightMm = presetH - 30f,
                                                        useAbsoluteCrop = true
                                                    )
                                                }
                                                "A4" -> {
                                                    val scale = minOf(presetW / 210f, presetH / 297f).coerceAtMost(1f)
                                                    val finalW = 210f * scale
                                                    val finalH = 297f * scale
                                                    viewModel.cropConfig.value = config.copy(
                                                        leftMm = (presetW - finalW) / 2f,
                                                        topMm = (presetH - finalH) / 2f,
                                                        widthMm = finalW,
                                                        heightMm = finalH,
                                                        useAbsoluteCrop = true
                                                    )
                                                }
                                                "Letter" -> {
                                                    val scale = minOf(presetW / 215.9f, presetH / 279.4f).coerceAtMost(1f)
                                                    val finalW = 215.9f * scale
                                                    val finalH = 279.4f * scale
                                                    viewModel.cropConfig.value = config.copy(
                                                        leftMm = (presetW - finalW) / 2f,
                                                        topMm = (presetH - finalH) / 2f,
                                                        widthMm = finalW,
                                                        heightMm = finalH,
                                                        useAbsoluteCrop = true
                                                    )
                                                }
                                                "Square" -> {
                                                    val side = minOf(presetW, presetH)
                                                    viewModel.cropConfig.value = config.copy(
                                                        leftMm = (presetW - side) / 2f,
                                                        topMm = (presetH - side) / 2f,
                                                        widthMm = side,
                                                        heightMm = side,
                                                        useAbsoluteCrop = true
                                                    )
                                                }
                                                "Custom" -> {
                                                    viewModel.cropConfig.value = config.copy(
                                                        useAbsoluteCrop = true
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    label = { Text(text = preset) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = containerColor,
                                        labelColor = textColor
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant
                                    )
                                )
                            }
                        }
                    }

                    // 3. Manual Adjustments Panel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Manual Adjustment",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Box(
                                modifier = Modifier
                                    .background(
                                        color = accentColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "MM",
                                    color = accentColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        val maxW = if (pageSize.first > 0) pageSize.first / 2.83464567f else 999f
                        val maxH = if (pageSize.second > 0) pageSize.second / 2.83464567f else 999f

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = leftInput,
                                onValueChange = {
                                    leftInput = it
                                    val parsed = it.toFloatOrNull()
                                    if (parsed != null) {
                                        viewModel.cropConfig.value = config.copy(
                                            leftMm = parsed.coerceIn(0f, maxW - config.widthMm),
                                            useAbsoluteCrop = true
                                        )
                                    }
                                },
                                label = { Text("Left") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = topInput,
                                onValueChange = {
                                    topInput = it
                                    val parsed = it.toFloatOrNull()
                                    if (parsed != null) {
                                        viewModel.cropConfig.value = config.copy(
                                            topMm = parsed.coerceIn(0f, maxH - config.heightMm),
                                            useAbsoluteCrop = true
                                        )
                                    }
                                },
                                label = { Text("Top") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = widthInput,
                                onValueChange = {
                                    widthInput = it
                                    val parsed = it.toFloatOrNull()
                                    if (parsed != null) {
                                        viewModel.cropConfig.value = config.copy(
                                            widthMm = parsed.coerceIn(10f, maxW - config.leftMm),
                                            useAbsoluteCrop = true
                                        )
                                    }
                                },
                                label = { Text("Width") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = heightInput,
                                onValueChange = {
                                    heightInput = it
                                    val parsed = it.toFloatOrNull()
                                    if (parsed != null) {
                                        viewModel.cropConfig.value = config.copy(
                                            heightMm = parsed.coerceIn(10f, maxH - config.topMm),
                                            useAbsoluteCrop = true
                                        )
                                    }
                                },
                                label = { Text("Height") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 4. Batch Processing Bar ("Apply to" toggle)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Apply to",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val applyToAll = config.applyToAllPages

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        color = if (!applyToAll) MaterialTheme.colorScheme.surface else Color.Transparent,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        viewModel.cropConfig.value = config.copy(applyToAllPages = false)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Current Page",
                                    color = if (!applyToAll) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        color = if (applyToAll) MaterialTheme.colorScheme.surface else Color.Transparent,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        viewModel.cropConfig.value = config.copy(applyToAllPages = true)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "All Pages",
                                    color = if (applyToAll) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Primary Bottom Action Button
            if (!isComplete) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = { viewModel.process(tool.id, context) },
                        enabled = pageSize.first > 0 && config.widthMm > 0 && config.heightMm > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Crop,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Confirm Selection",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Processing Progress overlay dialog
            if (isProcessing) {
                val uiStateVal = viewModel.uiState.collectAsState().value
                val statusMsg = (uiStateVal as? ToolUiState.Processing)?.statusMessage ?: "Trimming document pages..."
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
                            text = "Cropping PDF...",
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
