package com.example.pdftools.ui.screens.tools

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pdftools.R
import com.example.pdftools.data.PdfTool
import com.example.pdftools.ui.components.PdfPagePreview
import com.example.pdftools.ui.viewmodels.ToolViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordToolConfig(
    viewModel: ToolViewModel,
    tool: PdfTool,
    accentColor: Color
) {
    val config by viewModel.passwordConfig.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (tool.id == "protect_pdf") {
                stringResource(R.string.tool_set_password)
            } else {
                stringResource(R.string.tool_enter_password)
            },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = config.password,
            onValueChange = { viewModel.passwordConfig.value = config.copy(password = it) },
            placeholder = {
                Text(
                    text = if (tool.id == "protect_pdf") {
                        stringResource(R.string.tool_choose_strong_password)
                    } else {
                        stringResource(R.string.tool_enter_document_password)
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) {
                    stringResource(R.string.tool_hide_password)
                } else {
                    stringResource(R.string.tool_show_password)
                }
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            },
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
            text = if (tool.id == "protect_pdf") {
                stringResource(R.string.tool_protect_pdf_help)
            } else {
                stringResource(R.string.tool_unlock_pdf_help)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedactToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color
) {
    val config by viewModel.redactConfig.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val context = LocalContext.current
    var totalPages by remember { mutableStateOf(1) }

    LaunchedEffect(selectedFiles) {
        if (selectedFiles.isEmpty()) return@LaunchedEffect
        try {
            context.contentResolver.openInputStream(selectedFiles.first())?.use { input ->
                val tempFile = File.createTempFile("redact_page_count_", ".pdf", context.cacheDir)
                try {
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                    com.tom_roush.pdfbox.pdmodel.PDDocument.load(tempFile).use { doc ->
                        totalPages = doc.numberOfPages
                    }
                } finally {
                    tempFile.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Set initial custom values on active load if they are default to match tool screen
    LaunchedEffect(Unit) {
        if (config.x == 50f && config.y == 50f) {
            viewModel.redactConfig.value = config.copy(x = 100f, y = 500f, width = 200f, height = 40f)
        }
    }

    val selectedFile = selectedFiles.firstOrNull()
    val warningColor = MaterialTheme.colorScheme.error
    val surface = MaterialTheme.colorScheme.surfaceContainer
    val elevatedSurface = MaterialTheme.colorScheme.surfaceContainerHigh
    val outline = MaterialTheme.colorScheme.outlineVariant
    val primaryText = MaterialTheme.colorScheme.onSurface
    val secondaryText = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = warningColor.copy(alpha = 0.12f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, warningColor.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = warningColor,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Redaction is permanent. Removed content cannot be recovered from the exported PDF.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = warningColor
                )
            }
        }

        RedactionPreviewCard(
            selectedFile = selectedFile,
            pageIndex = config.pageIndex,
            totalPages = totalPages,
            x = config.x,
            y = config.y,
            width = config.width,
            height = config.height,
            style = config.redactionStyle,
            accentColor = accentColor,
            viewModel = viewModel
        )

        RedactionSummaryCard(
            textQuery = config.textToRedact,
            mode = config.redactionMode,
            hasManualArea = config.width > 0f && config.height > 0f,
            sanitizeMetadata = config.sanitizeMetadata,
            accentColor = accentColor
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, outline)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Intelligent Redaction Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryText
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(elevatedSurface, RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RedactionSegment(
                        label = "Text Search",
                        icon = Icons.Filled.Search,
                        selected = config.redactionMode == "text_search",
                        accentColor = accentColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.redactConfig.value = config.copy(redactionMode = "text_search")
                        }
                    )
                    RedactionSegment(
                        label = "Area Selection",
                        icon = Icons.Filled.CropFree,
                        selected = config.redactionMode == "area_selection",
                        accentColor = accentColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.redactConfig.value = config.copy(redactionMode = "area_selection")
                        }
                    )
                }

                if (config.redactionMode == "text_search") {
                    OutlinedTextField(
                        value = config.textToRedact,
                        onValueChange = { viewModel.redactConfig.value = config.copy(textToRedact = it) },
                        label = { Text("Sensitive text to find") },
                        placeholder = { Text("Email, SSN, client ID, or exact phrase") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = accentColor
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            focusedLabelColor = accentColor,
                            cursorColor = accentColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    if (totalPages > 1) {
                        RedactionSlider(
                            label = "Page",
                            valueText = "${config.pageIndex + 1} of $totalPages",
                            value = config.pageIndex.toFloat(),
                            onValueChange = {
                                viewModel.redactConfig.value = config.copy(pageIndex = it.toInt())
                            },
                            valueRange = 0f..(totalPages - 1).toFloat(),
                            steps = if (totalPages > 2) totalPages - 2 else 0,
                            accentColor = accentColor
                        )
                    }
                    RedactionSlider(
                        label = "X Position",
                        valueText = "${config.x.toInt()} pt",
                        value = config.x,
                        onValueChange = { viewModel.redactConfig.value = config.copy(x = it) },
                        valueRange = 0f..500f,
                        accentColor = accentColor
                    )
                    RedactionSlider(
                        label = "Y Position",
                        valueText = "${config.y.toInt()} pt",
                        value = config.y,
                        onValueChange = { viewModel.redactConfig.value = config.copy(y = it) },
                        valueRange = 0f..700f,
                        accentColor = accentColor
                    )
                    RedactionSlider(
                        label = "Width",
                        valueText = "${config.width.toInt()} pt",
                        value = config.width,
                        onValueChange = { viewModel.redactConfig.value = config.copy(width = it) },
                        valueRange = 10f..400f,
                        accentColor = accentColor
                    )
                    RedactionSlider(
                        label = "Height",
                        valueText = "${config.height.toInt()} pt",
                        value = config.height,
                        onValueChange = { viewModel.redactConfig.value = config.copy(height = it) },
                        valueRange = 10f..200f,
                        accentColor = accentColor
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, outline)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Redaction Style",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryText
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "black" to "Black",
                        "white" to "White",
                        "patterned" to "Patterned"
                    ).forEach { (style, label) ->
                        RedactionStyleOption(
                            label = label,
                            selected = config.redactionStyle == style,
                            style = style,
                            accentColor = accentColor,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.redactConfig.value = config.copy(redactionStyle = style)
                            }
                        )
                    }
                }

                HorizontalDivider(color = outline)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Sanitize Metadata",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = primaryText
                        )
                        Text(
                            text = "Remove author names, hidden data, comments, and layers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryText
                        )
                    }
                    Switch(
                        checked = config.sanitizeMetadata,
                        onCheckedChange = {
                            viewModel.redactConfig.value = config.copy(sanitizeMetadata = it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = accentColor,
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.10f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Preview every redaction zone before running the secure export.",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryText
                )
            }
        }
    }
}

