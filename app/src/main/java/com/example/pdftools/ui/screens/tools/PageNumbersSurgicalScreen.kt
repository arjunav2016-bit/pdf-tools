package com.example.pdftools.ui.screens.tools

import android.graphics.Bitmap
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
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pdftools.R
import com.example.pdftools.data.PdfTool
import com.example.pdftools.ui.components.PdfPagePreview
import com.example.pdftools.ui.screens.SuccessCard
import com.example.pdftools.ui.viewmodels.ToolUiState
import com.example.pdftools.ui.viewmodels.ToolViewModel
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageNumbersSurgicalScreen(
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
    val config by viewModel.pageNumberConfig.collectAsState()

    // Page indicator indexing
    var currentPageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedFile) {
        selectedFile?.let { viewModel.loadPageCount(context, it) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Page Numbers",
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
                    IconButton(onClick = { /* More actions menu */ }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options")
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
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 1. Live Preview Integration
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Live Preview",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Page ${currentPageIndex + 1} of ${pageCount ?: 1}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
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

                                    // Render Live Overlay
                                    val startPageVal = config.startFromPage.toIntOrNull() ?: 1
                                    val startNumVal = config.startingNumber.toIntOrNull() ?: 1

                                    val shouldNumber = when (config.rangeType) {
                                        "exclude_first" -> currentPageIndex > 0
                                        "start_from" -> currentPageIndex >= startPageVal - 1
                                        else -> true
                                    }

                                    if (shouldNumber) {
                                        val offset = when (config.rangeType) {
                                            "exclude_first" -> currentPageIndex - 1
                                            "start_from" -> currentPageIndex - (startPageVal - 1)
                                            else -> currentPageIndex
                                        }
                                        val pageNum = startNumVal + offset
                                        val totalPages = pageCount ?: 1
                                        
                                        val text = when (config.format) {
                                            "simple" -> "$pageNum"
                                            "prefixed" -> "Page $pageNum"
                                            "detailed" -> "Page $pageNum of $totalPages"
                                            else -> "$pageNum"
                                        }

                                        val overlayAlignment = when (config.position) {
                                            "top_left" -> Alignment.TopStart
                                            "top_center" -> Alignment.TopCenter
                                            "top_right" -> Alignment.TopEnd
                                            "bottom_left" -> Alignment.BottomStart
                                            "bottom_center" -> Alignment.BottomCenter
                                            "bottom_right" -> Alignment.BottomEnd
                                            else -> Alignment.BottomEnd
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            contentAlignment = overlayAlignment
                                        ) {
                                            val liveColor = try {
                                                Color(android.graphics.Color.parseColor(config.colorHex))
                                            } catch (_: Exception) {
                                                accentColor
                                            }
                                            Text(
                                                text = text,
                                                color = liveColor,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = (config.fontSize * 0.7f).sp
                                                ),
                                                fontWeight = FontWeight.SemiBold,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            } else {
                                CircularProgressIndicator(color = accentColor)
                            }
                        }

                        // Horizontal thumbnail / page chips navigation row
                        val totalPages = pageCount ?: 1
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { if (currentPageIndex > 0) currentPageIndex-- },
                                enabled = currentPageIndex > 0
                            ) {
                                Icon(Icons.Default.ChevronLeft, "Previous Page")
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val minPage = (currentPageIndex - 1).coerceAtLeast(0)
                                val maxPage = (currentPageIndex + 1).coerceAtMost(totalPages - 1)

                                if (minPage > 0) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("...", style = MaterialTheme.typography.bodySmall)
                                    }
                                }

                                for (i in minPage..maxPage) {
                                    val isSelected = i == currentPageIndex
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (isSelected) accentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) accentColor else Color.Transparent,
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clickable { currentPageIndex = i }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Page ${i + 1}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                if (maxPage < totalPages - 1) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("...", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            IconButton(
                                onClick = { if (currentPageIndex < totalPages - 1) currentPageIndex++ },
                                enabled = currentPageIndex < totalPages - 1
                            ) {
                                Icon(Icons.Default.ChevronRight, "Next Page")
                            }
                        }
                    }

                    // 2. Position Selection Grid
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Position",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select document header or footer alignment:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )

                            Column(
                                modifier = Modifier
                                    .width(136.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // 3x3 positions grid
                                val grid = listOf(
                                    listOf("top_left" to "↖", "top_center" to "↑", "top_right" to "↗"),
                                    listOf("" to "", "" to "", "" to ""),
                                    listOf("bottom_left" to "↙", "bottom_center" to "↓", "bottom_right" to "↘")
                                )

                                grid.forEach { row ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        row.forEach { (pos, arrow) ->
                                            if (pos.isEmpty()) {
                                                // Middle row disabled blocks
                                                Box(
                                                    modifier = Modifier
                                                        .size(34.dp)
                                                        .background(
                                                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                                                            shape = RoundedCornerShape(4.dp)
                                                        )
                                                )
                                            } else {
                                                val isSelected = config.position == pos
                                                Box(
                                                    modifier = Modifier
                                                        .size(34.dp)
                                                        .background(
                                                            color = if (isSelected) accentColor else MaterialTheme.colorScheme.surface,
                                                            shape = RoundedCornerShape(4.dp)
                                                        )
                                                        .border(
                                                            width = 1.dp,
                                                            color = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant,
                                                            shape = RoundedCornerShape(4.dp)
                                                        )
                                                        .clickable {
                                                            viewModel.pageNumberConfig.value = config.copy(position = pos)
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = arrow,
                                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Format Dropdown
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Format",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        var expanded by remember { mutableStateOf(false) }
                        val formatOptions = listOf(
                            "simple" to "Simple digits (1, 2, 3...)",
                            "prefixed" to "Prefixed (Page 1)",
                            "detailed" to "Detailed page status (Page 1 of N)"
                        )
                        val activeOptionLabel = formatOptions.firstOrNull { it.first == config.format }?.second ?: "Simple digits"

                        Box(modifier = Modifier.fillMaxWidth()) {
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = activeOptionLabel,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        focusedBorderColor = accentColor,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    formatOptions.forEach { (id, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                viewModel.pageNumberConfig.value = config.copy(format = id)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Font Size & Color Picker Input
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Font Size",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = config.fontSize.roundToInt().toString(),
                                onValueChange = { newVal ->
                                    val sizeVal = newVal.toFloatOrNull() ?: config.fontSize
                                    viewModel.pageNumberConfig.value = config.copy(fontSize = sizeVal.coerceIn(8f, 36f))
                                },
                                suffix = { Text("PT", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Color",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val bubbleColor = try {
                                Color(android.graphics.Color.parseColor(config.colorHex))
                            } catch (_: Exception) {
                                accentColor
                            }

                            var colorPickerExpanded by remember { mutableStateOf(false) }

                            OutlinedTextField(
                                value = config.colorHex.uppercase(),
                                onValueChange = { newVal ->
                                    viewModel.pageNumberConfig.value = config.copy(colorHex = newVal)
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(bubbleColor)
                                            .clickable { colorPickerExpanded = !colorPickerExpanded }
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Quick preset colors bubble list below or dropdown menu
                            if (colorPickerExpanded) {
                                val colors = listOf(
                                    "#80488D" to "Purple Accent",
                                    "#004080" to "Deep Blue",
                                    "#1E1E1E" to "Dark Charcoal",
                                    "#E74C3C" to "Red Warning",
                                    "#27AE60" to "Classic Green"
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    colors.forEach { (hex, _) ->
                                        val isSelected = config.colorHex.lowercase() == hex.lowercase()
                                        val itemColor = Color(android.graphics.Color.parseColor(hex))
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(itemColor)
                                                .drawBehind {
                                                    if (isSelected) {
                                                        drawCircle(
                                                            color = Color.White,
                                                            radius = 4.dp.toPx(),
                                                            style = Stroke(width = 2.dp.toPx())
                                                        )
                                                    }
                                                }
                                                .clickable {
                                                    viewModel.pageNumberConfig.value = config.copy(colorHex = hex)
                                                    colorPickerExpanded = false
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. Page Range Option Group
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Page Range",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Option: All Pages
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.pageNumberConfig.value = config.copy(rangeType = "all")
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = config.rangeType == "all",
                                onClick = {
                                    viewModel.pageNumberConfig.value = config.copy(rangeType = "all")
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "All Pages",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Option: Exclude First Page
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.pageNumberConfig.value = config.copy(rangeType = "exclude_first")
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = config.rangeType == "exclude_first",
                                onClick = {
                                    viewModel.pageNumberConfig.value = config.copy(rangeType = "exclude_first")
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Exclude First Page",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Option: Start from:
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = config.rangeType == "start_from",
                                onClick = {
                                    viewModel.pageNumberConfig.value = config.copy(rangeType = "start_from")
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start from:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.clickable {
                                    viewModel.pageNumberConfig.value = config.copy(rangeType = "start_from")
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            OutlinedTextField(
                                value = config.startFromPage,
                                onValueChange = {
                                    viewModel.pageNumberConfig.value = config.copy(
                                        startFromPage = it,
                                        rangeType = "start_from"
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier.width(72.dp)
                            )
                        }

                        // Starting Number Field
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Starting Number",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = config.startingNumber,
                                onValueChange = {
                                    viewModel.pageNumberConfig.value = config.copy(startingNumber = it)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Bottom Actions Panel
            if (!isComplete) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.resetCurrentRun() },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = { viewModel.process(tool.id, context) },
                        modifier = Modifier
                            .weight(1.5f)
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
                            Icon(Icons.Default.FormatListNumbered, null, modifier = Modifier.size(20.dp))
                            Text(
                                text = "Add Page Numbers",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Loading / Progress overlays
            if (isProcessing) {
                val uiStateVal = viewModel.uiState.collectAsState().value
                val statusMsg = (uiStateVal as? ToolUiState.Processing)?.statusMessage ?: "Formatting document pages..."
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
                            text = "Adding Page Numbers...",
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
