package com.example.pdftools.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pdftools.R
import com.example.pdftools.data.PdfTool
import com.example.pdftools.data.RecentFile
import com.example.pdftools.data.ToolCategory
import com.example.pdftools.theme.LocalDarkTheme
import com.example.pdftools.ui.components.ShimmerFileList
import com.example.pdftools.ui.components.staggeredFadeIn
import com.example.pdftools.ui.viewmodels.HomeViewModel
import com.example.pdftools.ui.viewmodels.RecentViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onToolClick: (PdfTool) -> Unit,
    onViewAllClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    recentViewModel: RecentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val recents by recentViewModel.recents.collectAsState()
    var showRecentSkeleton by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(180)
        showRecentSkeleton = false
    }

    // Grab the tools we need
    val mergeTool = remember { viewModel.allTools.find { it.id == "merge_pdf" } }
    val editTool = remember { viewModel.allTools.find { it.id == "edit_pdf" } }
    val compressTool = remember { viewModel.allTools.find { it.id == "compress_pdf" } }
    val scanTool = remember { viewModel.allTools.find { it.id == "scan_to_pdf" } }
    val organizeTool = remember { viewModel.allTools.find { it.id == "organize_pdf" } }
    val rotateTool = remember { viewModel.allTools.find { it.id == "rotate_pdf" } }
    val signTool = remember { viewModel.allTools.find { it.id == "sign_pdf" } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            HomeTopAppBar(
                onRecentFilesClick = onViewAllClick,
                onSettingsClick = onSettingsClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Search Bar
            item {
                SearchPillBar()
            }

            // 2. Quick Actions
            item {
                QuickActionsSection(
                    onMergeClick = { mergeTool?.let(onToolClick) },
                    onEditClick = { editTool?.let(onToolClick) },
                    onCompressClick = { compressTool?.let(onToolClick) },
                    onScanClick = { scanTool?.let(onToolClick) }
                )
            }

            // 3. Recent Documents
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Documents",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "View all",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.clickable(onClick = onViewAllClick)
                    )
                }
            }

            // List up to 3 recent items
            val displayRecents = recents.take(3)
            if (showRecentSkeleton) {
                item {
                    ShimmerFileList(count = 3)
                }
            } else if (displayRecents.isEmpty()) {
                item {
                    Text(
                        text = "No recent documents",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                itemsIndexed(displayRecents, key = { _, recent -> recent.id }) { index, recent ->
                    RecentDocumentItem(
                        recent = recent,
                        tool = recentViewModel.getToolById(recent.toolId),
                        onOpen = { recentViewModel.deleteRecent(recent.id); recentViewModel.insertRecent(recent) }, // update timestamp on open
                        onDelete = { recentViewModel.deleteRecent(recent.id) },
                        onShare = {
                            Toast.makeText(context, "Sharing: ${recent.fileName}", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.staggeredFadeIn(index + 4)
                    )
                }
            }

            // 4. Popular Tools
            item {
                Text(
                    text = "Popular Tools",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // Main popular card: Organize PDF
            item {
                OrganizePdfPromoCard(
                    onClick = { organizeTool?.let(onToolClick) },
                    modifier = Modifier.staggeredFadeIn(7)
                )
            }

            // Sub popular tools grid (Rotate & Sign)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PopularToolSubCard(
                        title = "Rotate",
                        subtitle = "Fix orientation",
                        icon = Icons.AutoMirrored.Filled.RotateRight,
                        category = ToolCategory.ORGANIZE_PDF,
                        modifier = Modifier
                            .weight(1f)
                            .staggeredFadeIn(8),
                        onClick = { rotateTool?.let(onToolClick) }
                    )
                    PopularToolSubCard(
                        title = "Sign",
                        subtitle = "E-signature tools",
                        icon = Icons.Filled.Draw,
                        category = ToolCategory.EDIT_PDF,
                        modifier = Modifier
                            .weight(1f)
                            .staggeredFadeIn(9),
                        onClick = { signTool?.let(onToolClick) }
                    )
                }
            }

            // Spacer to prevent overlap with the bottom navigation
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar(
    onRecentFilesClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Home",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        actions = {
            IconButton(onClick = onRecentFilesClick) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = "Recent Files",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // User Avatar
            IconButton(onClick = onSettingsClick) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
private fun SearchPillBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Search documents, tools, files...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun QuickActionsSection(
    onMergeClick: () -> Unit,
    onEditClick: () -> Unit,
    onCompressClick: () -> Unit,
    onScanClick: () -> Unit
) {
    val isDark = LocalDarkTheme.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Merge - Organize category
            val organizeAccent = if (isDark) ToolCategory.ORGANIZE_PDF.darkAccentColor else ToolCategory.ORGANIZE_PDF.accentColor
            val organizeContainer = if (isDark) ToolCategory.ORGANIZE_PDF.darkContainerColor else ToolCategory.ORGANIZE_PDF.containerColor
            QuickActionItem(
                label = "Merge",
                icon = Icons.Filled.Layers,
                bgColor = organizeContainer,
                iconColor = organizeAccent,
                onClick = onMergeClick,
                modifier = Modifier.staggeredFadeIn(0)
            )

            // Edit - Edit category
            val editAccent = if (isDark) ToolCategory.EDIT_PDF.darkAccentColor else ToolCategory.EDIT_PDF.accentColor
            val editContainer = if (isDark) ToolCategory.EDIT_PDF.darkContainerColor else ToolCategory.EDIT_PDF.containerColor
            QuickActionItem(
                label = "Edit",
                icon = Icons.Filled.Edit,
                bgColor = editContainer,
                iconColor = editAccent,
                onClick = onEditClick,
                modifier = Modifier.staggeredFadeIn(1)
            )

            // Compress - Optimize category
            val optimizeAccent = if (isDark) ToolCategory.OPTIMIZE_PDF.darkAccentColor else ToolCategory.OPTIMIZE_PDF.accentColor
            val optimizeContainer = if (isDark) ToolCategory.OPTIMIZE_PDF.darkContainerColor else ToolCategory.OPTIMIZE_PDF.containerColor
            QuickActionItem(
                label = "Compress",
                icon = Icons.Filled.Compress,
                bgColor = optimizeContainer,
                iconColor = optimizeAccent,
                onClick = onCompressClick,
                modifier = Modifier.staggeredFadeIn(2)
            )

            // Scan uses the primary brand action color.
            QuickActionItem(
                label = "Scan",
                icon = Icons.Filled.DocumentScanner,
                bgColor = MaterialTheme.colorScheme.primary,
                iconColor = MaterialTheme.colorScheme.onPrimary,
                onClick = onScanClick,
                modifier = Modifier.staggeredFadeIn(3)
            )
        }
    }
}

@Composable
private fun QuickActionItem(
    label: String,
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun RecentDocumentItem(
    recent: RecentFile,
    tool: PdfTool?,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    val formattedDate = rememberFormattedDate(recent.timestamp)
    val sizeStr = rememberFileSize(recent.filePath, context)

    // Use theme-aware PDF icon colors
    val isDark = LocalDarkTheme.current
    val pdfIconBg = if (isDark) ToolCategory.ORGANIZE_PDF.darkContainerColor else ToolCategory.ORGANIZE_PDF.containerColor
    val pdfIconTint = if (isDark) ToolCategory.ORGANIZE_PDF.darkAccentColor else ToolCategory.ORGANIZE_PDF.accentColor

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onOpen()
                openFile(context, recent)
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

            // Title & Info
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

            // Options Menu
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
                            onOpen()
                            openFile(context, recent)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = {
                            showMenu = false
                            onShare()
                            shareFile(context, recent)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrganizePdfPromoCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalDarkTheme.current
    // Use a rich primary-driven gradient for the promo card
    val cardColor = if (isDark) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }
    val contentColor = if (isDark) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimary
    }
    val buttonContainerColor = if (isDark) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onPrimary
    }
    val buttonContentColor = if (isDark) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(cardColor)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        // Decorative grid on the right
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 10.dp, y = 5.dp)
                .graphicsLayer(rotationZ = -15f)
                .alpha(0.12f)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(36.dp).background(contentColor, RoundedCornerShape(6.dp)))
                    Box(modifier = Modifier.size(36.dp).background(contentColor, RoundedCornerShape(6.dp)))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(36.dp).background(contentColor, RoundedCornerShape(6.dp)))
                    Box(modifier = Modifier.size(36.dp).background(contentColor, RoundedCornerShape(6.dp)))
                }
            }
        }

        // Card Content
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Organize PDF",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = "Rearrange, delete or add\npages to your document easily.",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonContainerColor,
                    contentColor = buttonContentColor
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Try now",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PopularToolSubCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    category: ToolCategory,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = LocalDarkTheme.current
    val accentColor = if (isDark) category.darkAccentColor else category.accentColor
    val containerColor = if (isDark) category.darkContainerColor else category.containerColor

    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            // Text
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
        when {
            diff < oneDay && SimpleDateFormat("d", Locale.getDefault()).format(Date(now)) == SimpleDateFormat("d", Locale.getDefault()).format(Date(timestamp)) -> {
                "Today"
            }
            diff < 2 * oneDay -> {
                "Yesterday"
            }
            else -> {
                val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                formatter.format(Date(timestamp))
            }
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

private fun openFile(context: Context, recent: RecentFile) {
    if (recent.filePath.startsWith("file:///mock/")) {
        Toast.makeText(context, "Mock file opened: ${recent.fileName}", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val fileUri = android.net.Uri.parse(recent.filePath)
        val file = File(fileUri.path ?: "")
        if (!file.exists()) {
            Toast.makeText(context, context.getString(R.string.file_no_longer_exists), Toast.LENGTH_SHORT).show()
            return
        }
        val contentUri = androidx.core.content.FileProvider.getUriForFile(context, "com.example.pdftools.fileprovider", file)
        val mimeType = if (recent.fileName.endsWith(".pdf", ignoreCase = true)) "application/pdf" else "image/jpeg"
        
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, mimeType)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, context.getString(R.string.open_file)))
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.error_opening_file, e.localizedMessage), Toast.LENGTH_SHORT).show()
    }
}

private fun shareFile(context: Context, recent: RecentFile) {
    if (recent.filePath.startsWith("file:///mock/")) {
        Toast.makeText(context, "Mock file sharing: ${recent.fileName}", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val fileUri = android.net.Uri.parse(recent.filePath)
        val file = File(fileUri.path ?: "")
        if (!file.exists()) {
            Toast.makeText(context, context.getString(R.string.file_no_longer_exists), Toast.LENGTH_SHORT).show()
            return
        }
        val contentUri = androidx.core.content.FileProvider.getUriForFile(context, "com.example.pdftools.fileprovider", file)
        val mimeType = if (recent.fileName.endsWith(".pdf", ignoreCase = true)) "application/pdf" else "image/jpeg"
        
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, context.getString(R.string.share_file)))
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.error_sharing_file, e.localizedMessage), Toast.LENGTH_SHORT).show()
    }
}
