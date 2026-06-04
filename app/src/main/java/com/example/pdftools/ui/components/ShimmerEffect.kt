package com.example.pdftools.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(
        initialValue = -600f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )
    val baseColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val highlightColor = MaterialTheme.colorScheme.surfaceContainerHigh

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                baseColor.copy(alpha = 0.55f),
                highlightColor.copy(alpha = 0.9f),
                baseColor.copy(alpha = 0.55f)
            ),
            start = Offset(offset, offset),
            end = Offset(offset + 320f, offset + 320f)
        )
    )
}

@Composable
fun ShimmerBox(
    width: Dp,
    height: Dp,
    shape: Shape,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(shape)
            .shimmer()
    )
}

@Composable
fun ShimmerToolCardGrid(
    columns: Int,
    rows: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(columns) {
                    BoxWithConstraints(modifier = Modifier.weight(1f)) {
                        ShimmerBox(
                            width = maxWidth,
                            height = 150.dp,
                            shape = RoundedCornerShape(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShimmerFileList(
    count: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(count) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerBox(
                    width = 44.dp,
                    height = 44.dp,
                    shape = RoundedCornerShape(8.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShimmerBox(
                        width = 180.dp,
                        height = 16.dp,
                        shape = RoundedCornerShape(8.dp)
                    )
                    ShimmerBox(
                        width = 120.dp,
                        height = 12.dp,
                        shape = RoundedCornerShape(6.dp)
                    )
                }
            }
        }
    }
}
