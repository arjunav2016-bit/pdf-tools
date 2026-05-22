package com.example.pdftools.ui.screens.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pdftools.R
import com.example.pdftools.data.PdfTool
import com.example.pdftools.ui.components.PageThumbnailGrid
import com.example.pdftools.ui.viewmodels.ToolViewModel
import com.example.pdftools.utils.PageRangeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color
) {
    val config by viewModel.cropConfig.collectAsState()
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
            text = stringResource(R.string.tool_crop_margins),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val options = listOf(
                0.05f to stringResource(R.string.tool_crop_slim),
                0.10f to stringResource(R.string.tool_crop_normal),
                0.20f to stringResource(R.string.tool_crop_wide)
            )
            options.forEach { (pct, label) ->
                val isSelected = config.marginPercentage == pct
                val cardBg = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceContainerLow
                val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                
                Card(
                    onClick = { viewModel.cropConfig.value = config.copy(marginPercentage = pct) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
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
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = stringResource(R.string.tool_pages_crop_optional),
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
                    viewModel.cropConfig.value = config.copy(
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
                viewModel.cropConfig.value = config.copy(
                    pageRange = pageRange,
                    selectedPages = pageCount?.let {
                        PageRangeUtils.parsePageRanges(pageRange, it).toSet()
                    } ?: emptySet()
                )
            },
            placeholder = { Text(stringResource(R.string.tool_page_range_all_placeholder)) },
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
            text = stringResource(R.string.tool_crop_help),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun RepairToolConfig(
    tool: PdfTool,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.name,
                    tint = accentColor,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Text(
                text = stringResource(R.string.tool_repair_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = stringResource(R.string.tool_repair_help),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceContainer,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val diagnosticSteps = listOf(
                    stringResource(R.string.tool_repair_step_parser),
                    stringResource(R.string.tool_repair_step_xref),
                    stringResource(R.string.tool_repair_step_trailer),
                    stringResource(R.string.tool_repair_step_integrity)
                )
                diagnosticSteps.forEach { step ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfaToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color
) {
    val config by viewModel.pdfaConfig.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.tool_pdfa_standards),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val formats = listOf(
                "pdfa_1b" to stringResource(R.string.tool_pdfa_1b_label),
                "pdfa_2b" to stringResource(R.string.tool_pdfa_2b_label)
            )
            formats.forEach { (id, label) ->
                val isSelected = config.conformanceLevel == id
                val cardBg = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceContainerLow
                val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                
                Card(
                    onClick = { viewModel.pdfaConfig.value = config.copy(conformanceLevel = id) },
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
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.tool_pdfa_details),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                val injectionDetails = listOf(
                    stringResource(R.string.tool_pdfa_color_title) to
                        stringResource(R.string.tool_pdfa_color_help),
                    stringResource(R.string.tool_pdfa_metadata_title) to
                        stringResource(R.string.tool_pdfa_metadata_help),
                    stringResource(R.string.tool_pdfa_font_title) to
                        stringResource(R.string.tool_pdfa_font_help)
                )
                injectionDetails.forEach { (title, desc) ->
                    Column {
                        Text(
                            text = "• $title",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                }
            }
        }
    }
}
