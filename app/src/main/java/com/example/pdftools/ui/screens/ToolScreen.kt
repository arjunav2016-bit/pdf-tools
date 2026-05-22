package com.example.pdftools.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pdftools.R
import com.example.pdftools.data.PdfTool
import com.example.pdftools.ui.screens.tools.CompareResultDisplayConfig
import com.example.pdftools.ui.screens.tools.CropToolConfig
import com.example.pdftools.ui.screens.tools.EditToolConfig
import com.example.pdftools.ui.screens.tools.FormsToolConfig
import com.example.pdftools.ui.screens.tools.HtmlToolConfig
import com.example.pdftools.ui.screens.tools.OcrResultDisplayConfig
import com.example.pdftools.ui.screens.tools.OrganizeToolConfig
import com.example.pdftools.ui.screens.tools.PageNumberToolConfig
import com.example.pdftools.ui.screens.tools.PageRangeToolConfig
import com.example.pdftools.ui.screens.tools.PasswordToolConfig
import com.example.pdftools.ui.screens.tools.PdfaToolConfig
import com.example.pdftools.ui.screens.tools.RedactToolConfig
import com.example.pdftools.ui.screens.tools.RotateToolConfig
import com.example.pdftools.ui.screens.tools.ScanToolConfig
import com.example.pdftools.ui.screens.tools.SignToolConfig
import com.example.pdftools.ui.screens.tools.WatermarkToolConfig
import com.example.pdftools.ui.viewmodels.ToolUiState
import com.example.pdftools.ui.viewmodels.ToolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolScreen(
    tool: PdfTool,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val accentColor = if (isDarkTheme) tool.category.darkAccentColor else tool.category.accentColor
    val containerColor = if (isDarkTheme) tool.category.darkContainerColor else tool.category.containerColor

    val viewModel: ToolViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val outputUris by viewModel.outputUris.collectAsState()
    val htmlConfig by viewModel.htmlConfig.collectAsState()
    val progress by viewModel.progress.collectAsState()
    var showDestructiveConfirmation by rememberSaveable(tool.id) { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(uiState) {
        when (uiState) {
            is ToolUiState.Error -> {
                Toast.makeText(context, "Failed: ${(uiState as ToolUiState.Error).message}", Toast.LENGTH_LONG).show()
            }
            is ToolUiState.Success -> {
                Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addFiles(uris)
            if (tool.id == "html_to_pdf") {
                try {
                    context.contentResolver.openInputStream(uris.first())?.use { input ->
                        val content = input.bufferedReader().readText()
                        viewModel.htmlConfig.value = htmlConfig.copy(htmlContent = content)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val isComplete = uiState is ToolUiState.Success
    val isProcessing = uiState is ToolUiState.Processing
    val processingState = uiState as? ToolUiState.Processing
    val requiresConfirmation = tool.id in setOf("remove_pages", "redact_pdf", "crop_pdf")

    if (showDestructiveConfirmation) {
        AlertDialog(
            onDismissRequest = { showDestructiveConfirmation = false },
            title = { Text(stringResource(R.string.destructive_tool_confirmation_title)) },
            text = { Text(stringResource(R.string.destructive_tool_confirmation_body)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDestructiveConfirmation = false
                        viewModel.process(tool.id, context)
                    }
                ) {
                    Text(stringResource(R.string.continue_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDestructiveConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = tool.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                actions = {
                    val isFav = viewModel.isFavorite(tool.id)
                    IconButton(onClick = { viewModel.toggleFavorite(tool.id) }) {
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Toggle favorite",
                            tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HeroSection(tool = tool, accentColor = accentColor, containerColor = containerColor)
            }

            // Input File Picker / Success Area
            item {
                if (isComplete) {
                    SuccessCard(
                        tool = tool,
                        outputUris = outputUris,
                        onClear = { viewModel.reset() },
                        accentColor = accentColor,
                        containerColor = containerColor
                    )
                } else {
                    FilePickerZone(
                        accentColor = accentColor,
                        onPickFiles = {
                            val mimeTypes = when (tool.id) {
                                "jpg_to_pdf", "scan_to_pdf" -> arrayOf("image/jpeg", "image/png", "image/webp")
                                "html_to_pdf" -> arrayOf("text/html", "text/plain")
                                "word_to_pdf" -> arrayOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword")
                                "ppt_to_pdf" -> arrayOf("application/vnd.openxmlformats-officedocument.presentationml.presentation", "application/vnd.ms-powerpoint")
                                "excel_to_pdf" -> arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel")
                                else -> arrayOf("application/pdf")
                            }
                            filePickerLauncher.launch(mimeTypes)
                        }
                    )
                }
            }

            // Selected files (only show when not complete)
            if (selectedFiles.isNotEmpty() && !isComplete) {
                item {
                    Text(
                        text = "Selected Files (${selectedFiles.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                itemsIndexed(selectedFiles) { index, uri ->
                    FileItem(
                        fileName = uri.lastPathSegment ?: "File ${index + 1}",
                        onRemove = { viewModel.removeFile(index) }
                    )
                }
            }

            // Tool-specific configurations
            if (selectedFiles.isNotEmpty() || (tool.id == "html_to_pdf" && htmlConfig.htmlContent.isNotEmpty())) {
                if (!isComplete) {
                    item {
                        ToolConfigSection(tool = tool, viewModel = viewModel, accentColor = accentColor)
                    }
                } else {
                    // Result display for tool outputs (like OCR or PDF Compare)
                    if (tool.id == "ocr_pdf" || tool.id == "compare_pdf") {
                        item {
                            ToolConfigSection(tool = tool, viewModel = viewModel, accentColor = accentColor)
                        }
                    }
                }
            }

            // Action button / Processing bar
            val showActionButton = (selectedFiles.isNotEmpty() || (tool.id == "html_to_pdf" && htmlConfig.htmlContent.isNotEmpty())) && !isComplete
            if (showActionButton) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))

                    if (isProcessing) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (progress != null) {
                                LinearProgressIndicator(
                                    progress = { progress ?: 0f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = accentColor,
                                    trackColor = containerColor
                                )
                            } else {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = accentColor,
                                    trackColor = containerColor
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = processingState?.statusMessage
                                        ?: stringResource(R.string.processing_locally),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(onClick = viewModel::cancelProcessing) {
                                    Text(stringResource(R.string.cancel))
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                if (requiresConfirmation) {
                                    showDestructiveConfirmation = true
                                } else {
                                    viewModel.process(tool.id, context)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor
                            )
                        ) {
                            Text(
                                text = getActionButtonText(tool.id),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ToolConfigSection(tool: PdfTool, viewModel: ToolViewModel, accentColor: Color) {
    when (tool.id) {
        "split_pdf", "remove_pages", "extract_pages" -> PageRangeToolConfig(viewModel, tool, accentColor)
        "rotate_pdf" -> RotateToolConfig(viewModel, accentColor)
        "protect_pdf", "unlock_pdf" -> PasswordToolConfig(viewModel, tool, accentColor)
        "add_watermark" -> WatermarkToolConfig(viewModel, accentColor)
        "add_page_numbers" -> PageNumberToolConfig(viewModel, accentColor)
        "crop_pdf" -> CropToolConfig(viewModel, accentColor)
        "organize_pdf" -> OrganizeToolConfig(viewModel, accentColor)
        "pdf_to_pdfa" -> PdfaToolConfig(viewModel, accentColor)
        "sign_pdf" -> SignToolConfig(viewModel, accentColor)
        "redact_pdf" -> RedactToolConfig(viewModel, accentColor)
        "pdf_forms" -> FormsToolConfig(viewModel, accentColor)
        "scan_to_pdf" -> ScanToolConfig(viewModel, accentColor)
        "edit_pdf" -> EditToolConfig(viewModel, accentColor)
        "html_to_pdf" -> HtmlToolConfig(viewModel, accentColor)
        "ocr_pdf" -> OcrResultDisplayConfig(viewModel, accentColor)
        "compare_pdf" -> CompareResultDisplayConfig(viewModel, accentColor)
    }
}
