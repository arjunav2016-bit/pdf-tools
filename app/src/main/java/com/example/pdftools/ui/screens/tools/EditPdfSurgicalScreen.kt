package com.example.pdftools.ui.screens.tools

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pdftools.data.PdfTool
import com.example.pdftools.data.TextAnnotation
import com.example.pdftools.data.ImageAnnotation
import com.example.pdftools.ui.screens.SuccessCard
import com.example.pdftools.ui.screens.getFileNameFromUri
import com.example.pdftools.ui.screens.getFileSizeFromUri
import com.example.pdftools.ui.viewmodels.ToolViewModel
import com.example.pdftools.ui.viewmodels.EditConfig
import java.util.UUID
import kotlin.math.roundToInt

enum class EditToolStep {
    DASHBOARD,
    TEXT_TOOL,
    OBJECTS_TOOL,
    MARKUP_TOOL
}

data class TextElement(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val x: Float, // percentage (0f..1f)
    val y: Float, // percentage (0f..1f)
    val fontSize: Float = 16f,
    val colorHex: String = "#2C3E50",
    val alignment: String = "left", // "left", "center", "right"
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val fontFamily: String = "Sans Serif",
    val pageIndex: Int
)

data class ObjectElement(
    val id: String = UUID.randomUUID().toString(),
    val type: String, // "image", "rectangle", "circle"
    val x: Float,
    val y: Float,
    val width: Float = 0.3f,
    val height: Float = 0.15f,
    val rotation: Float = 0f,
    val opacity: Float = 1.0f,
    val imageUri: Uri? = null,
    val fillColorHex: String = "#3498DB",
    val strokeColorHex: String = "#2980B9",
    val strokeWidth: Float = 2f,
    val pageIndex: Int
)

data class MarkupStroke(
    val id: String = UUID.randomUUID().toString(),
    val points: List<Offset>,
    val colorHex: String,
    val width: Float,
    val isHighlighter: Boolean = false,
    val pageIndex: Int
)

data class StickyNote(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val x: Float,
    val y: Float,
    val colorHex: String = "#F1C40F",
    val pageIndex: Int
)

