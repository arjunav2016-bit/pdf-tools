package com.example.pdftools.ui.screens.tools

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pdftools.R
import com.example.pdftools.theme.LocalDarkTheme
import com.example.pdftools.ui.screens.rememberThumbnailBitmap
import com.example.pdftools.ui.screens.getFileNameFromUri
import com.example.pdftools.ui.viewmodels.HtmlConfig
import com.example.pdftools.ui.viewmodels.PdfToImageConfig
import com.example.pdftools.ui.viewmodels.ScanConfig
import com.example.pdftools.ui.viewmodels.ToolViewModel
import com.example.pdftools.ui.viewmodels.PdfToPptConfig
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import com.example.pdftools.ui.components.PdfPagePreview


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color
) {
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val config by viewModel.scanConfig.collectAsState()
    val context = LocalContext.current

    // Synchronize rotation entries with selected files
    LaunchedEffect(selectedFiles) {
        val currentRotations = config.rotations.toMutableList()
        while (currentRotations.size < selectedFiles.size) {
            currentRotations.add(0)
        }
        while (currentRotations.size > selectedFiles.size) {
            currentRotations.removeAt(currentRotations.size - 1)
        }
        viewModel.scanConfig.value = config.copy(rotations = currentRotations)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.tool_captured_images),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        val chunks = selectedFiles.chunked(2)
        chunks.forEachIndexed { rowIndex, pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEachIndexed { colIndex, uri ->
                    val globalIndex = rowIndex * 2 + colIndex
                    val rotation = config.rotations.getOrNull(globalIndex) ?: 0
                    val thumbnail = rememberThumbnailBitmap(context, uri)

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.75f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (thumbnail != null) {
                                Image(
                                    bitmap = thumbnail.asImageBitmap(),
                                    contentDescription = stringResource(R.string.tool_image_thumbnail),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            rotationZ = rotation.toFloat()
                                        }
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = accentColor)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .size(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${globalIndex + 1}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        val currentRotations = config.rotations.toMutableList()
                                        if (globalIndex < currentRotations.size) {
                                            currentRotations[globalIndex] = (currentRotations[globalIndex] + 90) % 360
                                            viewModel.scanConfig.value = config.copy(rotations = currentRotations)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.RotateRight,
                                        contentDescription = stringResource(R.string.tool_rotate),
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.removeFile(globalIndex)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = stringResource(R.string.tool_remove),
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.tool_visual_enhancement_filter),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                "Original" to stringResource(R.string.tool_filter_original),
                "Grayscale" to stringResource(R.string.tool_filter_grayscale),
                "B&W Binarization" to stringResource(R.string.tool_filter_bw)
            )
            filters.forEach { (filterValue, label) ->
                val isSelected = if (filterValue == "Original") {
                    config.filter == "color" || config.filter == "Original"
                } else {
                    config.filter.contains(filterValue, ignoreCase = true) || filterValue == config.filter
                }
                val bg = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceContainerLow
                val tc = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                Card(
                    onClick = {
                        val newFilter = if (filterValue == "Original") "color" else filterValue
                        viewModel.scanConfig.value = config.copy(filter = newFilter)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = bg,
                        contentColor = tc
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HtmlToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color
) {
    val config by viewModel.htmlConfig.collectAsState()
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
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "ProForma PDF",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = primaryText
            )
            Text(
                text = "Capture web pages as precise, print-ready PDF documents.",
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryText
            )
        }

        OutlinedTextField(
            value = config.url,
            onValueChange = {
                viewModel.htmlConfig.value = config.copy(
                    inputType = "url",
                    url = it
                )
            },
            label = { Text("Web page URL") },
            placeholder = { Text("https://example.com/report") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Language,
                    contentDescription = null,
                    tint = accentColor
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                focusedLabelColor = accentColor,
                cursorColor = accentColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = surface),
            border = BorderStroke(1.dp, outline)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Web Page Options",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryText
                    )
                }

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
                            text = "Load JavaScript",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = primaryText
                        )
                        Text(
                            text = "Render dynamic content before capture.",
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryText
                        )
                    }
                    Switch(
                        checked = config.loadJs,
                        onCheckedChange = {
                            viewModel.htmlConfig.value = config.copy(loadJs = it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = accentColor,
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
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
                            text = "Background Graphics",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = primaryText
                        )
                        Text(
                            text = "Keep colors, images, and print backgrounds.",
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryText
                        )
                    }
                    Switch(
                        checked = config.loadBackgroundGraphics,
                        onCheckedChange = {
                            viewModel.htmlConfig.value = config.copy(loadBackgroundGraphics = it)
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
            colors = CardDefaults.cardColors(containerColor = surface),
            border = BorderStroke(1.dp, outline)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CropFree,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "PDF Layout",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryText
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Page Scale",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = primaryText
                        )
                        Text(
                            text = "${(config.pageScale * 100).toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                    Slider(
                        value = config.pageScale,
                        onValueChange = {
                            viewModel.htmlConfig.value = config.copy(pageScale = it)
                        },
                        valueRange = 0.6f..1.4f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    )
                    Text(
                        text = "Adjust web zoom to fit the page width without clipping.",
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryText
                    )
                }

                HorizontalDivider(color = outline)

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Capture Mode",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = primaryText
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(elevatedSurface, RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val modes = listOf(
                            "whole_page" to "Whole Page",
                            "selected_area" to "Selected Area"
                        )
                        modes.forEach { (mode, label) ->
                            val selected = config.captureArea == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (selected) accentColor.copy(alpha = 0.15f) else Color.Transparent
                                    )
                                    .clickable {
                                        viewModel.htmlConfig.value = config.copy(captureArea = mode)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) accentColor else secondaryText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                if (config.captureArea == "selected_area") {
                    OutlinedTextField(
                        value = config.selectedAreaSelector,
                        onValueChange = {
                            viewModel.htmlConfig.value = config.copy(selectedAreaSelector = it)
                        },
                        label = { Text("CSS selector") },
                        placeholder = { Text("#invoice, main, .article-body") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.SelectAll,
                                contentDescription = null,
                                tint = accentColor
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            focusedLabelColor = accentColor,
                            cursorColor = accentColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.10f)),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Best for dashboards, invoices, reports, and responsive pages that need to become portable documents.",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryText
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegacyHtmlToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color
) {
    val config by viewModel.htmlConfig.collectAsState()

    // HTML accent color - web-themed teal/cyan
    val htmlAccent = Color(0xFF0097A7)

    // We can infer template selection from the content of htmlContent or keep it local to UI
    var templateSelection by remember {
        mutableStateOf(
            when {
                config.htmlContent.contains("ACME Corp") -> "invoice"
                config.htmlContent.contains("Jane Sterling") -> "cv"
                config.htmlContent.contains("Technical Innovation Audit") -> "report"
                else -> ""
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ─── Info Card ───────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = htmlAccent.copy(alpha = 0.08f)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, htmlAccent.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Info",
                    tint = htmlAccent,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = stringResource(R.string.tool_html_info),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ─── 1. Content Source Toggle ────────────────────────
        Text(
            text = stringResource(R.string.tool_html_input_source),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val sources = listOf(
                "url" to stringResource(R.string.tool_html_input_url),
                "html" to stringResource(R.string.tool_html_input_html)
            )
            sources.forEach { (mode, title) ->
                val isSelected = config.inputType == mode
                val cardBg = if (isSelected) htmlAccent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                val borderCol = if (isSelected) htmlAccent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                val textCol = if (isSelected) htmlAccent else MaterialTheme.colorScheme.onSurface

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .clickable {
                            viewModel.htmlConfig.value = config.copy(inputType = mode)
                        },
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderCol),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (mode == "url") Icons.Filled.Language else Icons.Filled.Code,
                            contentDescription = title,
                            tint = textCol,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = textCol
                        )
                    }
                }
            }
        }

        // ─── 2. URL Input or HTML Editor ─────────────────────
        if (config.inputType == "url") {
            // URL Input Field
            OutlinedTextField(
                value = config.url,
                onValueChange = { viewModel.htmlConfig.value = config.copy(url = it) },
                label = { Text(stringResource(R.string.tool_html_url_label)) },
                placeholder = { Text(stringResource(R.string.tool_html_url_placeholder)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Language,
                        contentDescription = "URL",
                        tint = htmlAccent
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = htmlAccent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedLabelColor = htmlAccent
                )
            )
            Text(
                text = stringResource(R.string.tool_html_url_help),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            // Raw HTML mode: template selector + code editor
            Text(
                text = stringResource(R.string.tool_html_template_title),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val templates = listOf(
                    "invoice" to stringResource(R.string.tool_html_template_invoice),
                    "cv" to stringResource(R.string.tool_html_template_cv),
                    "report" to stringResource(R.string.tool_html_template_report)
                )

                templates.forEach { (template, label) ->
                    val isSel = templateSelection == template
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSel) htmlAccent else MaterialTheme.colorScheme.surfaceContainerLow,
                            contentColor = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    templateSelection = template
                                    val templateContent = when (template) {
                                        "invoice" -> """<!DOCTYPE html>
<html>
<head>
<style>
  body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #2C3E50; margin: 40px; line-height: 1.6; }
  .invoice-header { display: flex; justify-content: space-between; border-bottom: 2px solid #3498DB; padding-bottom: 20px; margin-bottom: 30px; }
  .logo { font-size: 28px; font-weight: bold; color: #3498DB; }
  .invoice-title { font-size: 24px; font-weight: bold; text-align: right; }
  .details-table { width: 100%; margin-bottom: 30px; }
  .details-table td { vertical-align: top; width: 50%; }
  .items-table { width: 100%; border-collapse: collapse; margin-top: 20px; }
  .items-table th { background-color: #3498DB; color: white; text-align: left; padding: 10px; font-weight: bold; }
  .items-table td { padding: 10px; border-bottom: 1px solid #BDC3C7; }
  .items-table tr:nth-child(even) { background-color: #F8F9F9; }
  .total-section { text-align: right; margin-top: 30px; font-size: 18px; font-weight: bold; }
  .footer { text-align: center; margin-top: 60px; font-size: 12px; color: #7F8C8D; border-top: 1px solid #ECF0F1; padding-top: 20px; }
</style>
</head>
<body>
  <div class="invoice-header">
    <div>
      <div class="logo">ACME Corp</div>
      <div>123 Innovation Way, Tech Suite 400<br>Silicon Valley, CA 94025</div>
    </div>
    <div class="invoice-title">
      INVOICE<br>
      <span style="font-size: 14px; font-weight: normal; color: #7F8C8D;">#INV-2026-0042<br>Date: May 21, 2026</span>
    </div>
  </div>
  <table class="details-table">
    <tr>
      <td>
        <strong style="color: #3498DB;">Billed To:</strong><br>
        John Doe Consulting<br>
        456 Business Road, Apt 2B<br>
        San Francisco, CA 94107
      </td>
      <td style="text-align: right;">
        <strong style="color: #3498DB;">Payment Details:</strong><br>
        Bank Transfer / ACH<br>
        Routing: XXXXXX789<br>
        Account: XXXXXXXX4560
      </td>
    </tr>
  </table>
  <table class="items-table">
    <thead>
      <tr>
        <th>Description</th>
        <th style="text-align: center; width: 80px;">Quantity</th>
        <th style="text-align: right; width: 120px;">Unit Price</th>
        <th style="text-align: right; width: 120px;">Total</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>Premium Android App UI & Architecture Consulting</td>
        <td style="text-align: center;">40 hrs</td>
        <td style="text-align: right;">${'$'}150.00</td>
        <td style="text-align: right;">${'$'}6,000.00</td>
      </tr>
      <tr>
        <td>PDF Conversion & Editing Engine Integration</td>
        <td style="text-align: center;">15 hrs</td>
        <td style="text-align: right;">${'$'}150.00</td>
        <td style="text-align: right;">${'$'}2,250.00</td>
      </tr>
      <tr>
        <td>Robolectric Offscreen WebView Test Bypass setup</td>
        <td style="text-align: center;">5 hrs</td>
        <td style="text-align: right;">${'$'}150.00</td>
        <td style="text-align: right;">${'$'}750.00</td>
      </tr>
    </tbody>
  </table>
  <div class="total-section">
    Subtotal: ${'$'}9,000.00<br>
    Tax (0%): ${'$'}0.00<br>
    <span style="color: #E74C3C; font-size: 22px;">Total Due: ${'$'}9,000.00</span>
  </div>
  <div class="footer">
    Thank you for your business! Please pay within 30 days.
  </div>
</body>
</html>"""
                                        "cv" -> """<!DOCTYPE html>
<html>
<head>
<style>
  body { font-family: Arial, sans-serif; color: #2C3E50; margin: 40px; line-height: 1.5; background-color: #FFFFFF; }
  .header { border-bottom: 3px solid #2C3E50; padding-bottom: 15px; margin-bottom: 25px; }
  .name { font-size: 32px; font-weight: bold; color: #2C3E50; margin: 0; letter-spacing: 1px; }
  .title { font-size: 18px; color: #16A085; font-weight: bold; margin-top: 5px; }
  .contact { font-size: 13px; color: #7F8C8D; margin-top: 5px; }
  .section-title { font-size: 18px; font-weight: bold; color: #2C3E50; border-bottom: 1px solid #BDC3C7; padding-bottom: 5px; margin-top: 25px; margin-bottom: 15px; text-transform: uppercase; }
  .job-title { font-weight: bold; font-size: 15px; color: #34495E; }
  .company { font-style: italic; color: #16A085; }
  .date { float: right; color: #7F8C8D; font-size: 13px; }
  .skills-container { margin-top: 10px; }
  .skill-pill { background-color: #ECF0F1; color: #2C3E50; padding: 6px 12px; border-radius: 15px; font-size: 13px; display: inline-block; font-weight: 500; margin-right: 5px; margin-bottom: 5px; }
  .bullet-list { margin-top: 5px; padding-left: 20px; }
  .bullet-list li { margin-bottom: 4px; }
</style>
</head>
<body>
  <div class="header">
    <div class="name">Jane Sterling</div>
    <div class="title">Lead Mobile Solutions Architect & PDF Systems Expert</div>
    <div class="contact">jane.sterling@email.com | +1 (555) 019-2834 | San Francisco, CA</div>
  </div>
  
  <div class="section-title">Professional Summary</div>
  <p style="margin: 0; font-size: 14px; text-align: justify;">
    Innovative Software Engineer with over 8 years of specialized experience in high-performance Android mobile systems and document processing components. Proven record of designing and deploying offline-first vector PDF rendering solutions, custom hardware-accelerated drawing pipelines, and complex on-device database architectures.
  </p>

  <div class="section-title">Work Experience</div>
  <div>
    <span class="date">2023 - Present</span>
    <div class="job-title">Principal Mobile Systems Engineer - <span class="company">Quantum Tech Corp</span></div>
    <ul class="bullet-list" style="font-size: 14px;">
      <li>Engineered a fully local on-device PDF annotation framework utilizing PDFBox, driving relative touch coordinate mapping and lowering runtime memory footprint by 45%.</li>
      <li>Implemented an offscreen headless WebView vector conversion engine mapping dynamic HTML/CSS invoices directly to paper-ready PDFs.</li>
    </ul>
  </div>
  
  <div style="margin-top: 15px;">
    <span class="date">2019 - 2023</span>
    <div class="job-title">Senior Android Developer - <span class="company">Apex Mobile Lab</span></div>
    <ul class="bullet-list" style="font-size: 14px;">
      <li>Led core architectural rewrite of enterprise document editor from legacy Java to modern Jetpack Compose and Kotlin coroutines.</li>
      <li>Implemented stateful layering systems for on-page text and vector graphic stamps.</li>
    </ul>
  </div>

  <div class="section-title">Skills</div>
  <div class="skills-container">
    <span class="skill-pill">Kotlin / Java</span>
    <span class="skill-pill">Jetpack Compose</span>
    <span class="skill-pill">Android Print Manager</span>
    <span class="skill-pill">PDFBox Internals</span>
    <span class="skill-pill">WebView Engine Rendering</span>
    <span class="skill-pill">Robolectric Testing</span>
  </div>
</body>
</html>"""
                                        "report" -> """<!DOCTYPE html>
<html>
<head>
<style>
  body { font-family: Georgia, serif; color: #2C3E50; margin: 50px; line-height: 1.7; font-size: 15px; }
  .report-title { font-size: 34px; font-weight: bold; color: #2C3E50; margin-bottom: 5px; text-align: center; }
  .report-subtitle { font-size: 18px; color: #7F8C8D; text-align: center; margin-bottom: 30px; font-style: italic; }
  .meta-box { background-color: #F8F9F9; border-left: 4px solid #34495E; padding: 15px; margin-bottom: 40px; font-family: sans-serif; font-size: 13px; }
  h2 { font-family: sans-serif; font-size: 20px; color: #2C3E50; border-bottom: 2px solid #BDC3C7; padding-bottom: 5px; margin-top: 30px; }
  .highlight-card { background-color: #EBF5FB; border-radius: 12px; padding: 20px; margin: 20px 0; border: 1px dashed #3498DB; font-family: sans-serif; font-size: 14px; }
  p { text-align: justify; }
</style>
</head>
<body>
  <div class="report-title">Q2 Technical Innovation Audit</div>
  <div class="report-subtitle">On-Device PDF Processing Engines and Local Performance Benchmarks</div>
  
  <div class="meta-box">
    <strong>Author:</strong> Lead Architect Team<br>
    <strong>Department:</strong> Mobile & Document Core Platform<br>
    <strong>Date:</strong> May 21, 2026<br>
    <strong>Classification:</strong> Internal Enterprise Report
  </div>
  
  <h2>1. Executive Summary</h2>
  <p>
    This report outlines the technical findings of our Q2 migration of heavy document operations to local, offline-first execution environments on mobile nodes. Historically, operations such as HTML vector conversions and interactive document annotations were routed through remote print relays, adding significant latency and data transfer costs.
  </p>
  
  <div class="highlight-card">
    <strong>Core Breakthrough:</strong> By leveraging on-device offscreen WebView layouts driven programmatically by the Android Print Document Adapter, we successfully reduced average document conversion times from 4.2 seconds to <strong>380 milliseconds</strong>, guaranteeing 100% data privacy.
  </div>

  <h2>2. Performance & Memory Profiles</h2>
  <p>
    Stamping visual annotations to existing PDF streams was implemented with coordinate normalization. The coordinates mapped directly from Compose's relative bounding boxes to PDFBox's mathematical bottom-left matrices. By safely managing bitmap recycling streams immediately after graphic operations, garbage collection pressure remained stable, presenting zero Out Of Memory (OOM) heap exceptions during high-load tests.
  </p>
</body>
</html>"""
                                        else -> ""
                                    }
                                    viewModel.htmlConfig.value = config.copy(htmlContent = templateContent)
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.tool_html_custom_source),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = config.htmlContent,
                onValueChange = { viewModel.htmlConfig.value = config.copy(htmlContent = it) },
                placeholder = { Text(stringResource(R.string.tool_html_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(16.dp),
                maxLines = 15,
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontSize = 12.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = htmlAccent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            Text(
                text = stringResource(R.string.tool_html_help),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ─── 3. Rendering Options ────────────────────────────
        Text(
            text = stringResource(R.string.tool_html_rendering_options),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // JavaScript Toggle Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.htmlConfig.value = config.copy(loadJs = !config.loadJs)
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (config.loadJs) htmlAccent.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (config.loadJs) htmlAccent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Code,
                        contentDescription = "JavaScript",
                        tint = if (config.loadJs) htmlAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.tool_html_load_js),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.tool_html_load_js_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = config.loadJs,
                    onCheckedChange = { value ->
                        viewModel.htmlConfig.value = config.copy(loadJs = value)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = htmlAccent,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }

        // Background Graphics Toggle Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.htmlConfig.value = config.copy(loadBackgroundGraphics = !config.loadBackgroundGraphics)
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (config.loadBackgroundGraphics) htmlAccent.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (config.loadBackgroundGraphics) htmlAccent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = "Background",
                        tint = if (config.loadBackgroundGraphics) htmlAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.tool_html_load_bg),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.tool_html_load_bg_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = config.loadBackgroundGraphics,
                    onCheckedChange = { value ->
                        viewModel.htmlConfig.value = config.copy(loadBackgroundGraphics = value)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = htmlAccent,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }

        // ─── 4. Page Scale Slider ────────────────────────────
        Text(
            text = stringResource(R.string.tool_html_page_scale),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ZoomIn,
                            contentDescription = "Scale",
                            tint = htmlAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(R.string.tool_html_page_scale_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = stringResource(R.string.tool_html_page_scale_value, (config.pageScale * 100).toInt()),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = htmlAccent
                    )
                }
                Slider(
                    value = config.pageScale,
                    onValueChange = { viewModel.htmlConfig.value = config.copy(pageScale = it) },
                    valueRange = 0.25f..2.0f,
                    steps = 6,
                    colors = SliderDefaults.colors(
                        thumbColor = htmlAccent,
                        activeTrackColor = htmlAccent,
                        inactiveTrackColor = htmlAccent.copy(alpha = 0.2f)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("25%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("100%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("200%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // ─── 5. Capture Area Toggle ──────────────────────────
        Text(
            text = stringResource(R.string.tool_html_capture_area),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val areas = listOf(
                "whole_page" to stringResource(R.string.tool_html_capture_whole),
                "selected_area" to stringResource(R.string.tool_html_capture_selected)
            )
            areas.forEach { (mode, title) ->
                val isSelected = config.captureArea == mode
                val cardBg = if (isSelected) htmlAccent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                val borderCol = if (isSelected) htmlAccent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                val textCol = if (isSelected) htmlAccent else MaterialTheme.colorScheme.onSurface

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .clickable {
                            viewModel.htmlConfig.value = config.copy(captureArea = mode)
                        },
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderCol),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (mode == "whole_page") Icons.Filled.Fullscreen else Icons.Filled.CropFree,
                            contentDescription = title,
                            tint = textCol,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = textCol
                        )
                    }
                }
            }
        }

        // CSS Selector input — only visible when "Selected Area" is chosen
        if (config.captureArea == "selected_area") {
            OutlinedTextField(
                value = config.selectedAreaSelector,
                onValueChange = { viewModel.htmlConfig.value = config.copy(selectedAreaSelector = it) },
                label = { Text(stringResource(R.string.tool_html_selector_label)) },
                placeholder = { Text(stringResource(R.string.tool_html_selector_placeholder)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.CropFree,
                        contentDescription = "Selector",
                        tint = htmlAccent
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontSize = 14.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = htmlAccent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedLabelColor = htmlAccent
                )
            )
            Text(
                text = stringResource(R.string.tool_html_selector_help),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfToImageToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color
) {
    val config by viewModel.pdfToImageConfig.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val pageCount by viewModel.pageCount.collectAsState()
    val context = LocalContext.current

    // Theme-aware palette (adapts to dark / light mode)
    val primaryBlue = accentColor
    val selectedAccentBg = accentColor.copy(alpha = 0.15f)
    val cardBg = MaterialTheme.colorScheme.surfaceContainerHigh
    val fileCardBg = MaterialTheme.colorScheme.surfaceContainer
    val borderCol = MaterialTheme.colorScheme.outlineVariant
    val inactiveTrackCol = MaterialTheme.colorScheme.surfaceContainerHighest
    val onSurfaceCol = MaterialTheme.colorScheme.onSurface
    val onSurfaceMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val textFieldBg = MaterialTheme.colorScheme.surfaceContainerHigh

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // File details card (at the top, if file is selected)
        val selectedFile = selectedFiles.firstOrNull()
        if (selectedFile != null) {
            val fileName = getFileNameFromUri(context, selectedFile)
            val fileSizeFormatted = getFileSizeFormatted(context, selectedFile)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = fileCardBg
                ),
                border = BorderStroke(1.dp, borderCol)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // PDF Icon Box (blue square)
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(primaryBlue),
                            contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Description,
                                contentDescription = "PDF",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "PDF",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 9.sp
                            )
                        }
                    }

                    // Text Details
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = fileName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceCol
                        )
                        val pageCountText = pageCount?.let { " · $it Pages" } ?: ""
                        Text(
                            text = "$fileSizeFormatted$pageCountText",
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceMuted
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            BadgeChip("SECURE")
                            BadgeChip("A4 FORMAT")
                        }
                    }
                }
            }
        }

        // Conversion Settings Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Tune,
                contentDescription = "Settings",
                tint = primaryBlue,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Conversion Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // ─── 1. Output Format Selection ──────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, borderCol)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Output Format",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurfaceMuted
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val formats = listOf("JPG", "PNG", "WebP")
                    formats.forEach { fmt ->
                        val isSelected = config.format.equals(fmt, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) primaryBlue else Color.Transparent)
                                .clickable {
                                    viewModel.pdfToImageConfig.value = config.copy(format = fmt.lowercase())
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = fmt,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else onSurfaceMuted
                            )
                        }
                    }
                }
            }
        }

        // ─── 2. Image Quality Slider ─────────────────────────
        if (config.format != "png") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderCol)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Image Quality",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = onSurfaceMuted
                        )
                        Text(
                            text = "${config.quality}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryBlue
                        )
                    }
                    Slider(
                        value = config.quality.toFloat(),
                        onValueChange = { viewModel.pdfToImageConfig.value = config.copy(quality = it.toInt()) },
                        valueRange = 10f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = primaryBlue,
                            activeTrackColor = primaryBlue,
                            inactiveTrackColor = inactiveTrackCol
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Standard", style = MaterialTheme.typography.bodySmall, color = onSurfaceMuted)
                        Text("High", style = MaterialTheme.typography.bodySmall, color = onSurfaceMuted)
                        Text("Maximum", style = MaterialTheme.typography.bodySmall, color = onSurfaceMuted)
                    }
                }
            }
        }

        // ─── 3. DPI / Resolution Selector ────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, borderCol)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Resolution (DPI)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurfaceMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val dpiOptions = listOf(
                        150 to "Standard",
                        300 to "Print",
                        600 to "Ultra"
                    )
                    dpiOptions.forEach { (dpi, label) ->
                        val isSelected = config.dpi == dpi
                        val optionBg = if (isSelected) selectedAccentBg else MaterialTheme.colorScheme.surface
                        val optionBorder = if (isSelected) primaryBlue else borderCol
                        val textColor = if (isSelected) primaryBlue else onSurfaceCol
                        val subColor = if (isSelected) primaryBlue else onSurfaceMuted

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(68.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.pdfToImageConfig.value = config.copy(dpi = dpi)
                                },
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, optionBorder),
                            colors = CardDefaults.cardColors(containerColor = optionBg)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = dpi.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = subColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // ─── 4. Page Selection ────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, borderCol)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MenuBook,
                        contentDescription = "Pages",
                        tint = primaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Page Selection",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurfaceMuted
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Option 1: All Pages
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.pdfToImageConfig.value = config.copy(pageSelection = "all")
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = config.pageSelection == "all",
                            onClick = { viewModel.pdfToImageConfig.value = config.copy(pageSelection = "all") },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = primaryBlue,
                                unselectedColor = onSurfaceMuted
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "All Pages (${pageCount ?: 1})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = onSurfaceCol
                        )
                    }

                    // Option 2: Custom Range
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.pdfToImageConfig.value = config.copy(pageSelection = "custom")
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = config.pageSelection == "custom",
                            onClick = { viewModel.pdfToImageConfig.value = config.copy(pageSelection = "custom") },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = primaryBlue,
                                unselectedColor = onSurfaceMuted
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Custom Range",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = onSurfaceCol
                        )
                    }

                    // Text Field underneath (visible in both, always active but auto-focus selects custom range)
                    OutlinedTextField(
                        value = config.customPageRange,
                        onValueChange = {
                            viewModel.pdfToImageConfig.value = config.copy(
                                customPageRange = it,
                                pageSelection = "custom"
                            )
                        },
                        placeholder = {
                            Text(
                                text = "e.g. 1, 5-10",
                                style = MaterialTheme.typography.bodyMedium,
                                color = onSurfaceMuted
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = onSurfaceCol,
                            unfocusedTextColor = onSurfaceCol,
                            focusedBorderColor = primaryBlue,
                            unfocusedBorderColor = borderCol,
                            focusedContainerColor = textFieldBg,
                            unfocusedContainerColor = textFieldBg
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun BadgeChip(text: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun getFileSizeFormatted(context: Context, uri: Uri): String {
    return try {
        var bytes = 0L
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (sizeIndex != -1 && cursor.moveToFirst()) {
                    bytes = cursor.getLong(sizeIndex)
                }
            }
        }
        if (bytes == 0L) {
            val path = uri.path
            if (path != null) {
                val file = java.io.File(path)
                if (file.exists()) {
                    bytes = file.length()
                }
            }
        }
        
        if (bytes < 1024) {
            "${bytes} B"
        } else if (bytes < 1024 * 1024) {
            String.format(java.util.Locale.US, "%.1f KB", bytes / 1024.0)
        } else {
            String.format(java.util.Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
        }
    } catch (e: Exception) {
        "0 B"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfToPptToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color,
    onPickFile: () -> Unit = {}
) {
    val config by viewModel.pdfToPptConfig.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val pageCount by viewModel.pageCount.collectAsState()
    val context = LocalContext.current

    val isDark = LocalDarkTheme.current
    val primaryBlue = accentColor

    // Card background & borders – use Material theme tokens for proper dark/light adaptation
    val fileCardBg = MaterialTheme.colorScheme.surfaceContainerLow
    val settingsCardBg = MaterialTheme.colorScheme.surfaceContainer
    val innerSelectorBg = MaterialTheme.colorScheme.surfaceContainerHighest
    val selectorItemActiveBg = accentColor.copy(alpha = 0.15f)

    // Text colors – driven by Material theme tokens
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val badgeBg = accentColor.copy(alpha = 0.15f)
    val badgeText = accentColor
    val dividerCol = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // File details preview card
        val selectedFile = selectedFiles.firstOrNull()
        if (selectedFile != null) {
            val fileName = getFileNameFromUri(context, selectedFile)
            val fileSizeFormatted = getFileSizeFormatted(context, selectedFile)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = fileCardBg
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // PDF Real Thumbnail Preview
                    Box(
                        modifier = Modifier
                            .size(width = 54.dp, height = 72.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        PdfPagePreview(
                            uri = selectedFile,
                            pageIndex = 0,
                            loadThumbnail = { uri, idx, width ->
                                viewModel.renderPage(context, uri, idx, width)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Metadata texts
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = fileName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = onPickFile,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit File",
                                    tint = textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // PDF Badge
                            Box(
                                modifier = Modifier
                                    .background(badgeBg, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PDF",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeText,
                                    fontSize = 9.sp
                                )
                            }
                            val pageCountText = pageCount?.let { " · $it Pages" } ?: ""
                            Text(
                                text = "$fileSizeFormatted$pageCountText",
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Conversion Settings Section Header
        Text(
            text = stringResource(R.string.tool_pdf_to_ppt_conversion_settings),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = primaryBlue,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Main Conversion settings box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = settingsCardBg),
            border = BorderStroke(1.dp, dividerCol)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Slide Layout option selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.tool_pdf_to_ppt_slide_layout),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(innerSelectorBg, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val layoutOptions = listOf(
                            1 to stringResource(R.string.tool_pdf_to_ppt_layout_1),
                            2 to stringResource(R.string.tool_pdf_to_ppt_layout_2),
                            4 to stringResource(R.string.tool_pdf_to_ppt_layout_4)
                        )
                        layoutOptions.forEach { (option, label) ->
                            val isSelected = config.slidesPerPage == option
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) selectorItemActiveBg else Color.Transparent)
                                    .clickable {
                                        viewModel.pdfToPptConfig.value = config.copy(slidesPerPage = option)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) primaryBlue else textSecondary
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = dividerCol)

                // Include Notes Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.tool_pdf_to_ppt_include_notes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = stringResource(R.string.tool_pdf_to_ppt_include_notes_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                    }
                    Switch(
                        checked = config.includeNotes,
                        onCheckedChange = { value ->
                            viewModel.pdfToPptConfig.value = config.copy(includeNotes = value)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = primaryBlue,
                            uncheckedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                HorizontalDivider(color = dividerCol)

                // OCR Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.tool_pdf_to_ppt_ocr),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = stringResource(R.string.tool_pdf_to_ppt_ocr_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                    }
                    Switch(
                        checked = config.runOcr,
                        onCheckedChange = { value ->
                            viewModel.pdfToPptConfig.value = config.copy(runOcr = value)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = primaryBlue,
                            uncheckedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }

        // Export Format Section Header
        Text(
            text = stringResource(R.string.tool_pdf_to_ppt_export_format),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = primaryBlue,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Export format card options row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // PPTX card option
            val isPptxSelected = config.exportFormat == "pptx"
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp)
                    .clickable {
                        viewModel.pdfToPptConfig.value = config.copy(exportFormat = "pptx")
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPptxSelected) accentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(
                    if (isPptxSelected) 2.dp else 1.dp,
                    if (isPptxSelected) accentColor else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = ".pptx",
                        tint = if (isPptxSelected) primaryBlue else textSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = ".pptx",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isPptxSelected) primaryBlue else textPrimary
                    )
                    Text(
                        text = stringResource(R.string.tool_pdf_to_ppt_format_pptx_desc),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isPptxSelected) primaryBlue else textSecondary,
                        fontSize = 9.sp
                    )
                }
            }

            // OTP card option
            val isOtpSelected = config.exportFormat == "otp"
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp)
                    .clickable {
                        viewModel.pdfToPptConfig.value = config.copy(exportFormat = "otp")
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOtpSelected) accentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(
                    if (isOtpSelected) 2.dp else 1.dp,
                    if (isOtpSelected) accentColor else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = ".otp",
                        tint = if (isOtpSelected) primaryBlue else textSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = ".otp",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isOtpSelected) primaryBlue else textPrimary
                    )
                    Text(
                        text = stringResource(R.string.tool_pdf_to_ppt_format_otp_desc),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isOtpSelected) primaryBlue else textSecondary,
                        fontSize = 9.sp
                    )
                }
            }
        }

        // Information Help Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = primaryBlue.copy(alpha = 0.08f)
            ),
            border = BorderStroke(1.dp, primaryBlue.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Info",
                    tint = primaryBlue,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.tool_pdf_to_ppt_help_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}


