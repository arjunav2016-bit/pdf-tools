package com.example.pdftools.ui.screens.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pdftools.R
import com.example.pdftools.data.FormFieldInfo
import com.example.pdftools.data.ImageAnnotation
import com.example.pdftools.data.PdfProcessor
import com.example.pdftools.data.TextAnnotation
import com.example.pdftools.ui.screens.PositionPreviewCard
import com.example.pdftools.ui.screens.SignaturePad
import com.example.pdftools.ui.screens.rememberThumbnailBitmap
import com.example.pdftools.ui.viewmodels.ToolViewModel
import com.example.pdftools.ui.viewmodels.FormConfig
import com.example.pdftools.ui.viewmodels.EditConfig
import com.example.pdftools.ui.viewmodels.WatermarkConfig
import com.example.pdftools.ui.viewmodels.PageNumberConfig
import com.example.pdftools.ui.viewmodels.SignConfig
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatermarkToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color
) {
    val config by viewModel.watermarkConfig.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.tool_watermark_text),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = config.text,
            onValueChange = { viewModel.watermarkConfig.value = config.copy(text = it) },
            placeholder = { Text(stringResource(R.string.tool_watermark_confidential)) },
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
            val presets = listOf(
                stringResource(R.string.tool_watermark_confidential),
                stringResource(R.string.tool_watermark_draft),
                stringResource(R.string.tool_watermark_copy),
                stringResource(R.string.tool_watermark_final)
            )
            presets.forEach { preset ->
                val isSelected = config.text == preset
                FilledTonalButton(
                    onClick = { viewModel.watermarkConfig.value = config.copy(text = preset) },
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
            text = stringResource(R.string.tool_watermark_color),
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
                "#7F8C8D" to stringResource(R.string.tool_color_gray),
                "#E74C3C" to stringResource(R.string.tool_color_red),
                "#2980B9" to stringResource(R.string.tool_color_blue),
                "#27AE60" to stringResource(R.string.tool_color_green)
            )
            colors.forEach { (hex, label) ->
                val isSelected = config.colorHex.lowercase() == hex.lowercase()
                val color = Color(android.graphics.Color.parseColor(hex))
                
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(color)
                        .drawBehind {
                            if (isSelected) {
                                drawCircle(
                                    color = Color.White,
                                    radius = 6.dp.toPx(),
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            }
                        }
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .drawBehind {
                            if (isSelected) {
                                drawCircle(
                                    color = accentColor,
                                    radius = 20.dp.toPx(),
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            }
                        }
                        .clickable { viewModel.watermarkConfig.value = config.copy(colorHex = hex) }
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
                    text = stringResource(R.string.tool_font_size_points, config.fontSize.toInt()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = config.fontSize,
                    onValueChange = { viewModel.watermarkConfig.value = config.copy(fontSize = it) },
                    valueRange = 20f..80f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor
                    )
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.tool_opacity_percent, (config.opacity * 100).toInt()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = config.opacity,
                    onValueChange = { viewModel.watermarkConfig.value = config.copy(opacity = it) },
                    valueRange = 0.1f..0.9f,
                    steps = 7,
                    colors = SliderDefaults.colors(
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
                    text = stringResource(R.string.tool_rotation_degrees, config.rotation.toInt()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = config.rotation,
                    onValueChange = { viewModel.watermarkConfig.value = config.copy(rotation = it) },
                    valueRange = -90f..90f,
                    steps = 11,
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = stringResource(R.string.tool_pages_watermark_optional),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = config.pageRange,
            onValueChange = { viewModel.watermarkConfig.value = config.copy(pageRange = it) },
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
            text = stringResource(R.string.tool_watermark_all_help),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageNumberToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color
) {
    val config by viewModel.pageNumberConfig.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.tool_numbering_style),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val formats = listOf(
                "simple" to stringResource(R.string.tool_numbering_simple),
                "prefixed" to stringResource(R.string.tool_numbering_prefixed),
                "detailed" to stringResource(R.string.tool_numbering_detailed)
            )
            formats.forEach { (id, label) ->
                val isSelected = config.format == id
                FilledTonalButton(
                    onClick = { viewModel.pageNumberConfig.value = config.copy(format = id) },
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
            text = stringResource(R.string.tool_alignment_position),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val positions = listOf(
                "top_right" to stringResource(R.string.tool_position_top_right),
                "bottom_center" to stringResource(R.string.tool_position_bottom_center),
                "bottom_right" to stringResource(R.string.tool_position_bottom_right)
            )
            positions.forEach { (id, label) ->
                val isSelected = config.position == id
                FilledTonalButton(
                    onClick = { viewModel.pageNumberConfig.value = config.copy(position = id) },
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
            text = stringResource(R.string.tool_font_size_points, config.fontSize.toInt()),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = config.fontSize,
            onValueChange = { viewModel.pageNumberConfig.value = config.copy(fontSize = it) },
            valueRange = 8f..18f,
            steps = 4,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = stringResource(R.string.tool_pages_number_optional),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = config.pageRange,
            onValueChange = { viewModel.pageNumberConfig.value = config.copy(pageRange = it) },
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
            text = stringResource(R.string.tool_number_all_help),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color
) {
    val config by viewModel.signConfig.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val context = LocalContext.current
    var totalPages by remember { mutableStateOf(1) }

    LaunchedEffect(selectedFiles) {
        if (selectedFiles.isEmpty()) return@LaunchedEffect
        try {
            context.contentResolver.openInputStream(selectedFiles.first())?.use { input ->
                val tempFile = File.createTempFile("sign_page_count_", ".pdf", context.cacheDir)
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
                    viewModel.signConfig.value = config.copy(signatureUri = Uri.fromFile(tempFile))
                    bitmap.recycle()
                    Toast.makeText(
                        context,
                        context.getString(R.string.tool_signature_saved),
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        context,
                        context.getString(R.string.tool_signature_save_failed, e.localizedMessage),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onClearSignature = {
                viewModel.signConfig.value = config.copy(signatureUri = null)
            },
            accentColor = accentColor
        )

        if (config.signatureUri != null) {
            Text(
                text = stringResource(R.string.tool_signature_parameters),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Page Index Selector
            if (totalPages > 1) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(
                            R.string.tool_target_page_count,
                            config.pageIndex + 1,
                            totalPages
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = config.pageIndex.toFloat(),
                        onValueChange = { viewModel.signConfig.value = config.copy(pageIndex = it.toInt()) },
                        valueRange = 0f..(totalPages - 1).toFloat(),
                        steps = if (totalPages > 2) totalPages - 2 else 0,
                        colors = SliderDefaults.colors(
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
                        text = stringResource(R.string.tool_signature_x, config.x.toInt()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = config.x,
                        onValueChange = { viewModel.signConfig.value = config.copy(x = it) },
                        valueRange = 0f..500f,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor
                        )
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.tool_signature_y, config.y.toInt()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = config.y,
                        onValueChange = { viewModel.signConfig.value = config.copy(y = it) },
                        valueRange = 0f..700f,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor
                        )
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.tool_signature_width, config.width.toInt()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = config.width,
                        onValueChange = { viewModel.signConfig.value = config.copy(width = it) },
                        valueRange = 50f..300f,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor
                        )
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.tool_signature_height, config.height.toInt()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = config.height,
                        onValueChange = { viewModel.signConfig.value = config.copy(height = it) },
                        valueRange = 20f..150f,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor
                        )
                    )
                }
            }

            PositionPreviewCard(
                x = config.x,
                y = config.y,
                width = config.width,
                height = config.height,
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.tool_signature_unlock_help),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color
) {
    val config by viewModel.editConfig.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val context = LocalContext.current
    val containerColor = MaterialTheme.colorScheme.surfaceContainerLow

    var editPageIndex by remember { mutableStateOf(0) }
    var editTotalPages by remember { mutableStateOf(1) }
    var editAnnotationType by remember { mutableStateOf("text") } // "text", "image"
    var editX by remember { mutableStateOf(0.1f) }
    var editY by remember { mutableStateOf(0.1f) }
    var editWidth by remember { mutableStateOf(0.2f) }
    var editHeight by remember { mutableStateOf(0.1f) }
    
    // Text fields local state
    var editTextAnnotationInput by remember { mutableStateOf("") }
    var editTextSize by remember { mutableStateOf(16f) }
    var editTextColor by remember { mutableStateOf("#2C3E50") }

    // Image fields local state
    var editImageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(selectedFiles) {
        if (selectedFiles.isEmpty()) return@LaunchedEffect
        try {
            context.contentResolver.openInputStream(selectedFiles.first())?.use { input ->
                val tempFile = File.createTempFile("edit_page_count_", ".pdf", context.cacheDir)
                try {
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                    com.tom_roush.pdfbox.pdmodel.PDDocument.load(tempFile).use { doc ->
                        editTotalPages = doc.numberOfPages
                    }
                } finally {
                    tempFile.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val annotationImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            editImageUri = uri
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.tool_editor_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.Start)
        )

        // Page navigation selector
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { if (editPageIndex > 0) editPageIndex-- },
                enabled = editPageIndex > 0,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = accentColor)
            ) {
                Text(stringResource(R.string.tool_previous))
            }

            Text(
                text = stringResource(R.string.tool_page_of_count, editPageIndex + 1, editTotalPages),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            Button(
                onClick = { if (editPageIndex < editTotalPages - 1) editPageIndex++ },
                enabled = editPageIndex < editTotalPages - 1,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = accentColor)
            ) {
                Text(stringResource(R.string.tool_next))
            }
        }

        // Simulated high-fidelity page preview card
        Card(
            modifier = Modifier
                .width(250.dp)
                .height(350.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(editPageIndex, editAnnotationType) {
                        detectTapGestures { offset ->
                            editX = (offset.x / size.width).coerceIn(0f, 1.0f)
                            editY = (offset.y / size.height).coerceIn(0f, 1.0f)
                        }
                    }
            ) {
                // Draw fake background lines to look like standard PDF content
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val startY = 30.dp.toPx()
                    val spacing = 20.dp.toPx()
                    val lineCount = 14
                    for (i in 0 until lineCount) {
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            start = androidx.compose.ui.geometry.Offset(20.dp.toPx(), startY + i * spacing),
                            end = androidx.compose.ui.geometry.Offset(size.width - 20.dp.toPx(), startY + i * spacing),
                            strokeWidth = 2f
                        )
                    }
                }

                // Render staged text annotations on this page
                config.textAnnotations.forEach { annot ->
                    if (annot.pageIndex == editPageIndex) {
                        val textColor = Color(android.graphics.Color.parseColor(annot.colorHex))
                        Box(
                            modifier = Modifier
                                .align(
                                    Alignment.TopStart
                                )
                                .padding(
                                    start = (annot.x * 250).dp - 20.dp,
                                    top = (annot.y * 350).dp - 10.dp
                                )
                                .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.8f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = annot.text,
                                color = textColor,
                                fontSize = androidx.compose.ui.unit.TextUnit(
                                    annot.fontSize / 2f,
                                    androidx.compose.ui.unit.TextUnitType.Sp
                                ),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Render staged image annotations on this page
                config.imageAnnotations.forEach { annot ->
                    if (annot.pageIndex == editPageIndex) {
                        val thumbnail = rememberThumbnailBitmap(context, Uri.parse(annot.imageUri))
                        Box(
                            modifier = Modifier
                                .align(
                                    Alignment.TopStart
                                )
                                .padding(
                                    start = (annot.x * 250).dp - 20.dp,
                                    top = (annot.y * 350).dp - 10.dp
                                )
                                .width((annot.width * 250).dp)
                                .height((annot.height * 350).dp)
                                .border(1.dp, accentColor, RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (thumbnail != null) {
                                Image(
                                    bitmap = thumbnail.asImageBitmap(),
                                    contentDescription = stringResource(R.string.annotation_image_preview),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.tool_stamp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Render currently staging coordinate selection box
                if (editAnnotationType == "text") {
                    Box(
                        modifier = Modifier
                            .align(
                                Alignment.TopStart
                            )
                            .padding(
                                start = (editX * 250).dp - 20.dp,
                                top = (editY * 350).dp - 10.dp
                            )
                            .border(1.dp, accentColor, RoundedCornerShape(4.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (editTextAnnotationInput.isEmpty()) {
                                stringResource(R.string.tool_text_tap)
                            } else {
                                editTextAnnotationInput
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .align(
                                Alignment.TopStart
                            )
                            .padding(
                                start = (editX * 250).dp - 20.dp,
                                top = (editY * 350).dp - 10.dp
                            )
                            .width((editWidth * 250).dp)
                            .height((editHeight * 350).dp)
                            .border(1.dp, accentColor, RoundedCornerShape(4.dp))
                            .background(accentColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.tool_image_stamp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }
        }

        Text(
            text = stringResource(
                R.string.tool_editor_position_help,
                (editX * 100).toInt(),
                (editY * 100).toInt()
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Mode Selector: Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "text" to stringResource(R.string.tool_text_annotation),
                "image" to stringResource(R.string.tool_image_stamp)
            ).forEach { (type, label) ->
                val isSel = editAnnotationType == type
                Card(
                    onClick = { editAnnotationType = type },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSel) accentColor else containerColor,
                        contentColor = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Box(Modifier.fillMaxWidth().clickable { editAnnotationType = type }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                        Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Tab options editor
        if (editAnnotationType == "text") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(R.string.tool_text_stamp_options),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = editTextAnnotationInput,
                        onValueChange = { editTextAnnotationInput = it },
                        label = { Text(stringResource(R.string.tool_text_content)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        stringResource(R.string.tool_font_size_points, editTextSize.toInt()),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Slider(
                        value = editTextSize,
                        onValueChange = { editTextSize = it },
                        valueRange = 8f..48f,
                        colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor)
                    )

                    Text(stringResource(R.string.tool_preset_colors), style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("#E74C3C" to Color(0xFFE74C3C), "#2980B9" to Color(0xFF2980B9), "#27AE60" to Color(0xFF27AE60), "#2C3E50" to Color(0xFF2C3E50), "#F39C12" to Color(0xFFF39C12)).forEach { (hex, cl) ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(cl)
                                    .border(if (editTextColor == hex) 3.dp else 0.dp, Color.White, CircleShape)
                                    .clickable { editTextColor = hex }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (editTextAnnotationInput.isNotEmpty()) {
                                val updated = config.textAnnotations + TextAnnotation(
                                    text = editTextAnnotationInput,
                                    x = editX,
                                    y = editY,
                                    colorHex = editTextColor,
                                    fontSize = editTextSize,
                                    pageIndex = editPageIndex
                                )
                                viewModel.editConfig.value = config.copy(textAnnotations = updated)
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.tool_text_annotation_staged),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text(stringResource(R.string.tool_stage_text_annotation))
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(R.string.tool_image_stamp_options),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { annotationImagePickerLauncher.launch(arrayOf("image/*")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = accentColor)
                    ) {
                        Text(
                            if (editImageUri != null) {
                                stringResource(R.string.tool_change_selected_image)
                            } else {
                                stringResource(R.string.tool_select_custom_image_stamp)
                            }
                        )
                    }

                    if (editImageUri != null) {
                        Text(
                            text = stringResource(
                                R.string.tool_selected_image,
                                editImageUri?.lastPathSegment.orEmpty()
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        stringResource(R.string.tool_stamp_width, (editWidth * 100).toInt()),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Slider(
                        value = editWidth,
                        onValueChange = { editWidth = it },
                        valueRange = 0.05f..0.8f,
                        colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor)
                    )

                    Text(
                        stringResource(R.string.tool_stamp_height, (editHeight * 100).toInt()),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Slider(
                        value = editHeight,
                        onValueChange = { editHeight = it },
                        valueRange = 0.05f..0.8f,
                        colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor)
                    )

                    Button(
                        onClick = {
                            if (editImageUri != null) {
                                val updated = config.imageAnnotations + ImageAnnotation(
                                    imageUri = editImageUri.toString(),
                                    x = editX,
                                    y = editY,
                                    width = editWidth,
                                    height = editHeight,
                                    pageIndex = editPageIndex
                                )
                                viewModel.editConfig.value = config.copy(imageAnnotations = updated)
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.tool_image_annotation_staged),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.tool_select_image_first),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        enabled = editImageUri != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text(stringResource(R.string.tool_stage_image_annotation))
                    }
                }
            }
        }

        // Staged layers summary list
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.tool_staged_layers_page, editPageIndex + 1),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                val pageTextAnnots = config.textAnnotations.filter { it.pageIndex == editPageIndex }
                val pageImgAnnots = config.imageAnnotations.filter { it.pageIndex == editPageIndex }

                if (pageTextAnnots.isEmpty() && pageImgAnnots.isEmpty()) {
                    Text(
                        text = stringResource(R.string.tool_no_annotations_staged),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        pageTextAnnots.forEach { annot ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        stringResource(R.string.tool_text_layer, annot.text),
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        stringResource(
                                            R.string.tool_text_layer_position,
                                            (annot.x * 100).toInt(),
                                            (annot.y * 100).toInt(),
                                            annot.fontSize.toInt()
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val updated = config.textAnnotations.toMutableList().apply { remove(annot) }
                                        viewModel.editConfig.value = config.copy(textAnnotations = updated)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = stringResource(R.string.tool_delete),
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        pageImgAnnots.forEach { annot ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        stringResource(R.string.tool_image_layer),
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        stringResource(
                                            R.string.tool_image_layer_position,
                                            (annot.x * 100).toInt(),
                                            (annot.y * 100).toInt(),
                                            (annot.width * 100).toInt(),
                                            (annot.height * 100).toInt()
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val updated = config.imageAnnotations.toMutableList().apply { remove(annot) }
                                        viewModel.editConfig.value = config.copy(imageAnnotations = updated)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = stringResource(R.string.tool_delete),
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
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
fun FormsToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color
) {
    val config by viewModel.formConfig.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val context = LocalContext.current
    var isFormLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(selectedFiles) {
        if (selectedFiles.isEmpty()) return@LaunchedEffect
        isFormLoaded = false
        try {
            val fields = viewModel.getFormFields(context, selectedFiles.first())
            val values = fields.associate { it.name to it.value }
            viewModel.formConfig.value = FormConfig(
                fields = fields,
                fieldValues = values
            )
            isFormLoaded = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.tool_form_fields_title),
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
        } else if (config.fields.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.tool_no_form_fields),
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
                    config.fields.forEach { field ->
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
                                    val isChecked = config.fieldValues[field.name] == "true"
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                val updatedMap = config.fieldValues.toMutableMap().apply {
                                                    put(field.name, checked.toString())
                                                }
                                                viewModel.formConfig.value = config.copy(fieldValues = updatedMap)
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = accentColor
                                            )
                                        )
                                        Text(
                                            text = if (isChecked) {
                                                stringResource(R.string.tool_checked)
                                            } else {
                                                stringResource(R.string.tool_unchecked)
                                            },
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                "choice" -> {
                                    var expanded by remember { mutableStateOf(false) }
                                    val selectedValue = config.fieldValues[field.name] ?: ""
                                    
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
                                                        val updatedMap = config.fieldValues.toMutableMap().apply {
                                                            put(field.name, option)
                                                        }
                                                        viewModel.formConfig.value = config.copy(fieldValues = updatedMap)
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    val textVal = config.fieldValues[field.name] ?: ""
                                    OutlinedTextField(
                                        value = textVal,
                                        onValueChange = {
                                            val updatedMap = config.fieldValues.toMutableMap().apply {
                                                put(field.name, it)
                                            }
                                            viewModel.formConfig.value = config.copy(fieldValues = updatedMap)
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
