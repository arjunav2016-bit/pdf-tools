package com.example.pdftools.ui.screens.tools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pdftools.R
import com.example.pdftools.data.PdfTool
import com.example.pdftools.ui.components.PdfPagePreview
import com.example.pdftools.ui.screens.SuccessCard
import com.example.pdftools.ui.viewmodels.ToolUiState
import com.example.pdftools.ui.viewmodels.ToolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatermarkSurgicalScreen(
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
    val config by viewModel.watermarkConfig.collectAsState()

    // Page indicator indexing
    var currentPageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedFile) {
        selectedFile?.let { viewModel.loadPageCount(context, it) }
    }

    // Dynamic ImageBitmap loader for Image Watermarks
    val imageBitmap by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = config.imageUri
    ) {
        value = config.imageUri?.let { uri ->
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.watermarkConfig.value = config.copy(imageUri = uri)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Watermark",
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
                        enabled = if (config.isImage) config.imageUri != null else config.text.isNotEmpty()
                    ) {
                        Text(
                            text = "Apply",
                            color = if (if (config.isImage) config.imageUri != null else config.text.isNotEmpty()) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
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
                    // 1. Live Preview Integration
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedFile != null) {
                            Box(
                                modifier = Modifier
                                    .aspectRatio(0.707f) // Standard aspect ratio A4/Letter
                                    .fillMaxHeight()
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    .background(Color.White)
                            ) {
                                // PDF Page Background Preview
                                PdfPagePreview(
                                    uri = selectedFile,
                                    pageIndex = currentPageIndex,
                                    loadThumbnail = { uri, idx, width ->
                                        viewModel.renderPage(context, uri, idx, width)
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Live Watermark overlay according to position alignment
                                val overlayAlignment = when (config.position) {
                                    "top_left" -> Alignment.TopStart
                                    "top_center" -> Alignment.TopCenter
                                    "top_right" -> Alignment.TopEnd
                                    "center_left" -> Alignment.CenterStart
                                    "center" -> Alignment.Center
                                    "center_right" -> Alignment.CenterEnd
                                    "bottom_left" -> Alignment.BottomStart
                                    "bottom_center" -> Alignment.BottomCenter
                                    "bottom_right" -> Alignment.BottomEnd
                                    else -> Alignment.Center
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp), // Simulate margins
                                    contentAlignment = overlayAlignment
                                ) {
                                    if (config.isImage) {
                                        val bitmap = imageBitmap
                                        if (bitmap != null) {
                                            Image(
                                                bitmap = bitmap,
                                                contentDescription = "Live Watermark Image",
                                                modifier = Modifier
                                                    .width((config.fontSize * 1.8f).dp)
                                                    .aspectRatio(bitmap.width.toFloat() / bitmap.height.toFloat())
                                                    .alpha(config.opacity)
                                                    .graphicsLayer { rotationZ = config.rotation },
                                                contentScale = ContentScale.Fit
                                            )
                                        } else {
                                            Row(
                                                modifier = Modifier
                                                    .background(Color.Black.copy(alpha = 0.08f), shape = RoundedCornerShape(4.dp))
                                                    .padding(8.dp)
                                                    .alpha(config.opacity)
                                                    .graphicsLayer { rotationZ = config.rotation },
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(Icons.Default.Image, null, modifier = Modifier.size(16.dp))
                                                Text("Image Watermark", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = config.text.ifEmpty { "WATERMARK" },
                                            color = run {
                                                val parsed = try {
                                                    Color(android.graphics.Color.parseColor(config.colorHex))
                                                } catch (_: Exception) {
                                                    Color.Gray
                                                }
                                                parsed.copy(alpha = config.opacity)
                                            },
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontSize = (config.fontSize * 0.5f).dp.value.dp.value.sp
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .graphicsLayer { rotationZ = config.rotation }
                                        )
                                    }
                                }
                            }
                        } else {
                            CircularProgressIndicator(color = accentColor)
                        }

                        // Floating Page Navigator
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
                                onClick = { if (currentPageIndex > 0) currentPageIndex-- },
                                enabled = currentPageIndex > 0,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Previous Page",
                                    tint = if (currentPageIndex > 0) Color.White else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Page ${currentPageIndex + 1} of ${pageCount ?: 1}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { if (currentPageIndex < (pageCount ?: 1) - 1) currentPageIndex++ },
                                enabled = currentPageIndex < (pageCount ?: 1) - 1,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Next Page",
                                    tint = if (currentPageIndex < (pageCount ?: 1) - 1) Color.White else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // 2. Dual Watermark Type Toggle
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Watermark Type",
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
                            val isImg = config.isImage

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        color = if (!isImg) MaterialTheme.colorScheme.surface else Color.Transparent,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        viewModel.watermarkConfig.value = config.copy(isImage = false)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Text Watermark",
                                    color = if (!isImg) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        color = if (isImg) MaterialTheme.colorScheme.surface else Color.Transparent,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        viewModel.watermarkConfig.value = config.copy(isImage = true)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Image Watermark",
                                    color = if (isImg) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // 3. Granular Styling Controls
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (!config.isImage) {
                            // Text Specific Inputs
                            OutlinedTextField(
                                value = config.text,
                                onValueChange = { viewModel.watermarkConfig.value = config.copy(text = it) },
                                label = { Text("Watermark Text") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Quick preset chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val presets = listOf("CONFIDENTIAL", "DRAFT", "COPY", "FINAL")
                                presets.forEach { p ->
                                    val isSel = config.text == p
                                    AssistChip(
                                        onClick = { viewModel.watermarkConfig.value = config.copy(text = p) },
                                        label = { Text(p) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = if (isSel) accentColor else MaterialTheme.colorScheme.surfaceContainerHigh,
                                            labelColor = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                                        ),
                                        border = BorderStroke(1.dp, if (isSel) accentColor else MaterialTheme.colorScheme.outlineVariant)
                                    )
                                }
                            }

                            // Text Color selection row
                            Text(
                                text = "Color",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val colors = listOf(
                                    "#7F8C8D" to "Gray",
                                    "#E74C3C" to "Red",
                                    "#2980B9" to "Blue",
                                    "#27AE60" to "Green"
                                )
                                colors.forEach { (hex, _) ->
                                    val isSelected = config.colorHex.lowercase() == hex.lowercase()
                                    val bubbleColor = Color(android.graphics.Color.parseColor(hex))
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(bubbleColor)
                                            .drawBehind {
                                                if (isSelected) {
                                                    drawCircle(
                                                        color = Color.White,
                                                        radius = 5.dp.toPx(),
                                                        style = Stroke(width = 3.dp.toPx())
                                                    )
                                                }
                                            }
                                            .clickable { viewModel.watermarkConfig.value = config.copy(colorHex = hex) }
                                    )
                                }
                            }
                        } else {
                            // Image Specific Inputs
                            Button(
                                onClick = { imagePickerLauncher.launch(arrayOf("image/*")) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Image, null, modifier = Modifier.size(20.dp))
                                    Text(
                                        text = if (config.imageUri != null) "Change Selected Image" else "Select Watermark Image",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (config.imageUri != null) {
                                Text(
                                    text = "Image Selected successfully",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = accentColor,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }

                        // Sliders Grid
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Size Slider
                            Column {
                                Text(
                                    text = if (config.isImage) "Image Scale: ${(config.fontSize * 2.5f).roundToInt()} pt" else "Font Size: ${config.fontSize.roundToInt()} pt",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Slider(
                                    value = config.fontSize,
                                    onValueChange = { viewModel.watermarkConfig.value = config.copy(fontSize = it) },
                                    valueRange = 20f..100f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = accentColor,
                                        activeTrackColor = accentColor
                                    )
                                )
                            }

                            // Opacity Slider
                            Column {
                                Text(
                                    text = "Opacity: ${(config.opacity * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Slider(
                                    value = config.opacity,
                                    onValueChange = { viewModel.watermarkConfig.value = config.copy(opacity = it) },
                                    valueRange = 0.1f..0.9f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = accentColor,
                                        activeTrackColor = accentColor
                                    )
                                )
                            }

                            // Rotation Slider
                            Column {
                                Text(
                                    text = "Rotation: ${config.rotation.roundToInt()}°",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Slider(
                                    value = config.rotation,
                                    onValueChange = { viewModel.watermarkConfig.value = config.copy(rotation = it) },
                                    valueRange = -90f..90f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = accentColor,
                                        activeTrackColor = accentColor
                                    )
                                )
                            }
                        }
                    }

                    // 4. Interactive Placement Grid
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Interactive Position Placement",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val positions = listOf(
                            listOf("top_left", "top_center", "top_right"),
                            listOf("center_left", "center", "center_right"),
                            listOf("bottom_left", "bottom_center", "bottom_right")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select grid tile to align watermark on page:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )

                            Column(
                                modifier = Modifier
                                    .width(116.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                positions.forEach { row ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        row.forEach { pos ->
                                            val isSel = config.position == pos
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(
                                                        color = if (isSel) accentColor else MaterialTheme.colorScheme.surfaceContainerLow,
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isSel) accentColor else MaterialTheme.colorScheme.outlineVariant,
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                                    .clickable {
                                                        viewModel.watermarkConfig.value = config.copy(position = pos)
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSel) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .background(Color.White, shape = CircleShape)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. Page Range Input
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Apply to Pages (Optional)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = config.pageRange,
                            onValueChange = { viewModel.watermarkConfig.value = config.copy(pageRange = it) },
                            placeholder = { Text("e.g. 1-3, 5 (Leave empty for all pages)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Bottom Confirm Button
            if (!isComplete) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    val isBtnEnabled = if (config.isImage) config.imageUri != null else config.text.isNotEmpty()
                    Button(
                        onClick = { viewModel.process(tool.id, context) },
                        enabled = isBtnEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Layers, null, modifier = Modifier.size(20.dp))
                            Text(
                                text = "Confirm & Apply Watermark",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Progress dialogue
            if (isProcessing) {
                val uiStateVal = viewModel.uiState.collectAsState().value
                val statusMsg = (uiStateVal as? ToolUiState.Processing)?.statusMessage ?: "Drawing watermark overlays..."
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
                            text = "Applying Watermark...",
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
