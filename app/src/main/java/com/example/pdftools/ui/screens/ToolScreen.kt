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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
        else -> "Process"
    }
}
