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
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import com.example.pdftools.theme.LocalDarkTheme
import com.example.pdftools.ui.components.PdfPagePreview
import com.example.pdftools.ui.screens.getFileNameFromUri
import com.example.pdftools.ui.viewmodels.CompressTier
import com.example.pdftools.ui.viewmodels.CompressConfig

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
    accentColor: Color,
    onPickFile: () -> Unit = {}
) {
    val config by viewModel.pdfaConfig.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val pageCount by viewModel.pageCount.collectAsState()
    val selectedFile = selectedFiles.firstOrNull()
    val context = LocalContext.current

    val isDark = LocalDarkTheme.current
    val primaryBlue = accentColor

    // Card background & borders – use Material theme tokens for proper dark/light adaptation
    val fileCardBg = MaterialTheme.colorScheme.surfaceContainerLow
    val settingsCardBg = MaterialTheme.colorScheme.surfaceContainer

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
        // --- 1. SELECTED FILE CARD ---
        if (selectedFile != null) {
            val fileName = getFileNameFromUri(context, selectedFile)
            val fileSizeFormatted = getFileSizeFormatted(context, selectedFile)
            val standardBadgeText = when (config.conformanceLevel) {
                "pdfa_1b" -> "PDF/A-1b"
                "pdfa_2b" -> "PDF/A-2b"
                "pdfa_3b" -> "PDF/A-3b"
                else -> "PDF/A-2b"
            }

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
                            // PDF/A Standard Badge
                            Box(
                                modifier = Modifier
                                    .background(badgeBg, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = standardBadgeText,
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

        // --- 2. CONFORMANCE LEVEL SELECTOR ---
        Text(
            text = stringResource(R.string.tool_pdfa_standards),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = primaryBlue,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        val conformanceOptions = listOf(
            Triple("pdfa_1b", stringResource(R.string.tool_pdfa_1b_label), stringResource(R.string.tool_pdfa_1b_desc)),
            Triple("pdfa_2b", stringResource(R.string.tool_pdfa_2b_label), stringResource(R.string.tool_pdfa_2b_desc)),
            Triple("pdfa_3b", stringResource(R.string.tool_pdfa_3b_label), stringResource(R.string.tool_pdfa_3b_desc))
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            conformanceOptions.forEach { (id, label, desc) ->
                val isSelected = config.conformanceLevel == id
                Card(
                    onClick = { viewModel.pdfaConfig.value = config.copy(conformanceLevel = id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) accentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) primaryBlue else textPrimary
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                    }
                }
            }
        }

        // --- 3. COMPLIANCE CONTROLS CARD ---
        Text(
            text = stringResource(R.string.tool_pdfa_compliance_controls),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = primaryBlue,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

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
                // Embed fonts switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.tool_pdfa_embed_fonts),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = stringResource(R.string.tool_pdfa_embed_fonts_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                    }
                    Switch(
                        checked = config.embedFonts,
                        onCheckedChange = { value ->
                            viewModel.pdfaConfig.value = config.copy(embedFonts = value)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = primaryBlue,
                            uncheckedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                HorizontalDivider(color = dividerCol)

                // Remove transparent objects switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.tool_pdfa_remove_transparencies),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = stringResource(R.string.tool_pdfa_remove_transparencies_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                    }
                    Switch(
                        checked = config.removeTransparencies,
                        onCheckedChange = { value ->
                            viewModel.pdfaConfig.value = config.copy(removeTransparencies = value)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = primaryBlue,
                            uncheckedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                HorizontalDivider(color = dividerCol)

                // Standardize color profile (sRGB) switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.tool_pdfa_convert_srgb),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = stringResource(R.string.tool_pdfa_convert_srgb_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                    }
                    Switch(
                        checked = config.convertSrgb,
                        onCheckedChange = { value ->
                            viewModel.pdfaConfig.value = config.copy(convertSrgb = value)
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

        // --- 4. ARCHIVAL METADATA CARD ---
        Text(
            text = stringResource(R.string.tool_pdfa_metadata),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = primaryBlue,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = settingsCardBg),
            border = BorderStroke(1.dp, dividerCol)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title Field
                OutlinedTextField(
                    value = config.title,
                    onValueChange = { viewModel.pdfaConfig.value = config.copy(title = it) },
                    label = { Text(stringResource(R.string.tool_pdfa_title_label)) },
                    placeholder = { Text(stringResource(R.string.tool_pdfa_title_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Author Field
                OutlinedTextField(
                    value = config.author,
                    onValueChange = { viewModel.pdfaConfig.value = config.copy(author = it) },
                    label = { Text(stringResource(R.string.tool_pdfa_author_label)) },
                    placeholder = { Text(stringResource(R.string.tool_pdfa_author_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Subject Field
                OutlinedTextField(
                    value = config.subject,
                    onValueChange = { viewModel.pdfaConfig.value = config.copy(subject = it) },
                    label = { Text(stringResource(R.string.tool_pdfa_subject_label)) },
                    placeholder = { Text(stringResource(R.string.tool_pdfa_subject_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // --- 5. CONTEXTUAL GUIDANCE BOX ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            border = BorderStroke(1.dp, dividerCol)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = primaryBlue,
                    modifier = Modifier.size(24.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.tool_pdfa_guidance_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = stringResource(R.string.tool_pdfa_guidance_text),
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun CompressToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color
) {
    val config by viewModel.compressConfig.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "COMPRESSION LEVEL",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CompressTier.values().forEach { tier ->
            val isSelected = config.tier == tier
            val title = when (tier) {
                CompressTier.BASIC -> "Basic"
                CompressTier.RECOMMENDED -> "Recommended"
                CompressTier.EXTREME -> "Extreme"
            }
            val desc = when (tier) {
                CompressTier.BASIC -> "High quality, minimal size reduction (~10%)"
                CompressTier.RECOMMENDED -> "Good balance of quality and size (~40%)"
                CompressTier.EXTREME -> "Maximum compression, lower quality (~70%)"
            }

            Card(
                onClick = { viewModel.compressConfig.value = config.copy(tier = tier) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        accentColor.copy(alpha = 0.08f)
                    else
                        MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = if (isSelected) BorderStroke(2.dp, accentColor)
                else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { viewModel.compressConfig.value = config.copy(tier = tier) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = accentColor,
                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    }
}