data class HistoryState(
    val textElements: List<TextElement>,
    val objectElements: List<ObjectElement>,
    val markupStrokes: List<MarkupStroke>,
    val stickyNotes: List<StickyNote>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPdfSurgicalScreen(
    tool: PdfTool,
    viewModel: ToolViewModel,
    selectedFiles: List<Uri>,
    isProcessing: Boolean,
    isComplete: Boolean,
    outputUris: List<Uri>,
    progress: Float?,
    innerPadding: PaddingValues,
    accentColor: Color,
    containerColor: Color,
    onPickFiles: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(EditToolStep.DASHBOARD) }
    var activePageIndex by remember { mutableStateOf(0) }
    var pageCount by remember { mutableStateOf(1) }

    // Core annotation lists
    val textElements = remember { mutableStateListOf<TextElement>() }
    val objectElements = remember { mutableStateListOf<ObjectElement>() }
    val markupStrokes = remember { mutableStateListOf<MarkupStroke>() }
    val stickyNotes = remember { mutableStateListOf<StickyNote>() }

    // Undo/Redo Stacks
    val undoStack = remember { mutableStateListOf<HistoryState>() }
    val redoStack = remember { mutableStateListOf<HistoryState>() }

    fun captureState(): HistoryState {
        return HistoryState(
            textElements = textElements.toList(),
            objectElements = objectElements.toList(),
            markupStrokes = markupStrokes.toList(),
            stickyNotes = stickyNotes.toList()
        )
    }

    fun pushHistory() {
        undoStack.add(captureState())
        redoStack.clear()
    }

    fun restoreState(state: HistoryState) {
        textElements.clear()
        textElements.addAll(state.textElements)
        objectElements.clear()
        objectElements.addAll(state.objectElements)
        markupStrokes.clear()
        markupStrokes.addAll(state.markupStrokes)
        stickyNotes.clear()
        stickyNotes.addAll(state.stickyNotes)
    }

    fun handleUndo() {
        if (undoStack.isNotEmpty()) {
            val currentState = captureState()
            redoStack.add(currentState)
            val previousState = undoStack.removeAt(undoStack.lastIndex)
            restoreState(previousState)
        }
    }

    fun handleRedo() {
        if (redoStack.isNotEmpty()) {
            val currentState = captureState()
            undoStack.add(currentState)
            val nextState = redoStack.removeAt(redoStack.lastIndex)
            restoreState(nextState)
        }
    }

    // Auto-load metadata if file picked
    LaunchedEffect(selectedFiles) {
        if (selectedFiles.isNotEmpty()) {
            try {
                pageCount = viewModel.getPageCountSuspend(context, selectedFiles.first())
            } catch (e: Exception) {
                pageCount = 3 // Fallback for mock / sample
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
                    onClear = {
                        viewModel.resetCurrentRun()
                        textElements.clear()
                        objectElements.clear()
                        markupStrokes.clear()
                        stickyNotes.clear()
                        undoStack.clear()
                        redoStack.clear()
                        activePageIndex = 0
                        currentStep = EditToolStep.DASHBOARD
                    },
                    accentColor = accentColor,
                    containerColor = containerColor
                )
            }
        } else {
            when (currentStep) {
                EditToolStep.DASHBOARD -> {
                    EditDashboard(
                        selectedFile = selectedFiles.firstOrNull(),
                        pageCount = pageCount,
                        activePageIndex = activePageIndex,
                        accentColor = accentColor,
                        containerColor = containerColor,
                        onPickFile = onPickFiles,
                        onLoadSample = {
                            viewModel.updateSelectedFiles(listOf(Uri.parse("android.resource://com.example.pdftools/raw/sample")))
                            pageCount = 3
                        },
                        onPrevPage = { activePageIndex = (activePageIndex - 1).coerceAtLeast(0) },
                        onNextPage = { activePageIndex = (activePageIndex + 1).coerceAtMost(pageCount - 1) },
                        onNavigateToTool = { currentStep = it },
                        onExport = {
                            // Map to viewmodel annotations
                            val mappedText = textElements.map {
                                TextAnnotation(
                                    text = it.text,
                                    x = it.x,
                                    y = it.y,
                                    colorHex = it.colorHex,
                                    fontSize = it.fontSize,
                                    pageIndex = it.pageIndex
                                )
                            }
                            // Compile sticky notes as styled text annotations in the output file
                            val notesText = stickyNotes.map {
                                TextAnnotation(
                                    text = "[Note: ${it.text}]",
                                    x = it.x,
                                    y = it.y,
                                    colorHex = it.colorHex,
                                    fontSize = 12f,
                                    pageIndex = it.pageIndex
                                )
                            }
                            val mappedImg = objectElements.map {
                                ImageAnnotation(
                                    imageUri = it.imageUri?.toString() ?: "",
                                    x = it.x,
                                    y = it.y,
                                    width = it.width,
                                    height = it.height,
                                    pageIndex = it.pageIndex
                                )
                            }
                            viewModel.editConfig.value = EditConfig(
                                textAnnotations = mappedText + notesText,
                                imageAnnotations = mappedImg
                            )
                            viewModel.process("edit_pdf", context)
                        }
                    )
                }
                EditToolStep.TEXT_TOOL -> {
                    TextToolView(
                        activePageIndex = activePageIndex,
                        textElements = textElements,
                        accentColor = accentColor,
                        onBack = { currentStep = EditToolStep.DASHBOARD },
                        onPushHistory = { pushHistory() },
                        undoAvailable = undoStack.isNotEmpty(),
                        redoAvailable = redoStack.isNotEmpty(),
                        onUndo = { handleUndo() },
                        onRedo = { handleRedo() }
                    )
                }
                EditToolStep.OBJECTS_TOOL -> {
                    ObjectsToolView(
                        activePageIndex = activePageIndex,
                        objectElements = objectElements,
                        accentColor = accentColor,
                        onBack = { currentStep = EditToolStep.DASHBOARD },
                        onPushHistory = { pushHistory() },
                        undoAvailable = undoStack.isNotEmpty(),
                        redoAvailable = redoStack.isNotEmpty(),
                        onUndo = { handleUndo() },
                        onRedo = { handleRedo() }
                    )
                }
                EditToolStep.MARKUP_TOOL -> {
                    MarkupToolView(
                        activePageIndex = activePageIndex,
                        markupStrokes = markupStrokes,
                        stickyNotes = stickyNotes,
                        accentColor = accentColor,
                        onBack = { currentStep = EditToolStep.DASHBOARD },
                        onPushHistory = { pushHistory() },
                        undoAvailable = undoStack.isNotEmpty(),
                        redoAvailable = redoStack.isNotEmpty(),
                        onUndo = { handleUndo() },
                        onRedo = { handleRedo() }
                    )
                }
            }
        }
    }
}

