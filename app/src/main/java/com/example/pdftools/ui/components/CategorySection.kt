package com.example.pdftools.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pdftools.data.PdfTool
import com.example.pdftools.data.ToolCategory
import com.example.pdftools.theme.LocalDarkTheme
import kotlinx.coroutines.delay

@Composable
fun CategorySection(
    category: ToolCategory,
    tools: List<PdfTool>,
    onToolClick: (PdfTool) -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember(category) { mutableStateOf(false) }

    LaunchedEffect(key1 = category) {
        delay(100)
        visible = true
    }

    val categoryIcon = remember(category) {
        when (category) {
            ToolCategory.ORGANIZE_PDF -> Icons.Filled.SwapVert
            ToolCategory.OPTIMIZE_PDF -> Icons.Filled.Speed
            ToolCategory.CONVERT_TO_PDF -> Icons.Filled.SwapHoriz
            ToolCategory.CONVERT_FROM_PDF -> Icons.Filled.Apps
            ToolCategory.EDIT_PDF -> Icons.Filled.Edit
            ToolCategory.PDF_SECURITY -> Icons.Filled.Security
        }
    }

    val isDarkTheme = LocalDarkTheme.current
    val accentColor = remember(isDarkTheme, category) {
        if (isDarkTheme) category.darkAccentColor else category.accentColor
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 400)) +
                slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = tween(durationMillis = 400)
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
                        .width(6.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(accentColor)
                )
                
                // Category Icon
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = stringResource(category.displayNameResId),
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = stringResource(category.displayNameResId),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(durationMillis = 250))
                ) {
                    Text(
                        text = if (tools.size == 1) "(1 tool)" else "(${tools.size} tools)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                val columns = (maxWidth / 180.dp).toInt().coerceIn(2, 4)
                val rowsCount = (tools.size + columns - 1) / columns

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