@Composable
private fun RedactionSegment(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) accentColor.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RedactionSlider(
    label: String,
    valueText: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    accentColor: Color,
    steps: Int = 0,
    enabled: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        )
    }
}

@Composable
private fun RedactionStyleOption(
    label: String,
    selected: Boolean,
    style: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(86.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) accentColor.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) accentColor else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RedactionSwatch(style = style, modifier = Modifier.size(width = 46.dp, height = 22.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RedactionSwatch(
    style: String,
    modifier: Modifier = Modifier
) {
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
    ) {
        when (style) {
            "white" -> drawRect(Color.White, size = size)
            "patterned" -> {
                drawRect(Color.Black, size = size)
                var start = -size.height
                while (start < size.width) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.7f),
                        start = Offset(start, size.height),
                        end = Offset(start + size.height, 0f),
                        strokeWidth = 3f
                    )
                    start += 10f
                }
            }
            else -> drawRect(Color.Black, size = size)
        }
    }
}

@Composable
private fun RedactionPreviewCard(
    selectedFile: android.net.Uri?,
    pageIndex: Int,
    totalPages: Int,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    style: String,
    accentColor: Color,
    viewModel: ToolViewModel
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Redaction Preview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Page ${pageIndex + 1} of $totalPages",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(230.dp)
                        .height(320.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                ) {
                    if (selectedFile != null) {
                        PdfPagePreview(
                            uri = selectedFile,
                            pageIndex = pageIndex.coerceAtLeast(0),
                            loadThumbnail = { uri, idx, previewWidth ->
                                viewModel.renderPage(context, uri, idx, previewWidth)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        SimulatedDocumentLines()
                    }

                    RedactionOverlayBox(
                        x = x,
                        y = y,
                        width = width,
                        height = height,
                        style = style
                    )
                }
            }
        }
    }
}

@Composable
private fun SimulatedDocumentLines() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineColor = Color.LightGray.copy(alpha = 0.45f)
        for (i in 1..14) {
            val lineY = i * (size.height / 16f)
            val end = if (i % 4 == 0) size.width * 0.55f else size.width * 0.86f
            drawLine(
                color = lineColor,
                start = Offset(size.width * 0.12f, lineY),
                end = Offset(end, lineY),
                strokeWidth = if (i % 4 == 0) 5f else 3f
            )
        }
    }
}

@Composable
private fun BoxScope.RedactionOverlayBox(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    style: String
) {
    val pageWidth = 612f
    val pageHeight = 792f
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val left = maxWidth * (x / pageWidth).coerceIn(0f, 0.95f)
        val top = maxHeight * (1f - ((y + height) / pageHeight)).coerceIn(0f, 0.95f)
        val rectWidth = maxWidth * (width / pageWidth).coerceIn(0.04f, 1f)
        val rectHeight = maxHeight * (height / pageHeight).coerceIn(0.025f, 1f)

        Box(
            modifier = Modifier
                .offset(x = left, y = top)
                .size(width = rectWidth, height = rectHeight)
                .clip(RoundedCornerShape(3.dp))
                .background(
                    when (style) {
                        "white" -> Color.White
                        else -> Color.Black
                    }
                )
                .border(
                    1.dp,
                    if (style == "white") Color.Black else Color.White.copy(alpha = 0.65f),
                    RoundedCornerShape(3.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (style == "patterned") {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var start = -size.height
                    while (start < size.width) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.45f),
                            start = Offset(start, size.height),
                            end = Offset(start + size.height, 0f),
                            strokeWidth = 4f
                        )
                        start += 12f
                    }
                }
            }
            Text(
                text = "REDACTED",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = if (style == "white") Color.Black else Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RedactionSummaryCard(
    textQuery: String,
    mode: String,
    hasManualArea: Boolean,
    sanitizeMetadata: Boolean,
    accentColor: Color
) {
    val textRules = if (textQuery.isBlank()) 0 else 1
    val manualAreas = if (hasManualArea) 1 else 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Redaction Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RedactionSummaryMetric(
                    value = if (mode == "text_search") textRules.toString() else "0",
                    label = "text rules",
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )
                RedactionSummaryMetric(
                    value = if (mode == "area_selection") manualAreas.toString() else "1",
                    label = "manual area",
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )
                RedactionSummaryMetric(
                    value = if (sanitizeMetadata) "On" else "Off",
                    label = "metadata",
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RedactionSummaryMetric(
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(accentColor.copy(alpha = 0.10f))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            maxLines = 1
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
