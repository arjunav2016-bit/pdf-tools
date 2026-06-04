package com.example.pdftools.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

fun Modifier.staggeredFadeIn(
    index: Int,
    baseDelay: Int = 50
): Modifier = composed {
    var visible by remember(index, baseDelay) { mutableStateOf(false) }
    LaunchedEffect(index, baseDelay) {
        delay((index * baseDelay).toLong())
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "staggeredFadeAlpha"
    )
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 12.dp,
        animationSpec = tween(durationMillis = 350),
        label = "staggeredFadeOffset"
    )
    val density = LocalDensity.current

    graphicsLayer {
        this.alpha = alpha
        translationY = with(density) { offsetY.toPx() }
    }
}

fun Modifier.pressScale(
    interactionSource: MutableInteractionSource
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pressScale"
    )

    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
