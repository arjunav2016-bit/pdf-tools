package com.example.pdftools.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pdftools.data.PdfTool

/**
 * Data class for organizing pages in the Organize PDF tool.
 */
data class OrganizePageItem(
    val id: String,
    val originalIndex: Int,
    val rotation: Int
)

/**
 * Hero banner section at the top of each tool screen.
 */
@Composable
internal fun HeroSection(
    tool: PdfTool,
    accentColor: Color,
    containerColor: Color
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

/**
 * Dashed-border file picker drop zone.
 */
@Composable
internal fun FilePickerZone(
    accentColor: Color,
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

/**
 * Row item showing a selected file with a remove button.
 */
@Composable
internal fun FileItem(
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

/**
 * Success card displayed after processing completes.
 */
@Composable
internal fun SuccessCard(
    tool: PdfTool,
    outputUris: List<Uri>,
    onClear: () -> Unit,
    accentColor: Color,
    containerColor: Color,
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

/**
 * Handwritten signature drawing pad for Sign PDF tool.
 */
@Composable
fun SignaturePad(
    onSaveSignature: (Bitmap) -> Unit,
    onClearSignature: () -> Unit,
    accentColor: Color,
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
        border = BorderStroke(
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
                Canvas(modifier = Modifier.fillMaxSize()) {
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

/**
 * Visual preview card showing signature or redaction position on a simulated A4 page.
 */
@Composable
fun PositionPreviewCard(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    isRedaction: Boolean,
    accentColor: Color,
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
        border = BorderStroke(
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
                Canvas(modifier = Modifier.fillMaxSize()) {
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

/**
 * Remembers a downsampled thumbnail bitmap from a URI.
 */
@Composable
internal fun rememberThumbnailBitmap(context: Context, uri: Uri): Bitmap? {
    return remember(uri) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 8
                }
                BitmapFactory.decodeStream(stream, null, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
