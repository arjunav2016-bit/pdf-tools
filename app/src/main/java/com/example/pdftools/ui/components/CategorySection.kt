package com.example.pdftools.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.example.pdftools.data.PdfTool
import com.example.pdftools.data.ToolCategory
import kotlinx.coroutines.delay

@Composable
fun CategorySection(
    category: ToolCategory,
    tools: List<PdfTool>,
    onToolClick: (PdfTool) -> Unit,
    modifier: Modifier = Modifier,
    sectionIndex: Int = 0
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(sectionIndex * 100L)
        isVisible = true
    }

    val isDarkTheme = isSystemInDarkTheme()
    val accentColor = if (isDarkTheme) category.darkAccentColor else category.accentColor

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(400)) +
                slideInVertically(
                    animationSpec = tween(400),
                    initialOffsetY = { it / 4 }
                )
    ) {
        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            // Category header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                // Colored accent bar
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accentColor)
                )
                Text(
                    text = category.displayName.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = accentColor,
                    letterSpacing = MaterialTheme.typography.titleSmall.letterSpacing
                )
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                val columns = when {
                    maxWidth < 360.dp -> 2
                    maxWidth < 600.dp -> 3
                    else -> 4
                }
                val rowsCount = (tools.size + columns - 1) / columns

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (rowIdx in 0 until rowsCount) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (colIdx in 0 until columns) {
                                val itemIdx = rowIdx * columns + colIdx
                                if (itemIdx < tools.size) {
                                    val tool = tools[itemIdx]
                                    ToolCard(
                                        tool = tool,
                                        onClick = { onToolClick(tool) },
                                        animationDelay = itemIdx * 50,
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
