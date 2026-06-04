package com.example.pdftools.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.HistoryToggleOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pdftools.R
import com.example.pdftools.theme.LocalDarkTheme
import com.example.pdftools.ui.components.staggeredFadeIn
import com.example.pdftools.ui.viewmodels.RecentViewModel
import com.example.pdftools.data.RecentFile
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentScreen(
    modifier: Modifier = Modifier,
    scrollState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    viewModel: RecentViewModel = hiltViewModel()
) {
    val recents by viewModel.recents.collectAsState()
    val context = LocalContext.current
    var showClearConfirmation by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Clear History?") },
            text = { Text("Are you sure you want to clear your recent documents history? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmation = false
                        viewModel.clear()
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.recent_files),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.recent_files_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = stringResource(R.string.recent_files),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                actions = {
                    if (recents.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirmation = true }) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = stringResource(R.string.clear_all_history),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (recents.isEmpty()) {
                EmptyRecentsState()
            } else {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(recents, key = { _, recent -> recent.id }) { index, recent ->
                        val swipeState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.StartToEnd || value == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteRecent(recent.id)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = context.getString(R.string.removed_from_recents),
                                            actionLabel = context.getString(R.string.undo),
                                            duration = androidx.compose.material3.SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.insertRecent(recent)
                                        }
                                    }
                                    true
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = swipeState,
                            backgroundContent = { SwipeBackground(dismissState = swipeState) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .staggeredFadeIn(index)
                        ) {
                            RecentFileItem(
                                recent = recent,
                                tool = viewModel.getToolById(recent.toolId),
                                onOpen = { ctx -> openFile(ctx, recent) },
                                onShare = { ctx -> shareFile(ctx, recent) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentFileItem(
    recent: RecentFile,
    tool: com.example.pdftools.data.PdfTool?,
    onOpen: (Context) -> Unit,
    onShare: (Context) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    val formattedDate = rememberFormattedDate(recent.timestamp)
    val sizeStr = rememberFileSize(recent.filePath, context)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onOpen(context)
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PDF Icon Square (rounded)
            val isDark = LocalDarkTheme.current
            val pdfIconBg = if (isDark) com.example.pdftools.data.ToolCategory.ORGANIZE_PDF.darkContainerColor else com.example.pdftools.data.ToolCategory.ORGANIZE_PDF.containerColor
            val pdfIconTint = if (isDark) com.example.pdftools.data.ToolCategory.ORGANIZE_PDF.darkAccentColor else com.example.pdftools.data.ToolCategory.ORGANIZE_PDF.accentColor

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(pdfIconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PictureAsPdf,
                    contentDescription = "PDF File",
                    tint = pdfIconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = recent.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$formattedDate - $sizeStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Actions Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Open") },
                        onClick = {
                            showMenu = false
                            onOpen(context)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = {
                            showMenu = false
                            onShare(context)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberFormattedDate(timestamp: Long): String {
    return remember(timestamp) {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val oneDay = 24 * 60 * 60 * 1000L
        if (diff < oneDay && SimpleDateFormat("d", Locale.getDefault()).format(Date(now)) == SimpleDateFormat("d", Locale.getDefault()).format(Date(timestamp))) {
            "Today"
        } else if (diff < 2 * oneDay) {
            "Yesterday"
        } else {
            val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
    }
}

@Composable
private fun rememberFileSize(filePath: String, context: Context): String {
    return remember(filePath) {
        try {
            val uri = android.net.Uri.parse(filePath)
            getFileSizeFromUri(context, uri)
        } catch (e: Exception) {
            "Unknown size"
        }
    }
}


@Composable
private fun EmptyRecentsState() {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(400)) + scaleIn(tween(400)),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.HistoryToggleOff,
                    contentDescription = stringResource(R.string.no_recent_files),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.no_recent_files),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.no_recent_files_detail),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

private fun openFile(context: Context, recent: RecentFile) {
    try {
        val fileUri = Uri.parse(recent.filePath)
        val file = File(fileUri.path ?: "")
        if (!file.exists()) {
            Toast.makeText(context, context.getString(R.string.file_no_longer_exists), Toast.LENGTH_SHORT).show()
            return
        }
        val contentUri = FileProvider.getUriForFile(context, "com.example.pdftools.fileprovider", file)
        val mimeType = if (recent.fileName.endsWith(".pdf", ignoreCase = true)) "application/pdf" else "image/jpeg"
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.open_file)))
    } catch (e: Exception) {
        Toast.makeText(
            context,
            context.getString(R.string.error_opening_file, e.localizedMessage),
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun shareFile(context: Context, recent: RecentFile) {
    try {
        val fileUri = Uri.parse(recent.filePath)
        val file = File(fileUri.path ?: "")
        if (!file.exists()) {
            Toast.makeText(context, context.getString(R.string.file_no_longer_exists), Toast.LENGTH_SHORT).show()
            return
        }
        val contentUri = FileProvider.getUriForFile(context, "com.example.pdftools.fileprovider", file)
        val mimeType = if (recent.fileName.endsWith(".pdf", ignoreCase = true)) "application/pdf" else "image/jpeg"
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_file)))
    } catch (e: Exception) {
        Toast.makeText(
            context,
            context.getString(R.string.error_sharing_file, e.localizedMessage),
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
private fun StaggeredListItem(
    index: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        delay(index * 50L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 400)) +
                slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(durationMillis = 400)
                )
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBackground(dismissState: SwipeToDismissBoxState) {
    val direction = dismissState.dismissDirection
    val color = when (direction) {
        SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
        else -> Color.Transparent
    }
    
    val alignment = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        else -> Alignment.Center
    }
    
    val icon = Icons.Filled.Delete
    
    val padding = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> PaddingValues(start = 16.dp)
        SwipeToDismissBoxValue.EndToStart -> PaddingValues(end = 16.dp)
        else -> PaddingValues()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .padding(padding),
        contentAlignment = alignment
    ) {
        if (direction != SwipeToDismissBoxValue.Settled) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(R.string.tool_delete),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun Modifier.bounceClick(
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = LocalIndication.current,
            onClick = onClick
        )
}
