package com.example.pdftools.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pdftools.R
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import com.example.pdftools.data.PdfTool
import com.example.pdftools.data.OcrModuleStatus
import com.example.pdftools.theme.LocalDarkTheme
import com.example.pdftools.ui.screens.tools.CompareResultDisplayConfig
import com.example.pdftools.ui.screens.tools.CompressToolConfig
import com.example.pdftools.ui.screens.tools.MergeToolConfig
import com.example.pdftools.ui.screens.tools.CropToolConfig
import com.example.pdftools.ui.screens.tools.EditToolConfig
import com.example.pdftools.ui.screens.tools.FormsToolConfig
import com.example.pdftools.ui.screens.tools.HtmlToolConfig
import com.example.pdftools.ui.screens.tools.OcrResultDisplayConfig
import com.example.pdftools.ui.screens.tools.OrganizeToolConfig
import com.example.pdftools.ui.screens.tools.PageNumberToolConfig
import com.example.pdftools.ui.screens.tools.PageRangeToolConfig
import com.example.pdftools.ui.screens.tools.SplitToolConfig
import com.example.pdftools.ui.screens.tools.RemovePagesToolConfig
import com.example.pdftools.ui.screens.tools.PasswordToolConfig
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import com.example.pdftools.ui.screens.tools.PdfaToolConfig
import com.example.pdftools.ui.screens.tools.RedactToolConfig
import com.example.pdftools.ui.screens.tools.RotateToolConfig
import com.example.pdftools.ui.screens.tools.ScanToolConfig
import com.example.pdftools.ui.screens.tools.SignToolConfig
import com.example.pdftools.ui.screens.tools.SignPdfSurgicalScreen
import com.example.pdftools.ui.screens.tools.ProtectPdfSurgicalScreen
import com.example.pdftools.ui.screens.tools.UnlockPdfSurgicalScreen
import com.example.pdftools.ui.screens.tools.PdfFormsSurgicalScreen
import com.example.pdftools.ui.screens.tools.EditPdfSurgicalScreen
import com.example.pdftools.ui.screens.tools.CropPdfSurgicalScreen
import com.example.pdftools.ui.screens.tools.RotatePdfSurgicalScreen
import com.example.pdftools.ui.screens.tools.WatermarkToolConfig
import com.example.pdftools.ui.screens.tools.WatermarkSurgicalScreen
import com.example.pdftools.ui.screens.tools.PageNumbersSurgicalScreen
import com.example.pdftools.ui.screens.tools.PdfToImageToolConfig
import com.example.pdftools.ui.screens.tools.PdfToPptToolConfig

