package com.example.pdftools.ui.screens.tools

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pdftools.data.PdfTool
import com.example.pdftools.ui.screens.SuccessCard
import com.example.pdftools.ui.screens.getFileNameFromUri
import com.example.pdftools.ui.screens.getFileSizeFromUri
import com.example.pdftools.ui.viewmodels.ToolViewModel
import com.example.pdftools.ui.viewmodels.ToolUiState
import com.example.pdftools.ui.viewmodels.FormConfig
import com.example.pdftools.data.FormFieldInfo
import java.util.UUID

enum class FormsStep {
    DASHBOARD,
    BUILDER,
    FILLER
}

data class BuilderField(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String, // "text", "checkbox", "radio", "dropdown"
    val x: Float, // percentage (0f..1f)
    val y: Float, // percentage (0f..1f)
    val width: Float = 0.35f,
    val height: Float = 0.05f,
    val isRequired: Boolean = false,
    val options: List<String> = emptyList(),
    val value: String = "",
    val borderStyle: String = "solid" // "solid", "dashed", "filled"
)

data class TemplateForm(
    val name: String,
    val fieldsCount: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val fields: List<BuilderField>
)

data class RecentFormDoc(
    val name: String,
    val fieldsFilled: Int,
    val totalFields: Int,
    val lastModified: String,
    val fields: List<BuilderField>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfFormsSurgicalScreen(
    tool: PdfTool,
    viewModel: ToolViewModel,
    selectedFiles: List<Uri>,
    isProcessing: Boolean,
    isComplete: Boolean,
    outputUris: List<Uri>,
    progress: Float?,
    innerPadding: PaddingValues,
    accentColor: Color,
    containerColor: Color,
    onPickFiles: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(FormsStep.DASHBOARD) }
    var activeIntent by remember { mutableStateOf("") } // "build" or "fill"

    // Builder State
    val builderFields = remember { mutableStateListOf<BuilderField>() }
    var activeFieldType by remember { mutableStateOf("text") } // "text", "checkbox", "radio", "dropdown"
    var selectedFieldIdForConfig by remember { mutableStateOf<String?>(null) }

    // Filler State
    var fillerFields = remember { mutableStateListOf<BuilderField>() }
    var activeFieldIndex by remember { mutableStateOf(0) }

    // Predefined templates
    val templates = remember {
        listOf(
            TemplateForm(
                name = "W-4 Form",
                fieldsCount = 5,
                icon = Icons.Filled.Assignment,
                fields = listOf(
                    BuilderField(name = "First Name", type = "text", x = 0.15f, y = 0.2f, isRequired = true),
                    BuilderField(name = "Last Name", type = "text", x = 0.5f, y = 0.2f, isRequired = true),
                    BuilderField(name = "SSN", type = "text", x = 0.15f, y = 0.35f, isRequired = true),
                    BuilderField(name = "Filing Status", type = "dropdown", x = 0.15f, y = 0.5f, options = listOf("Single", "Married filing jointly", "Head of household")),
                    BuilderField(name = "I agree to terms", type = "checkbox", x = 0.15f, y = 0.7f, isRequired = true)
                )
            ),
            TemplateForm(
                name = "Non-Disclosure (NDA)",
                fieldsCount = 4,
                icon = Icons.Filled.Gavel,
                fields = listOf(
                    BuilderField(name = "Disclosing Party", type = "text", x = 0.15f, y = 0.25f, isRequired = true),
                    BuilderField(name = "Receiving Party", type = "text", x = 0.15f, y = 0.4f, isRequired = true),
                    BuilderField(name = "Execution Date", type = "text", x = 0.15f, y = 0.6f, isRequired = true),
                    BuilderField(name = "Agree to terms", type = "checkbox", x = 0.15f, y = 0.75f, isRequired = true)
                )
            ),
            TemplateForm(
                name = "Rental Agreement",
                fieldsCount = 4,
                icon = Icons.Filled.Home,
                fields = listOf(
                    BuilderField(name = "Landlord Name", type = "text", x = 0.15f, y = 0.2f, isRequired = true),
                    BuilderField(name = "Tenant Name", type = "text", x = 0.15f, y = 0.35f, isRequired = true),
                    BuilderField(name = "Monthly Rent ($)", type = "text", x = 0.15f, y = 0.5f, isRequired = true),
                    BuilderField(name = "Pets Allowed", type = "checkbox", x = 0.15f, y = 0.65f)
                )
            ),
            TemplateForm(
                name = "Standard Invoice",
                fieldsCount = 3,
                icon = Icons.Filled.Receipt,
                fields = listOf(
                    BuilderField(name = "Invoice Number", type = "text", x = 0.15f, y = 0.25f, isRequired = true),
                    BuilderField(name = "Amount Due", type = "text", x = 0.15f, y = 0.4f, isRequired = true),
                    BuilderField(name = "Payment Terms", type = "dropdown", x = 0.15f, y = 0.55f, options = listOf("Due on Receipt", "Net 15", "Net 30"))
                )
            )
        )
    }

    // Predefined recent documents
    val recentDocs = remember {
        listOf(
            RecentFormDoc(
                name = "Employment_Contract_Draft.pdf",
                fieldsFilled = 3,
                totalFields = 5,
                lastModified = "Modified 2 hours ago",
                fields = listOf(
                    BuilderField(name = "Employee Name", type = "text", x = 0.15f, y = 0.2f, value = "Jane Doe", isRequired = true),
                    BuilderField(name = "Start Date", type = "text", x = 0.15f, y = 0.35f, value = "06/15/2026", isRequired = true),
                    BuilderField(name = "Salary Amount", type = "text", x = 0.15f, y = 0.5f, value = "$85,000", isRequired = true),
                    BuilderField(name = "Department", type = "dropdown", x = 0.15f, y = 0.65f, options = listOf("Engineering", "Marketing", "HR", "Sales")),
                    BuilderField(name = "Accept Overtime Policy", type = "checkbox", x = 0.15f, y = 0.8f)
                )
            ),
            RecentFormDoc(
                name = "Tax_Declaration_1040.pdf",
                fieldsFilled = 1,
                totalFields = 4,
                lastModified = "Modified 1 day ago",
                fields = listOf(
                    BuilderField(name = "Taxpayer Name", type = "text", x = 0.15f, y = 0.2f, value = "John Smith", isRequired = true),
                    BuilderField(name = "Tax Year", type = "text", x = 0.15f, y = 0.35f, isRequired = true),
                    BuilderField(name = "Total Income", type = "text", x = 0.15f, y = 0.5f, isRequired = true),
                    BuilderField(name = "Authorize Direct Deposit", type = "checkbox", x = 0.15f, y = 0.65f)
                )
            )
        )
    }

    // Auto-transition when file is picked
    LaunchedEffect(selectedFiles) {
        if (selectedFiles.isNotEmpty() && currentStep == FormsStep.DASHBOARD) {
            if (activeIntent == "build") {
                builderFields.clear()
                currentStep = FormsStep.BUILDER
            } else if (activeIntent == "fill") {
                // Read fields if available, otherwise launch builder
                try {
                    val fields = viewModel.getFormFields(context, selectedFiles.first())
                    if (fields.isNotEmpty()) {
                        fillerFields.clear()
                        fields.forEachIndexed { idx, field ->
                            fillerFields.add(
                                BuilderField(
                                    id = idx.toString(),
                                    name = field.name,
                                    type = when (field.type) {
                                        "checkbox" -> "checkbox"
                                        "choice" -> "dropdown"
                                        else -> "text"
                                    },
                                    x = 0.15f,
                                    y = 0.15f + idx * 0.12f,
                                    options = field.options,
                                    value = field.value
                                )
                            )
                        }
                        activeFieldIndex = 0
                        currentStep = FormsStep.FILLER
                    } else {
                        Toast.makeText(context, "No form fields found. Redirecting to Form Builder.", Toast.LENGTH_LONG).show()
                        builderFields.clear()
                        currentStep = FormsStep.BUILDER
                    }
                } catch (e: Exception) {
                    builderFields.clear()
                    currentStep = FormsStep.BUILDER
                }
            }
        }
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
                        currentStep = FormsStep.DASHBOARD
                        activeIntent = ""
                    },
                    accentColor = accentColor,
                    containerColor = containerColor
                )
            }
        } else {
            when (currentStep) {
                FormsStep.DASHBOARD -> {
                    FormsDashboard(
                        templates = templates,
                        recentDocs = recentDocs,
                        accentColor = accentColor,
                        containerColor = containerColor,
                        onNewBuild = {
                            activeIntent = "build"
                            onPickFiles()
                        },
                        onNewFill = {
                            activeIntent = "fill"
                            onPickFiles()
                        },
                        onLoadTemplate = { template ->
                            fillerFields.clear()
                            fillerFields.addAll(template.fields)
                            activeFieldIndex = 0
                            currentStep = FormsStep.FILLER
                        },
                        onLoadRecent = { recent ->
                            fillerFields.clear()
                            fillerFields.addAll(recent.fields)
                            activeFieldIndex = 0
                            currentStep = FormsStep.FILLER
                        }
                    )
                }
                FormsStep.BUILDER -> {
                    FormBuilderView(
                        selectedFile = selectedFiles.firstOrNull(),
                        builderFields = builderFields,
                        activeFieldType = activeFieldType,
                        accentColor = accentColor,
                        containerColor = containerColor,
                        selectedFieldId = selectedFieldIdForConfig,
                        onFieldSelectType = { activeFieldType = it },
                        onFieldSelectForConfig = { selectedFieldIdForConfig = it },
                        onBack = {
                            viewModel.resetCurrentRun()
                            currentStep = FormsStep.DASHBOARD
                        },
                        onStartFilling = {
                            fillerFields.clear()
                            fillerFields.addAll(builderFields)
                            activeFieldIndex = 0
                            currentStep = FormsStep.FILLER
                        }
                    )
                }
                FormsStep.FILLER -> {
                    FormFillingView(
                        selectedFile = selectedFiles.firstOrNull(),
                        fillerFields = fillerFields,
                        activeFieldIndex = activeFieldIndex,
                        accentColor = accentColor,
                        containerColor = containerColor,
                        onFieldIndexChange = { activeFieldIndex = it },
                        onFieldValChange = { idx, value ->
                            fillerFields[idx] = fillerFields[idx].copy(value = value)
                        },
                        onBack = {
                            if (selectedFiles.isEmpty()) {
                                currentStep = FormsStep.DASHBOARD
                            } else {
                                currentStep = FormsStep.BUILDER
                            }
                        },
                        onSubmit = {
                            // Populate values in viewModel formConfig and process
                            val valuesMap = fillerFields.associate { it.name to it.value }
                            val fieldsList = fillerFields.map { bf ->
                                FormFieldInfo(
                                    name = bf.name,
                                    type = when (bf.type) {
                                        "checkbox" -> "checkbox"
                                        "dropdown" -> "choice"
                                        else -> "text"
                                    },
                                    value = bf.value,
                                    options = bf.options
                                )
                            }
                            viewModel.formConfig.value = FormConfig(
                                fields = fieldsList,
                                fieldValues = valuesMap
                            )
                            viewModel.process("fill_pdf_fields", context)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FormsDashboard(
    templates: List<TemplateForm>,
    recentDocs: List<RecentFormDoc>,
    accentColor: Color,
    containerColor: Color,
    onNewBuild: () -> Unit,
    onNewFill: () -> Unit,
    onLoadTemplate: (TemplateForm) -> Unit,
    onLoadRecent: (RecentFormDoc) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Welcome and Primary Actions Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "PDF Forms Studio",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Convert static files to interactive forms or fill existing PDF documents easily on your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNewBuild,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Icon(Icons.Filled.Build, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Build Form", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onNewFill,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
                        border = BorderStroke(1.dp, accentColor)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fill Form", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Templates library
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Templates Library",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(templates) { template ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLoadTemplate(template) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = template.icon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = template.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${template.fieldsCount} interactive fields",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Recent documents
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Continue Filling",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (recentDocs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No recent documents found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    recentDocs.forEach { doc ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLoadRecent(doc) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = doc.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = doc.lastModified,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Progress tag
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${doc.fieldsFilled}/${doc.totalFields}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                    val progressPct = doc.fieldsFilled.toFloat() / doc.totalFields.toFloat()
                                    LinearProgressIndicator(
                                        progress = { progressPct },
                                        color = accentColor,
                                        trackColor = MaterialTheme.colorScheme.outlineVariant,
                                        modifier = Modifier
                                            .width(50.dp)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormBuilderView(
    selectedFile: Uri?,
    builderFields: MutableList<BuilderField>,
    activeFieldType: String,
    accentColor: Color,
    containerColor: Color,
    selectedFieldId: String?,
    onFieldSelectType: (String) -> Unit,
    onFieldSelectForConfig: (String?) -> Unit,
    onBack: () -> Unit,
    onStartFilling: () -> Unit
) {
    val context = LocalContext.current
    var showPropertiesSheet by remember { mutableStateOf(false) }

    // Display Properties Sheet when field selected
    LaunchedEffect(selectedFieldId) {
        if (selectedFieldId != null) {
            showPropertiesSheet = true
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Builder Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("PDF Form Builder", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onStartFilling,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Start Filling")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Interactive Document Canvas
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val xPct = offset.x / size.width
                            val yPct = offset.y / size.height
                            // Place field
                            val fieldName = "field_${builderFields.size + 1}"
                            val newField = BuilderField(
                                name = fieldName,
                                type = activeFieldType,
                                x = xPct,
                                y = yPct,
                                options = if (activeFieldType == "dropdown") listOf("Option 1", "Option 2") else emptyList()
                            )
                            builderFields.add(newField)
                            onFieldSelectForConfig(newField.id)
                        }
                    }
            ) {
                // Mock doc background representation
                SimulatedDocumentLines()

                // Render placed fields
                builderFields.forEach { field ->
                    val fieldLeft = maxWidth * field.x
                    val fieldTop = maxHeight * field.y
                    val fieldWidth = maxWidth * field.width
                    val fieldHeight = maxHeight * field.height

                    val isFieldSelected = field.id == selectedFieldId

                    Box(
                        modifier = Modifier
                            .offset(x = fieldLeft, y = fieldTop)
                            .size(width = fieldWidth, height = fieldHeight)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                color = if (isFieldSelected) accentColor.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                            .border(
                                width = if (isFieldSelected) 2.dp else 1.dp,
                                color = if (isFieldSelected) accentColor
                                else when (field.borderStyle) {
                                    "dashed" -> accentColor.copy(alpha = 0.5f)
                                    "filled" -> Color.Transparent
                                    else -> MaterialTheme.colorScheme.outline
                                },
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable {
                                onFieldSelectForConfig(field.id)
                            },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = when (field.type) {
                                        "checkbox" -> Icons.Filled.CheckBox
                                        "radio" -> Icons.Filled.RadioButtonChecked
                                        "dropdown" -> Icons.Filled.ArrowDropDownCircle
                                        else -> Icons.Filled.TextFields
                                    },
                                    contentDescription = null,
                                    tint = accentColor.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = field.name,
                                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (field.isRequired) {
                                Text(
                                    text = "*",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Floating Field Selector Capsule
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(24.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        "text" to "Text",
                        "checkbox" to "Check",
                        "radio" to "Radio",
                        "dropdown" to "List"
                    ).forEach { (type, label) ->
                        val isSelected = activeFieldType == type
                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) accentColor else Color.Transparent)
                                .clickable { onFieldSelectType(type) }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Contextual Properties sheet
    if (showPropertiesSheet && selectedFieldId != null) {
        val fieldIndex = builderFields.indexOfFirst { it.id == selectedFieldId }
        if (fieldIndex != -1) {
            val field = builderFields[fieldIndex]
            ModalBottomSheet(
                onDismissRequest = {
                    showPropertiesSheet = false
                    onFieldSelectForConfig(null)
                },
                dragHandle = { BottomSheetDefaults.DragHandle() },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                FieldPropertiesForm(
                    field = field,
                    accentColor = accentColor,
                    onUpdate = { updatedField ->
                        builderFields[fieldIndex] = updatedField
                    },
                    onDelete = {
                        builderFields.removeAt(fieldIndex)
                        showPropertiesSheet = false
                        onFieldSelectForConfig(null)
                    },
                    onClose = {
                        showPropertiesSheet = false
                        onFieldSelectForConfig(null)
                    }
                )
            }
        }
    }
}

@Composable
fun FieldPropertiesForm(
    field: BuilderField,
    accentColor: Color,
    onUpdate: (BuilderField) -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit
) {
    var newOptionText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Field Properties",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete field", tint = MaterialTheme.colorScheme.error)
            }
        }

        // Name
        OutlinedTextField(
            value = field.name,
            onValueChange = { onUpdate(field.copy(name = it)) },
            label = { Text("Field Label/Name") },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor),
            modifier = Modifier.fillMaxWidth()
        )

        // Required switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Required Field", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("Users must fill this field before submitting.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = field.isRequired,
                onCheckedChange = { onUpdate(field.copy(isRequired = it)) },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = accentColor)
            )
        }

        // Style selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Visual Border Style", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "solid" to "Solid Border",
                    "dashed" to "Dashed Border",
                    "filled" to "Frameless"
                ).forEach { (style, label) ->
                    val isSelected = field.borderStyle == style
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) accentColor.copy(alpha = 0.08f)
                                else MaterialTheme.colorScheme.surfaceContainer
                            )
                            .clickable { onUpdate(field.copy(borderStyle = style)) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Dropdown options list editor
        if (field.type == "dropdown") {
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Text("Dropdown Options", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

            // Horizontal chips list
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                field.options.forEach { opt ->
                    InputChip(
                        selected = false,
                        onClick = {},
                        label = { Text(opt) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        onUpdate(field.copy(options = field.options.filter { it != opt }))
                                    }
                            )
                        },
                        colors = InputChipDefaults.inputChipColors(
                            containerColor = accentColor.copy(alpha = 0.1f),
                            labelColor = accentColor
                        )
                    )
                }
            }

            // Add option input row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newOptionText,
                    onValueChange = { newOptionText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("New Option") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor),
                    shape = RoundedCornerShape(8.dp)
                )

                IconButton(
                    onClick = {
                        if (newOptionText.isNotBlank() && !field.options.contains(newOptionText.trim())) {
                            onUpdate(field.copy(options = field.options + newOptionText.trim()))
                            newOptionText = ""
                        }
                    },
                    modifier = Modifier.background(accentColor, RoundedCornerShape(8.dp))
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Option", tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text("Apply Properties", fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormFillingView(
    selectedFile: Uri?,
    fillerFields: List<BuilderField>,
    activeFieldIndex: Int,
    accentColor: Color,
    containerColor: Color,
    onFieldIndexChange: (Int) -> Unit,
    onFieldValChange: (Int, String) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    val activeField = fillerFields.getOrNull(activeFieldIndex)

    Column(modifier = Modifier.fillMaxSize()) {
        // Filler Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("Fill PDF Form", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Interactive Document Canvas
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                SimulatedDocumentLines()

                // Render placed fields overlay
                fillerFields.forEachIndexed { idx, field ->
                    val fieldLeft = maxWidth * field.x
                    val fieldTop = maxHeight * field.y
                    val fieldWidth = maxWidth * field.width
                    val fieldHeight = maxHeight * field.height

                    val isActive = idx == activeFieldIndex

                    Box(
                        modifier = Modifier
                            .offset(x = fieldLeft, y = fieldTop)
                            .size(width = fieldWidth, height = fieldHeight)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                color = if (isActive) accentColor.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
                            )
                            .border(
                                width = if (isActive) 2.dp else 1.dp,
                                color = if (isActive) accentColor else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable {
                                onFieldIndexChange(idx)
                            },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = field.value.ifEmpty { field.name },
                            style = TextStyle(fontSize = 11.sp, fontWeight = if (field.value.isNotEmpty()) FontWeight.Normal else FontWeight.Bold),
                            color = if (field.value.isNotEmpty()) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                    }
                }
            }
        }

        // Smart Bottom Input Navigation bar
        if (activeField != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Field Label and focus navigator row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = activeField.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (activeField.isRequired) {
                                Text(
                                    text = "*",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Navigation arrows
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onFieldIndexChange(activeFieldIndex - 1) },
                                enabled = activeFieldIndex > 0
                            ) {
                                Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Prev Field", modifier = Modifier.size(16.dp))
                            }

                            Text(
                                text = "Field ${activeFieldIndex + 1} of ${fillerFields.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            IconButton(
                                onClick = { onFieldIndexChange(activeFieldIndex + 1) },
                                enabled = activeFieldIndex < fillerFields.size - 1
                            ) {
                                Icon(Icons.Filled.ArrowForwardIos, contentDescription = "Next Field", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Field Input Widget
                    when (activeField.type) {
                        "checkbox" -> {
                            val isChecked = activeField.value == "true"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                    .clickable { onFieldValChange(activeFieldIndex, (!isChecked).toString()) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        onFieldValChange(activeFieldIndex, checked.toString())
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = accentColor)
                                )
                                Text(
                                    text = "Checked / Selected",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        "radio" -> {
                            val isSelected = activeField.value == "true"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                    .clickable { onFieldValChange(activeFieldIndex, (!isSelected).toString()) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onFieldValChange(activeFieldIndex, (!isSelected).toString()) },
                                    colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                                )
                                Text(
                                    text = "Selected Option",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        "dropdown" -> {
                            var dropdownExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = activeField.value,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        IconButton(onClick = { dropdownExpanded = true }) {
                                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                DropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    activeField.options.forEach { opt ->
                                        DropdownMenuItem(
                                            text = { Text(opt) },
                                            onClick = {
                                                onFieldValChange(activeFieldIndex, opt)
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        else -> {
                            // Text Input
                            OutlinedTextField(
                                value = activeField.value,
                                onValueChange = { onFieldValChange(activeFieldIndex, it) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor),
                                singleLine = true,
                                placeholder = { Text("Enter response...") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Primary submit button / action
                    val isLastField = activeFieldIndex == fillerFields.size - 1
                    Button(
                        onClick = {
                            if (isLastField) {
                                onSubmit()
                            } else {
                                onFieldIndexChange(activeFieldIndex + 1)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text(
                            text = if (isLastField) "Submit Form" else "Next Field",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SimulatedDocumentLines() {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val lineColor = Color.LightGray.copy(alpha = 0.35f)
        for (i in 1..18) {
            val lineY = i * (size.height / 20f)
            val end = if (i % 5 == 0) size.width * 0.45f else size.width * 0.88f
            drawLine(
                color = lineColor,
                start = Offset(size.width * 0.1f, lineY),
                end = Offset(end, lineY),
                strokeWidth = if (i % 5 == 0) 5f else 3f
            )
        }
    }
}
