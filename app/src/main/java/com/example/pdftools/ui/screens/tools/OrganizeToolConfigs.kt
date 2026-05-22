package com.example.pdftools.ui.screens.tools

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pdftools.data.PdfProcessor
import com.example.pdftools.data.PdfTool
import com.example.pdftools.ui.screens.OrganizePageItem
import com.example.pdftools.ui.viewmodels.OrganizeConfig
import com.example.pdftools.ui.viewmodels.ToolViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageRangeToolConfig(
    viewModel: ToolViewModel,
    tool: PdfTool,
    accentColor: Color
) {
    val config by viewModel.pageRangeConfig.collectAsState()
    
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
            value = config.pageRange,
            onValueChange = { viewModel.pageRangeConfig.value = config.copy(pageRange = it) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RotateToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color
) {
    val config by viewModel.rotateConfig.collectAsState()
    
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
                val isSelected = config.degrees == angle
                val cardBg = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceContainerLow
                val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                
                Card(
                    onClick = { viewModel.rotateConfig.value = config.copy(degrees = angle) },
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
            value = config.pageRange,
            onValueChange = { viewModel.rotateConfig.value = config.copy(pageRange = it) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizeToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color
) {
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val context = LocalContext.current
    val organizePages = remember { mutableStateListOf<OrganizePageItem>() }

    LaunchedEffect(selectedFiles) {
        if (selectedFiles.isEmpty()) {
            organizePages.clear()
            viewModel.organizeConfig.value = OrganizeConfig(emptyList())
            return@LaunchedEffect
        }

        organizePages.clear()
        try {
            context.contentResolver.openInputStream(selectedFiles.first())?.use { input ->
                val tempFile = File.createTempFile("organize_page_count_", ".pdf", context.cacheDir)
                try {
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                    com.tom_roush.pdfbox.pdmodel.PDDocument.load(tempFile).use { doc ->
                        val list = mutableListOf<OrganizePageItem>()
                        for (i in 0 until doc.numberOfPages) {
                            list.add(
                                OrganizePageItem(
                                    id = java.util.UUID.randomUUID().toString(),
                                    originalIndex = i,
                                    rotation = 0
                                )
                            )
                        }
                        organizePages.addAll(list)
                        viewModel.organizeConfig.value = OrganizeConfig(
                            list.map { PdfProcessor.PageTransform(it.originalIndex, it.rotation) }
                        )
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
                        border = BorderStroke(
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
                                            viewModel.organizeConfig.value = OrganizeConfig(
                                                organizePages.map { PdfProcessor.PageTransform(it.originalIndex, it.rotation) }
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
                                            viewModel.organizeConfig.value = OrganizeConfig(
                                                organizePages.map { PdfProcessor.PageTransform(it.originalIndex, it.rotation) }
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
                                            viewModel.organizeConfig.value = OrganizeConfig(
                                                organizePages.map { PdfProcessor.PageTransform(it.originalIndex, it.rotation) }
                                            )
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
                                            viewModel.organizeConfig.value = OrganizeConfig(
                                                organizePages.map { PdfProcessor.PageTransform(it.originalIndex, it.rotation) }
                                            )
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
                                            viewModel.organizeConfig.value = OrganizeConfig(
                                                organizePages.map { PdfProcessor.PageTransform(it.originalIndex, it.rotation) }
                                            )
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