@Composable
fun EditDashboard(
    selectedFile: Uri?,
    pageCount: Int,
    activePageIndex: Int,
    accentColor: Color,
    containerColor: Color,
    onPickFile: () -> Unit,
    onLoadSample: () -> Unit,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    onNavigateToTool: (EditToolStep) -> Unit,
    onExport: () -> Unit
) {
    val context = LocalContext.current

    if (selectedFile == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Edit PDF Suite",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Stitch annotations, modify text layers, insert media elements, and sketch markups offline.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = onPickFile,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Icon(Icons.Filled.FolderOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open PDF Document", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onLoadSample,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
                        border = BorderStroke(1.dp, accentColor)
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Load Sample Template", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Recent Items list
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Recent Documents",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                listOf(
                    "Tax_Invoice_May.pdf" to "Modified 2 hours ago",
                    "Service_Agreement_Final.pdf" to "Modified Yesterday"
                ).forEach { (name, date) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLoadSample() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null, tint = accentColor)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Document loaded layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // File Context Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.PictureAsPdf,
                        contentDescription = null,
                        tint = Color(0xFFC0392B),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = getFileNameFromUri(context, selectedFile),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = getFileSizeFromUri(context, selectedFile),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Canvas & Navigation preview area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            ) {
                // Large visual preview represents standard background
                SimulatedDocumentLines()

                // Nav controllers inside page
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onPrevPage, enabled = activePageIndex > 0) {
                        Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Prev Page")
                    }
                    Text(
                        text = "Page ${activePageIndex + 1} of $pageCount",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onNextPage, enabled = activePageIndex < pageCount - 1) {
                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Next Page")
                    }
                }
            }

            // Specialized Editing Grid Tools
            Text(
                text = "Specialized Editing Engine Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("Text Tool", Icons.Filled.TextFields, EditToolStep.TEXT_TOOL),
                    Triple("Objects", Icons.Filled.Category, EditToolStep.OBJECTS_TOOL),
                    Triple("Markup", Icons.Filled.Gesture, EditToolStep.MARKUP_TOOL)
                ).forEach { (label, icon, step) ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToTool(step) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(icon, contentDescription = null, tint = accentColor)
                            Text(
                                label,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            // Save Action Button
            Button(
                onClick = onExport,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply & Export PDF", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TextToolView(
    activePageIndex: Int,
    textElements: MutableList<TextElement>,
    accentColor: Color,
    onBack: () -> Unit,
    onPushHistory: () -> Unit,
    undoAvailable: Boolean,
    redoAvailable: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit
) {
    val context = LocalContext.current
    var selectedTextId by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Selected or Active Properties
    val selectedElement = textElements.firstOrNull { it.id == selectedTextId }

    Column(modifier = Modifier.fillMaxSize()) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("Text Editing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onUndo, enabled = undoAvailable) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo")
                }
                IconButton(onClick = onRedo, enabled = redoAvailable) {
                    Icon(Icons.Default.Redo, contentDescription = "Redo")
                }
            }
        }

        // Visual Workspace Canvas
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .pointerInput(activePageIndex) {
                    detectTapGestures { offset ->
                        val pctX = offset.x / size.width
                        val pctY = offset.y / size.height
                        // Place new text element
                        onPushHistory()
                        val newElem = TextElement(
                            text = "New Text Block",
                            x = pctX,
                            y = pctY,
                            pageIndex = activePageIndex
                        )
                        textElements.add(newElem)
                        selectedTextId = newElem.id
                        showEditDialog = true
                    }
                }
        ) {
            val workspaceWidth = maxWidth
            val workspaceHeight = maxHeight

            SimulatedDocumentLines()

            // Render text overlays
            textElements.filter { it.pageIndex == activePageIndex }.forEach { item ->
                val left = workspaceWidth * item.x
                val top = workspaceHeight * item.y
                val isSelected = item.id == selectedTextId

                Box(
                    modifier = Modifier
                        .offset(x = left, y = top)
                        .wrapContentSize()
                        .pointerInput(item.id) {
                            detectDragGestures(
                                onDragStart = {
                                    selectedTextId = item.id
                                    onPushHistory()
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val idx = textElements.indexOfFirst { it.id == item.id }
                                    if (idx != -1) {
                                        val cur = textElements[idx]
                                        val newX = (cur.x + dragAmount.x / size.width).coerceIn(0f, 1f)
                                        val newY = (cur.y + dragAmount.y / size.height).coerceIn(0f, 1f)
                                        textElements[idx] = cur.copy(x = newX, y = newY)
                                    }
                                }
                            )
                        }
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(Color.White.copy(alpha = 0.8f))
                        .clickable {
                            selectedTextId = item.id
                            showEditDialog = true
                        }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = item.text,
                        fontSize = item.fontSize.sp,
                        color = Color(android.graphics.Color.parseColor(item.colorHex)),
                        fontWeight = if (item.isBold) FontWeight.Bold else FontWeight.Normal,
                        textAlign = when (item.alignment) {
                            "center" -> TextAlign.Center
                            "right" -> TextAlign.Right
                            else -> TextAlign.Left
                        }
                    )
                }
            }
        }

        // Contextual Properties Bar
        if (selectedElement != null) {
            Surface(
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
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
                        Text("Text Properties", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = {
                            onPushHistory()
                            textElements.remove(selectedElement)
                            selectedTextId = null
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete text element", tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    // Properties Adjustment Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Font family selector
                        var fontMenuExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { fontMenuExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(selectedElement.fontFamily)
                            }
                            DropdownMenu(
                                expanded = fontMenuExpanded,
                                onDismissRequest = { fontMenuExpanded = false }
                            ) {
                                listOf("Sans Serif", "Serif", "Monospace").forEach { font ->
                                    DropdownMenuItem(
                                        text = { Text(font) },
                                        onClick = {
                                            onPushHistory()
                                            val idx = textElements.indexOfFirst { it.id == selectedElement.id }
                                            if (idx != -1) {
                                                textElements[idx] = selectedElement.copy(fontFamily = font)
                                            }
                                            fontMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Alignments
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(8.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("left" to Icons.Filled.AlignHorizontalLeft, "center" to Icons.Filled.AlignHorizontalCenter, "right" to Icons.Filled.AlignHorizontalRight).forEach { (align, icon) ->
                                val isSel = selectedElement.alignment == align
                                IconButton(
                                    onClick = {
                                        onPushHistory()
                                        val idx = textElements.indexOfFirst { it.id == selectedElement.id }
                                        if (idx != -1) {
                                            textElements[idx] = selectedElement.copy(alignment = align)
                                        }
                                    },
                                    modifier = Modifier.background(if (isSel) accentColor.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(4.dp))
                                ) {
                                    Icon(icon, contentDescription = null, tint = if (isSel) accentColor else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    // Font Size Slider
                    Column {
                        Text("Font Size: ${selectedElement.fontSize.toInt()}sp", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = selectedElement.fontSize,
                            onValueChange = {
                                val idx = textElements.indexOfFirst { it.id == selectedElement.id }
                                if (idx != -1) {
                                    textElements[idx] = selectedElement.copy(fontSize = it)
                                }
                            },
                            onValueChangeFinished = { onPushHistory() },
                            valueRange = 8f..36f,
                            colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor)
                        )
                    }

                    // Bold/Italic style toggles & Color Palette
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconToggleButton(
                                checked = selectedElement.isBold,
                                onCheckedChange = {
                                    onPushHistory()
                                    val idx = textElements.indexOfFirst { it.id == selectedElement.id }
                                    if (idx != -1) {
                                        textElements[idx] = selectedElement.copy(isBold = it)
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.FormatBold, contentDescription = "Bold")
                            }

                            IconToggleButton(
                                checked = selectedElement.isItalic,
                                onCheckedChange = {
                                    onPushHistory()
                                    val idx = textElements.indexOfFirst { it.id == selectedElement.id }
                                    if (idx != -1) {
                                        textElements[idx] = selectedElement.copy(isItalic = it)
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.FormatItalic, contentDescription = "Italic")
                            }
                        }

                        // Colors Palette
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("#2C3E50", "#E74C3C", "#2980B9", "#27AE60", "#F39C12").forEach { hex ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(hex)))
                                        .border(
                                            width = if (selectedElement.colorHex == hex) 2.dp else 0.dp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            onPushHistory()
                                            val idx = textElements.indexOfFirst { it.id == selectedElement.id }
                                            if (idx != -1) {
                                                textElements[idx] = selectedElement.copy(colorHex = hex)
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Edit Dialog for text contents
    if (showEditDialog && selectedElement != null) {
        var localTextVal by remember(selectedElement.id) { mutableStateOf(selectedElement.text) }

        Dialog(onDismissRequest = { showEditDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Edit Text Content", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = localTextVal,
                        onValueChange = { localTextVal = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showEditDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                onPushHistory()
                                val idx = textElements.indexOfFirst { it.id == selectedElement.id }
                                if (idx != -1) {
                                    textElements[idx] = selectedElement.copy(text = localTextVal)
                                }
                                showEditDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ObjectsToolView(
    activePageIndex: Int,
    objectElements: MutableList<ObjectElement>,
    accentColor: Color,
    onBack: () -> Unit,
    onPushHistory: () -> Unit,
    undoAvailable: Boolean,
    redoAvailable: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit
) {
    var selectedObjId by remember { mutableStateOf<String?>(null) }
    var activeObjectType by remember { mutableStateOf("rectangle") } // "rectangle", "circle", "image"
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val objectSelected = objectElements.firstOrNull { it.id == selectedObjId }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            // auto-trigger object placement
            onPushHistory()
            val newObj = ObjectElement(
                type = "image",
                x = 0.35f,
                y = 0.35f,
                imageUri = uri,
                pageIndex = activePageIndex
            )
            objectElements.add(newObj)
            selectedObjId = newObj.id
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("Objects Tool", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onUndo, enabled = undoAvailable) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo")
                }
                IconButton(onClick = onRedo, enabled = redoAvailable) {
                    Icon(Icons.Default.Redo, contentDescription = "Redo")
                }
            }
        }

        // Visual Workspace Canvas
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .pointerInput(activePageIndex, activeObjectType) {
                    detectTapGestures { offset ->
                        val pctX = offset.x / size.width
                        val pctY = offset.y / size.height

                        if (activeObjectType == "image") {
                            photoPickerLauncher.launch(arrayOf("image/*"))
                        } else {
                            onPushHistory()
                            val newObj = ObjectElement(
                                type = activeObjectType,
                                x = pctX,
                                y = pctY,
                                pageIndex = activePageIndex
                            )
                            objectElements.add(newObj)
                            selectedObjId = newObj.id
                        }
                    }
                }
        ) {
            val workspaceWidth = maxWidth
            val workspaceHeight = maxHeight

            SimulatedDocumentLines()

            // Render visual object overlays
            objectElements.filter { it.pageIndex == activePageIndex }.forEach { item ->
                val left = workspaceWidth * item.x
                val top = workspaceHeight * item.y
                val width = workspaceWidth * item.width
                val height = workspaceHeight * item.height
                val isSelected = item.id == selectedObjId

                Box(
                    modifier = Modifier
                        .offset(x = left, y = top)
                        .size(width = width, height = height)
                        .pointerInput(item.id) {
                            detectDragGestures(
                                onDragStart = {
                                    selectedObjId = item.id
                                    onPushHistory()
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val idx = objectElements.indexOfFirst { it.id == item.id }
                                    if (idx != -1) {
                                        val cur = objectElements[idx]
                                        val newX = (cur.x + dragAmount.x / size.width).coerceIn(0f, 1f)
                                        val newY = (cur.y + dragAmount.y / size.height).coerceIn(0f, 1f)
                                        objectElements[idx] = cur.copy(x = newX, y = newY)
                                    }
                                }
                            )
                        }
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable { selectedObjId = item.id }
                ) {
                    // Draw visual preview representations
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeColor = Color(android.graphics.Color.parseColor(item.strokeColorHex))
                        val fillColor = Color(android.graphics.Color.parseColor(item.fillColorHex)).copy(alpha = item.opacity)

                        when (item.type) {
                            "rectangle" -> {
                                drawRect(color = fillColor)
                                drawRect(color = strokeColor, style = Stroke(width = item.strokeWidth))
                            }
                            "circle" -> {
                                drawCircle(color = fillColor)
                                drawCircle(color = strokeColor, style = Stroke(width = item.strokeWidth))
                            }
                            "image" -> {
                                // Draw a mock placeholder for images
                                drawRect(color = fillColor)
                                drawRect(color = strokeColor, style = Stroke(width = item.strokeWidth))
                                drawLine(color = strokeColor, start = Offset(0f, 0f), end = Offset(size.width, size.height))
                                drawLine(color = strokeColor, start = Offset(0f, size.height), end = Offset(size.width, 0f))
                            }
                        }
                    }

                    // Resizing controls overlay
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(16.dp)
                                .background(accentColor, RoundedCornerShape(topStart = 8.dp))
                                .pointerInput(item.id) {
                                    detectDragGestures(
                                        onDragStart = { onPushHistory() },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            val idx = objectElements.indexOfFirst { it.id == item.id }
                                            if (idx != -1) {
                                                val cur = objectElements[idx]
                                                val newW = (cur.width + dragAmount.x / size.width).coerceIn(0.05f, 0.9f)
                                                val newH = (cur.height + dragAmount.y / size.height).coerceIn(0.05f, 0.9f)
                                                objectElements[idx] = cur.copy(width = newW, height = newH)
                                            }
                                        }
                                    )
                                }
                        )
                    }
                }
            }

            // Object type selector capsule
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(24.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    listOf(
                        "rectangle" to "Box",
                        "circle" to "Oval",
                        "image" to "Photo"
                    ).forEach { (type, label) ->
                        val isSel = activeObjectType == type
                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSel) accentColor else Color.Transparent)
                                .clickable { activeObjectType = type }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Contextual Properties sheet
        if (objectSelected != null) {
            Surface(
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
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
                        Text("Object Settings", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = {
                            onPushHistory()
                            objectElements.remove(objectSelected)
                            selectedObjId = null
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    // Opacity Slider
                    Column {
                        Text("Opacity: ${(objectSelected.opacity * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = objectSelected.opacity,
                            onValueChange = {
                                val idx = objectElements.indexOfFirst { it.id == objectSelected.id }
                                if (idx != -1) {
                                    objectElements[idx] = objectSelected.copy(opacity = it)
                                }
                            },
                            onValueChangeFinished = { onPushHistory() },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor)
                        )
                    }

                    // Rotation slider
                    Column {
                        Text("Rotation: ${objectSelected.rotation.toInt()}°", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = objectSelected.rotation,
                            onValueChange = {
                                val idx = objectElements.indexOfFirst { it.id == objectSelected.id }
                                if (idx != -1) {
                                    objectElements[idx] = objectSelected.copy(rotation = it)
                                }
                            },
                            onValueChangeFinished = { onPushHistory() },
                            valueRange = 0f..360f,
                            colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor)
                        )
                    }

                    // Preset Color Palettes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Fill Color", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("#3498DB", "#E74C3C", "#2ECC71", "#F1C40F", "#9B59B6").forEach { hex ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(hex)))
                                        .border(
                                            width = if (objectSelected.fillColorHex == hex) 2.dp else 0.dp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            onPushHistory()
                                            val idx = objectElements.indexOfFirst { it.id == objectSelected.id }
                                            if (idx != -1) {
                                                objectElements[idx] = objectSelected.copy(fillColorHex = hex)
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarkupToolView(
    activePageIndex: Int,
    markupStrokes: MutableList<MarkupStroke>,
    stickyNotes: MutableList<StickyNote>,
    accentColor: Color,
    onBack: () -> Unit,
    onPushHistory: () -> Unit,
    undoAvailable: Boolean,
    redoAvailable: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit
) {
    val context = LocalContext.current
    var activeToolMode by remember { mutableStateOf("pen") } // "pen", "highlighter", "eraser", "sticky_note"
    var activeColorHex by remember { mutableStateOf("#E74C3C") }
    var activeStrokeWidth by remember { mutableFloatStateOf(4f) }

    var currentPoints = remember { mutableStateListOf<Offset>() }
    var stickyNoteTextDialog by remember { mutableStateOf(false) }
    var nextStickyPosition by remember { mutableStateOf(Offset.Zero) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("Markup & Annotation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = {
                    onPushHistory()
                    markupStrokes.clear()
                    stickyNotes.clear()
                }) {
                    Icon(Icons.Default.ClearAll, contentDescription = "Clear All")
                }
                IconButton(onClick = onUndo, enabled = undoAvailable) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo")
                }
                IconButton(onClick = onRedo, enabled = redoAvailable) {
                    Icon(Icons.Default.Redo, contentDescription = "Redo")
                }
            }
        }

        // Visual Workspace Canvas
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .pointerInput(activePageIndex, activeToolMode, activeStrokeWidth, activeColorHex) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (activeToolMode == "pen" || activeToolMode == "highlighter") {
                                currentPoints.clear()
                                currentPoints.add(offset)
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (activeToolMode == "pen" || activeToolMode == "highlighter") {
                                val lastPt = currentPoints.lastOrNull() ?: Offset.Zero
                                currentPoints.add(lastPt + dragAmount)
                            }
                        },
                        onDragEnd = {
                            if ((activeToolMode == "pen" || activeToolMode == "highlighter") && currentPoints.isNotEmpty()) {
                                onPushHistory()
                                markupStrokes.add(
                                    MarkupStroke(
                                        points = currentPoints.toList(),
                                        colorHex = activeColorHex,
                                        width = activeStrokeWidth,
                                        isHighlighter = activeToolMode == "highlighter",
                                        pageIndex = activePageIndex
                                    )
                                )
                                currentPoints.clear()
                            }
                        }
                    )
                }
                .pointerInput(activePageIndex, activeToolMode) {
                    detectTapGestures { offset ->
                        if (activeToolMode == "sticky_note") {
                            nextStickyPosition = Offset(offset.x / size.width, offset.y / size.height)
                            stickyNoteTextDialog = true
                        } else if (activeToolMode == "eraser") {
                            // Erase hit-test on sketches
                            val pctX = offset.x / size.width
                            val pctY = offset.y / size.height
                            // Try to erase sticky note first
                            val noteHit = stickyNotes.firstOrNull { note ->
                                val distanceX = Math.abs((note.x * size.width) - offset.x)
                                val distanceY = Math.abs((note.y * size.height) - offset.y)
                                distanceX < 30f && distanceY < 30f
                            }
                            if (noteHit != null) {
                                onPushHistory()
                                stickyNotes.remove(noteHit)
                            } else {
                                // Try to erase markup paths
                                val strokeHit = markupStrokes.firstOrNull { stroke ->
                                    stroke.points.any { pt ->
                                        Math.abs(pt.x - offset.x) < 25f && Math.abs(pt.y - offset.y) < 25f
                                    }
                                }
                                if (strokeHit != null) {
                                    onPushHistory()
                                    markupStrokes.remove(strokeHit)
                                }
                            }
                        }
                    }
                }
        ) {
            val width = maxWidth
            val height = maxHeight

            SimulatedDocumentLines()

            // Draw sketches
            Canvas(modifier = Modifier.fillMaxSize()) {
                markupStrokes.filter { it.pageIndex == activePageIndex }.forEach { stroke ->
                    val path = Path().apply {
                        if (stroke.points.isNotEmpty()) {
                            moveTo(stroke.points[0].x, stroke.points[0].y)
                            for (i in 1 until stroke.points.size) {
                                lineTo(stroke.points[i].x, stroke.points[i].y)
                            }
                        }
                    }
                    val drawCol = Color(android.graphics.Color.parseColor(stroke.colorHex))
                        .copy(alpha = if (stroke.isHighlighter) 0.4f else 1.0f)
                    drawPath(
                        path = path,
                        color = drawCol,
                        style = Stroke(width = stroke.width)
                    )
                }

                // Render current points actively drawn
                if (currentPoints.isNotEmpty()) {
                    val activePath = Path().apply {
                        moveTo(currentPoints[0].x, currentPoints[0].y)
                        for (i in 1 until currentPoints.size) {
                            lineTo(currentPoints[i].x, currentPoints[i].y)
                        }
                    }
                    val activeColor = Color(android.graphics.Color.parseColor(activeColorHex))
                        .copy(alpha = if (activeToolMode == "highlighter") 0.4f else 1.0f)
                    drawPath(
                        path = activePath,
                        color = activeColor,
                        style = Stroke(width = activeStrokeWidth)
                    )
                }
            }

            // Render placed Sticky Notes
            stickyNotes.filter { it.pageIndex == activePageIndex }.forEach { note ->
                val left = width * note.x
                val top = height * note.y

                Box(
                    modifier = Modifier
                        .offset(x = left - 15.dp, y = top - 15.dp)
                        .size(30.dp)
                        .background(Color(android.graphics.Color.parseColor(note.colorHex)), CircleShape)
                        .border(1.dp, Color.White, CircleShape)
                        .clickable {
                            Toast.makeText(context, note.text, Toast.LENGTH_LONG).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Comment, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            // pen palette controls capsule
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(24.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    listOf(
                        "pen" to "Pen",
                        "highlighter" to "Highlight",
                        "eraser" to "Eraser",
                        "sticky_note" to "Note"
                    ).forEach { (mode, label) ->
                        val isSel = activeToolMode == mode
                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSel) accentColor else Color.Transparent)
                                .clickable { activeToolMode = mode }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Properties settings at bottom
        if (activeToolMode == "pen" || activeToolMode == "highlighter") {
            Surface(
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Stroke width
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Thickness", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Slider(
                            value = activeStrokeWidth,
                            onValueChange = { activeStrokeWidth = it },
                            valueRange = 2f..24f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor)
                        )
                    }

                    // Color presets row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Color Preset", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("#E74C3C", "#2980B9", "#2ECC71", "#F1C40F", "#9B59B6", "#1ABC9C").forEach { hex ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(hex)))
                                        .border(
                                            width = if (activeColorHex == hex) 2.dp else 0.dp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            shape = CircleShape
                                        )
                                        .clickable { activeColorHex = hex }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Sticky Note modal text dialog
    if (stickyNoteTextDialog) {
        var localNoteVal by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { stickyNoteTextDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Add Sticky Note Comment", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = localNoteVal,
                        onValueChange = { localNoteVal = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Comment text...") },
                        shape = RoundedCornerShape(8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { stickyNoteTextDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (localNoteVal.isNotEmpty()) {
                                    onPushHistory()
                                    // Map offsets relative to box bounds
                                    stickyNotes.add(
                                        StickyNote(
                                            text = localNoteVal,
                                            x = nextStickyPosition.x,
                                            y = nextStickyPosition.y,
                                            pageIndex = activePageIndex
                                        )
                                    )
                                }
                                stickyNoteTextDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimulatedDocumentLines() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineColor = Color.LightGray.copy(alpha = 0.35f)
        for (i in 1..20) {
            val lineY = i * (size.height / 22f)
            val end = if (i % 6 == 0) size.width * 0.4f else size.width * 0.85f
            drawLine(
                color = lineColor,
                start = Offset(size.width * 0.1f, lineY),
                end = Offset(end, lineY),
                strokeWidth = if (i % 6 == 0) 5f else 2f
            )
        }
    }
}
