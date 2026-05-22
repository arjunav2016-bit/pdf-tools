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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
