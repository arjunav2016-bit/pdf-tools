package com.example.pdftools.ui.screens.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pdftools.R
import com.example.pdftools.data.PdfTool
import com.example.pdftools.data.UserPreferencesRepository
import com.example.pdftools.ui.components.PdfPagePreview
import com.example.pdftools.ui.viewmodels.FieldType
import com.example.pdftools.ui.viewmodels.PlacedField
import com.example.pdftools.ui.viewmodels.SignConfig
import com.example.pdftools.ui.viewmodels.ToolViewModel
import com.example.pdftools.ui.screens.SuccessCard
import com.example.pdftools.ui.screens.getFileNameFromUri
import com.example.pdftools.ui.screens.getFileSizeFromUri
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignPdfSurgicalScreen(
    tool: PdfTool,
    viewModel: ToolViewModel,
    selectedFiles: List<Uri>,
    pageCount: Int?,
    isProcessing: Boolean,
    isComplete: Boolean,
    outputUris: List<Uri>,
    progress: Float?,
    innerPadding: PaddingValues,
    accentColor: Color,
    containerColor: Color
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val selectedFile = selectedFiles.firstOrNull()
    val config by viewModel.signConfig.collectAsState()

    // Preferences repository to load/save signatures
    val userPrefsRepo = remember { UserPreferencesRepository(context) }
    val userPrefs by userPrefsRepo.preferences.collectAsState(initial = null)

    var currentStep by remember { mutableStateOf(1) } // 1: Config, 2: Place Fields
    var activePageIndex by remember { mutableStateOf(0) }
    var activeFieldIdForSignatureSelection by remember { mutableStateOf<String?>(null) }
    var showDrawSignatureModal by remember { mutableStateOf(false) }
    var activeFieldIdForTextEditing by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedFile) {
        selectedFile?.let { viewModel.loadPageCount(context, it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        if (isComplete) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                SuccessCard(
                    tool = tool,
                    outputUris = outputUris,
                    onClear = {
                        viewModel.resetCurrentRun()
                        currentStep = 1
                    },
                    accentColor = accentColor,
                    containerColor = containerColor
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Stepper Header
                StepperHeader(currentStep = currentStep, accentColor = accentColor)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (currentStep == 1) {
                        // Configuration Panel
                        SignConfigPanel(
                            config = config,
                            onConfigChange = { viewModel.signConfig.value = it },
                            accentColor = accentColor,
                            containerColor = containerColor,
                            onNext = {
                                if (config.signers.isEmpty()) {
                                    Toast.makeText(context, "Please add at least one signer", Toast.LENGTH_SHORT).show()
                                } else {
                                    currentStep = 2
                                }
                            }
                        )
                    } else {
                        // Interactive Placement Canvas
                        selectedFile?.let { uri ->
                            SignaturePlacementCanvas(
                                uri = uri,
                                pageCount = pageCount ?: 1,
                                activePageIndex = activePageIndex,
                                config = config,
                                onConfigChange = { viewModel.signConfig.value = it },
                                accentColor = accentColor,
                                containerColor = containerColor,
                                loadThumbnail = { u, idx, w -> viewModel.renderPage(context, u, idx, w) },
                                onPrevPage = { activePageIndex = (activePageIndex - 1).coerceAtLeast(0) },
                                onNextPage = { activePageIndex = (activePageIndex + 1).coerceAtMost((pageCount ?: 1) - 1) },
                                onSelectSignature = { fieldId ->
                                    activeFieldIdForSignatureSelection = fieldId
                                },
                                onEditText = { fieldId ->
                                    activeFieldIdForTextEditing = fieldId
                                },
                                onBack = { currentStep = 1 },
                                onProcess = {
                                    if (config.fields.isEmpty()) {
                                        Toast.makeText(context, "Please place at least one field before signing", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val incompleteFields = config.fields.filter {
                                            (it.type == FieldType.SIGNATURE || it.type == FieldType.INITIAL) && it.signatureUri == null
                                        }
                                        if (incompleteFields.isNotEmpty()) {
                                            Toast.makeText(context, "Please click on placement boxes to sign/initial", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.process(tool.id, context)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // 1. Stored Signature Selector / Vault bottom dialog
        if (activeFieldIdForSignatureSelection != null) {
            Dialog(onDismissRequest = { activeFieldIdForSignatureSelection = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Apply Signature Mark",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Choose a stored professional mark or create a new signature.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Library Horizontal List
                        val savedSigs = userPrefs?.savedSignatures ?: emptySet()
                        if (savedSigs.isNotEmpty()) {
                            Text(
                                text = "Your Stored Library",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(savedSigs.toList()) { path ->
                                    val bitmap = remember(path) {
                                        runCatching { BitmapFactory.decodeFile(path) }.getOrNull()
                                    }
                                    if (bitmap != null) {
                                        Box(
                                            modifier = Modifier
                                                .size(130.dp, 80.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White)
                                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                                .clickable {
                                                    // Set field's signature Uri
                                                    val fieldId = activeFieldIdForSignatureSelection ?: ""
                                                    viewModel.signConfig.value = config.copy(
                                                        fields = config.fields.map {
                                                            if (it.id == fieldId) it.copy(signatureUri = Uri.fromFile(File(path))) else it
                                                        }
                                                    )
                                                    activeFieldIdForSignatureSelection = null
                                                }
                                        ) {
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = "Signature image",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(6.dp),
                                                contentScale = ContentScale.Fit
                                            )

                                            // Delete icon
                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        userPrefsRepo.removeSavedSignature(path)
                                                        runCatching { File(path).delete() }
                                                    }
                                                },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(24.dp)
                                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.8f), CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete from vault",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(70.dp)
                                    .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Your signature vault is empty.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { activeFieldIdForSignatureSelection = null },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    showDrawSignatureModal = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                            ) {
                                Icon(Icons.Default.Gesture, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Draw New")
                            }
                        }
                    }
                }
            }
        }

        // 2. Custom Draw Modal Overlay
        if (showDrawSignatureModal) {
            DrawSignatureModal(
                accentColor = accentColor,
                onDismiss = { showDrawSignatureModal = false },
                onSave = { bitmap ->
                    try {
                        val signaturesDir = File(context.filesDir, "signatures").apply { mkdirs() }
                        val file = File(signaturesDir, "sig_${UUID.randomUUID()}.png")
                        FileOutputStream(file).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        
                        // Register in preferences database
                        scope.launch {
                            userPrefsRepo.addSavedSignature(file.absolutePath)
                        }

                        // Apply to the active field
                        val fieldId = activeFieldIdForSignatureSelection ?: ""
                        viewModel.signConfig.value = config.copy(
                            fields = config.fields.map {
                                if (it.id == fieldId) it.copy(signatureUri = Uri.fromFile(file)) else it
                            }
                        )

                        bitmap.recycle()
                        showDrawSignatureModal = false
                        activeFieldIdForSignatureSelection = null
                        Toast.makeText(context, "Saved & Inserted successfully", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Failed to save signature mark", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // 3. Text Editing dialog (for TEXT boxes)
        if (activeFieldIdForTextEditing != null) {
            val fieldId = activeFieldIdForTextEditing!!
            val field = config.fields.firstOrNull { it.id == fieldId }
            if (field != null) {
                var textVal by remember(fieldId) { mutableStateOf(field.value) }

                Dialog(onDismissRequest = { activeFieldIdForTextEditing = null }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Enter Text Value",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            OutlinedTextField(
                                value = textVal,
                                onValueChange = { textVal = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Type something...") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { activeFieldIdForTextEditing = null },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Cancel")
                                }

                                Button(
                                    onClick = {
                                        viewModel.signConfig.value = config.copy(
                                            fields = config.fields.map {
                                                if (it.id == fieldId) it.copy(value = textVal) else it
                                            }
                                        )
                                        activeFieldIdForTextEditing = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                                ) {
                                    Text("Apply")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Standard dynamic processing / progress popup overlay
        if (isProcessing) {
            Dialog(onDismissRequest = {}) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = accentColor,
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "Applying Signature Overlay...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepperHeader(currentStep: Int, accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Step 1
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(if (currentStep >= 1) accentColor else MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("1", color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Configure",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (currentStep == 1) FontWeight.Bold else FontWeight.Normal,
            color = if (currentStep == 1) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(16.dp))
        Divider(modifier = Modifier.width(32.dp), color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.width(16.dp))

        // Step 2
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(if (currentStep >= 2) accentColor else MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("2", color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Place & Draw",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (currentStep == 2) FontWeight.Bold else FontWeight.Normal,
            color = if (currentStep == 2) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignConfigPanel(
    config: SignConfig,
    onConfigChange: (SignConfig) -> Unit,
    accentColor: Color,
    containerColor: Color,
    onNext: () -> Unit
) {
    var newSignerName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Signers Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Document Signers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Manage recipients designated to sign this document.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Chips display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    config.signers.forEachIndexed { idx, name ->
                        InputChip(
                            selected = false,
                            onClick = {},
                            label = { Text(name) },
                            trailingIcon = {
                                if (idx > 0) { // Don't delete owner
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove signer",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                onConfigChange(
                                                    config.copy(
                                                        signers = config.signers.filterIndexed { i, _ -> i != idx },
                                                        currentSignerIndex = if (config.currentSignerIndex >= idx) {
                                                            (config.currentSignerIndex - 1).coerceAtLeast(0)
                                                        } else {
                                                            config.currentSignerIndex
                                                        }
                                                    )
                                                )
                                            }
                                    )
                                }
                            },
                            colors = InputChipDefaults.inputChipColors(
                                containerColor = accentColor.copy(alpha = 0.1f),
                                labelColor = accentColor
                            )
                        )
                    }
                }

                // Add Signer input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newSignerName,
                        onValueChange = { newSignerName = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Signer Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    IconButton(
                        onClick = {
                            if (newSignerName.isNotBlank()) {
                                onConfigChange(config.copy(signers = config.signers + newSignerName.trim()))
                                newSignerName = ""
                            }
                        },
                        modifier = Modifier
                            .background(accentColor, RoundedCornerShape(8.dp))
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Signer", tint = Color.White)
                    }
                }
            }
        }

        // 2. Security Configuration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Security & Verification",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // OTP Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Require SMS OTP Verification",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Verifies identity via instant passcode check.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = config.otpEnabled,
                        onCheckedChange = { onConfigChange(config.copy(otpEnabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.5f))
                    )
                }

                AnimatedVisibility(visible = config.otpEnabled) {
                    OutlinedTextField(
                        value = config.otpRecipient,
                        onValueChange = { onConfigChange(config.copy(otpRecipient = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Recipient Phone Number") },
                        placeholder = { Text("+1 (555) 019-2834") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Digital Certificate Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Apply Digital Cryptographic Certificate",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Embeds tamper-evident digital validation (AATL).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = config.digitalCertificateEnabled,
                        onCheckedChange = { onConfigChange(config.copy(digitalCertificateEnabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.5f))
                    )
                }

                AnimatedVisibility(visible = config.digitalCertificateEnabled) {
                    var menuExpanded by remember { mutableStateOf(false) }
                    val certs = listOf("ProForma Default CA", "Adobe Trust Certificate", "GlobalSign Premium Certificate")

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = config.selectedCertificate,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Select Authority Certificate") },
                            trailingIcon = {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            certs.forEach { cert ->
                                DropdownMenuItem(
                                    text = { Text(cert) },
                                    onClick = {
                                        onConfigChange(config.copy(selectedCertificate = cert))
                                        menuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Next Button
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Place Signature Fields", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun SignaturePlacementCanvas(
    uri: Uri,
    pageCount: Int,
    activePageIndex: Int,
    config: SignConfig,
    onConfigChange: (SignConfig) -> Unit,
    accentColor: Color,
    containerColor: Color,
    loadThumbnail: suspend (Uri, Int, Int) -> Bitmap,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    onSelectSignature: (String) -> Unit,
    onEditText: (String) -> Unit,
    onBack: () -> Unit,
    onProcess: () -> Unit
) {
    val localDensity = LocalDensity.current
    var canvasWidthPx by remember { mutableStateOf(100f) }
    var canvasHeightPx by remember { mutableStateOf(100f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toolbar with Quick Stamps
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolStampButton(icon = Icons.Default.Gesture, label = "Signature", accentColor = accentColor) {
                    val field = PlacedField(
                        id = UUID.randomUUID().toString(),
                        type = FieldType.SIGNATURE,
                        pageIndex = activePageIndex,
                        x = 0.25f, y = 0.4f, width = 0.35f, height = 0.12f
                    )
                    onConfigChange(config.copy(fields = config.fields + field))
                }

                ToolStampButton(icon = Icons.Default.Create, label = "Initials", accentColor = accentColor) {
                    val field = PlacedField(
                        id = UUID.randomUUID().toString(),
                        type = FieldType.INITIAL,
                        pageIndex = activePageIndex,
                        x = 0.4f, y = 0.6f, width = 0.2f, height = 0.08f
                    )
                    onConfigChange(config.copy(fields = config.fields + field))
                }

                ToolStampButton(icon = Icons.Default.DateRange, label = "Date", accentColor = accentColor) {
                    val dateStr = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date())
                    val field = PlacedField(
                        id = UUID.randomUUID().toString(),
                        type = FieldType.DATE,
                        pageIndex = activePageIndex,
                        x = 0.35f, y = 0.5f, width = 0.3f, height = 0.06f,
                        value = dateStr
                    )
                    onConfigChange(config.copy(fields = config.fields + field))
                }

                ToolStampButton(icon = Icons.Default.TextFields, label = "Text Box", accentColor = accentColor) {
                    val field = PlacedField(
                        id = UUID.randomUUID().toString(),
                        type = FieldType.TEXT,
                        pageIndex = activePageIndex,
                        x = 0.3f, y = 0.3f, width = 0.4f, height = 0.07f,
                        value = "Signer Name"
                    )
                    onConfigChange(config.copy(fields = config.fields + field))
                }
            }
        }

        // Active page indicator and navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevPage, enabled = activePageIndex > 0) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Prev page")
            }

            Text(
                text = "Page ${activePageIndex + 1} of $pageCount",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onNextPage, enabled = activePageIndex < pageCount - 1) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next page")
            }
        }

        // Render PDF page inside Box with overlays
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .onGloballyPositioned { coordinates ->
                    canvasWidthPx = coordinates.size.width.toFloat()
                    canvasHeightPx = coordinates.size.height.toFloat()
                },
            contentAlignment = Alignment.Center
        ) {
            PdfPagePreview(
                uri = uri,
                pageIndex = activePageIndex,
                loadThumbnail = loadThumbnail,
                modifier = Modifier.fillMaxSize()
            )

            // Render active fields overlaid
            config.fields.filter { it.pageIndex == activePageIndex }.forEach { field ->
                val xPx = field.x * canvasWidthPx
                val yPx = field.y * canvasHeightPx
                val wPx = field.width * canvasWidthPx
                val hPx = field.height * canvasHeightPx

                Box(
                    modifier = Modifier
                        .offset { IntOffset(xPx.roundToInt(), yPx.roundToInt()) }
                        .size(
                            width = with(localDensity) { wPx.toDp() },
                            height = with(localDensity) { hPx.toDp() }
                        )
                        .background(
                            color = when (field.type) {
                                FieldType.SIGNATURE -> Color(0xFF3498DB).copy(alpha = 0.12f)
                                FieldType.INITIAL -> Color(0xFF9B59B6).copy(alpha = 0.12f)
                                FieldType.DATE -> Color(0xFF2ECC71).copy(alpha = 0.12f)
                                FieldType.TEXT -> Color(0xFFF1C40F).copy(alpha = 0.12f)
                            },
                            shape = RoundedCornerShape(6.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = when (field.type) {
                                FieldType.SIGNATURE -> Color(0xFF3498DB)
                                FieldType.INITIAL -> Color(0xFF9B59B6)
                                FieldType.DATE -> Color(0xFF2ECC71)
                                FieldType.TEXT -> Color(0xFFF1C40F)
                            },
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable {
                            if (field.type == FieldType.SIGNATURE || field.type == FieldType.INITIAL) {
                                onSelectSignature(field.id)
                            } else if (field.type == FieldType.TEXT) {
                                onEditText(field.id)
                            }
                        }
                        .pointerInput(canvasWidthPx, canvasHeightPx) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val newX = (field.x + dragAmount.x / canvasWidthPx).coerceIn(0f, 1f - field.width)
                                val newY = (field.y + dragAmount.y / canvasHeightPx).coerceIn(0f, 1f - field.height)
                                onConfigChange(
                                    config.copy(
                                        fields = config.fields.map {
                                            if (it.id == field.id) it.copy(x = newX, y = newY) else it
                                        }
                                    )
                                )
                            }
                        }
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        when (field.type) {
                            FieldType.SIGNATURE, FieldType.INITIAL -> {
                                if (field.signatureUri != null) {
                                    val bitmap = remember(field.signatureUri) {
                                        runCatching {
                                            BitmapFactory.decodeFile(field.signatureUri.path)
                                        }.getOrNull()
                                    }
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(6.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = if (field.type == FieldType.SIGNATURE) Icons.Default.Gesture else Icons.Default.Create,
                                            contentDescription = null,
                                            tint = if (field.type == FieldType.SIGNATURE) Color(0xFF3498DB) else Color(0xFF9B59B6),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = if (field.type == FieldType.SIGNATURE) "Tap to Sign" else "Tap to Initial",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (field.type == FieldType.SIGNATURE) Color(0xFF2980B9) else Color(0xFF8E44AD)
                                        )
                                    }
                                }
                            }
                            FieldType.DATE, FieldType.TEXT -> {
                                Text(
                                    text = field.value,
                                    color = Color.Black,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }

                    // Trash / Delete icon top right
                    IconButton(
                        onClick = {
                            onConfigChange(
                                config.copy(
                                    fields = config.fields.filter { it.id != field.id }
                                )
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove field",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    // Bottom right resize handle corner
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(12.dp)
                            .background(
                                color = when (field.type) {
                                    FieldType.SIGNATURE -> Color(0xFF3498DB)
                                    FieldType.INITIAL -> Color(0xFF9B59B6)
                                    FieldType.DATE -> Color(0xFF2ECC71)
                                    FieldType.TEXT -> Color(0xFFF1C40F)
                                },
                                shape = RoundedCornerShape(topStart = 4.dp)
                            )
                            .pointerInput(canvasWidthPx, canvasHeightPx) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val deltaW = dragAmount.x / canvasWidthPx
                                    val deltaH = dragAmount.y / canvasHeightPx
                                    val newW = (field.width + deltaW).coerceIn(0.08f, 0.6f)
                                    val newH = (field.height + deltaH).coerceIn(0.03f, 0.25f)
                                    onConfigChange(
                                        config.copy(
                                            fields = config.fields.map {
                                                if (it.id == field.id) it.copy(width = newW, height = newH) else it
                                            }
                                        )
                                    )
                                }
                            }
                    )
                }
            }
        }

        // Action controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(0.4f)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Back")
            }

            Button(
                onClick = onProcess,
                modifier = Modifier
                    .weight(0.6f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sign Document", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ToolStampButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(accentColor.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = accentColor, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun DrawSignatureModal(
    accentColor: Color,
    onDismiss: () -> Unit,
    onSave: (Bitmap) -> Unit
) {
    val paths = remember { mutableStateListOf<List<Offset>>() }
    var currentPathPoints = remember { mutableStateOf<List<Offset>>(emptyList()) }
    var hasSigned by remember { mutableStateOf(false) }

    var canvasWidth by remember { mutableStateOf(0) }
    var canvasHeight by remember { mutableStateOf(0) }

    // Color choices: Black, Blue, Red
    val colors = listOf(Color.Black, Color(0xFF1E3A8A), Color(0xFFB91C1C))
    var selectedColorIndex by remember { mutableStateOf(0) }
    val inkColor = colors[selectedColorIndex]

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Draw Digital Signature",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close dialog")
                    }
                }

                // Ink Color Selectors
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Ink Color:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    colors.forEachIndexed { index, color ->
                        val isSelected = index == selectedColorIndex
                        val borderAlpha = if (isSelected) 1f else 0f
                        val borderScale by animateFloatAsState(targetValue = borderAlpha)

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .shadow(if (isSelected) 2.dp else 0.dp, CircleShape)
                                .background(color, CircleShape)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // Drawing Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
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
                        // Guideline dashed
                        val baselineY = size.height * 0.75f
                        drawLine(
                            color = Color.LightGray,
                            start = Offset(40f, baselineY),
                            end = Offset(size.width - 40f, baselineY),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )

                        // Render historic lines
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
                                    color = inkColor,
                                    style = Stroke(
                                        width = 6f,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }

                        // Render active in-progress path
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
                                color = inkColor,
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

                // Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            paths.clear()
                            currentPathPoints.value = emptyList()
                            hasSigned = false
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Clear Canvas")
                    }

                    Button(
                        onClick = {
                            if (paths.isEmpty() || canvasWidth == 0 || canvasHeight == 0) return@Button

                            // High-res signature bitmap
                            val highResW = 600
                            val highResH = 300
                            val bitmap = Bitmap.createBitmap(highResW, highResH, Bitmap.Config.ARGB_8888)
                            val canvas = android.graphics.Canvas(bitmap)

                            val paint = Paint().apply {
                                color = when (selectedColorIndex) {
                                    0 -> android.graphics.Color.BLACK
                                    1 -> android.graphics.Color.rgb(30, 58, 138)
                                    2 -> android.graphics.Color.rgb(185, 28, 28)
                                    else -> android.graphics.Color.BLACK
                                }
                                style = Paint.Style.STROKE
                                strokeWidth = 8f
                                strokeCap = Paint.Cap.ROUND
                                strokeJoin = Paint.Join.ROUND
                                isAntiAlias = true
                            }

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

                            onSave(bitmap)
                        },
                        enabled = hasSigned,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("Confirm Signature", color = Color.White)
                    }
                }
            }
        }
    }
}