import com.example.pdftools.ui.viewmodels.ToolUiState
import com.example.pdftools.ui.viewmodels.ToolViewModel
import com.example.pdftools.ui.viewmodels.ExcelToPdfConfig
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.AspectRatio

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.runtime.remember
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.pdftools.ui.screens.getFileSizeFromUri
import com.example.pdftools.utils.PageRangeUtils
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallSplit
import com.example.pdftools.ui.components.DragHandleIndicator
import com.example.pdftools.ui.components.PdfPagePreview
import com.example.pdftools.ui.viewmodels.OrganizeConfig
import com.example.pdftools.data.PdfProcessor
import com.example.pdftools.ui.viewmodels.CompressConfig
import com.example.pdftools.ui.viewmodels.CompressTier
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.GridView
import kotlin.math.roundToInt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.filled.Slideshow
import com.example.pdftools.ui.viewmodels.PptToPdfConfig
import com.example.pdftools.ui.viewmodels.PdfToPptConfig
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Verified


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolScreen(
    tool: PdfTool,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = LocalDarkTheme.current
    val accentColor = if (isDarkTheme) tool.category.darkAccentColor else tool.category.accentColor
    val containerColor = if (isDarkTheme) tool.category.darkContainerColor else tool.category.containerColor

    val viewModel: ToolViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val outputUris by viewModel.outputUris.collectAsState()
    val htmlConfig by viewModel.htmlConfig.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val pageCount by viewModel.pageCount.collectAsState()
    val pageRangeConfig by viewModel.pageRangeConfig.collectAsState()
    val pdfToImageConfig by viewModel.pdfToImageConfig.collectAsState()
    val pdfToPptConfig by viewModel.pdfToPptConfig.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    var showDestructiveConfirmation by rememberSaveable(tool.id) { mutableStateOf(false) }
    var showCancelConfirmation by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Reset ViewModel state every time this screen enters composition
    // so re-entering from main menu doesn't show stale results
    androidx.compose.runtime.DisposableEffect(tool.id) {
        viewModel.setActiveTool(tool.id)
        viewModel.reset()
        onDispose {
            viewModel.cancelProcessing()
        }
    }

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
        if (uris.isNotEmpty() && viewModel.currentToolId == tool.id) {
            if (tool.id != "html_to_pdf") {
                viewModel.addFiles(uris)
            } else {
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

    androidx.activity.compose.BackHandler(enabled = isProcessing) {
        showCancelConfirmation = true
    }

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

    if (showCancelConfirmation) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmation = false },
            title = { Text("Cancel Processing?") },
            text = { Text("Are you sure you want to stop the current PDF task? Any progress will be lost.") },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelConfirmation = false
                        viewModel.cancelProcessing()
                        onBack()
                    }
                ) {
                    Text("Yes, Stop")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmation = false }) {
                    Text("Cancel")
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
                    IconButton(
                        onClick = {
                            if (isProcessing) {
                                showCancelConfirmation = true
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                actions = {
                    if (tool.id == "pdf_to_ppt") {
                        IconButton(onClick = { /* Search action */ }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (tool.id == "pdf_to_jpg") {
                        IconButton(onClick = { /* Settings action */ }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        val isFav = favorites.contains(tool.id)
                        IconButton(onClick = { viewModel.toggleFavorite(tool.id) }) {
                            Icon(
                                imageVector = if (isFav) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Toggle favorite",
                                tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val isWideScreen = configuration.screenWidthDp >= 600

        if (tool.id == "extract_pages" && selectedFiles.isNotEmpty()) {
            ExtractPagesSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                pageCount = pageCount,
                config = pageRangeConfig,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "split_pdf" && selectedFiles.isNotEmpty()) {
            SplitPdfSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                pageCount = pageCount,
                config = pageRangeConfig,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "remove_pages" && selectedFiles.isNotEmpty()) {
            RemovePagesSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                pageCount = pageCount,
                config = pageRangeConfig,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "organize_pdf" && selectedFiles.isNotEmpty()) {
            OrganizePdfSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                pageCount = pageCount,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "compress_pdf" && selectedFiles.isNotEmpty()) {
            CompressPdfSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "repair_pdf" && selectedFiles.isNotEmpty()) {
            RepairPdfSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "ocr_pdf" && selectedFiles.isNotEmpty()) {
            OcrPdfSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "jpg_to_pdf" && selectedFiles.isNotEmpty()) {
            JpgToPdfSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "word_to_pdf" && selectedFiles.isNotEmpty()) {
            WordToPdfSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "pdf_to_word" && selectedFiles.isNotEmpty()) {
            PdfToWordSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "pdf_to_excel" && selectedFiles.isNotEmpty()) {
            PdfToExcelSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                pageCount = pageCount,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "ppt_to_pdf" && selectedFiles.isNotEmpty()) {
            PptToPdfSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "excel_to_pdf" && selectedFiles.isNotEmpty()) {
            ExcelToPdfSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "pdf_to_jpg" && selectedFiles.isNotEmpty()) {
            PdfToImageSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "pdf_to_ppt" && selectedFiles.isNotEmpty()) {
            PdfToPptSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "pdf_to_pdfa" && selectedFiles.isNotEmpty()) {
            PdfToPdfaSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "merge_pdf") {
            MergePdfSurgicalScreen(

                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "protect_pdf" && selectedFiles.isNotEmpty()) {
            ProtectPdfSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                pageCount = pageCount,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "unlock_pdf" && selectedFiles.isNotEmpty()) {
            UnlockPdfSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "crop_pdf" && selectedFiles.isNotEmpty()) {
            CropPdfSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                pageCount = pageCount,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "rotate_pdf" && selectedFiles.isNotEmpty()) {
            RotatePdfSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                pageCount = pageCount,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "add_watermark" && selectedFiles.isNotEmpty()) {
            WatermarkSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                pageCount = pageCount,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "add_page_numbers" && selectedFiles.isNotEmpty()) {
            PageNumbersSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                pageCount = pageCount,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "sign_pdf" && selectedFiles.isNotEmpty()) {
            SignPdfSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                pageCount = pageCount,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor
            )
        } else if (tool.id == "pdf_forms") {
            PdfFormsSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor,
                onPickFiles = {
                    filePickerLauncher.launch(arrayOf("application/pdf"))
                }
            )
        } else if (tool.id == "edit_pdf") {
            EditPdfSurgicalScreen(
                tool = tool,
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                isProcessing = isProcessing,
                isComplete = isComplete,
                outputUris = outputUris,
                progress = progress,
                innerPadding = innerPadding,
                accentColor = accentColor,
                containerColor = containerColor,
                onPickFiles = {
                    filePickerLauncher.launch(arrayOf("application/pdf"))
                }
            )
        } else if (isWideScreen) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left pane: Controls & configurations
                LazyColumn(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Picker zone (when not complete)
                    if (!isComplete) {
                        item {
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

                    // Selected files
                    if (selectedFiles.isNotEmpty() && !isComplete && tool.id != "merge_pdf" && tool.id != "pdf_to_jpg" && tool.id != "pdf_to_ppt" && tool.id != "pdf_to_pdfa") {
                        item {
                            Text(
                                text = "Selected Files (${selectedFiles.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        itemsIndexed(selectedFiles) { index, uri ->
                            FileItem(
                                fileName = getFileNameFromUri(context, uri),
                                onRemove = { viewModel.removeFile(index) }
                            )
                        }
                    }

                    // Config
                    if (selectedFiles.isNotEmpty() || (tool.id == "html_to_pdf" && htmlConfig.htmlContent.isNotEmpty())) {
                        if (!isComplete) {
                            item {
                                ToolConfigSection(tool = tool, viewModel = viewModel, accentColor = accentColor)
                            }
                        }
                    }

                    // Action button
                    val showActionButton = (selectedFiles.isNotEmpty() || (tool.id == "html_to_pdf" && htmlConfig.htmlContent.isNotEmpty())) && !isComplete
                    if (showActionButton) {
                        item {
                            if (isProcessing) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    ShimmerProgressIndicator(
                                        progress = progress,
                                        accentColor = accentColor,
                                        containerColor = containerColor,
                                        modifier = Modifier.fillMaxWidth()
                                    )
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
                                    if (tool.id == "split_pdf") {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.CallSplit,
                                            contentDescription = null,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    } else if (tool.id == "remove_pages") {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    } else if (tool.id == "pdf_to_ppt") {
                                        Icon(
                                            imageVector = Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    } else if (tool.id == "pdf_to_jpg") {
                                        Icon(
                                            imageVector = Icons.Filled.Autorenew,
                                            contentDescription = null,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    } else if (tool.id == "redact_pdf") {
                                        Icon(
                                            imageVector = Icons.Filled.Lock,
                                            contentDescription = null,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    }
                                    val selectedPagesCount = pageRangeConfig.selectedPages.size
                                    Text(
                                        text = if (tool.id == "pdf_to_jpg") {
                                            "Convert to ${pdfToImageConfig.format.uppercase()}"
                                        } else if (tool.id == "pdf_to_ppt") {
                                            "Convert to ${pdfToPptConfig.exportFormat.uppercase()}"
                                        } else if (tool.id == "remove_pages") {
                                            if (selectedPagesCount == 1) {
                                                stringResource(R.string.tool_remove_action_button_single)
                                            } else if (selectedPagesCount > 0) {
                                                stringResource(R.string.tool_remove_action_button_count, selectedPagesCount)
                                            } else {
                                                stringResource(R.string.tool_remove_action_button)
                                            }
                                        } else {
                                            getActionButtonText(tool.id)
                                        },
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

                // Right pane: Hero, results & success card
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        HeroSection(tool = tool, accentColor = accentColor, containerColor = containerColor)
                    }

                    if (isComplete) {
                        item {
                            SuccessCard(
                                tool = tool,
                                outputUris = outputUris,
                                onClear = { viewModel.resetCurrentRun() },
                                accentColor = accentColor,
                                containerColor = containerColor
                            )
                        }
                    }

                    // Result display configuration for OCR/Compare when complete
                    if (isComplete && (tool.id == "ocr_pdf" || tool.id == "compare_pdf")) {
                        item {
                            ToolConfigSection(tool = tool, viewModel = viewModel, accentColor = accentColor)
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        } else {
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
                    AnimatedContent(
                        targetState = isComplete,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        },
                        label = "picker_success_transition"
                    ) { complete ->
                        if (complete) {
                            SuccessCard(
                                tool = tool,
                                outputUris = outputUris,
                                onClear = { viewModel.resetCurrentRun() },
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
                }

                // Selected files (only show when not complete)
                if (selectedFiles.isNotEmpty() && !isComplete && tool.id != "merge_pdf" && tool.id != "pdf_to_jpg" && tool.id != "pdf_to_ppt" && tool.id != "pdf_to_pdfa") {
                    item {
                        Text(
                            text = "Selected Files (${selectedFiles.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    itemsIndexed(selectedFiles) { index, uri ->
                        FileItem(
                            fileName = getFileNameFromUri(context, uri),
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
                                ShimmerProgressIndicator(
                                    progress = progress,
                                    accentColor = accentColor,
                                    containerColor = containerColor,
                                    modifier = Modifier.fillMaxWidth()
                                )
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
                                if (tool.id == "split_pdf") {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.CallSplit,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                } else if (tool.id == "remove_pages") {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                } else if (tool.id == "pdf_to_ppt") {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                } else if (tool.id == "pdf_to_jpg") {
                                    Icon(
                                        imageVector = Icons.Filled.Autorenew,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                                val selectedPagesCount = pageRangeConfig.selectedPages.size
                                Text(
                                    text = if (tool.id == "pdf_to_jpg") {
                                        "Convert to ${pdfToImageConfig.format.uppercase()}"
                                    } else if (tool.id == "pdf_to_ppt") {
                                        "Convert to ${pdfToPptConfig.exportFormat.uppercase()}"
                                    } else if (tool.id == "remove_pages") {
                                        if (selectedPagesCount == 1) {
                                            stringResource(R.string.tool_remove_action_button_single)
                                        } else if (selectedPagesCount > 0) {
                                            stringResource(R.string.tool_remove_action_button_count, selectedPagesCount)
                                        } else {
                                            stringResource(R.string.tool_remove_action_button)
                                        }
                                    } else {
                                        getActionButtonText(tool.id)
                                    },
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
}

@Composable
private fun ShimmerProgressIndicator(
    progress: Float?,
    accentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            accentColor,
            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
            accentColor
        ),
        start = Offset(translateAnim, 0f),
        end = Offset(translateAnim + 150f, 150f)
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
    ) {
        // Track
        drawRoundRect(
            color = containerColor,
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
        // Progress / Shimmer
        if (progress != null) {
            val progressWidth = size.width * progress
            drawRoundRect(
                brush = shimmerBrush,
                size = Size(progressWidth, size.height),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        } else {
            // Indeterminate
            drawRoundRect(
                brush = shimmerBrush,
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}

@Composable
fun ToolConfigSection(tool: PdfTool, viewModel: ToolViewModel, accentColor: Color) {
    when (tool.id) {
        "merge_pdf" -> MergeToolConfig(viewModel, accentColor)
        "split_pdf" -> SplitToolConfig(viewModel, accentColor)
        "remove_pages" -> RemovePagesToolConfig(viewModel, accentColor)
        "extract_pages" -> PageRangeToolConfig(viewModel, tool, accentColor)
        "rotate_pdf" -> RotateToolConfig(viewModel, accentColor)
        "protect_pdf", "unlock_pdf" -> PasswordToolConfig(viewModel, tool, accentColor)
        "add_watermark" -> WatermarkToolConfig(viewModel, accentColor)
        "add_page_numbers" -> PageNumberToolConfig(viewModel, accentColor)
        "crop_pdf" -> CropToolConfig(viewModel, accentColor)
        "compress_pdf" -> CompressToolConfig(viewModel, accentColor)
        "organize_pdf" -> OrganizeToolConfig(viewModel, accentColor)
        "pdf_to_pdfa" -> PdfaToolConfig(viewModel, accentColor)
        "sign_pdf" -> SignToolConfig(viewModel, accentColor)
        "redact_pdf" -> RedactToolConfig(viewModel, accentColor)
        "pdf_forms" -> FormsToolConfig(viewModel, accentColor)
        "scan_to_pdf" -> ScanToolConfig(viewModel, accentColor)
        "edit_pdf" -> EditToolConfig(viewModel, accentColor)
        "html_to_pdf" -> HtmlToolConfig(viewModel, accentColor)
        "pdf_to_jpg" -> PdfToImageToolConfig(viewModel, accentColor)
        "pdf_to_ppt" -> PdfToPptToolConfig(viewModel, accentColor)

        "ocr_pdf" -> OcrResultDisplayConfig(viewModel, accentColor)
        "compare_pdf" -> CompareResultDisplayConfig(viewModel, accentColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtractPagesSurgicalScreen(
    tool: PdfTool,
    viewModel: ToolViewModel,
    selectedFiles: List<Uri>,
    pageCount: Int?,
    config: com.example.pdftools.ui.viewmodels.PageRangeConfig,
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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
            Column(modifier = Modifier.fillMaxSize()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    // 1. Selection Summary display card
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        selectedFile?.let { uri ->
                            val fileName = getFileNameFromUri(context, uri)
                            val fileSize = getFileSizeFromUri(context, uri)
                            val totalPages = pageCount ?: 0
                            val selectedCount = config.selectedPages.size

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = containerColor.copy(alpha = 0.4f)
                                ),
                                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(accentColor, shape = RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.InsertDriveFile,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = fileName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$fileSize · $totalPages Pages total",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "$selectedCount",
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = accentColor
                                        )
                                        Text(
                                            text = "PAGES\nSELECTED",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Page Range Input section
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Page Range",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

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
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = stringResource(R.string.tool_page_range_extract_help),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }

                    // 3. Visual Selection Pill Buttons row
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Visual Selection",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        val totalCount = pageCount ?: 0
                                        val updatedPages = (0 until totalCount).toSet()
                                        viewModel.pageRangeConfig.value = config.copy(
                                            selectedPages = updatedPages,
                                            pageRange = PageRangeUtils.formatPageRanges(updatedPages)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        text = "Select All",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Button(
                                    onClick = {
                                        val updatedPages = emptySet<Int>()
                                        viewModel.pageRangeConfig.value = config.copy(
                                            selectedPages = updatedPages,
                                            pageRange = ""
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        text = "Clear",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // 4. Page Grid Items
                    val totalPages = pageCount ?: 0
                    if (selectedFile != null && totalPages > 0) {
                        items(
                            items = (0 until totalPages).toList(),
                            key = { pageIndex -> pageIndex }
                        ) { pageIndex ->
                            com.example.pdftools.ui.components.ExtractPdfPageThumbnail(
                                uri = selectedFile,
                                pageIndex = pageIndex,
                                selected = pageIndex in config.selectedPages,
                                accentColor = accentColor,
                                loadThumbnail = { uri, idx, width ->
                                    viewModel.renderPage(context, uri, idx, width)
                                },
                                onClick = {
                                    val updatedPages = config.selectedPages.toMutableSet().apply {
                                        if (!add(pageIndex)) {
                                            remove(pageIndex)
                                        }
                                    }
                                    viewModel.pageRangeConfig.value = config.copy(
                                        selectedPages = updatedPages,
                                        pageRange = PageRangeUtils.formatPageRanges(updatedPages)
                                    )
                                }
                            )
                        }
                    }
                }

                // Sticky Footer Action / Progress bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        if (isProcessing) {
                            val processingState = viewModel.uiState.collectAsState().value as? ToolUiState.Processing
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (progress != null) {
                                    androidx.compose.material3.LinearProgressIndicator(
                                        progress = { progress },
                                        color = accentColor,
                                        trackColor = containerColor,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    androidx.compose.material3.LinearProgressIndicator(
                                        color = accentColor,
                                        trackColor = containerColor,
                                        modifier = Modifier.fillMaxWidth()
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
                                onClick = { viewModel.process(tool.id, context) },
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
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Extract to New PDF",
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MergePdfSurgicalScreen(
    tool: PdfTool,
    viewModel: ToolViewModel,
    selectedFiles: List<Uri>,
    isProcessing: Boolean,
    isComplete: Boolean,
    outputUris: List<Uri>,
    progress: Float?,
    innerPadding: PaddingValues,
    accentColor: Color,
    containerColor: Color
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val fileMetadata = remember { mutableStateMapOf<Uri, Pair<String, Int>>() }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty() && viewModel.currentToolId == tool.id) {
            viewModel.addFiles(uris)
        }
    }

    LaunchedEffect(selectedFiles) {
        val currentUris = selectedFiles.toSet()
        val keysToRemove = fileMetadata.keys.filter { it !in currentUris }
        keysToRemove.forEach { fileMetadata.remove(it) }

        selectedFiles.forEach { uri ->
            if (!fileMetadata.containsKey(uri)) {
                val sizeStr = getFileSizeFromUri(context, uri)
                val pageCount = viewModel.getPageCountSuspend(context, uri)
                fileMetadata[uri] = Pair(sizeStr, pageCount)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    item {
                        val dashedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .drawBehind {
                                    drawRoundRect(
                                        color = dashedBorderColor,
                                        style = Stroke(
                                            width = 1.5.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(
                                                floatArrayOf(10.dp.toPx(), 6.dp.toPx()),
                                                0f
                                            )
                                        ),
                                        cornerRadius = CornerRadius(16.dp.toPx())
                                    )
                                }
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { filePickerLauncher.launch(arrayOf("application/pdf")) }
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(accentColor.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Files",
                                        tint = accentColor,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.tool_merge_add_files),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.tool_merge_add_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    if (selectedFiles.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(R.string.tool_merge_instruction),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    itemsIndexed(
                        items = selectedFiles,
                        key = { _, uri -> uri.toString() }
                    ) { index, uri ->
                        var dragOffsetY by remember(uri) { mutableFloatStateOf(0f) }
                        val (formattedSize, pageCount) = fileMetadata[uri] ?: Pair("...", 0)
                        val pageString = if (pageCount == 1) {
                            stringResource(R.string.tool_merge_metadata_pages_single)
                        } else {
                            stringResource(R.string.tool_merge_metadata_pages, pageCount)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    translationY = dragOffsetY
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DragHandleIndicator(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .pointerInput(uri) {
                                            detectDragGestures(
                                                onDragCancel = { dragOffsetY = 0f },
                                                onDragEnd = {
                                                    val currentIdx = selectedFiles.indexOf(uri)
                                                    val heightPx = with(density) { 76.dp.toPx() }
                                                    val shift = (dragOffsetY / heightPx).roundToInt()
                                                    val targetIdx = (currentIdx + shift).coerceIn(0, selectedFiles.lastIndex)
                                                    if (currentIdx >= 0 && targetIdx != currentIdx) {
                                                        val list = selectedFiles.toMutableList()
                                                        val item = list.removeAt(currentIdx)
                                                        list.add(targetIdx, item)
                                                        viewModel.updateSelectedFiles(list)
                                                    }
                                                    dragOffsetY = 0f
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragOffsetY += dragAmount.y
                                                }
                                            )
                                        }
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(accentColor.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.InsertDriveFile,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = getFileNameFromUri(context, uri),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$formattedSize  •  $pageString",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.removeFile(index) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.remove_file),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (selectedFiles.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Transparent,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            if (isProcessing) {
                                Button(
                                    onClick = { },
                                    enabled = false,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(28.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentColor.copy(alpha = 0.4f),
                                        contentColor = Color.White.copy(alpha = 0.6f),
                                        disabledContainerColor = accentColor.copy(alpha = 0.4f),
                                        disabledContentColor = Color.White.copy(alpha = 0.6f)
                                    )
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CallSplit,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = Color.White.copy(alpha = 0.6f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Merge Files",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.process(tool.id, context) },
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
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CallSplit,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Merge Files",
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

        // --- CENTRAL MERGING DIALOG OVERLAY ---
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .width(280.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp, horizontal = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Circular spinner
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(52.dp),
                            color = accentColor,
                            strokeWidth = 4.dp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Title
                        Text(
                            text = "Merging...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Subtitle
                        Text(
                            text = "Processing your document securely",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Indeterminate linear progress bar with rounded ends
                        androidx.compose.material3.LinearProgressIndicator(
                            color = accentColor,
                            trackColor = accentColor.copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitPdfSurgicalScreen(
    tool: PdfTool,
    viewModel: ToolViewModel,
    selectedFiles: List<Uri>,
    pageCount: Int?,
    config: com.example.pdftools.ui.viewmodels.PageRangeConfig,
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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
            Column(modifier = Modifier.fillMaxSize()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    // --- 1. Configuration header ---
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Configuration",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            val totalCount = pageCount ?: 0
                            if (totalCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = accentColor.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (totalCount == 1) {
                                            stringResource(R.string.tool_split_pages_total_single)
                                        } else {
                                            stringResource(R.string.tool_split_pages_total, totalCount)
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                }
                            }
                        }
                    }

                    // --- 2. Page Range Input ---
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                label = { Text(stringResource(R.string.tool_split_page_range_label)) },
                                placeholder = {
                                    Text(
                                        text = "1-3, 5",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = null,
                                        tint = accentColor
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    focusedLabelColor = accentColor,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = stringResource(R.string.tool_split_info_text),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // --- 3. Page Preview header with Select All ---
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        val totalCount = pageCount ?: 0
                        val isAllSelected = config.selectedPages.size == totalCount && totalCount > 0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.tool_split_page_preview),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            OutlinedButton(
                                onClick = {
                                    val updatedPages = if (isAllSelected) {
                                        emptySet()
                                    } else {
                                        (0 until totalCount).toSet()
                                    }
                                    viewModel.pageRangeConfig.value = config.copy(
                                        selectedPages = updatedPages,
                                        pageRange = PageRangeUtils.formatPageRanges(updatedPages)
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.GridView,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isAllSelected) {
                                            stringResource(R.string.tool_split_deselect_all)
                                        } else {
                                            stringResource(R.string.tool_split_select_all)
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = accentColor
                                    )
                                }
                            }
                        }
                    }

                    // --- 4. Page Thumbnail Grid Items ---
                    val totalPages = pageCount ?: 0
                    if (selectedFile != null && totalPages > 0) {
                        items(
                            items = (0 until totalPages).toList(),
                            key = { pageIndex -> pageIndex }
                        ) { pageIndex ->
                            com.example.pdftools.ui.components.SplitPdfPageThumbnail(
                                uri = selectedFile,
                                pageIndex = pageIndex,
                                selected = pageIndex in config.selectedPages,
                                accentColor = accentColor,
                                loadThumbnail = { uri, idx, width ->
                                    viewModel.renderPage(context, uri, idx, width)
                                },
                                onClick = {
                                    val updatedPages = config.selectedPages.toMutableSet().apply {
                                        if (!add(pageIndex)) {
                                            remove(pageIndex)
                                        }
                                    }
                                    viewModel.pageRangeConfig.value = config.copy(
                                        selectedPages = updatedPages,
                                        pageRange = PageRangeUtils.formatPageRanges(updatedPages)
                                    )
                                }
                            )
                        }
                    }
                }

                // --- Sticky Footer Action Bar ---
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        if (isProcessing) {
                            Button(
                                onClick = { },
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor.copy(alpha = 0.4f),
                                    contentColor = Color.White.copy(alpha = 0.6f),
                                    disabledContainerColor = accentColor.copy(alpha = 0.4f),
                                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.CallSplit,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.White.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.tool_split_action_button),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.process(tool.id, context) },
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
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.CallSplit,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.tool_split_action_button),
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

        // --- Processing Dialog Overlay ---
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.width(280.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp, horizontal = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(52.dp),
                            color = accentColor,
                            strokeWidth = 4.dp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Splitting...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Processing your document securely",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        androidx.compose.material3.LinearProgressIndicator(
                            color = accentColor,
                            trackColor = accentColor.copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemovePagesSurgicalScreen(
    tool: PdfTool,
    viewModel: ToolViewModel,
    selectedFiles: List<Uri>,
    pageCount: Int?,
    config: com.example.pdftools.ui.viewmodels.PageRangeConfig,
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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
            Column(modifier = Modifier.fillMaxSize()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    // --- 1. File Info Card ---
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        selectedFile?.let { uri ->
                            val fileName = getFileNameFromUri(context, uri)
                            val totalPages = pageCount ?: 0
                            val selectedCount = config.selectedPages.size
                            val isAllSelected = selectedCount == totalPages && totalPages > 0

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = accentColor.copy(alpha = 0.08f)
                                ),
                                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(accentColor, shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.InsertDriveFile,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = fileName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (selectedCount == 0) {
                                                "No pages selected"
                                            } else {
                                                "$selectedCount page${if (selectedCount != 1) "s" else ""} selected for removal"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    TextButton(
                                        onClick = {
                                            val updatedPages = if (isAllSelected) {
                                                emptySet()
                                            } else {
                                                (0 until totalPages).toSet()
                                            }
                                            viewModel.pageRangeConfig.value = config.copy(
                                                selectedPages = updatedPages,
                                                pageRange = PageRangeUtils.formatPageRanges(updatedPages)
                                            )
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (isAllSelected) "Deselect" else "Select All",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = accentColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // --- 2. Page Preview label ---
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "Page Preview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // --- 3. Configuration section ---
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Configuration",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

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
                                label = { Text(stringResource(R.string.tool_split_page_range_label)) },
                                placeholder = {
                                    Text(
                                        text = "1, 3, 4",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    focusedLabelColor = accentColor,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = stringResource(R.string.tool_split_info_text),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // --- 4. Page Thumbnail Grid Items ---
                    val totalPages = pageCount ?: 0
                    if (selectedFile != null && totalPages > 0) {
                        items(
                            items = (0 until totalPages).toList(),
                            key = { pageIndex -> pageIndex }
                        ) { pageIndex ->
                            com.example.pdftools.ui.components.RemovePageThumbnail(
                                uri = selectedFile,
                                pageIndex = pageIndex,
                                selected = pageIndex in config.selectedPages,
                                loadThumbnail = { uri, idx, width ->
                                    viewModel.renderPage(context, uri, idx, width)
                                },
                                onClick = {
                                    val updatedPages = config.selectedPages.toMutableSet().apply {
                                        if (!add(pageIndex)) {
                                            remove(pageIndex)
                                        }
                                    }
                                    viewModel.pageRangeConfig.value = config.copy(
                                        selectedPages = updatedPages,
                                        pageRange = PageRangeUtils.formatPageRanges(updatedPages)
                                    )
                                }
                            )
                        }
                    }
                }

                // --- Sticky Footer Action Button ---
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        val selectedCount = config.selectedPages.size

                        if (isProcessing) {
                            Button(
                                onClick = { },
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor.copy(alpha = 0.4f),
                                    contentColor = Color.White.copy(alpha = 0.6f),
                                    disabledContainerColor = accentColor.copy(alpha = 0.4f),
                                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.White.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Removing...",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.process(tool.id, context) },
                                enabled = selectedCount > 0,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor,
                                    contentColor = Color.White,
                                    disabledContainerColor = accentColor.copy(alpha = 0.3f),
                                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (selectedCount == 0) {
                                            "Remove Pages"
                                        } else {
                                            "Remove $selectedCount Page${if (selectedCount != 1) "s" else ""}"
                                        },
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

        // --- Processing Dialog Overlay ---
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.width(280.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp, horizontal = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(52.dp),
                            color = accentColor,
                            strokeWidth = 4.dp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Removing Pages...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Processing your document securely",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        androidx.compose.material3.LinearProgressIndicator(
                            color = accentColor,
                            trackColor = accentColor.copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizePdfSurgicalScreen(
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
    val organizePages = remember { mutableStateListOf<OrganizePageItem>() }

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

    fun resetOrder() {
        val totalPages = pageCount ?: return
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // --- 1. File Info Card ---
                    selectedFile?.let { uri ->
                        val fileName = getFileNameFromUri(context, uri)
                        val organizedCount = organizePages.size

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = accentColor.copy(alpha = 0.08f)
                            ),
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(accentColor, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.InsertDriveFile,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = fileName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "$organizedCount page${if (organizedCount != 1) "s" else ""} in custom order",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                TextButton(
                                    onClick = { resetOrder() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Reset Order",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = accentColor
                                    )
                                }
                            }
                        }
                    }

                    // --- 2. Configuration Section ---
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Configuration",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Drag cards or use arrow buttons to reorder pages. Rotate, duplicate, or delete as needed.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // --- 3. Page Preview Label ---
                    Text(
                        text = "Page Preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // --- 4. Page Grid-like Cards Layout ---
                    val itemsPerRow = 2
                    val chunkedPages = organizePages.chunked(itemsPerRow)
                    val reorderThreshold = with(LocalDensity.current) { 56.dp.toPx() }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        chunkedPages.forEachIndexed { rowIndex, rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                rowItems.forEachIndexed { colIndex, pageItem ->
                                    val globalIndex = rowIndex * itemsPerRow + colIndex
                                    var dragOffsetX by remember(pageItem.id) { mutableFloatStateOf(0f) }
                                    var dragOffsetY by remember(pageItem.id) { mutableFloatStateOf(0f) }

                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
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
                                                        val horizontalDrag = kotlin.math.abs(dragOffsetX) > kotlin.math.abs(dragOffsetY)
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
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Thumbnail Preview
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(130.dp)
                                                    .clip(RoundedCornerShape(10.dp))
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
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .graphicsLayer {
                                                                rotationZ = pageItem.rotation.toFloat()
                                                            }
                                                    )
                                                }

                                                // Rotation Badge
                                                if (pageItem.rotation != 0) {
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(6.dp)
                                                            .background(accentColor, shape = RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "${pageItem.rotation}°",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            }

                                            // Info Row (Organized index and original index)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Page ${globalIndex + 1}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Orig: ${pageItem.originalIndex + 1}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                                )
                                            }

                                            Spacer(
                                                modifier = Modifier
                                                    .height(1.dp)
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                            )

                                            // Page Control Actions Row: Rotate, Duplicate, Delete
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Rotate Button
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
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .background(accentColor.copy(alpha = 0.08f), CircleShape)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.RotateRight,
                                                        contentDescription = "Rotate 90° Clockwise",
                                                        tint = accentColor,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }

                                                // Duplicate Button
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
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .background(accentColor.copy(alpha = 0.08f), CircleShape)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Add,
                                                        contentDescription = "Duplicate Page",
                                                        tint = accentColor,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }

                                                // Delete Button
                                                IconButton(
                                                    onClick = {
                                                        if (organizePages.size > 1) {
                                                            organizePages.remove(pageItem)
                                                            viewModel.organizeConfig.value = OrganizeConfig(
                                                                organizePages.map { PdfProcessor.PageTransform(it.originalIndex, it.rotation) }
                                                            )
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f), CircleShape),
                                                    enabled = organizePages.size > 1
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Delete,
                                                        contentDescription = "Delete Page",
                                                        tint = if (organizePages.size > 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            // Reorder Control Bar: Move Previous / Drag Icon / Move Next
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(
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
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "Move Left",
                                                        tint = if (globalIndex > 0) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }

                                                Icon(
                                                    imageVector = Icons.Filled.Menu,
                                                    contentDescription = "Drag to reorder",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                                    modifier = Modifier.size(16.dp)
                                                )

                                                IconButton(
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
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                        contentDescription = "Move Right",
                                                        tint = if (globalIndex < organizePages.size - 1) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                                        modifier = Modifier.size(16.dp)
                                                    )
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

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- Sticky Footer Action Button ---
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        val organizedCount = organizePages.size

                        if (isProcessing) {
                            Button(
                                onClick = { },
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor.copy(alpha = 0.4f),
                                    contentColor = Color.White.copy(alpha = 0.6f),
                                    disabledContainerColor = accentColor.copy(alpha = 0.4f),
                                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.GridView,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.White.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Saving PDF...",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.process(tool.id, context) },
                                enabled = organizedCount > 0,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor,
                                    contentColor = Color.White,
                                    disabledContainerColor = accentColor.copy(alpha = 0.3f),
                                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.GridView,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Organize PDF",
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

        // --- Processing Dialog Overlay ---
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.width(280.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp, horizontal = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(52.dp),
                            color = accentColor,
                            strokeWidth = 4.dp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Saving PDF...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Applying page transforms securely",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        androidx.compose.material3.LinearProgressIndicator(
                            color = accentColor,
                            trackColor = accentColor.copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompressPdfSurgicalScreen(
    tool: PdfTool,
    viewModel: ToolViewModel,
    selectedFiles: List<Uri>,
    isProcessing: Boolean,
    isComplete: Boolean,
    outputUris: List<Uri>,
    progress: Float?,
    innerPadding: PaddingValues,
    accentColor: Color,
    containerColor: Color
) {
    val context = LocalContext.current
    val config by viewModel.compressConfig.collectAsState()
    val selectedFile = selectedFiles.firstOrNull()

    val originalSize = remember(selectedFile) {
        selectedFile?.let { getFileSize(context, it) } ?: 0L
    }

    val fileName = remember(selectedFile) {
        selectedFile?.let { getFileNameFromUri(context, it) } ?: ""
    }

    val savingsFactor = when (config.tier) {
        CompressTier.BASIC -> 0.10f
        CompressTier.RECOMMENDED -> 0.40f
        CompressTier.EXTREME -> 0.70f
    }

    val estimatedNewSize = (originalSize * (1f - savingsFactor)).toLong()
    val savedSize = originalSize - estimatedNewSize

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // --- 1. File Info Card ---
                    selectedFile?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(accentColor, accentColor.copy(alpha = 0.8f))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PictureAsPdf,
                                        contentDescription = "PDF Icon",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = fileName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row {
                                        Text(
                                            text = "Current Size: ",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = formatFileSize(originalSize),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // --- 2. Compression Level Section ---
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "COMPRESSION LEVEL",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CompressTier.values().forEach { tier ->
                                val isSelected = config.tier == tier
                                val border = if (isSelected) {
                                    BorderStroke(2.dp, accentColor)
                                } else {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                }
                                val title = when (tier) {
                                    CompressTier.BASIC -> "Basic"
                                    CompressTier.RECOMMENDED -> "Recommended"
                                    CompressTier.EXTREME -> "Extreme"
                                }
                                val desc = when (tier) {
                                    CompressTier.BASIC -> "High quality, ~10% reduction"
                                    CompressTier.RECOMMENDED -> "Good quality, ~40% reduction"
                                    CompressTier.EXTREME -> "Low quality, ~70% reduction"
                                }

                                Card(
                                    onClick = { viewModel.compressConfig.value = config.copy(tier = tier) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) accentColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceContainerLow,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = border
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 18.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )

                                                if (tier == CompressTier.RECOMMENDED) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(accentColor)
                                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "BEST VALUE",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = desc,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = "Selected",
                                                tint = accentColor,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- 3. Estimated Result Card ---
                    if (originalSize > 0L) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Estimated Result",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = formatFileSize(estimatedNewSize),
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = accentColor
                                    )
                                    Text(
                                        text = formatFileSize(originalSize),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            textDecoration = TextDecoration.LineThrough
                                        ),
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(1f - savingsFactor)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(accentColor)
                                        )
                                    }
                                }

                                Text(
                                    text = "Compression saves approximately ${formatFileSize(savedSize)} of storage.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- Sticky Footer Action Button ---
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        if (isProcessing) {
                            Button(
                                onClick = { },
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor.copy(alpha = 0.4f),
                                    contentColor = Color.White.copy(alpha = 0.6f),
                                    disabledContainerColor = accentColor.copy(alpha = 0.4f),
                                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CompressIcon(
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Compressing...",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.process(tool.id, context) },
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
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CompressIcon(
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Compress PDF",
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

        // --- Processing Dialog Overlay ---
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.width(280.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp, horizontal = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(52.dp),
                            color = accentColor,
                            strokeWidth = 4.dp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Compressing...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Optimizing document size securely",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        androidx.compose.material3.LinearProgressIndicator(
                            color = accentColor,
                            trackColor = accentColor.copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompressIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val width = size.width
        val height = size.height
        val strokeWidth = 2.dp.toPx()
        
        drawLine(
            color = tint,
            start = Offset(width * 0.2f, height * 0.5f),
            end = Offset(width * 0.8f, height * 0.5f),
            strokeWidth = strokeWidth
        )
        
        val upperArrowYEnd = height * 0.4f
        drawLine(
            color = tint,
            start = Offset(width * 0.5f, height * 0.15f),
            end = Offset(width * 0.5f, upperArrowYEnd),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = tint,
            start = Offset(width * 0.35f, height * 0.3f),
            end = Offset(width * 0.5f, upperArrowYEnd),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = tint,
            start = Offset(width * 0.65f, height * 0.3f),
            end = Offset(width * 0.5f, upperArrowYEnd),
            strokeWidth = strokeWidth
        )
        
        val lowerArrowYEnd = height * 0.6f
        drawLine(
            color = tint,
            start = Offset(width * 0.5f, height * 0.85f),
            end = Offset(width * 0.5f, lowerArrowYEnd),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = tint,
            start = Offset(width * 0.35f, height * 0.7f),
            end = Offset(width * 0.5f, lowerArrowYEnd),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = tint,
            start = Offset(width * 0.65f, height * 0.7f),
            end = Offset(width * 0.5f, lowerArrowYEnd),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
fun RepairPdfSurgicalScreen(
    tool: PdfTool,
    viewModel: ToolViewModel,
    selectedFiles: List<Uri>,
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

    val originalSize = remember(selectedFile) {
        selectedFile?.let { getFileSize(context, it) } ?: 0L
    }

    val fileName = remember(selectedFile) {
        selectedFile?.let { getFileNameFromUri(context, it) } ?: ""
    }

    // Checked states of the 4 configuration options
    var fixStructure by remember { mutableStateOf(true) }
    var recoverData by remember { mutableStateOf(false) }
    var repairMetadata by remember { mutableStateOf(false) }
    var optimizeWeb by remember { mutableStateOf(false) }

    // Dynamic corruption check
    var isCorrupted by remember(selectedFile) { mutableStateOf(false) }
    var checkingCorruption by remember(selectedFile) { mutableStateOf(true) }

    LaunchedEffect(selectedFile) {
        if (selectedFile != null) {
            checkingCorruption = true
            try {
                val count = viewModel.getPageCountSuspend(context, selectedFile)
                isCorrupted = (count <= 0)
            } catch (e: Exception) {
                isCorrupted = true
            } finally {
                checkingCorruption = false
            }
        } else {
            isCorrupted = false
            checkingCorruption = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Selected File Section Label
                    Text(
                        text = "Selected File",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 1. Selected File Card (State: Corrupted/Healthy/Checking)
                    selectedFile?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                val iconBg = when {
                                    checkingCorruption -> MaterialTheme.colorScheme.surfaceVariant
                                    isCorrupted -> MaterialTheme.colorScheme.errorContainer
                                    else -> containerColor.copy(alpha = 0.5f)
                                }
                                val iconTint = when {
                                    checkingCorruption -> MaterialTheme.colorScheme.onSurfaceVariant
                                    isCorrupted -> MaterialTheme.colorScheme.error
                                    else -> accentColor
                                }

                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(iconBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PictureAsPdf,
                                        contentDescription = "PDF Icon",
                                        tint = iconTint,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = fileName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = formatFileSize(originalSize),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        val statusColor = when {
                                            checkingCorruption -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            isCorrupted -> MaterialTheme.colorScheme.error
                                            else -> accentColor
                                        }
                                        val statusText = when {
                                            checkingCorruption -> "Checking..."
                                            isCorrupted -> "Corrupted"
                                            else -> "Healthy"
                                        }

                                        Text(
                                            text = "•",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = statusColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = statusText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Informative Guidance Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = containerColor.copy(alpha = 0.5f) // Category light green background tint
                        ),
                        border = BorderStroke(
                            1.dp,
                            accentColor.copy(alpha = 0.3f)
                        )
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
                                contentDescription = "Info Icon",
                                tint = accentColor, // Category green accent color
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "This tool attempts to restore the PDF's internal structure and data without changing the visible content. High-performance recovery logic is used for military-grade document stability.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Configuration Section Label
                    Text(
                        text = "Configuration",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 3. 4 Configuration checkboxes
                    RepairOptionCard(
                        title = "Fix Document Structure",
                        description = "Rebuild XRef tables and streams",
                        checked = fixStructure,
                        onCheckedChange = { fixStructure = it },
                        accentColor = accentColor,
                        containerColor = containerColor
                    )

                    RepairOptionCard(
                        title = "Recover Corrupt Data",
                        description = "Scan for raw text and image fragments",
                        checked = recoverData,
                        onCheckedChange = { recoverData = it },
                        accentColor = accentColor,
                        containerColor = containerColor
                    )

                    RepairOptionCard(
                        title = "Repair Metadata & Fonts",
                        description = "Restore missing mapping and font subsets",
                        checked = repairMetadata,
                        onCheckedChange = { repairMetadata = it },
                        accentColor = accentColor,
                        containerColor = containerColor
                    )

                    RepairOptionCard(
                        title = "Optimize for Web Viewing",
                        description = "Enable linearization for fast loading",
                        checked = optimizeWeb,
                        onCheckedChange = { optimizeWeb = it },
                        accentColor = accentColor,
                        containerColor = containerColor
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 4. Sticky Bottom Action Bar (with float/transparent fix)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        if (isProcessing) {
                            Button(
                                onClick = { },
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor.copy(alpha = 0.4f),
                                    contentColor = Color.White.copy(alpha = 0.6f),
                                    disabledContainerColor = accentColor.copy(alpha = 0.4f),
                                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Build,
                                        contentDescription = "Build Icon",
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Repairing...",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.process(tool.id, context) },
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
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Build,
                                        contentDescription = "Build Icon",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Repair PDF",
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

        // --- Processing Dialog Overlay ---
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.width(280.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp, horizontal = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(52.dp),
                            color = accentColor,
                            strokeWidth = 4.dp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Repairing...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Restoring internal structures",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        androidx.compose.material3.LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = accentColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RepairOptionCard(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color,
    containerColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (checked) containerColor.copy(alpha = 0.5f) else Color.Transparent
        ),
        border = BorderStroke(
            if (checked) 2.dp else 1.dp,
            if (checked) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = accentColor,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Composable
fun OcrPdfSurgicalScreen(
    tool: PdfTool,
    viewModel: ToolViewModel,
    selectedFiles: List<Uri>,
    isProcessing: Boolean,
    isComplete: Boolean,
    outputUris: List<Uri>,
    progress: Float?,
    innerPadding: PaddingValues,
    accentColor: Color,
    containerColor: Color
) {
    val context = LocalContext.current
    val isDark = LocalDarkTheme.current
    val selectedFile = selectedFiles.firstOrNull()

    val originalSize = remember(selectedFile) {
        selectedFile?.let { getFileSize(context, it) } ?: 0L
    }

    val fileName = remember(selectedFile) {
        selectedFile?.let { getFileNameFromUri(context, it) } ?: ""
    }

    LaunchedEffect(Unit) {
        viewModel.checkOcrStatuses()
    }

    // Config states
    val ocrConfig by viewModel.ocrConfig.collectAsState()
    val ocrResultText = ocrConfig.ocrResultText
    val selectedLangCode = ocrConfig.ocrLanguage
    val moduleStatuses = ocrConfig.moduleStatuses

    val languageMap = mapOf(
        "English" to "latin",
        "Chinese" to "chinese",
        "Hindi" to "devanagari",
        "Japanese" to "japanese",
        "Korean" to "korean"
    )
    val languages = listOf("English", "Chinese", "Hindi", "Japanese", "Korean")
    var dropdownExpanded by remember { mutableStateOf(false) }

    val selectedLanguageDisplayName = when (selectedLangCode) {
        "chinese" -> "Chinese"
        "devanagari" -> "Hindi"
        "japanese" -> "Japanese"
        "korean" -> "Korean"
        else -> "English"
    }

    val selectedStatus = if (selectedLangCode == "latin") OcrModuleStatus.Ready else moduleStatuses[selectedLangCode] ?: OcrModuleStatus.NotDownloaded

    // Format selection: "searchable_pdf" or "raw_text"
    var selectedFormat by remember { mutableStateOf("searchable_pdf") }

    // Optimization Slider: 0f = Speed, 1f = Balanced, 2f = Accuracy
    var sliderPosition by remember { mutableFloatStateOf(1f) }

    val activeTierTitle = when (sliderPosition.toInt()) {
        0 -> "Speed"
        1 -> "Balanced"
        else -> "Accuracy"
    }

    val activeTierDesc = when (sliderPosition.toInt()) {
        0 -> "Fast recognition, suitable for clean printed documents."
        1 -> "Optimized balance of speed and character recognition accuracy."
        else -> "Deep neural network analysis for low-contrast or complex layouts (slower)."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        if (isComplete && ocrResultText.isNotEmpty()) {
            // Success Screen showing extracted text
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Green Success Confirmation Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = containerColor.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(accentColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Filled.CheckCircle,
                                    contentDescription = "Success",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Text Extracted Successfully",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${ocrResultText.length} characters recognized",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Extracted Text Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Extracted Content Preview",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Extracted Text", ocrResultText)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.InsertDriveFile,
                                            contentDescription = "Copy Text",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, ocrResultText)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share Extracted Text"))
                                        },
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Share,
                                            contentDescription = "Share Text",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp, max = 360.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .padding(12.dp)
                            ) {
                                val scrollState = rememberScrollState()
                                androidx.compose.foundation.text.selection.SelectionContainer {
                                    Text(
                                        text = ocrResultText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .verticalScroll(scrollState)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Sticky footer in completed state
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.resetCurrentRun() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Done",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            // Configuration layout
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Selected File section header
                    Text(
                        text = "Selected File",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 1. Selected File Card (State: Scanned Image)
                    selectedFile?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // PDF Icon in soft orange container
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isDark) accentColor.copy(alpha = 0.15f) else containerColor), // Theme-aware background tint
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PictureAsPdf,
                                        contentDescription = "PDF Icon",
                                        tint = accentColor,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = fileName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = formatFileSize(originalSize),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "•",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = accentColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Scanned Image",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Guidance / Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = containerColor.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
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
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Optical Character Recognition (OCR) analyzes the selected document to recognize characters and reconstruct text layers, transforming static images into highly functional documents.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 3. Intelligent Configuration Section
                    Text(
                        text = "Intelligent Configuration",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Language Selector
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Document Language",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedLanguageDisplayName,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    IconButton(onClick = { dropdownExpanded = !dropdownExpanded }) {
                                        Icon(
                                            imageVector = if (dropdownExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                            contentDescription = "Language Selector"
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                languages.forEach { lang ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = lang, style = MaterialTheme.typography.bodyLarge)
                                                val itemLangCode = languageMap[lang] ?: "latin"
                                                val itemStatus = if (itemLangCode == "latin") OcrModuleStatus.Ready else moduleStatuses[itemLangCode] ?: OcrModuleStatus.NotDownloaded
                                                val labelText = when (itemStatus) {
                                                    is OcrModuleStatus.Ready -> "Available"
                                                    is OcrModuleStatus.NotDownloaded -> "Not Installed"
                                                    is OcrModuleStatus.Downloading -> "Downloading... ${(itemStatus.progress * 100).toInt()}%"
                                                    is OcrModuleStatus.Error -> "Error"
                                                }
                                                val labelColor = when (itemStatus) {
                                                    is OcrModuleStatus.Ready -> accentColor
                                                    is OcrModuleStatus.Downloading -> accentColor.copy(alpha = 0.8f)
                                                    is OcrModuleStatus.Error -> MaterialTheme.colorScheme.error
                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                }
                                                Text(
                                                    text = labelText,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = labelColor,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        },
                                        onClick = {
                                            val targetLang = languageMap[lang] ?: "latin"
                                            viewModel.updateOcrLanguage(targetLang)
                                            val targetStatus = if (targetLang == "latin") OcrModuleStatus.Ready else moduleStatuses[targetLang] ?: OcrModuleStatus.NotDownloaded
                                            if (targetStatus is OcrModuleStatus.NotDownloaded || targetStatus is OcrModuleStatus.Error) {
                                                viewModel.downloadOcrLanguage(targetLang)
                                            }
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Availability status card
                        if (selectedLangCode != "latin" && selectedStatus !is OcrModuleStatus.Ready) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Language Pack Required",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val desc = when (selectedStatus) {
                                            is OcrModuleStatus.Downloading -> "Downloading model files... ${(selectedStatus.progress * 100).toInt()}%"
                                            is OcrModuleStatus.Error -> "Failed to download model files: ${selectedStatus.message}"
                                            else -> "This language pack needs to be downloaded for offline use."
                                        }
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    if (selectedStatus is OcrModuleStatus.Downloading) {
                                        CircularProgressIndicator(
                                            progress = { selectedStatus.progress },
                                            modifier = Modifier.size(24.dp),
                                            color = accentColor,
                                            strokeWidth = 2.5.dp
                                        )
                                    } else {
                                        Button(
                                            onClick = { viewModel.downloadOcrLanguage(selectedLangCode) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = accentColor
                                            ),
                                            shape = RoundedCornerShape(20.dp)
                                        ) {
                                            Text(
                                                text = if (selectedStatus is OcrModuleStatus.Error) "Retry" else "Download",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Format Selection Cards
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Output Format",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Searchable PDF Format Card
                        val isSearchablePdf = selectedFormat == "searchable_pdf"
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedFormat = "searchable_pdf" },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSearchablePdf) containerColor.copy(alpha = 0.5f) else Color.Transparent
                            ),
                            border = BorderStroke(
                                if (isSearchablePdf) 2.dp else 1.dp,
                                if (isSearchablePdf) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                RadioButton(
                                    selected = isSearchablePdf,
                                    onClick = { selectedFormat = "searchable_pdf" },
                                    colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Searchable PDF",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Preserves the original document look with an invisible copy-pasteable text layer.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Text Document Format Card
                        val isRawText = selectedFormat == "raw_text"
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedFormat = "raw_text" },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isRawText) containerColor.copy(alpha = 0.5f) else Color.Transparent
                            ),
                            border = BorderStroke(
                                if (isRawText) 2.dp else 1.dp,
                                if (isRawText) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                RadioButton(
                                    selected = isRawText,
                                    onClick = { selectedFormat = "raw_text" },
                                    colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Text Document",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Extracts raw character content directly into a clean, standalone text document (.txt).",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // 4. Performance & Accuracy Control
                    Text(
                        text = "Performance Control",
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
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
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
                                    text = "Accuracy Tier: $activeTierTitle",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = activeTierDesc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Slider(
                                value = sliderPosition,
                                onValueChange = { sliderPosition = it },
                                valueRange = 0f..2f,
                                steps = 1,
                                colors = SliderDefaults.colors(
                                    thumbColor = accentColor,
                                    activeTrackColor = accentColor,
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Speed",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (activeTierTitle == "Speed") accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (activeTierTitle == "Speed") FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "Balanced",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (activeTierTitle == "Balanced") accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (activeTierTitle == "Balanced") FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "Accuracy",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (activeTierTitle == "Accuracy") accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (activeTierTitle == "Accuracy") FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Sticky action bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        if (isProcessing) {
                            Button(
                                onClick = { },
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor.copy(alpha = 0.4f),
                                    contentColor = Color.White.copy(alpha = 0.6f),
                                    disabledContainerColor = accentColor.copy(alpha = 0.4f),
                                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DocumentScanner,
                                        contentDescription = "OCR Running",
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Performing OCR...",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.process(tool.id, context) },
                                enabled = selectedLangCode == "latin" || selectedStatus is OcrModuleStatus.Ready,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor,
                                    contentColor = Color.White,
                                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DocumentScanner,
                                        contentDescription = "Run OCR",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "OCR PDF",
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

        // --- Processing Dialog Overlay ---
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.width(280.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp, horizontal = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(52.dp),
                            color = accentColor,
                            strokeWidth = 4.dp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Performing OCR...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Extracting searchable text layer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = accentColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JpgToPdfSurgicalScreen(
    tool: PdfTool,
    viewModel: ToolViewModel,
    selectedFiles: List<Uri>,
    isProcessing: Boolean,
    isComplete: Boolean,
    outputUris: List<Uri>,
    progress: Float?,
    innerPadding: PaddingValues,
    accentColor: Color,
    containerColor: Color
) {
    val context = LocalContext.current
    val isDark = LocalDarkTheme.current
    val config by viewModel.jpgToPdfConfig.collectAsState()

    val combinedSize = remember(selectedFiles) {
        selectedFiles.sumOf { getFileSize(context, it) }
    }

    var pageSizeExpanded by remember { mutableStateOf(false) }
    var maxSizeExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Selected File Section Label
                    Text(
                        text = "Selected Files",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 1. Selected Files Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) accentColor.copy(alpha = 0.15f) else containerColor), // theme-aware tint
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PictureAsPdf,
                                    contentDescription = "Images to PDF",
                                    tint = accentColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${selectedFiles.size} Image" + if (selectedFiles.size > 1) "s" else "",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Total Combined Size: ${formatFileSize(combinedSize)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Horizontal scroll of selected image thumbnails
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(selectedFiles) { index, uri ->
                            val thumbnail = rememberThumbnailBitmap(context, uri)
                            Card(
                                modifier = Modifier
                                    .size(90.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    if (thumbnail != null) {
                                        Image(
                                            bitmap = thumbnail.asImageBitmap(),
                                            contentDescription = "Selected Image ${index + 1}",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                color = accentColor,
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.dp
                                            )
                                        }
                                    }

                                    // Image index badge
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(6.dp)
                                            .size(22.dp)
                                            .background(Color.Black.copy(alpha = 0.65f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Info / Guidance Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = containerColor.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
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
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Convert selected JPG/PNG images into a single PDF document. Configure page layout, margins, and set file size limits with automatic quality optimization.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 3. Layout Options Section Label
                    Text(
                        text = "Layout Configuration",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Page Size Selector
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Page Size",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Box(modifier = Modifier.fillMaxWidth()) {
                            val displaySizeName = when (config.pageSize.lowercase()) {
                                "a4" -> "A4"
                                "letter" -> "Letter"
                                else -> "Auto (Same as image)"
                            }

                            OutlinedTextField(
                                value = displaySizeName,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    IconButton(onClick = { pageSizeExpanded = !pageSizeExpanded }) {
                                        Icon(
                                            imageVector = if (pageSizeExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                            contentDescription = "Page Size Selector"
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            DropdownMenu(
                                expanded = pageSizeExpanded,
                                onDismissRequest = { pageSizeExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Auto (Same as image)") },
                                    onClick = {
                                        viewModel.jpgToPdfConfig.value = config.copy(pageSize = "auto")
                                        pageSizeExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("A4") },
                                    onClick = {
                                        viewModel.jpgToPdfConfig.value = config.copy(pageSize = "a4")
                                        pageSizeExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Letter") },
                                    onClick = {
                                        viewModel.jpgToPdfConfig.value = config.copy(pageSize = "letter")
                                        pageSizeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Orientation Selector (Disabled when Auto is selected)
                    val isOrientationEnabled = config.pageSize != "auto"
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Orientation",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOrientationEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            listOf("portrait" to "Portrait", "landscape" to "Landscape").forEach { (type, name) ->
                                val isSelected = config.orientation == type
                                val cardBg = if (isSelected && isOrientationEnabled) {
                                    accentColor.copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                }
                                val borderCol = if (isSelected && isOrientationEnabled) {
                                    accentColor
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                }
                                val textCol = if (isSelected && isOrientationEnabled) {
                                    accentColor
                                } else if (!isOrientationEnabled) {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(cardBg)
                                        .clickable(enabled = isOrientationEnabled) {
                                            viewModel.jpgToPdfConfig.value = config.copy(orientation = type)
                                        },
                                    border = BorderStroke(1.dp, borderCol),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = textCol
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Margin Selector
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Margins",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val margins = listOf(
                                0f to "None",
                                12f to "Small",
                                24f to "Medium",
                                36f to "Large"
                            )
                            margins.forEach { (marginVal, marginName) ->
                                val isSelected = config.margin == marginVal
                                val cardBg = if (isSelected) {
                                    accentColor.copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                }
                                val borderCol = if (isSelected) {
                                    accentColor
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                }
                                val textCol = if (isSelected) {
                                    accentColor
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(cardBg)
                                        .clickable {
                                            viewModel.jpgToPdfConfig.value = config.copy(margin = marginVal)
                                        },
                                    border = BorderStroke(1.dp, borderCol),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = marginName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = textCol
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Max PDF Size Selector
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Max PDF Size Limit",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Box(modifier = Modifier.fillMaxWidth()) {
                            val displaySize = when (config.maxSizeMb) {
                                null -> "Unlimited"
                                1 -> "1 MB"
                                2 -> "2 MB"
                                5 -> "5 MB"
                                else -> "${config.maxSizeMb} MB"
                            }

                            OutlinedTextField(
                                value = displaySize,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    IconButton(onClick = { maxSizeExpanded = !maxSizeExpanded }) {
                                        Icon(
                                            imageVector = if (maxSizeExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                            contentDescription = "Max Size Selector"
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            DropdownMenu(
                                expanded = maxSizeExpanded,
                                onDismissRequest = { maxSizeExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Unlimited") },
                                    onClick = {
                                        viewModel.jpgToPdfConfig.value = config.copy(maxSizeMb = null)
                                        maxSizeExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("1 MB") },
                                    onClick = {
                                        viewModel.jpgToPdfConfig.value = config.copy(maxSizeMb = 1)
                                        maxSizeExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("2 MB") },
                                    onClick = {
                                        viewModel.jpgToPdfConfig.value = config.copy(maxSizeMb = 2)
                                        maxSizeExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("5 MB") },
                                    onClick = {
                                        viewModel.jpgToPdfConfig.value = config.copy(maxSizeMb = 5)
                                        maxSizeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Sticky Footer Action Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.process(tool.id, context) },
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PictureAsPdf,
                                    contentDescription = "Convert",
                                    tint = Color.White
                                )
                                Text(
                                    text = "Convert to PDF",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Processing Dialog Overlay ---
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.width(280.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp, horizontal = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(52.dp),
                            color = accentColor,
                            strokeWidth = 4.dp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Converting Images...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Structuring layout and compressing pages",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        androidx.compose.material3.LinearProgressIndicator(
                            color = accentColor,
                            trackColor = accentColor.copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PdfToWordSurgicalScreen(
    tool: PdfTool,
    viewModel: ToolViewModel,
    selectedFiles: List<Uri>,
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
    val config by viewModel.pdfToWordConfig.collectAsState()
    val pageCount by viewModel.pageCount.collectAsState()
    val wordBlue = accentColor
    val panelBackground = MaterialTheme.colorScheme.surfaceContainer
    val selectedBackground = accentColor.copy(alpha = 0.15f)

    val fileSize = remember(selectedFile) {
        selectedFile?.let { getFileSize(context, it) } ?: 0L
    }
    val fileName = remember(selectedFile) {
        selectedFile?.let { getFileNameFromUri(context, it) } ?: "Contract_Final.pdf"
    }

    LaunchedEffect(selectedFile) {
        selectedFile?.let { viewModel.loadPageCount(context, it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Selected File",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    selectedFile?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PictureAsPdf,
                                        contentDescription = "PDF Document",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = fileName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = buildString {
                                            append(formatFileSize(fileSize))
                                            append(" - PDF Document")
                                            pageCount?.takeIf { it > 0 }?.let { append(" - $it pages") }
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.removeFile(0) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Remove file",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(selectedBackground),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DocumentScanner,
                                        contentDescription = "Conversion quality",
                                        tint = wordBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = "Conversion Quality",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(panelBackground, RoundedCornerShape(14.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("standard" to "Standard", "ocr" to "OCR").forEach { (mode, label) ->
                                    val selected = config.conversionMode == mode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .clip(RoundedCornerShape(11.dp))
                                            .background(if (selected) wordBlue else Color.Transparent)
                                            .clickable {
                                                viewModel.pdfToWordConfig.value = config.copy(conversionMode = mode)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.pdfToWordConfig.value = config.copy(
                                    keepOriginalLayout = !config.keepOriginalLayout
                                )
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(selectedBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AspectRatio,
                                    contentDescription = "Layout logic",
                                    tint = wordBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Layout Logic",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Keep Original Layout",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = config.keepOriginalLayout,
                                onCheckedChange = { value ->
                                    viewModel.pdfToWordConfig.value = config.copy(keepOriginalLayout = value)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = wordBlue,
                                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = selectedBackground),
                        border = BorderStroke(1.dp, wordBlue.copy(alpha = 0.25f))
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
                                contentDescription = "Mode guidance",
                                tint = wordBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (config.conversionMode == "ocr") {
                                    "OCR Mode is best for scanned PDFs and image-only pages. It extracts readable text before creating the editable Word document."
                                } else {
                                    "Standard Mode is best for editable forms and text-heavy contracts. It preserves fonts and formatting with high accuracy. For handwritten notes or flat images, switch to OCR."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.process(tool.id, context) },
                            enabled = !isProcessing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = wordBlue,
                                contentColor = Color.White
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Description,
                                    contentDescription = "Convert to Word",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Convert to Word",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.width(280.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 34.dp, horizontal = 26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(52.dp),
                            color = wordBlue,
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(22.dp))
                        Text(
                            text = "Converting to Word...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (config.conversionMode == "ocr") {
                                "Recognizing text and rebuilding document structure"
                            } else {
                                "Extracting text and preserving document layout"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(22.dp))
                        if (progress != null) {
                            LinearProgressIndicator(
                                progress = { progress },
                                color = wordBlue,
                                trackColor = wordBlue.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        } else {
                            LinearProgressIndicator(
                                color = wordBlue,
                                trackColor = wordBlue.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WordToPdfSurgicalScreen(
    tool: PdfTool,
    viewModel: ToolViewModel,
    selectedFiles: List<Uri>,
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
    val config by viewModel.wordToPdfConfig.collectAsState()

    val fileSize = remember(selectedFile) {
        selectedFile?.let { getFileSize(context, it) } ?: 0L
    }
    val fileName = remember(selectedFile) {
        selectedFile?.let { getFileNameFromUri(context, it) } ?: ""
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Section Label
                    Text(
                        text = "Selected File",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 1. Word Document Selection Card
                    selectedFile?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Word Document Blue Icon box
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(accentColor.copy(alpha = 0.15f)), // light blue tint
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Description,
                                        contentDescription = "Word Document",
                                        tint = accentColor, // Word blue
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = fileName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = formatFileSize(fileSize),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "•",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = accentColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Microsoft Word",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Info / Guidance Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = containerColor.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
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
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Convert Microsoft Word files locally. Tuning layout, compression, and text extraction options enables higher output precision and searchability.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 3. Layout Options Header
                    Text(
                        text = "Fidelity Configuration",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Maintain Layout Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.wordToPdfConfig.value = config.copy(maintainLayout = !config.maintainLayout)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (config.maintainLayout) {
                                accentColor.copy(alpha = 0.12f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (config.maintainLayout) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Maintain Original Layout",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Preserve fonts, alignment, margins, and exact paragraph spacing.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = config.maintainLayout,
                                onCheckedChange = { value ->
                                    viewModel.wordToPdfConfig.value = config.copy(maintainLayout = value)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = accentColor,
                                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }

                    // 4. Quality & Processing Header
                    Text(
                        text = "Quality & post-processing",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Segmented Image Compression Quality
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Image Quality",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("low" to "Low", "medium" to "Medium", "high" to "High").forEach { (tierValue, tierName) ->
                                val isSelected = config.imageQuality == tierValue
                                val cardBg = if (isSelected) {
                                    accentColor.copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                }
                                val borderCol = if (isSelected) {
                                    accentColor
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                }
                                val textCol = if (isSelected) {
                                    accentColor
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(cardBg)
                                        .clickable {
                                            viewModel.wordToPdfConfig.value = config.copy(imageQuality = tierValue)
                                        },
                                    border = BorderStroke(1.dp, borderCol),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tierName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = textCol
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // OCR Checkbox Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.wordToPdfConfig.value = config.copy(runOcr = !config.runOcr)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (config.runOcr) {
                                accentColor.copy(alpha = 0.12f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (config.runOcr) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Perform OCR on Conversion",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Extract and index searchable text layers from images embedded in the document.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Checkbox(
                                checked = config.runOcr,
                                onCheckedChange = { value ->
                                    viewModel.wordToPdfConfig.value = config.copy(runOcr = value)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = accentColor,
                                    checkmarkColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Sticky Footer Action Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.process(tool.id, context) },
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PictureAsPdf,
                                    contentDescription = "Convert",
                                    tint = Color.White
                                )
                                Text(
                                    text = "Convert to PDF",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Processing Dialog Overlay ---
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.width(280.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp, horizontal = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(52.dp),
                            color = accentColor,
                            strokeWidth = 4.dp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Converting Document...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Extracting elements and structuring page layouts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        androidx.compose.material3.LinearProgressIndicator(
                            color = accentColor,
                            trackColor = accentColor.copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExcelToPdfSurgicalScreen(
    tool: PdfTool,
    viewModel: ToolViewModel,
    selectedFiles: List<Uri>,
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
    val config by viewModel.excelToPdfConfig.collectAsState()

    val fileSize = remember(selectedFile) {
        selectedFile?.let { getFileSize(context, it) } ?: 0L
    }
    val fileName = remember(selectedFile) {
        selectedFile?.let { getFileNameFromUri(context, it) } ?: ""
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Section Label: Selected File
                    Text(
                        text = "Selected File",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Excel Document Selection Card
                    selectedFile?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Excel Green Icon Box
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(accentColor.copy(alpha = 0.15f)), // light green tint
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.TableChart,
                                        contentDescription = "Excel Sheet",
                                        tint = accentColor, // Excel green
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = fileName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = formatFileSize(fileSize),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "•",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = accentColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Microsoft Excel",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Guidance / Help Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = containerColor.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
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
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Convert spreadsheets seamlessly with full control over pages, columns fitting, and cell outlines to generate beautiful, readable PDF reports.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 1. Sheet-Level Control
                    Text(
                        text = "Sheet Conversion Range",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val ranges = listOf(
                            "active_sheet" to "Active Sheet Only",
                            "all_sheets" to "All Sheets"
                        )
                        ranges.forEach { (mode, title) ->
                            val isSelected = config.convertMode == mode
                            val cardBg = if (isSelected) {
                                accentColor.copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            }
                            val borderCol = if (isSelected) {
                                accentColor
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            }
                            val textCol = if (isSelected) {
                                accentColor
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(68.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(cardBg)
                                    .clickable {
                                        viewModel.excelToPdfConfig.value = config.copy(convertMode = mode)
                                    },
                                border = BorderStroke(1.dp, borderCol),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (mode == "active_sheet") Icons.Filled.LayersClear else Icons.Filled.Layers,
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

                    // 2. Intelligent Scaling Options
                    Text(
                        text = "Intelligent Scaling Mode",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    var dropdownExpanded by remember { mutableStateOf(false) }
                    val currentModeLabel = when (config.scalingMode) {
                        "fit_columns" -> "Fit All Columns on One Page"
                        "fit_rows" -> "Fit All Rows on One Page"
                        "fit_all" -> "Fit Entire Sheet on One Page"
                        else -> "No Scaling (Original Size)"
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { dropdownExpanded = true },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
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
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AspectRatio,
                                        contentDescription = "Scaling",
                                        tint = accentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = currentModeLabel,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Ensure cells and columns remain legible",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = "Expand",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            val options = listOf(
                                "fit_columns" to ("Fit Columns on Page" to "Compresses horizontally so every column fits"),
                                "fit_rows" to ("Fit Rows on Page" to "Compresses vertically so every row fits"),
                                "fit_all" to ("Fit Entire Sheet on One Page" to "Packs columns and rows onto a single page"),
                                "no_scaling" to ("No Scaling (Original size)" to "Renders cell tables at original dimensions")
                            )

                            options.forEach { (mode, info) ->
                                val (title, desc) = info
                                val isSelected = config.scalingMode == mode

                                DropdownMenuItem(
                                    text = {
                                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                            Text(
                                                text = title,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = desc,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.excelToPdfConfig.value = config.copy(scalingMode = mode)
                                        dropdownExpanded = false
                                    },
                                    modifier = Modifier.background(
                                        if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent
                                    )
                                )
                            }
                        }
                    }

                    // 3. Gridlines Toggle Card
                    Text(
                        text = "Sheet Layout Options",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.excelToPdfConfig.value = config.copy(showGridlines = !config.showGridlines)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (config.showGridlines) {
                                accentColor.copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (config.showGridlines) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Preserve Gridlines",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Draw gridlines around table cells to retain the spreadsheet's original structure.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = config.showGridlines,
                                onCheckedChange = { value ->
                                    viewModel.excelToPdfConfig.value = config.copy(showGridlines = value)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = accentColor,
                                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Sticky Footer Action Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.process(tool.id, context) },
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PictureAsPdf,
                                    contentDescription = "Convert",
                                    tint = Color.White
                                )
                                Text(
                                    text = "Convert to PDF",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Processing Dialog Overlay ---
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.width(280.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp, horizontal = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(52.dp),
                            color = accentColor,
                            strokeWidth = 4.dp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Converting Excel...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Recalculating layouts and adjusting print margins",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        androidx.compose.material3.LinearProgressIndicator(
                            color = accentColor,
                            trackColor = accentColor.copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PptToPdfSurgicalScreen(

    tool: PdfTool,
    viewModel: ToolViewModel,
    selectedFiles: List<Uri>,
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
    val config by viewModel.pptToPdfConfig.collectAsState()
    val pptPreviewPdfUri by viewModel.pptPreviewPdfUri.collectAsState()
    val pptPreviewProgress by viewModel.pptPreviewProgress.collectAsState()

    val fileSize = remember(selectedFile) {
        selectedFile?.let { getFileSize(context, it) } ?: 0L
    }
    val fileName = remember(selectedFile) {
        selectedFile?.let { getFileNameFromUri(context, it) } ?: ""
    }

    // Load slide count and preview texts
    var slideCount by remember { mutableStateOf<Int?>(null) }
    var slidePreviewTexts by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(selectedFile) {
        selectedFile?.let { uri ->
            slideCount = viewModel.getSlideCount(context, uri)
            slidePreviewTexts = viewModel.getSlidePreviewTexts(context, uri)
            viewModel.preparePptPreview(context, uri)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Section Label
                    Text(
                        text = "Selected File",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 1. File Verification Card
                    selectedFile?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // PowerPoint Orange Icon
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(accentColor.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Slideshow,
                                        contentDescription = "Presentation",
                                        tint = accentColor,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = fileName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = formatFileSize(fileSize),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "•",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = accentColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Presentation",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor
                                        )
                                        slideCount?.let { count ->
                                            Text(
                                                text = "•",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "$count Slides",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Guidance Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = containerColor.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
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
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Convert PowerPoint slides to a high-fidelity PDF. Adjust slide range, handout layout, and quality for optimal results.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 3. Slide Range Section
                    Text(
                        text = "SLIDE RANGE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // All Slides Card
                    val isAllSlides = config.slideRange == "all"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.pptToPdfConfig.value = config.copy(slideRange = "all", selectedSlides = emptySet(), customRange = "")
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAllSlides) accentColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(
                            if (isAllSlides) 2.dp else 1.dp,
                            if (isAllSlides) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "All Slides",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = slideCount?.let { "Convert all $it slides" } ?: "Convert every slide in the presentation",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isAllSlides) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // Custom Range Card (expandable)
                    val isCustomRange = config.slideRange == "custom"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.pptToPdfConfig.value = config.copy(slideRange = "custom")
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCustomRange) accentColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(
                            if (isCustomRange) 2.dp else 1.dp,
                            if (isCustomRange) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Custom Range",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (config.selectedSlides.isNotEmpty()) "${config.selectedSlides.size} slides selected" else "Select specific slides to convert",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isCustomRange) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = accentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Expandable content
                            AnimatedVisibility(
                                visible = isCustomRange,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Text field for manual range input
                                    OutlinedTextField(
                                        value = config.customRange,
                                        onValueChange = { newValue ->
                                            // Parse custom range to selected slides
                                            val parsed = parseCustomSlideRange(newValue, slideCount ?: 0)
                                            viewModel.pptToPdfConfig.value = config.copy(
                                                customRange = newValue,
                                                selectedSlides = parsed
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = {
                                            Text("e.g. 1-5, 8, 10-12")
                                        },
                                        label = { Text("Slide Range") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = accentColor,
                                            cursorColor = accentColor,
                                            focusedLabelColor = accentColor
                                        ),
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Filled.Edit,
                                                contentDescription = "Edit",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    )

                                    // Visual Slide Selection Grid
                                    slideCount?.let { totalSlides ->
                                        val progressVal = pptPreviewProgress
                                        if (pptPreviewPdfUri == null && progressVal != null) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 12.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Generating preview...",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = "${(progressVal * 100).toInt()}%",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = accentColor
                                                    )
                                                }
                                                LinearProgressIndicator(
                                                    progress = { progressVal },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(4.dp)
                                                        .clip(RoundedCornerShape(2.dp)),
                                                    color = accentColor,
                                                    trackColor = accentColor.copy(alpha = 0.15f)
                                                )
                                            }
                                        }
                                        if (totalSlides > 0) {
                                            Text(
                                                text = "Tap slides to select",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            // Quick actions row
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        val allSlideSet = (1..totalSlides).toSet()
                                                        val rangeText = "1-$totalSlides"
                                                        viewModel.pptToPdfConfig.value = config.copy(
                                                            selectedSlides = allSlideSet,
                                                            customRange = rangeText
                                                        )
                                                    },
                                                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                                                    shape = RoundedCornerShape(20.dp),
                                                    modifier = Modifier.height(32.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                                ) {
                                                    Text(
                                                        text = "Select All",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = accentColor,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        viewModel.pptToPdfConfig.value = config.copy(
                                                            selectedSlides = emptySet(),
                                                            customRange = ""
                                                        )
                                                    },
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                                    shape = RoundedCornerShape(20.dp),
                                                    modifier = Modifier.height(32.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                                ) {
                                                    Text(
                                                        text = "Clear",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            // 3-column Slide Grid with text previews
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            ) {
                                                val rows = (totalSlides + 2) / 3
                                                for (row in 0 until rows) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        for (col in 0 until 3) {
                                                            val slideNum = row * 3 + col + 1
                                                            if (slideNum <= totalSlides) {
                                                                val isSelected = slideNum in config.selectedSlides
                                                                val previewText = slidePreviewTexts.getOrNull(slideNum - 1) ?: ""
                                                                Card(
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                        .height(90.dp)
                                                                        .padding(bottom = 6.dp)
                                                                        .clickable {
                                                                            val newSet = if (isSelected) {
                                                                                config.selectedSlides - slideNum
                                                                            } else {
                                                                                config.selectedSlides + slideNum
                                                                            }
                                                                            val newRange = buildRangeString(newSet)
                                                                            viewModel.pptToPdfConfig.value = config.copy(
                                                                                selectedSlides = newSet,
                                                                                customRange = newRange
                                                                            )
                                                                        },
                                                                    shape = RoundedCornerShape(10.dp),
                                                                    colors = CardDefaults.cardColors(
                                                                        containerColor = if (isSelected)
                                                                            accentColor.copy(alpha = 0.15f)
                                                                        else
                                                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                                                    ),
                                                                    border = BorderStroke(
                                                                        if (isSelected) 2.dp else 1.dp,
                                                                        if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                                                    )
                                                                ) {
                                                                    Box(
                                                                        modifier = Modifier.fillMaxSize()
                                                                    ) {
                                                                        // Slide content preview area
                                                                        val previewPdfUri = pptPreviewPdfUri
                                                                        if (previewPdfUri != null) {
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .fillMaxSize()
                                                                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                                                                                    .padding(bottom = 18.dp)
                                                                                    .clip(RoundedCornerShape(6.dp))
                                                                                    .background(Color.White),
                                                                                contentAlignment = Alignment.Center
                                                                            ) {
                                                                                PdfPagePreview(
                                                                                    uri = previewPdfUri,
                                                                                    pageIndex = slideNum - 1,
                                                                                    loadThumbnail = { u, idx, w ->
                                                                                        viewModel.renderPage(context, u, idx, w)
                                                                                    },
                                                                                    modifier = Modifier.fillMaxSize()
                                                                                )
                                                                            }
                                                                        } else {
                                                                            Column(
                                                                                modifier = Modifier
                                                                                    .fillMaxSize()
                                                                                    .padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 20.dp)
                                                                            ) {
                                                                                if (previewText.isNotBlank()) {
                                                                                    // Show first line as bold title
                                                                                    val lines = previewText.split("\n")
                                                                                    Text(
                                                                                        text = lines.first(),
                                                                                        style = MaterialTheme.typography.labelSmall,
                                                                                        fontWeight = FontWeight.Bold,
                                                                                        color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface,
                                                                                        maxLines = 1,
                                                                                        overflow = TextOverflow.Ellipsis,
                                                                                        fontSize = androidx.compose.ui.unit.TextUnit(8f, androidx.compose.ui.unit.TextUnitType.Sp)
                                                                                    )
                                                                                    // Show remaining lines as body text
                                                                                    if (lines.size > 1) {
                                                                                        Spacer(modifier = Modifier.height(2.dp))
                                                                                        Text(
                                                                                            text = lines.drop(1).joinToString(" "),
                                                                                            style = MaterialTheme.typography.labelSmall,
                                                                                            color = if (isSelected) accentColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                                                            maxLines = 2,
                                                                                            overflow = TextOverflow.Ellipsis,
                                                                                            fontSize = androidx.compose.ui.unit.TextUnit(7f, androidx.compose.ui.unit.TextUnitType.Sp),
                                                                                            lineHeight = androidx.compose.ui.unit.TextUnit(9f, androidx.compose.ui.unit.TextUnitType.Sp)
                                                                                        )
                                                                                    }
                                                                                } else {
                                                                                    // Empty slide — show loading circle
                                                                                    Box(
                                                                                        modifier = Modifier.fillMaxSize(),
                                                                                        contentAlignment = Alignment.Center
                                                                                    ) {
                                                                                        CircularProgressIndicator(
                                                                                            modifier = Modifier.size(16.dp),
                                                                                            strokeWidth = 2.dp,
                                                                                            color = accentColor
                                                                                        )
                                                                                    }
                                                                                }
                                                                            }
                                                                        }

                                                                        // Slide number badge bottom-left
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .align(Alignment.BottomStart)
                                                                                .padding(start = 8.dp, bottom = 4.dp)
                                                                        ) {
                                                                            Text(
                                                                                text = "$slideNum",
                                                                                style = MaterialTheme.typography.labelSmall,
                                                                                fontWeight = FontWeight.Bold,
                                                                                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                                                                            )
                                                                        }

                                                                        // Check badge top-right
                                                                        if (isSelected) {
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .align(Alignment.TopEnd)
                                                                                    .padding(4.dp)
                                                                                    .size(18.dp)
                                                                                    .clip(CircleShape)
                                                                                    .background(accentColor),
                                                                                contentAlignment = Alignment.Center
                                                                            ) {
                                                                                Icon(
                                                                                    imageVector = Icons.Filled.Check,
                                                                                    contentDescription = "Selected",
                                                                                    tint = Color.White,
                                                                                    modifier = Modifier.size(12.dp)
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            } else {
                                                                Spacer(modifier = Modifier.weight(1f))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Slides Per Page Section
                    Text(
                        text = "SLIDES PER PAGE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 2, 4).forEach { perPage ->
                            val isSelected = config.slidesPerPage == perPage
                            val cardBg = if (isSelected) accentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            val borderCol = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            val textCol = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(cardBg)
                                    .clickable {
                                        viewModel.pptToPdfConfig.value = config.copy(slidesPerPage = perPage)
                                    },
                                border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderCol),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "$perPage",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = textCol
                                    )
                                    Text(
                                        text = if (perPage == 1) "slide" else "slides",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textCol.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    // 5. Professional Options Header
                    Text(
                        text = "PROFESSIONAL OPTIONS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Include Speaker Notes Toggle
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.pptToPdfConfig.value = config.copy(includeNotes = !config.includeNotes)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (config.includeNotes) accentColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (config.includeNotes) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Include Speaker Notes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Append presentation notes below each slide in the exported PDF.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = config.includeNotes,
                                onCheckedChange = { value ->
                                    viewModel.pptToPdfConfig.value = config.copy(includeNotes = value)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = accentColor,
                                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }

                    // Quality Segmented Row
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Output Quality",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("low" to "Low", "medium" to "Medium", "high" to "High").forEach { (tierValue, tierName) ->
                                val isSelected = config.quality == tierValue
                                val cardBg = if (isSelected) accentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                val borderCol = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                val textCol = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(cardBg)
                                        .clickable {
                                            viewModel.pptToPdfConfig.value = config.copy(quality = tierValue)
                                        },
                                    border = BorderStroke(1.dp, borderCol),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tierName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = textCol
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Sticky Footer Action Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.process(tool.id, context) },
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PictureAsPdf,
                                    contentDescription = "Convert",
                                    tint = Color.White
                                )
                                Text(
                                    text = "Convert to PDF",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Processing Dialog Overlay ---
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.width(280.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp, horizontal = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(52.dp),
                            color = accentColor,
                            strokeWidth = 4.dp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Converting Presentation...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Rendering slides and building PDF layout",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        androidx.compose.material3.LinearProgressIndicator(
                            color = accentColor,
                            trackColor = accentColor.copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}

/**
 * Builds a compact range string (e.g. "1-3, 5, 7-9") from a set of selected slide numbers.
 */
private fun buildRangeString(slides: Set<Int>): String {
    if (slides.isEmpty()) return ""
    val sorted = slides.sorted()
    val ranges = mutableListOf<String>()
    var start = sorted[0]
    var end = sorted[0]
    for (i in 1 until sorted.size) {
        if (sorted[i] == end + 1) {
            end = sorted[i]
        } else {
            ranges.add(if (start == end) "$start" else "$start-$end")
            start = sorted[i]
            end = sorted[i]
        }
    }
    ranges.add(if (start == end) "$start" else "$start-$end")
    return ranges.joinToString(", ")
}

/**
 * Parses a custom slide range string into a set of 1-indexed slide numbers.
 */
private fun parseCustomSlideRange(range: String, totalSlides: Int): Set<Int> {
    if (totalSlides <= 0 || range.isBlank()) return emptySet()
    val result = mutableSetOf<Int>()
    val parts = range.split(",").map { it.trim() }
    for (part in parts) {
        if (part.contains("-")) {
            val bounds = part.split("-").map { it.trim().toIntOrNull() }
            if (bounds.size == 2 && bounds[0] != null && bounds[1] != null) {
                val start = bounds[0]!!.coerceIn(1, totalSlides)
                val end = bounds[1]!!.coerceIn(1, totalSlides)
                result.addAll(start..end)
            }
        } else {
            part.toIntOrNull()?.let { num ->
                if (num in 1..totalSlides) result.add(num)
            }
        }
    }
    return result
}

private fun getFileSize(context: Context, uri: Uri): Long {
    var size = 0L
    try {
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (index >= 0) {
                        size = cursor.getLong(index)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (size == 0L) {
            val path = uri.path
            if (path != null) {
                val file = java.io.File(path)
                if (file.exists()) {
                    size = file.length()
                }
            }
        }
    } catch (e: Exception) {
        // Fallback
    }
    return size
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = listOf("B", "KB", "MB", "GB")
    var digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    if (digitGroups >= units.size) digitGroups = units.size - 1
    return String.format(java.util.Locale.US, "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

@Composable
fun PdfToImageSurgicalScreen(
    tool: PdfTool,
    viewModel: ToolViewModel,
    selectedFiles: List<Uri>,
    isProcessing: Boolean,
    isComplete: Boolean,
    outputUris: List<Uri>,
    progress: Float?,
    innerPadding: PaddingValues,
    accentColor: Color,
    containerColor: Color
) {
    val context = LocalContext.current
    val config by viewModel.pdfToImageConfig.collectAsState()
    val selectedFile = selectedFiles.firstOrNull()

    LaunchedEffect(selectedFile) {
        selectedFile?.let { viewModel.loadPageCount(context, it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Scrollable Content using LazyColumn
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    item {
                        // Render the high-fidelity configuration cards:
                        // file card, output format, quality (if jpg/webp), DPI chips, page selection
                        PdfToImageToolConfig(viewModel = viewModel, accentColor = accentColor)
                    }
                }

                // Sticky Footer Action Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        if (isProcessing) {
                            Button(
                                onClick = { },
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor.copy(alpha = 0.4f),
                                    contentColor = Color.White.copy(alpha = 0.6f),
                                    disabledContainerColor = accentColor.copy(alpha = 0.4f),
                                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Converting...",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.process(tool.id, context) },
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
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Autorenew,
                                        contentDescription = "Convert",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    val formatDisplay = when (config.format.lowercase()) {
                                        "jpg" -> "JPG"
                                        "png" -> "PNG"
                                        "webp" -> "WebP"
                                        else -> config.format.uppercase()
                                    }
                                    
                                    Text(
                                        text = "Convert to $formatDisplay",
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

        // --- Processing Dialog Overlay ---
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.width(280.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp, horizontal = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(52.dp),
                            color = accentColor,
                            strokeWidth = 4.dp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Converting PDF...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Rendering pages to images",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        androidx.compose.material3.LinearProgressIndicator(
                            color = accentColor,
                            trackColor = accentColor.copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfToPptSurgicalScreen(
    tool: PdfTool,
    viewModel: ToolViewModel,
    selectedFiles: List<Uri>,
    isProcessing: Boolean,
    isComplete: Boolean,
    outputUris: List<Uri>,
    progress: Float?,
    innerPadding: PaddingValues,
    accentColor: Color,
    containerColor: Color
) {
    val context = LocalContext.current
    val config by viewModel.pdfToPptConfig.collectAsState()
    val selectedFile = selectedFiles.firstOrNull()

    LaunchedEffect(selectedFile) {
        selectedFile?.let { viewModel.loadPageCount(context, it) }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty() && viewModel.currentToolId == tool.id) {
            viewModel.updateSelectedFiles(uris)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    item {
                        PdfToPptToolConfig(
                            viewModel = viewModel,
                            accentColor = accentColor,
                            onPickFile = {
                                filePickerLauncher.launch(arrayOf("application/pdf"))
                            }
                        )
                    }
                }

                // Sticky Footer Action Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        if (isProcessing) {
                            Button(
                                onClick = { },
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor.copy(alpha = 0.4f),
                                    contentColor = Color.White.copy(alpha = 0.6f),
                                    disabledContainerColor = accentColor.copy(alpha = 0.4f),
                                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Converting...",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.process(tool.id, context) },
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
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = "Convert",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    val formatDisplay = config.exportFormat.uppercase()
                                    
                                    Text(
                                        text = "Convert to $formatDisplay",
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

        // --- Processing Dialog Overlay ---
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.width(280.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp, horizontal = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(52.dp),
                            color = accentColor,
                            strokeWidth = 4.dp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Converting PDF...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Generating PowerPoint slides",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        androidx.compose.material3.LinearProgressIndicator(
                            color = accentColor,
                            trackColor = accentColor.copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfToPdfaSurgicalScreen(
    tool: PdfTool,
    viewModel: ToolViewModel,
    selectedFiles: List<Uri>,
    isProcessing: Boolean,
    isComplete: Boolean,
    outputUris: List<Uri>,
    progress: Float?,
    innerPadding: PaddingValues,
    accentColor: Color,
    containerColor: Color
) {
    val context = LocalContext.current
    val config by viewModel.pdfaConfig.collectAsState()
    val selectedFile = selectedFiles.firstOrNull()

    LaunchedEffect(selectedFile) {
        selectedFile?.let { viewModel.loadPageCount(context, it) }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty() && viewModel.currentToolId == tool.id) {
            viewModel.updateSelectedFiles(uris)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    item {
                        PdfaToolConfig(
                            viewModel = viewModel,
                            accentColor = accentColor,
                            onPickFile = {
                                filePickerLauncher.launch(arrayOf("application/pdf"))
                            }
                        )
                    }
                }

                // Sticky Footer Action Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        if (isProcessing) {
                            Button(
                                onClick = { },
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor.copy(alpha = 0.4f),
                                    contentColor = Color.White.copy(alpha = 0.6f),
                                    disabledContainerColor = accentColor.copy(alpha = 0.4f),
                                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Converting...",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.process(tool.id, context) },
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
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Verified,
                                        contentDescription = "Convert",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.tool_pdfa_convert_action),
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

        // --- Processing Dialog Overlay ---
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.width(280.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp, horizontal = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(52.dp),
                            color = accentColor,
                            strokeWidth = 4.dp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Converting PDF...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Injecting archival standards",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        androidx.compose.material3.LinearProgressIndicator(
                            color = accentColor,
                            trackColor = accentColor.copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfToExcelSurgicalScreen(
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
    val excelGreen = accentColor
    val panelBackground = MaterialTheme.colorScheme.surfaceContainer
    val selectedBackground = accentColor.copy(alpha = 0.15f)

    val fileSize = remember(selectedFile) {
        selectedFile?.let { getFileSize(context, it) } ?: 0L
    }
    val fileName = remember(selectedFile) {
        selectedFile?.let { getFileNameFromUri(context, it) } ?: "Quarterly_Report.pdf"
    }

    LaunchedEffect(selectedFile) {
        selectedFile?.let { viewModel.loadPageCount(context, it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Selected File",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    selectedFile?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PictureAsPdf,
                                        contentDescription = "PDF Document",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = fileName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = buildString {
                                            append(formatFileSize(fileSize))
                                            append(" - PDF Document")
                                            pageCount?.takeIf { it > 0 }?.let { append(" - $it pages") }
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.removeFile(0) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Remove file",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Spreadsheet configuration info card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(selectedBackground),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.GridView,
                                        contentDescription = "Column alignment",
                                        tint = excelGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = "Column Alignment",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "Columns are auto-detected using tab and whitespace heuristics. Tabular data is split into separate cells so each value lands in its own column without manual adjustment.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(panelBackground, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.GridView,
                                    contentDescription = null,
                                    tint = excelGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Tab + space detection",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Mode guidance card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = selectedBackground),
                        border = BorderStroke(1.dp, excelGreen.copy(alpha = 0.25f))
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
                                contentDescription = "Mode guidance",
                                tint = excelGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Structured tabular forms and financial spreadsheets are converted into separate sheets, one per page. Each table is preserved with its header row, so you can sort, filter, and recalculate immediately in Excel.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.process(tool.id, context) },
                            enabled = !isProcessing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = excelGreen,
                                contentColor = Color.White
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.GridView,
                                    contentDescription = "Convert to Excel",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Convert to Excel",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.width(280.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 34.dp, horizontal = 26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(52.dp),
                            color = excelGreen,
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(22.dp))
                        Text(
                            text = "Converting to Excel...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Detecting columns and rebuilding sheets",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(22.dp))
                        if (progress != null) {
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { progress },
                                color = excelGreen,
                                trackColor = excelGreen.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        } else {
                            androidx.compose.material3.LinearProgressIndicator(
                                color = excelGreen,
                                trackColor = excelGreen.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}
