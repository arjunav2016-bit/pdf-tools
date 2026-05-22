package com.example.pdftools.ui.screens.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pdftools.data.PdfTool
import com.example.pdftools.ui.screens.PositionPreviewCard
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
            text = if (tool.id == "protect_pdf") "Set Password" else "Enter Password",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = config.password,
            onValueChange = { viewModel.passwordConfig.value = config.copy(password = it) },
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

        if (totalPages > 1) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Target Page: ${config.pageIndex + 1} of $totalPages",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = config.pageIndex.toFloat(),
                    onValueChange = { viewModel.redactConfig.value = config.copy(pageIndex = it.toInt()) },
                    valueRange = 0f..(totalPages - 1).toFloat(),
                    steps = if (totalPages > 2) totalPages - 2 else 0,
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor
                    )
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Horizontal Offset (X): ${config.x.toInt()} pt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = config.x,
                    onValueChange = { viewModel.redactConfig.value = config.copy(x = it) },
                    valueRange = 0f..500f,
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor
                    )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Vertical Offset (Y): ${config.y.toInt()} pt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = config.y,
                    onValueChange = { viewModel.redactConfig.value = config.copy(y = it) },
                    valueRange = 0f..700f,
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor
                    )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Redaction Box Width: ${config.width.toInt()} pt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = config.width,
                    onValueChange = { viewModel.redactConfig.value = config.copy(width = it) },
                    valueRange = 10f..400f,
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor
                    )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Redaction Box Height: ${config.height.toInt()} pt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = config.height,
                    onValueChange = { viewModel.redactConfig.value = config.copy(height = it) },
                    valueRange = 10f..200f,
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor
                    )
                )
            }
        }

        OutlinedTextField(
            value = config.textToRedact,
            onValueChange = { viewModel.redactConfig.value = config.copy(textToRedact = it) },
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
            x = config.x,
            y = config.y,
            width = config.width,
            height = config.height,
            isRedaction = true,
            accentColor = accentColor
        )
    }
}
