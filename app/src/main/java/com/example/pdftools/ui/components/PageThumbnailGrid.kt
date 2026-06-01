package com.example.pdftools.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pdftools.R
import kotlin.math.ceil

private const val ThumbnailWidthPx = 360

@Composable
fun PageThumbnailGrid(
    uri: Uri,
    pageCount: Int,
    selectedPages: Set<Int>,
    onTogglePage: (Int) -> Unit,
    accentColor: Color,
    loadThumbnail: suspend (Uri, Int, Int) -> Bitmap,
    modifier: Modifier = Modifier
) {
    val visibleRows = ceil(pageCount / 2f).toInt().coerceIn(1, 3)

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxWidth()
            .height((visibleRows * 196).dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(
            items = (0 until pageCount).toList(),
            key = { pageIndex -> pageIndex }
        ) { pageIndex ->
            PdfPageThumbnail(
                uri = uri,
                pageIndex = pageIndex,
                selected = pageIndex in selectedPages,
                accentColor = accentColor,
                loadThumbnail = loadThumbnail,
                onClick = { onTogglePage(pageIndex) }
            )
        }
    }
}

@Composable
fun PdfPageThumbnail(
    uri: Uri,
    pageIndex: Int,
    accentColor: Color,
    loadThumbnail: suspend (Uri, Int, Int) -> Bitmap,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) accentColor else MaterialTheme.colorScheme.outlineVariant,
        label = "thumbnailBorder"
    )
    val accessibilityLabel = if (selected) {
        stringResource(R.string.page_thumbnail_selected, pageIndex + 1)
    } else {
        stringResource(R.string.page_thumbnail, pageIndex + 1)
    }
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(186.dp)
            .then(clickModifier)
            .semantics { contentDescription = accessibilityLabel },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                PdfPagePreview(
                    uri = uri,
                    pageIndex = pageIndex,
                    loadThumbnail = loadThumbnail,
                    modifier = Modifier.fillMaxSize()
                )

                if (selected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = stringResource(R.string.selected),
                        tint = accentColor,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(22.dp)
                    )
                }
            }
            Text(
                text = stringResource(R.string.page_number, pageIndex + 1),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PdfPagePreview(
    uri: Uri,
    pageIndex: Int,
    loadThumbnail: suspend (Uri, Int, Int) -> Bitmap,
    modifier: Modifier = Modifier
) {
    val bitmap by produceState<Bitmap?>(
        initialValue = null,
        key1 = uri,
        key2 = pageIndex
    ) {
        value = runCatching {
            loadThumbnail(uri, pageIndex, ThumbnailWidthPx)
        }.getOrNull()
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (val pageBitmap = bitmap) {
            null -> CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
            else -> Image(
                bitmap = pageBitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.page_thumbnail, pageIndex + 1),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun SplitPageThumbnailGrid(
    uri: Uri,
    pageCount: Int,
    selectedPages: Set<Int>,
    onTogglePage: (Int) -> Unit,
    accentColor: Color,
    loadThumbnail: suspend (Uri, Int, Int) -> Bitmap,
    modifier: Modifier = Modifier
) {
    val visibleRows = ceil(pageCount / 2f).toInt().coerceIn(1, 3)

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxWidth()
            .height((visibleRows * 196).dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(
            items = (0 until pageCount).toList(),
            key = { pageIndex -> pageIndex }
        ) { pageIndex ->
            SplitPdfPageThumbnail(
                uri = uri,
                pageIndex = pageIndex,
                selected = pageIndex in selectedPages,
                accentColor = accentColor,
                loadThumbnail = loadThumbnail,
                onClick = { onTogglePage(pageIndex) }
            )
        }
    }
}

@Composable
fun SplitPdfPageThumbnail(
    uri: Uri,
    pageIndex: Int,
    accentColor: Color,
    loadThumbnail: suspend (Uri, Int, Int) -> Bitmap,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) accentColor else Color.Transparent,
        label = "splitThumbnailBorder"
    )
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .then(clickModifier)
            .semantics { 
                contentDescription = if (selected) {
                    "Page ${pageIndex + 1}, Selected"
                } else {
                    "Page ${pageIndex + 1}"
                }
            },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            if (selected) 2.dp else 1.dp, 
            if (selected) borderColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            PdfPagePreview(
                uri = uri,
                pageIndex = pageIndex,
                loadThumbnail = loadThumbnail,
                modifier = Modifier.fillMaxSize()
            )

            // Top-left circle badge with page number
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(28.dp)
                    .background(
                        color = if (selected) accentColor else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${pageIndex + 1}",
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Top-right checkmark (only when selected)
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .background(Color.White, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = stringResource(R.string.selected),
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RemovePageThumbnailGrid(
    uri: Uri,
    pageCount: Int,
    selectedPages: Set<Int>,
    onTogglePage: (Int) -> Unit,
    accentColor: Color,
    loadThumbnail: suspend (Uri, Int, Int) -> Bitmap,
    modifier: Modifier = Modifier
) {
    val visibleRows = ceil(pageCount / 2f).toInt().coerceIn(1, 3)

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxWidth()
            .height((visibleRows * 196).dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(
            items = (0 until pageCount).toList(),
            key = { pageIndex -> pageIndex }
        ) { pageIndex ->
            RemovePdfPageThumbnail(
                uri = uri,
                pageIndex = pageIndex,
                selected = pageIndex in selectedPages,
                accentColor = accentColor,
                loadThumbnail = loadThumbnail,
                onClick = { onTogglePage(pageIndex) }
            )
        }
    }
}

@Composable
fun RemovePdfPageThumbnail(
    uri: Uri,
    pageIndex: Int,
    accentColor: Color,
    loadThumbnail: suspend (Uri, Int, Int) -> Bitmap,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val destructiveRed = Color(0xFFC0392B)
    val borderColor by animateColorAsState(
        targetValue = if (selected) destructiveRed else Color.Transparent,
        label = "removeThumbnailBorder"
    )
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .then(clickModifier)
            .semantics { 
                contentDescription = if (selected) {
                    "Page ${pageIndex + 1}, Selected for removal"
                } else {
                    "Page ${pageIndex + 1}"
                }
            },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            if (selected) 2.dp else 1.dp, 
            if (selected) borderColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            PdfPagePreview(
                uri = uri,
                pageIndex = pageIndex,
                loadThumbnail = loadThumbnail,
                modifier = Modifier.fillMaxSize()
            )

            // Red overlay if selected for removal
            if (selected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(destructiveRed.copy(alpha = 0.2f))
                )
            }

            // Top-left circle badge with page number
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(28.dp)
                    .background(
                        color = if (selected) destructiveRed else Color.White,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${pageIndex + 1}",
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Central X overlay circle if selected for removal
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(destructiveRed, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.remove_file),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ExtractPagesThumbnailGrid(
    uri: Uri,
    pageCount: Int,
    selectedPages: Set<Int>,
    onTogglePage: (Int) -> Unit,
    accentColor: Color,
    loadThumbnail: suspend (Uri, Int, Int) -> Bitmap,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(
            items = (0 until pageCount).toList(),
            key = { pageIndex -> pageIndex }
        ) { pageIndex ->
            ExtractPdfPageThumbnail(
                uri = uri,
                pageIndex = pageIndex,
                selected = pageIndex in selectedPages,
                accentColor = accentColor,
                loadThumbnail = loadThumbnail,
                onClick = { onTogglePage(pageIndex) }
            )
        }
    }
}

@Composable
fun ExtractPdfPageThumbnail(
    uri: Uri,
    pageIndex: Int,
    accentColor: Color,
    loadThumbnail: suspend (Uri, Int, Int) -> Bitmap,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) accentColor else Color(0xFFE2E8F0),
        label = "extractThumbnailBorder"
    )
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .then(clickModifier),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = borderColor
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 4.dp else 1.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7FAFC))
        ) {
            ExtractPagePreview(
                uri = uri,
                pageIndex = pageIndex,
                loadThumbnail = loadThumbnail,
                modifier = Modifier.fillMaxSize()
            )

            // Bottom-left page number overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(Color(0xFF4A5568).copy(alpha = 0.8f), shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${pageIndex + 1}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Top-right checkmark badge
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .background(accentColor, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ExtractPagePreview(
    uri: Uri,
    pageIndex: Int,
    loadThumbnail: suspend (Uri, Int, Int) -> Bitmap,
    modifier: Modifier = Modifier
) {
    val bitmap by produceState<Bitmap?>(
        initialValue = null,
        key1 = uri,
        key2 = pageIndex
    ) {
        value = runCatching {
            loadThumbnail(uri, pageIndex, ThumbnailWidthPx)
        }.getOrNull()
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (val pageBitmap = bitmap) {
            null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFECEFF3))
                ) {
                    Icon(
                        imageVector = Icons.Filled.InsertDriveFile,
                        contentDescription = null,
                        tint = Color(0xFFA0AEC0),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            else -> {
                Image(
                    bitmap = pageBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun RemovePageThumbnail(
    uri: Uri,
    pageIndex: Int,
    loadThumbnail: suspend (Uri, Int, Int) -> Bitmap,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val removeRed = Color(0xFFC0392B)
    val borderColor by animateColorAsState(
        targetValue = if (selected) removeRed else Color.Transparent,
        label = "removeThumbnailBorder"
    )
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .then(clickModifier)
            .semantics {
                contentDescription = if (selected) {
                    "Page ${pageIndex + 1}, Marked for removal"
                } else {
                    "Page ${pageIndex + 1}"
                }
            },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            if (selected) 2.5.dp else 1.dp,
            if (selected) borderColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            PdfPagePreview(
                uri = uri,
                pageIndex = pageIndex,
                loadThumbnail = loadThumbnail,
                modifier = Modifier.fillMaxSize()
            )

            // Dark overlay + centered red X when selected for removal
            if (selected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(removeRed.copy(alpha = 0.9f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Remove",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Top-left circle badge with page number
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(28.dp)
                    .background(
                        color = if (selected) removeRed else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${pageIndex + 1}",
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DragHandleIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    Canvas(modifier = modifier.size(12.dp, 18.dp)) {
        val dotRadius = 2.dp.toPx()
        val gapX = 6.dp.toPx()
        val gapY = 6.dp.toPx()
        val startX = (size.width - gapX) / 2
        val startY = (size.height - 2 * gapY) / 2

        for (row in 0..2) {
            for (col in 0..1) {
                drawCircle(
                    color = color,
                    radius = dotRadius,
                    center = Offset(
                        x = startX + col * gapX,
                        y = startY + row * gapY
                    )
                )
            }
        }
    }
}


