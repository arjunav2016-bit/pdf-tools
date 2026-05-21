package com.example.pdftools.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.pdftools.data.FavoritesRepository
import com.example.pdftools.data.PdfTool
import com.example.pdftools.data.PdfProcessor
import com.example.pdftools.data.RecentFilesRepository
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import android.graphics.Bitmap
import android.graphics.BitmapFactory

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

    val selectedFiles = remember { mutableStateListOf<Uri>() }
    val outputUris = remember { mutableStateListOf<Uri>() }
    var isProcessing by remember { mutableStateOf(false) }
    var isComplete by remember { mutableStateOf(false) }
    var pageRangeInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rotationAngleInput by remember { mutableStateOf(90) }
    var watermarkTextInput by remember { mutableStateOf("CONFIDENTIAL") }
    var watermarkColorHex by remember { mutableStateOf("#7F8C8D") }
    var watermarkFontSize by remember { mutableStateOf(40f) }
    var watermarkRotation by remember { mutableStateOf(45f) }
    var watermarkOpacity by remember { mutableStateOf(0.3f) }
    var pageNumFormat by remember { mutableStateOf("prefixed") }
    var pageNumPosition by remember { mutableStateOf("bottom_center") }
    var pageNumFontSize by remember { mutableStateOf(12f) }
    var cropMarginPercentage by remember { mutableStateOf(0.10f) }
    var pdfaConformance by remember { mutableStateOf("pdfa_1b") }
    var totalPagesForOrganize by remember { mutableStateOf(0) }
    val organizePages = remember { mutableStateListOf<OrganizePageItem>() }

    // State for Sign PDF
    var signatureUri by remember { mutableStateOf<Uri?>(null) }
    var sigPageIndex by remember { mutableStateOf(0) }
    var sigX by remember { mutableStateOf(100f) }
    var sigY by remember { mutableStateOf(100f) }
    var sigWidth by remember { mutableStateOf(150f) }
    var sigHeight by remember { mutableStateOf(60f) }

    // State for Redact PDF
    var redactPageIndex by remember { mutableStateOf(0) }
    var redactX by remember { mutableStateOf(100f) }
    var redactY by remember { mutableStateOf(500f) }
    var redactWidth by remember { mutableStateOf(200f) }
    var redactHeight by remember { mutableStateOf(40f) }
    var redactText by remember { mutableStateOf("") }

    // State for PDF Forms
    val formFields = remember { mutableStateListOf<com.example.pdftools.data.FormFieldInfo>() }
    val formValues = remember { androidx.compose.runtime.mutableStateMapOf<String, String>() }
    var isFormLoaded by remember { mutableStateOf(false) }

    // Page count state for Sign & Redact page index sliders
    var totalPagesForSignRedact by remember { mutableStateOf(1) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedFiles.toList()) {
        if (selectedFiles.isEmpty()) return@LaunchedEffect

        // Organize PDF page loading
        if (tool.id == "organize_pdf") {
            organizePages.clear()
            try {
                context.contentResolver.openInputStream(selectedFiles.first())?.use { input ->
                    val tempFile = File.createTempFile("organize_page_count_", ".pdf", context.cacheDir)
                    try {
                        tempFile.outputStream().use { output -> input.copyTo(output) }
                        com.tom_roush.pdfbox.pdmodel.PDDocument.load(tempFile).use { doc ->
                            totalPagesForOrganize = doc.numberOfPages
                            for (i in 0 until doc.numberOfPages) {
                                organizePages.add(
                                    OrganizePageItem(
                                        id = java.util.UUID.randomUUID().toString(),
                                        originalIndex = i,
                                        rotation = 0
                                    )
                                )
                            }
                        }
                    } finally {
                        tempFile.delete()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Sign PDF & Redact PDF page count loading
        if (tool.id == "sign_pdf" || tool.id == "redact_pdf" || tool.id == "pdf_forms") {
            try {
                context.contentResolver.openInputStream(selectedFiles.first())?.use { input ->
                    val tempFile = File.createTempFile("page_count_check_", ".pdf", context.cacheDir)
                    try {
                        tempFile.outputStream().use { output -> input.copyTo(output) }
                        com.tom_roush.pdfbox.pdmodel.PDDocument.load(tempFile).use { doc ->
                            totalPagesForSignRedact = doc.numberOfPages
                        }
                    } finally {
                        tempFile.delete()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // PDF Forms fields loading
        if (tool.id == "pdf_forms") {
            isFormLoaded = false
            formFields.clear()
            formValues.clear()
            try {
                val fields = PdfProcessor.getFormFields(context, selectedFiles.first())
                formFields.addAll(fields)
                fields.forEach { field ->
                    formValues[field.name] = field.value
                }
                isFormLoaded = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedFiles.addAll(uris)
            isComplete = false
            outputUris.clear()
        }
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
                    val isFav = FavoritesRepository.isFavorite(tool.id)
                    IconButton(onClick = { FavoritesRepository.toggleFavorite(context, tool.id) }) {
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
                if (isComplete && outputUris.isNotEmpty()) {
                    SuccessCard(
                        tool = tool,
                        outputUris = outputUris.toList(),
                        onClear = {
                            selectedFiles.clear()
                            outputUris.clear()
                            isComplete = false
                            pageRangeInput = ""
                            passwordInput = ""
                            passwordVisible = false
                        },
                        accentColor = accentColor,
                        containerColor = containerColor
                    )
                } else {
                    FilePickerZone(
                        accentColor = accentColor,
                        onPickFiles = {
                            val mimeTypes = if (tool.id == "jpg_to_pdf") {
                                arrayOf("image/jpeg", "image/png", "image/webp")
                            } else {
                                arrayOf("application/pdf")
                            }
                            filePickerLauncher.launch(mimeTypes)
                        }
                    )
                }
            }

            // Selected files (only show when not complete or processing)
            if (selectedFiles.isNotEmpty() && !isComplete) {
                item {
                    Text(
                        text = "Selected Files (${selectedFiles.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                itemsIndexed(selectedFiles.toList()) { index, uri ->
                    FileItem(
                        fileName = uri.lastPathSegment ?: "File ${index + 1}",
                        onRemove = { selectedFiles.removeAt(index) }
                    )
                }
            }

            if (selectedFiles.isNotEmpty() && !isComplete && (tool.id == "split_pdf" || tool.id == "remove_pages" || tool.id == "extract_pages")) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when (tool.id) {
                                "split_pdf" -> "Page Range to Keep"
                                "extract_pages" -> "Pages to Extract"
                                else -> "Pages to Remove"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = pageRangeInput,
                            onValueChange = { pageRangeInput = it },
                            placeholder = {
                                Text(
                                    text = if (tool.id == "split_pdf" || tool.id == "extract_pages") "e.g., 1-3, 5" else "e.g., 2, 4",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
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
                            text = when (tool.id) {
                                "split_pdf" -> "Enter comma-separated ranges/numbers (e.g. 1-3, 5 to keep pages 1, 2, 3, and 5)."
                                "extract_pages" -> "Enter page numbers or ranges to extract into a new PDF (e.g. 1-3, 5)."
                                else -> "Enter page numbers or ranges to remove (e.g. 2, 4 to delete pages 2 and 4)."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            if (selectedFiles.isNotEmpty() && !isComplete && tool.id == "rotate_pdf") {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Select Rotation Angle",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val angles = listOf(90, 180, 270)
                            angles.forEach { angle ->
                                val isSelected = rotationAngleInput == angle
                                val cardBg = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceContainerLow
                                val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                
                                Card(
                                    onClick = { rotationAngleInput = angle },
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
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$angle°",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = when (angle) {
                                                    90 -> "Right"
                                                    180 -> "Upside Down"
                                                    270 -> "Left"
                                                    else -> ""
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = contentColor.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Pages to Rotate (Optional)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = pageRangeInput,
                            onValueChange = { pageRangeInput = it },
                            placeholder = {
                                Text(
                                    text = "e.g., 1-3, 5 (leave empty for all)",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
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
                            text = "Enter page numbers or ranges to rotate. If left blank, all pages will be rotated.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            if (selectedFiles.isNotEmpty() && !isComplete && (tool.id == "protect_pdf" || tool.id == "unlock_pdf")) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (tool.id == "protect_pdf") "Set Password" else "Enter Password",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            placeholder = {
                                Text(
                                    text = if (tool.id == "protect_pdf") "Choose a strong password" else "Enter document password",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                val description = if (passwordVisible) "Hide password" else "Show password"
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
                            text = if (tool.id == "protect_pdf") 
                                "This will encrypt the PDF with standard 128-bit security. Keep this password safe as it cannot be recovered." 
                                else "Provide the correct password to decrypt the document and remove its security lock.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }


            if (selectedFiles.isNotEmpty() && !isComplete && tool.id == "add_watermark") {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Watermark Text",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = watermarkTextInput,
                            onValueChange = { watermarkTextInput = it },
                            placeholder = { Text("CONFIDENTIAL") },
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
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val presets = listOf("CONFIDENTIAL", "DRAFT", "COPY", "FINAL")
                            presets.forEach { preset ->
                                val isSelected = watermarkTextInput == preset
                                FilledTonalButton(
                                    onClick = { watermarkTextInput = preset },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceContainerLow,
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = preset,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Watermark Color",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
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
                            colors.forEach { (hex, label) ->
                                val isSelected = watermarkColorHex.lowercase() == hex.lowercase()
                                val color = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(hex))
                                
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .drawBehind {
                                            if (isSelected) {
                                                drawCircle(
                                                    color = androidx.compose.ui.graphics.Color.White,
                                                    radius = 6.dp.toPx(),
                                                    style = Stroke(width = 3.dp.toPx())
                                                )
                                            }
                                        }
                                        .clip(CircleShape)
                                        .background(androidx.compose.ui.graphics.Color.Transparent)
                                        .drawBehind {
                                            if (isSelected) {
                                                drawCircle(
                                                    color = accentColor,
                                                    radius = 20.dp.toPx(),
                                                    style = Stroke(width = 3.dp.toPx())
                                                )
                                            }
                                        }
                                        .clickable { watermarkColorHex = hex }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Font Size: ${watermarkFontSize.toInt()} pt",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                androidx.compose.material3.Slider(
                                    value = watermarkFontSize,
                                    onValueChange = { watermarkFontSize = it },
                                    valueRange = 20f..80f,
                                    steps = 5,
                                    colors = androidx.compose.material3.SliderDefaults.colors(
                                        thumbColor = accentColor,
                                        activeTrackColor = accentColor
                                    )
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Opacity: ${(watermarkOpacity * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                androidx.compose.material3.Slider(
                                    value = watermarkOpacity,
                                    onValueChange = { watermarkOpacity = it },
                                    valueRange = 0.1f..0.9f,
                                    steps = 7,
                                    colors = androidx.compose.material3.SliderDefaults.colors(
                                        thumbColor = accentColor,
                                        activeTrackColor = accentColor
                                    )
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Rotation Angle: ${watermarkRotation.toInt()}°",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                androidx.compose.material3.Slider(
                                    value = watermarkRotation,
                                    onValueChange = { watermarkRotation = it },
                                    valueRange = -90f..90f,
                                    steps = 11,
                                    colors = androidx.compose.material3.SliderDefaults.colors(
                                        thumbColor = accentColor,
                                        activeTrackColor = accentColor
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Pages to Watermark (Optional)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = pageRangeInput,
                            onValueChange = { pageRangeInput = it },
                            placeholder = { Text("e.g., 1-3, 5 (leave empty for all)") },
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
                            text = "Leave blank to apply the watermark on all pages in the PDF document.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            if (selectedFiles.isNotEmpty() && !isComplete && tool.id == "add_page_numbers") {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Numbering Style / Format",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val formats = listOf(
                                "simple" to "1, 2, 3...",
                                "prefixed" to "Page X",
                                "detailed" to "Page X of N"
                            )
                            formats.forEach { (id, label) ->
                                val isSelected = pageNumFormat == id
                                FilledTonalButton(
                                    onClick = { pageNumFormat = id },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceContainerLow,
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Alignment & Position",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val positions = listOf(
                                "top_right" to "Top Right",
                                "bottom_center" to "Bottom Center",
                                "bottom_right" to "Bottom Right"
                            )
                            positions.forEach { (id, label) ->
                                val isSelected = pageNumPosition == id
                                FilledTonalButton(
                                    onClick = { pageNumPosition = id },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceContainerLow,
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Font Size: ${pageNumFontSize.toInt()} pt",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        androidx.compose.material3.Slider(
                            value = pageNumFontSize,
                            onValueChange = { pageNumFontSize = it },
                            valueRange = 8f..18f,
                            steps = 4,
                            colors = androidx.compose.material3.SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Pages to Number (Optional)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = pageRangeInput,
                            onValueChange = { pageRangeInput = it },
                            placeholder = { Text("e.g., 1-3, 5 (leave empty for all)") },
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
                            text = "Leave blank to number all pages in the PDF document.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            if (selectedFiles.isNotEmpty() && !isComplete && tool.id == "crop_pdf") {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Crop Margins",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val options = listOf(
                                0.05f to "Slim (5%)",
                                0.10f to "Normal (10%)",
                                0.20f to "Wide (20%)"
                            )
                            options.forEach { (pct, label) ->
                                val isSelected = cropMarginPercentage == pct
                                val cardBg = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceContainerLow
                                val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                
                                Card(
                                    onClick = { cropMarginPercentage = pct },
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
                            text = "Pages to Crop (Optional)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = pageRangeInput,
                            onValueChange = { pageRangeInput = it },
                            placeholder = { Text("e.g., 1-3, 5 (leave empty for all)") },
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
                            text = "Crop will trim off the selected margin percentage from all four edges of the pages. Leave blank to crop all pages.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            if (selectedFiles.isNotEmpty() && !isComplete && tool.id == "organize_pdf") {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Arrange & Transform Pages",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "You can reorder, rotate, duplicate, or delete individual pages below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val itemsPerRow = 2
                        val chunkedPages = organizePages.chunked(itemsPerRow)
                        
                        chunkedPages.forEachIndexed { rowIndex, rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowItems.forEachIndexed { colIndex, pageItem ->
                                    val globalIndex = rowIndex * itemsPerRow + colIndex
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(bottom = 8.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(100.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.InsertDriveFile,
                                                    contentDescription = null,
                                                    tint = accentColor.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                if (pageItem.rotation != 0) {
                                                    Text(
                                                        text = "${pageItem.rotation}°",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = accentColor,
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(4.dp)
                                                            .background(
                                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                                                RoundedCornerShape(4.dp)
                                                            )
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Page ${globalIndex + 1}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "(Orig. ${pageItem.originalIndex + 1})",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        val currentIdx = organizePages.indexOf(pageItem)
                                                        if (currentIdx != -1) {
                                                            organizePages[currentIdx] = pageItem.copy(
                                                                rotation = (pageItem.rotation + 90) % 360
                                                            )
                                                        }
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.RotateRight,
                                                        contentDescription = "Rotate 90 degrees",
                                                        modifier = Modifier.size(18.dp),
                                                        tint = accentColor
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        val currentIdx = organizePages.indexOf(pageItem)
                                                        if (currentIdx != -1) {
                                                            organizePages.add(
                                                                currentIdx + 1,
                                                                pageItem.copy(id = java.util.UUID.randomUUID().toString())
                                                            )
                                                        }
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Add,
                                                        contentDescription = "Duplicate page",
                                                        modifier = Modifier.size(18.dp),
                                                        tint = accentColor
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        if (organizePages.size > 1) {
                                                            organizePages.remove(pageItem)
                                                        } else {
                                                            Toast.makeText(context, "Cannot delete all pages", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Delete,
                                                        contentDescription = "Delete page",
                                                        modifier = Modifier.size(18.dp),
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                TextButton(
                                                    onClick = {
                                                        val currentIdx = organizePages.indexOf(pageItem)
                                                        if (currentIdx > 0) {
                                                            organizePages.removeAt(currentIdx)
                                                            organizePages.add(currentIdx - 1, pageItem)
                                                        }
                                                    },
                                                    enabled = globalIndex > 0,
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Text("← Move", style = MaterialTheme.typography.bodySmall)
                                                }
                                                
                                                TextButton(
                                                    onClick = {
                                                        val currentIdx = organizePages.indexOf(pageItem)
                                                        if (currentIdx < organizePages.size - 1) {
                                                            organizePages.removeAt(currentIdx)
                                                            organizePages.add(currentIdx + 1, pageItem)
                                                        }
                                                    },
                                                    enabled = globalIndex < organizePages.size - 1,
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Text("Move →", style = MaterialTheme.typography.bodySmall)
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
                }
            }

            if (selectedFiles.isNotEmpty() && !isComplete && tool.id == "repair_pdf") {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = androidx.compose.foundation.BorderStroke(
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
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            
                            Text(
                                text = "Self-Healing Diagnostic System",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Text(
                                text = "If your PDF file is corrupted, fails to open, or shows blank pages, our diagnostic recovery parses sequential indirect objects and reconstructs broken XREF cross-reference tables locally.",
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
                                    "Sequential parser: Scanning indirect PDF objects...",
                                    "XREF analyzer: Checking cross-reference markers...",
                                    "Trailer recovery: Parsing root catalog structures...",
                                    "File integrity: Repairing byte stream offsets..."
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
            }

            if (selectedFiles.isNotEmpty() && !isComplete && tool.id == "pdf_to_pdfa") {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Archive Conformance Standards",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val formats = listOf(
                                "pdfa_1b" to "PDF/A-1b (ISO 19005-1)",
                                "pdfa_2b" to "PDF/A-2b (ISO 19005-2)"
                            )
                            formats.forEach { (id, label) ->
                                val isSelected = pdfaConformance == id
                                val cardBg = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceContainerLow
                                val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                
                                Card(
                                    onClick = { pdfaConformance = id },
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
                                    text = "Archival Injection Details",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                val injectionDetails = listOf(
                                    "Color Preservation" to "Links sRGB Output Intent to establish device-independent color rendering.",
                                    "Archival Standards metadata" to "Injects XML metadata schemas declaring ISO 19005 compliance.",
                                    "Font embedding protection" to "Standardizes structure ensuring long-term digital preservation."
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
            }

            if (selectedFiles.isNotEmpty() && !isComplete && tool.id == "sign_pdf") {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SignaturePad(
                            onSaveSignature = { bitmap ->
                                try {
                                    val tempFile = File.createTempFile("sig_", ".png", context.cacheDir)
                                    tempFile.outputStream().use { out ->
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                    }
                                    signatureUri = Uri.fromFile(tempFile)
                                    bitmap.recycle()
                                    Toast.makeText(context, "Signature saved successfully!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(context, "Failed to save signature: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onClearSignature = {
                                signatureUri = null
                            },
                            accentColor = accentColor
                        )

                        if (signatureUri != null) {
                            Text(
                                text = "Signature Parameters",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Page Index Selector
                            if (totalPagesForSignRedact > 1) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Target Page: ${sigPageIndex + 1} of $totalPagesForSignRedact",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    androidx.compose.material3.Slider(
                                        value = sigPageIndex.toFloat(),
                                        onValueChange = { sigPageIndex = it.toInt() },
                                        valueRange = 0f..(totalPagesForSignRedact - 1).toFloat(),
                                        steps = totalPagesForSignRedact - 2,
                                        colors = androidx.compose.material3.SliderDefaults.colors(
                                            thumbColor = accentColor,
                                            activeTrackColor = accentColor
                                        )
                                    )
                                }
                            }

                            // Coordinate adjustments
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Horizontal Offset (X): ${sigX.toInt()} pt",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    androidx.compose.material3.Slider(
                                        value = sigX,
                                        onValueChange = { sigX = it },
                                        valueRange = 0f..500f,
                                        colors = androidx.compose.material3.SliderDefaults.colors(
                                            thumbColor = accentColor,
                                            activeTrackColor = accentColor
                                        )
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Vertical Offset (Y): ${sigY.toInt()} pt",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    androidx.compose.material3.Slider(
                                        value = sigY,
                                        onValueChange = { sigY = it },
                                        valueRange = 0f..700f,
                                        colors = androidx.compose.material3.SliderDefaults.colors(
                                            thumbColor = accentColor,
                                            activeTrackColor = accentColor
                                        )
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Signature Width: ${sigWidth.toInt()} pt",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    androidx.compose.material3.Slider(
                                        value = sigWidth,
                                        onValueChange = { sigWidth = it },
                                        valueRange = 50f..300f,
                                        colors = androidx.compose.material3.SliderDefaults.colors(
                                            thumbColor = accentColor,
                                            activeTrackColor = accentColor
                                        )
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Signature Height: ${sigHeight.toInt()} pt",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    androidx.compose.material3.Slider(
                                        value = sigHeight,
                                        onValueChange = { sigHeight = it },
                                        valueRange = 20f..150f,
                                        colors = androidx.compose.material3.SliderDefaults.colors(
                                            thumbColor = accentColor,
                                            activeTrackColor = accentColor
                                        )
                                    )
                                }
                            }

                            PositionPreviewCard(
                                x = sigX,
                                y = sigY,
                                width = sigWidth,
                                height = sigHeight,
                                isRedaction = false,
                                accentColor = accentColor
                            )
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Please write a signature and tap 'Confirm Signature' to unlock coordinate positioning and signing options.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (selectedFiles.isNotEmpty() && !isComplete && tool.id == "redact_pdf") {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Redaction Parameters",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (totalPagesForSignRedact > 1) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Target Page: ${redactPageIndex + 1} of $totalPagesForSignRedact",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                androidx.compose.material3.Slider(
                                    value = redactPageIndex.toFloat(),
                                    onValueChange = { redactPageIndex = it.toInt() },
                                    valueRange = 0f..(totalPagesForSignRedact - 1).toFloat(),
                                    steps = totalPagesForSignRedact - 2,
                                    colors = androidx.compose.material3.SliderDefaults.colors(
                                        thumbColor = accentColor,
                                        activeTrackColor = accentColor
                                    )
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Horizontal Offset (X): ${redactX.toInt()} pt",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                androidx.compose.material3.Slider(
                                    value = redactX,
                                    onValueChange = { redactX = it },
                                    valueRange = 0f..500f,
                                    colors = androidx.compose.material3.SliderDefaults.colors(
                                        thumbColor = accentColor,
                                        activeTrackColor = accentColor
                                    )
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Vertical Offset (Y): ${redactY.toInt()} pt",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                androidx.compose.material3.Slider(
                                    value = redactY,
                                    onValueChange = { redactY = it },
                                    valueRange = 0f..700f,
                                    colors = androidx.compose.material3.SliderDefaults.colors(
                                        thumbColor = accentColor,
                                        activeTrackColor = accentColor
                                    )
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Redaction Box Width: ${redactWidth.toInt()} pt",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                androidx.compose.material3.Slider(
                                    value = redactWidth,
                                    onValueChange = { redactWidth = it },
                                    valueRange = 10f..400f,
                                    colors = androidx.compose.material3.SliderDefaults.colors(
                                        thumbColor = accentColor,
                                        activeTrackColor = accentColor
                                    )
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Redaction Box Height: ${redactHeight.toInt()} pt",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                androidx.compose.material3.Slider(
                                    value = redactHeight,
                                    onValueChange = { redactHeight = it },
                                    valueRange = 10f..200f,
                                    colors = androidx.compose.material3.SliderDefaults.colors(
                                        thumbColor = accentColor,
                                        activeTrackColor = accentColor
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = redactText,
                            onValueChange = { redactText = it },
                            label = { Text("Reason or Text Reference (Optional)") },
                            placeholder = { Text("e.g. SSN Redaction") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        PositionPreviewCard(
                            x = redactX,
                            y = redactY,
                            width = redactWidth,
                            height = redactHeight,
                            isRedaction = true,
                            accentColor = accentColor
                        )
                    }
                }
            }

            if (selectedFiles.isNotEmpty() && !isComplete && tool.id == "pdf_forms") {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Interactive PDF Form Fields",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (!isFormLoaded) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = accentColor)
                            }
                        } else if (formFields.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No interactive AcroForm fields were detected in this PDF file.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    formFields.forEach { field ->
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = field.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            
                                            when (field.type) {
                                                "checkbox" -> {
                                                    val isChecked = formValues[field.name] == "true"
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Checkbox(
                                                            checked = isChecked,
                                                            onCheckedChange = { checked ->
                                                                formValues[field.name] = checked.toString()
                                                            },
                                                            colors = CheckboxDefaults.colors(
                                                                checkedColor = accentColor
                                                            )
                                                        )
                                                        Text(
                                                            text = if (isChecked) "Checked" else "Unchecked",
                                                            style = MaterialTheme.typography.bodyMedium
                                                        )
                                                    }
                                                }
                                                "choice" -> {
                                                    var expanded by remember { mutableStateOf(false) }
                                                    val selectedValue = formValues[field.name] ?: ""
                                                    
                                                    ExposedDropdownMenuBox(
                                                        expanded = expanded,
                                                        onExpandedChange = { expanded = !expanded }
                                                    ) {
                                                        OutlinedTextField(
                                                            value = selectedValue,
                                                            onValueChange = {},
                                                            readOnly = true,
                                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                                            modifier = Modifier
                                                                .menuAnchor()
                                                                .fillMaxWidth(),
                                                            shape = RoundedCornerShape(12.dp),
                                                            colors = OutlinedTextFieldDefaults.colors(
                                                                focusedBorderColor = accentColor,
                                                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                                            )
                                                        )
                                                        
                                                        ExposedDropdownMenu(
                                                            expanded = expanded,
                                                            onDismissRequest = { expanded = false }
                                                        ) {
                                                            field.options.forEach { option ->
                                                                DropdownMenuItem(
                                                                    text = { Text(text = option) },
                                                                    onClick = {
                                                                        formValues[field.name] = option
                                                                        expanded = false
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    val textVal = formValues[field.name] ?: ""
                                                    OutlinedTextField(
                                                        value = textVal,
                                                        onValueChange = {
                                                            formValues[field.name] = it
                                                        },
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedBorderColor = accentColor,
                                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                                        ),
                                                        modifier = Modifier.fillMaxWidth()
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
            }

            // Action button / Processing bar
            if (selectedFiles.isNotEmpty() && !isComplete) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))

                    if (isProcessing) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp)),
                                color = accentColor,
                                trackColor = containerColor
                            )
                            Text(
                                text = "Processing PDF tools locally...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                isProcessing = true
                                isComplete = false
                                scope.launch {
                                    try {
                                        when (tool.id) {
                                            "merge_pdf" -> {
                                                val uri = PdfProcessor.mergePdfs(context, selectedFiles.toList())
                                                outputUris.add(uri)
                                                RecentFilesRepository.addRecent(context, "Merged_${System.currentTimeMillis().toString().takeLast(4)}.pdf", tool.id, uri.toString())
                                            }
                                            "compress_pdf" -> {
                                                val uri = PdfProcessor.compressPdf(context, selectedFiles.first())
                                                outputUris.add(uri)
                                                RecentFilesRepository.addRecent(context, "Compressed_${System.currentTimeMillis().toString().takeLast(4)}.pdf", tool.id, uri.toString())
                                            }
                                            "jpg_to_pdf" -> {
                                                val uri = PdfProcessor.convertImagesToPdf(context, selectedFiles.toList())
                                                outputUris.add(uri)
                                                RecentFilesRepository.addRecent(context, "Converted_${System.currentTimeMillis().toString().takeLast(4)}.pdf", tool.id, uri.toString())
                                            }
                                            "pdf_to_jpg" -> {
                                                val uris = PdfProcessor.convertPdfToImages(context, selectedFiles.first())
                                                outputUris.addAll(uris)
                                                uris.firstOrNull()?.let { uri ->
                                                    RecentFilesRepository.addRecent(context, "Page_1_${System.currentTimeMillis().toString().takeLast(4)}.jpg", tool.id, uri.toString())
                                                }
                                            }
                                            "split_pdf" -> {
                                                if (pageRangeInput.trim().isEmpty()) {
                                                    throw IllegalArgumentException("Please enter a page range.")
                                                }
                                                val uri = PdfProcessor.splitPdf(context, selectedFiles.first(), pageRangeInput)
                                                outputUris.add(uri)
                                                RecentFilesRepository.addRecent(context, "Split_${System.currentTimeMillis().toString().takeLast(4)}.pdf", tool.id, uri.toString())
                                            }
                                            "remove_pages" -> {
                                                if (pageRangeInput.trim().isEmpty()) {
                                                    throw IllegalArgumentException("Please enter pages to remove.")
                                                }
                                                val uri = PdfProcessor.removePages(context, selectedFiles.first(), pageRangeInput)
                                                outputUris.add(uri)
                                                RecentFilesRepository.addRecent(context, "Removed_${System.currentTimeMillis().toString().takeLast(4)}.pdf", tool.id, uri.toString())
                                            }
                                            "protect_pdf" -> {
                                                if (passwordInput.trim().isEmpty()) {
                                                    throw IllegalArgumentException("Please enter a password.")
                                                }
                                                val uri = PdfProcessor.protectPdf(context, selectedFiles.first(), passwordInput)
                                                outputUris.add(uri)
                                                RecentFilesRepository.addRecent(context, "Protected_${System.currentTimeMillis().toString().takeLast(4)}.pdf", tool.id, uri.toString())
                                            }
                                            "unlock_pdf" -> {
                                                if (passwordInput.trim().isEmpty()) {
                                                    throw IllegalArgumentException("Please enter the password.")
                                                }
                                                val uri = PdfProcessor.unlockPdf(context, selectedFiles.first(), passwordInput)
                                                outputUris.add(uri)
                                                RecentFilesRepository.addRecent(context, "Unlocked_${System.currentTimeMillis().toString().takeLast(4)}.pdf", tool.id, uri.toString())
                                            }
                                            "extract_pages" -> {
                                                if (pageRangeInput.trim().isEmpty()) {
                                                    throw IllegalArgumentException("Please enter pages to extract.")
                                                }
                                                val uri = PdfProcessor.extractPages(context, selectedFiles.first(), pageRangeInput)
                                                outputUris.add(uri)
                                                RecentFilesRepository.addRecent(context, "Extracted_${System.currentTimeMillis().toString().takeLast(4)}.pdf", tool.id, uri.toString())
                                            }
                                            "rotate_pdf" -> {
                                                val uri = PdfProcessor.rotatePdf(context, selectedFiles.first(), rotationAngleInput, pageRangeInput)
                                                outputUris.add(uri)
                                                RecentFilesRepository.addRecent(context, "Rotated_${System.currentTimeMillis().toString().takeLast(4)}.pdf", tool.id, uri.toString())
                                            }
                                            "add_watermark" -> {
                                                val uri = PdfProcessor.addWatermark(
                                                    context = context,
                                                    uri = selectedFiles.first(),
                                                    text = watermarkTextInput,
                                                    colorHex = watermarkColorHex,
                                                    fontSize = watermarkFontSize,
                                                    rotation = watermarkRotation,
                                                    opacity = watermarkOpacity,
                                                    pageRange = pageRangeInput
                                                )
                                                outputUris.add(uri)
                                                RecentFilesRepository.addRecent(context, "Watermarked_${System.currentTimeMillis().toString().takeLast(4)}.pdf", tool.id, uri.toString())
                                            }
                                            "add_page_numbers" -> {
                                                val uri = PdfProcessor.addPageNumbers(
                                                    context = context,
                                                    uri = selectedFiles.first(),
                                                    format = pageNumFormat,
                                                    position = pageNumPosition,
                                                    fontSize = pageNumFontSize,
                                                    pageRange = pageRangeInput
                                                )
                                                outputUris.add(uri)
                                                RecentFilesRepository.addRecent(context, "Numbered_${System.currentTimeMillis().toString().takeLast(4)}.pdf", tool.id, uri.toString())
                                            }
                                            "crop_pdf" -> {
                                                val uri = PdfProcessor.cropPdf(
                                                    context = context,
                                                    uri = selectedFiles.first(),
                                                    marginPercentage = cropMarginPercentage,
                                                    pageRange = pageRangeInput
                                                )
                                                outputUris.add(uri)
                                                RecentFilesRepository.addRecent(context, "Cropped_${System.currentTimeMillis().toString().takeLast(4)}.pdf", tool.id, uri.toString())
                                            }
                                            "organize_pdf" -> {
                                                val transforms = organizePages.map { PdfProcessor.PageTransform(it.originalIndex, it.rotation) }
                                                val uri = PdfProcessor.organizePdf(context, selectedFiles.first(), transforms)
                                                outputUris.add(uri)
                                                RecentFilesRepository.addRecent(context, "Organized_${System.currentTimeMillis().toString().takeLast(4)}.pdf", tool.id, uri.toString())
                                            }
                                            "repair_pdf" -> {
                                                val uri = PdfProcessor.repairPdf(context, selectedFiles.first())
                                                outputUris.add(uri)
                                                RecentFilesRepository.addRecent(context, "Repaired_${System.currentTimeMillis().toString().takeLast(4)}.pdf", tool.id, uri.toString())
                                            }
                                            "pdf_to_pdfa" -> {
                                                val uri = PdfProcessor.convertToPdfA(context, selectedFiles.first(), pdfaConformance)
                                                outputUris.add(uri)
                                                RecentFilesRepository.addRecent(context, "Archived_${System.currentTimeMillis().toString().takeLast(4)}.pdf", tool.id, uri.toString())
                                            }
                                            "sign_pdf" -> {
                                                if (signatureUri == null) {
                                                    throw IllegalArgumentException("Please create and confirm your signature first.")
                                                }
                                                val uri = PdfProcessor.signPdf(
                                                    context = context,
                                                    uri = selectedFiles.first(),
                                                    signatureUri = signatureUri!!,
                                                    pageIndex = sigPageIndex,
                                                    x = sigX,
                                                    y = sigY,
                                                    width = sigWidth,
                                                    height = sigHeight
                                                )
                                                outputUris.add(uri)
                                                RecentFilesRepository.addRecent(context, "Signed_${System.currentTimeMillis().toString().takeLast(4)}.pdf", tool.id, uri.toString())
                                            }
                                            "redact_pdf" -> {
                                                val uri = PdfProcessor.redactPdf(
                                                    context = context,
                                                    uri = selectedFiles.first(),
                                                    pageIndex = redactPageIndex,
                                                    x = redactX,
                                                    y = redactY,
                                                    width = redactWidth,
                                                    height = redactHeight,
                                                    textToRedact = redactText.takeIf { it.isNotEmpty() }
                                                )
                                                outputUris.add(uri)
                                                RecentFilesRepository.addRecent(context, "Redacted_${System.currentTimeMillis().toString().takeLast(4)}.pdf", tool.id, uri.toString())
                                            }
                                            "pdf_forms" -> {
                                                val uri = PdfProcessor.fillPdfFields(
                                                    context = context,
                                                    uri = selectedFiles.first(),
                                                    fieldValues = formValues.toMap()
                                                )
                                                outputUris.add(uri)
                                                RecentFilesRepository.addRecent(context, "Filled_${System.currentTimeMillis().toString().takeLast(4)}.pdf", tool.id, uri.toString())
                                            }
                                        }
                                        isComplete = true
                                        Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                        e.printStackTrace()
                                    } finally {
                                        isProcessing = false
                                    }
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
private fun HeroSection(
    tool: PdfTool,
    accentColor: androidx.compose.ui.graphics.Color,
    containerColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tool.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FilePickerZone(
    accentColor: androidx.compose.ui.graphics.Color,
    onPickFiles: () -> Unit
) {
    val borderColor = accentColor.copy(alpha = 0.4f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(12.dp.toPx(), 8.dp.toPx()),
                            0f
                        )
                    ),
                    cornerRadius = CornerRadius(20.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CloudUpload,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Select your files",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            FilledTonalButton(
                onClick = onPickFiles,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Choose Files")
            }
        }
    }
}

@Composable
private fun FileItem(
    fileName: String,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.InsertDriveFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove file",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SuccessCard(
    tool: PdfTool,
    outputUris: List<Uri>,
    onClear: () -> Unit,
    accentColor: androidx.compose.ui.graphics.Color,
    containerColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(400)) + scaleIn(tween(400))
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "Processing Complete!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = if (tool.id == "pdf_to_jpg") {
                        "${outputUris.size} page(s) successfully converted to images."
                    } else {
                        "Your document has been processed and saved securely."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Actions Layout
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Open File
                    Button(
                        onClick = { openOutputUris(context, tool, outputUris) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Result")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Share
                        FilledTonalButton(
                            onClick = { shareOutputUris(context, tool, outputUris) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share")
                        }

                        // Save to downloads
                        FilledTonalButton(
                            onClick = {
                                saveOutputUrisToDownloads(context, tool, outputUris)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export")
                        }
                    }

                    // Reset button
                    TextButton(
                        onClick = onClear,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Process another file", color = accentColor)
                    }
                }
            }
        }
    }
}

private fun openOutputUris(context: Context, tool: PdfTool, uris: List<Uri>) {
    try {
        val targetUri = uris.firstOrNull() ?: return
        val file = File(targetUri.path ?: "")
        if (!file.exists()) {
            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
            return
        }
        val contentUri = FileProvider.getUriForFile(context, "com.example.pdftools.fileprovider", file)
        val mimeType = if (tool.id == "pdf_to_jpg") "image/jpeg" else "application/pdf"
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Open File"))
    } catch (e: Exception) {
        Toast.makeText(context, "Open failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

private fun shareOutputUris(context: Context, tool: PdfTool, uris: List<Uri>) {
    try {
        if (uris.isEmpty()) return
        
        if (uris.size == 1) {
            val file = File(uris.first().path ?: "")
            val contentUri = FileProvider.getUriForFile(context, "com.example.pdftools.fileprovider", file)
            val mimeType = if (tool.id == "pdf_to_jpg") "image/jpeg" else "application/pdf"
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share File"))
        } else {
            // Share multiple files
            val arrayList = ArrayList<Uri>()
            for (uri in uris) {
                arrayList.add(FileProvider.getUriForFile(context, "com.example.pdftools.fileprovider", File(uri.path ?: "")))
            }
            
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/jpeg"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayList)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Images"))
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Share failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

private fun saveOutputUrisToDownloads(context: Context, tool: PdfTool, uris: List<Uri>) {
    try {
        val resolver = context.contentResolver
        var count = 0
        
        uris.forEachIndexed { index, uri ->
            val file = File(uri.path ?: "")
            val inputStream = file.inputStream()
            val extension = if (tool.id == "pdf_to_jpg") "jpg" else "pdf"
            val mimeType = if (tool.id == "pdf_to_jpg") "image/jpeg" else "application/pdf"
            val displayTitle = if (tool.id == "pdf_to_jpg") "Page_${index + 1}_${System.currentTimeMillis()}.$extension" else "${tool.name.replace(" ", "_")}_${System.currentTimeMillis()}.$extension"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayTitle)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/PDFTools")
                }
                val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
                val destUri = resolver.insert(collection, contentValues) ?: throw Exception("Failed to create Downloads entry")
                
                resolver.openOutputStream(destUri)?.use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                count++
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val pdfToolsDir = File(downloadsDir, "PDFTools")
                pdfToolsDir.mkdirs()
                val destFile = File(pdfToolsDir, displayTitle)
                
                destFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                count++
            }
        }
        
        Toast.makeText(context, "Successfully exported $count file(s) to Downloads/PDFTools", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Export failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

private fun getActionButtonText(toolId: String): String {
    return when (toolId) {
        "merge_pdf" -> "Merge PDFs"
        "compress_pdf" -> "Compress PDF"
        "jpg_to_pdf" -> "Convert to PDF"
        "pdf_to_jpg" -> "Convert to JPG"
        "split_pdf" -> "Split PDF"
        "remove_pages" -> "Remove Pages"
        "protect_pdf" -> "Protect PDF"
        "unlock_pdf" -> "Unlock PDF"
        "extract_pages" -> "Extract Pages"
        "rotate_pdf" -> "Rotate PDF"
        "add_watermark" -> "Add Watermark"
        "add_page_numbers" -> "Add Page Numbers"
        "crop_pdf" -> "Crop PDF"
        "organize_pdf" -> "Organize PDF"
        "repair_pdf" -> "Repair PDF"
        "pdf_to_pdfa" -> "Convert to PDF/A"
        "sign_pdf" -> "Sign PDF"
        "redact_pdf" -> "Redact PDF"
        "pdf_forms" -> "Fill PDF Forms"
        else -> "Process"
    }
}

data class OrganizePageItem(
    val id: String,
    val originalIndex: Int,
    val rotation: Int
)

@Composable
fun SignaturePad(
    onSaveSignature: (Bitmap) -> Unit,
    onClearSignature: () -> Unit,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    var canvasWidth by remember { mutableStateOf(0) }
    var canvasHeight by remember { mutableStateOf(0) }
    val paths = remember { mutableStateListOf<List<Offset>>() }
    var currentPathPoints = remember { mutableStateOf<List<Offset>>(emptyList()) }
    var hasSigned by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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
                    text = "Draw Your Signature",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (hasSigned) {
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.bodySmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .onGloballyPositioned { coordinates ->
                        canvasWidth = coordinates.size.width
                        canvasHeight = coordinates.size.height
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPathPoints.value = listOf(offset)
                                hasSigned = true
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                currentPathPoints.value = currentPathPoints.value + change.position
                            },
                            onDragEnd = {
                                if (currentPathPoints.value.isNotEmpty()) {
                                    paths.add(currentPathPoints.value)
                                    currentPathPoints.value = emptyList()
                                }
                            }
                        )
                    }
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw a dashed signature helper baseline
                    val baselineY = size.height * 0.75f
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(40f, baselineY),
                        end = Offset(size.width - 40f, baselineY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // Draw historical paths
                    paths.forEach { points ->
                        if (points.size > 1) {
                            val path = Path().apply {
                                val first = points.first()
                                moveTo(first.x, first.y)
                                for (i in 1 until points.size) {
                                    val point = points[i]
                                    lineTo(point.x, point.y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = Color.Black,
                                style = Stroke(
                                    width = 6f,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    // Draw current path in progress
                    val points = currentPathPoints.value
                    if (points.size > 1) {
                        val path = Path().apply {
                            val first = points.first()
                            moveTo(first.x, first.y)
                            for (i in 1 until points.size) {
                                val point = points[i]
                                lineTo(point.x, point.y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = Color.Black,
                            style = Stroke(
                                width = 6f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                if (!hasSigned) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign here with finger",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        paths.clear()
                        currentPathPoints.value = emptyList()
                        hasSigned = false
                        onClearSignature()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Clear Pad")
                }

                Button(
                    onClick = {
                        if (paths.isEmpty() || canvasWidth == 0 || canvasHeight == 0) {
                            Toast.makeText(context, "Please sign first", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        // Render signature onto high-resolution Bitmap (600x300 transparent PNG)
                        val highResW = 600
                        val highResH = 300
                        val bitmap = Bitmap.createBitmap(highResW, highResH, Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(bitmap)
                        
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            style = android.graphics.Paint.Style.STROKE
                            strokeWidth = 8f
                            strokeCap = android.graphics.Paint.Cap.ROUND
                            strokeJoin = android.graphics.Paint.Join.ROUND
                            isAntiAlias = true
                        }

                        // Map paths to high res canvas using coordinate scaling
                        val scaleX = highResW.toFloat() / canvasWidth
                        val scaleY = highResH.toFloat() / canvasHeight

                        paths.forEach { points ->
                            if (points.size > 1) {
                                val path = android.graphics.Path().apply {
                                    val first = points.first()
                                    moveTo(first.x * scaleX, first.y * scaleY)
                                    for (i in 1 until points.size) {
                                        val pt = points[i]
                                        lineTo(pt.x * scaleX, pt.y * scaleY)
                                    }
                                }
                                canvas.drawPath(path, paint)
                            }
                        }

                        onSaveSignature(bitmap)
                    },
                    enabled = hasSigned,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor
                    )
                ) {
                    Text("Confirm Signature")
                }
            }
        }
    }
}

@Composable
fun PositionPreviewCard(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    isRedaction: Boolean,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isRedaction) "Redaction Position Preview" else "Signature Position Preview",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Start)
            )

            // Simulating A4 Page
            Box(
                modifier = Modifier
                    .size(width = 140.dp, height = 181.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White)
                    .drawBehind {
                        // Draw simulated document borders
                        drawRect(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Background simulated text lines
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val lineCount = 12
                    val lineSpacing = size.height / (lineCount + 1)
                    val paintColor = Color.LightGray.copy(alpha = 0.3f)
                    
                    for (i in 1..lineCount) {
                        val lineY = i * lineSpacing
                        // Draw some lines
                        if (i % 3 == 0) {
                            // Shorter lines / heading-like
                            drawLine(
                                color = paintColor,
                                start = Offset(15f, lineY),
                                end = Offset(size.width * 0.4f, lineY),
                                strokeWidth = 3f
                            )
                        } else {
                            drawLine(
                                color = paintColor,
                                start = Offset(15f, lineY),
                                end = Offset(size.width - 15f, lineY),
                                strokeWidth = 2f
                            )
                        }
                    }

                    // Render preview box
                    // standard PDF coordinate bounds: Width 612, Height 792
                    val scaleX = size.width / 612f
                    val scaleY = size.height / 792f

                    val rectX = x * scaleX
                    // Invert Y coordinate since PDF is bottom-left origin
                    val rectY = size.height - (y * scaleY) - (height * scaleY)
                    val rectW = width * scaleX
                    val rectH = height * scaleY

                    if (isRedaction) {
                        // Redaction: Solid black box representing permanent content removal
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(rectX, rectY),
                            size = androidx.compose.ui.geometry.Size(rectW, rectH)
                        )
                    } else {
                        // Signature: Translucent green box with dotted / solid border and accent color fill
                        drawRect(
                            color = accentColor.copy(alpha = 0.2f),
                            topLeft = Offset(rectX, rectY),
                            size = androidx.compose.ui.geometry.Size(rectW, rectH)
                        )
                        drawRect(
                            color = accentColor,
                            topLeft = Offset(rectX, rectY),
                            size = androidx.compose.ui.geometry.Size(rectW, rectH),
                            style = Stroke(width = 2f)
                        )
                        // A small cross inside
                        drawLine(
                            color = accentColor.copy(alpha = 0.4f),
                            start = Offset(rectX, rectY),
                            end = Offset(rectX + rectW, rectY + rectH),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = accentColor.copy(alpha = 0.4f),
                            start = Offset(rectX, rectY + rectH),
                            end = Offset(rectX + rectW, rectY),
                            strokeWidth = 1f
                        )
                    }
                }
            }

            Text(
                text = "Coordinates: X: ${x.toInt()} pt, Y: ${y.toInt()} pt (${width.toInt()}x${height.toInt()} pt)\n(Coordinates mapped to standard 612 x 792 pt page format)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
