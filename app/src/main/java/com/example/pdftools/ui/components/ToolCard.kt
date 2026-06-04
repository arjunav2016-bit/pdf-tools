package com.example.pdftools.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pdftools.data.PdfTool
import com.example.pdftools.theme.LocalDarkTheme
import kotlinx.coroutines.delay

@Composable
fun ToolCard(
    tool: PdfTool,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    animationDelay: Int = 0
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )
    val pressRotation by animateFloatAsState(
        targetValue = if (isPressed) {
            if (tool.id.hashCode() % 2 == 0) 1f else -1f
        } else {
            0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardRotation"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 1.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardElevation"
    )

    var entered by remember(tool.id, animationDelay) { mutableStateOf(false) }
    LaunchedEffect(tool.id, animationDelay) {
        delay(animationDelay.toLong())
        entered = true
    }
    val entranceAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(durationMillis = 320),
        label = "cardEntranceAlpha"
    )
    val entranceOffset by animateDpAsState(
        targetValue = if (entered) 0.dp else 14.dp,
        animationSpec = tween(durationMillis = 320),
        label = "cardEntranceOffset"
    )
    val density = LocalDensity.current

    val isDarkTheme = LocalDarkTheme.current
    val accentColor = remember(isDarkTheme, tool.category) {
        if (isDarkTheme) tool.category.darkAccentColor else tool.category.accentColor
    }
    val containerColor = remember(isDarkTheme, tool.category) {
        if (isDarkTheme) tool.category.darkContainerColor else tool.category.containerColor
    }

    val haptic = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .graphicsLayer {
                alpha = entranceAlpha
                translationY = with(density) { entranceOffset.toPx() }
                scaleX = pressScale
                scaleY = pressScale
                rotationZ = pressRotation
            }
            .defaultMinSize(minHeight = 132.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation,
            pressedElevation = elevation
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.name,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Tool name
            Text(
                text = tool.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Coming soon badge
            if (!tool.isImplemented) {
                Text(
                    text = "Coming Soon",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .graphicsLayer { alpha = 0.8f }
                )
            }
        }
    }
}
