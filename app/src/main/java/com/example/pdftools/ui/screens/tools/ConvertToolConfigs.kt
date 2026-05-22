package com.example.pdftools.ui.screens.tools

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pdftools.R
import com.example.pdftools.ui.screens.rememberThumbnailBitmap
import com.example.pdftools.ui.viewmodels.HtmlConfig
import com.example.pdftools.ui.viewmodels.ScanConfig
import com.example.pdftools.ui.viewmodels.ToolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color
) {
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val config by viewModel.scanConfig.collectAsState()
    val context = LocalContext.current

    // Synchronize rotation entries with selected files
    LaunchedEffect(selectedFiles) {
        val currentRotations = config.rotations.toMutableList()
        while (currentRotations.size < selectedFiles.size) {
            currentRotations.add(0)
        }
        while (currentRotations.size > selectedFiles.size) {
            currentRotations.removeAt(currentRotations.size - 1)
        }
        viewModel.scanConfig.value = config.copy(rotations = currentRotations)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.tool_captured_images),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        val chunks = selectedFiles.chunked(2)
        chunks.forEachIndexed { rowIndex, pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEachIndexed { colIndex, uri ->
                    val globalIndex = rowIndex * 2 + colIndex
                    val rotation = config.rotations.getOrNull(globalIndex) ?: 0
                    val thumbnail = rememberThumbnailBitmap(context, uri)

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.75f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (thumbnail != null) {
                                Image(
                                    bitmap = thumbnail.asImageBitmap(),
                                    contentDescription = stringResource(R.string.tool_image_thumbnail),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            rotationZ = rotation.toFloat()
                                        }
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = accentColor)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .size(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${globalIndex + 1}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        val currentRotations = config.rotations.toMutableList()
                                        if (globalIndex < currentRotations.size) {
                                            currentRotations[globalIndex] = (currentRotations[globalIndex] + 90) % 360
                                            viewModel.scanConfig.value = config.copy(rotations = currentRotations)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.RotateRight,
                                        contentDescription = stringResource(R.string.tool_rotate),
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.removeFile(globalIndex)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = stringResource(R.string.tool_remove),
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.tool_visual_enhancement_filter),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                "Original" to stringResource(R.string.tool_filter_original),
                "Grayscale" to stringResource(R.string.tool_filter_grayscale),
                "B&W Binarization" to stringResource(R.string.tool_filter_bw)
            )
            filters.forEach { (filterValue, label) ->
                val isSelected = if (filterValue == "Original") {
                    config.filter == "color" || config.filter == "Original"
                } else {
                    config.filter.contains(filterValue, ignoreCase = true) || filterValue == config.filter
                }
                val bg = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceContainerLow
                val tc = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                Card(
                    onClick = {
                        val newFilter = if (filterValue == "Original") "color" else filterValue
                        viewModel.scanConfig.value = config.copy(filter = newFilter)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = bg,
                        contentColor = tc
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HtmlToolConfig(
    viewModel: ToolViewModel,
    accentColor: Color
) {
    val config by viewModel.htmlConfig.collectAsState()
    val containerColor = MaterialTheme.colorScheme.surfaceContainerLow

    // We can infer template selection from the content of htmlContent or keep it local to UI
    var templateSelection by remember {
        mutableStateOf(
            when {
                config.htmlContent.contains("ACME Corp") -> "invoice"
                config.htmlContent.contains("Jane Sterling") -> "cv"
                config.htmlContent.contains("Technical Innovation Audit") -> "report"
                else -> ""
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.tool_html_designer_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = stringResource(R.string.tool_html_template_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val templates = listOf(
                "invoice" to stringResource(R.string.tool_html_template_invoice),
                "cv" to stringResource(R.string.tool_html_template_cv),
                "report" to stringResource(R.string.tool_html_template_report)
            )

            templates.forEach { (template, label) ->
                val isSel = templateSelection == template
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSel) accentColor else containerColor,
                        contentColor = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                templateSelection = template
                                val templateContent = when (template) {
                                    "invoice" -> """<!DOCTYPE html>
<html>
<head>
<style>
  body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #2C3E50; margin: 40px; line-height: 1.6; }
  .invoice-header { display: flex; justify-content: space-between; border-bottom: 2px solid #3498DB; padding-bottom: 20px; margin-bottom: 30px; }
  .logo { font-size: 28px; font-weight: bold; color: #3498DB; }
  .invoice-title { font-size: 24px; font-weight: bold; text-align: right; }
  .details-table { width: 100%; margin-bottom: 30px; }
  .details-table td { vertical-align: top; width: 50%; }
  .items-table { width: 100%; border-collapse: collapse; margin-top: 20px; }
  .items-table th { background-color: #3498DB; color: white; text-align: left; padding: 10px; font-weight: bold; }
  .items-table td { padding: 10px; border-bottom: 1px solid #BDC3C7; }
  .items-table tr:nth-child(even) { background-color: #F8F9F9; }
  .total-section { text-align: right; margin-top: 30px; font-size: 18px; font-weight: bold; }
  .footer { text-align: center; margin-top: 60px; font-size: 12px; color: #7F8C8D; border-top: 1px solid #ECF0F1; padding-top: 20px; }
</style>
</head>
<body>
  <div class="invoice-header">
    <div>
      <div class="logo">ACME Corp</div>
      <div>123 Innovation Way, Tech Suite 400<br>Silicon Valley, CA 94025</div>
    </div>
    <div class="invoice-title">
      INVOICE<br>
      <span style="font-size: 14px; font-weight: normal; color: #7F8C8D;">#INV-2026-0042<br>Date: May 21, 2026</span>
    </div>
  </div>
  <table class="details-table">
    <tr>
      <td>
        <strong style="color: #3498DB;">Billed To:</strong><br>
        John Doe Consulting<br>
        456 Business Road, Apt 2B<br>
        San Francisco, CA 94107
      </td>
      <td style="text-align: right;">
        <strong style="color: #3498DB;">Payment Details:</strong><br>
        Bank Transfer / ACH<br>
        Routing: XXXXXX789<br>
        Account: XXXXXXXX4560
      </td>
    </tr>
  </table>
  <table class="items-table">
    <thead>
      <tr>
        <th>Description</th>
        <th style="text-align: center; width: 80px;">Quantity</th>
        <th style="text-align: right; width: 120px;">Unit Price</th>
        <th style="text-align: right; width: 120px;">Total</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>Premium Android App UI & Architecture Consulting</td>
        <td style="text-align: center;">40 hrs</td>
        <td style="text-align: right;">$150.00</td>
        <td style="text-align: right;">$6,000.00</td>
      </tr>
      <tr>
        <td>PDF Conversion & Editing Engine Integration</td>
        <td style="text-align: center;">15 hrs</td>
        <td style="text-align: right;">$150.00</td>
        <td style="text-align: right;">$2,250.00</td>
      </tr>
      <tr>
        <td>Robolectric Offscreen WebView Test Bypass setup</td>
        <td style="text-align: center;">5 hrs</td>
        <td style="text-align: right;">$150.00</td>
        <td style="text-align: right;">$750.00</td>
      </tr>
    </tbody>
  </table>
  <div class="total-section">
    Subtotal: $9,000.00<br>
    Tax (0%): $0.00<br>
    <span style="color: #E74C3C; font-size: 22px;">Total Due: $9,000.00</span>
  </div>
  <div class="footer">
    Thank you for your business! Please pay within 30 days.
  </div>
</body>
</html>"""
                                    "cv" -> """<!DOCTYPE html>
<html>
<head>
<style>
  body { font-family: Arial, sans-serif; color: #2C3E50; margin: 40px; line-height: 1.5; background-color: #FFFFFF; }
  .header { border-bottom: 3px solid #2C3E50; padding-bottom: 15px; margin-bottom: 25px; }
  .name { font-size: 32px; font-weight: bold; color: #2C3E50; margin: 0; letter-spacing: 1px; }
  .title { font-size: 18px; color: #16A085; font-weight: bold; margin-top: 5px; }
  .contact { font-size: 13px; color: #7F8C8D; margin-top: 5px; }
  .section-title { font-size: 18px; font-weight: bold; color: #2C3E50; border-bottom: 1px solid #BDC3C7; padding-bottom: 5px; margin-top: 25px; margin-bottom: 15px; text-transform: uppercase; }
  .job-title { font-weight: bold; font-size: 15px; color: #34495E; }
  .company { font-style: italic; color: #16A085; }
  .date { float: right; color: #7F8C8D; font-size: 13px; }
  .skills-container { margin-top: 10px; }
  .skill-pill { background-color: #ECF0F1; color: #2C3E50; padding: 6px 12px; border-radius: 15px; font-size: 13px; display: inline-block; font-weight: 500; margin-right: 5px; margin-bottom: 5px; }
  .bullet-list { margin-top: 5px; padding-left: 20px; }
  .bullet-list li { margin-bottom: 4px; }
</style>
</head>
<body>
  <div class="header">
    <div class="name">Jane Sterling</div>
    <div class="title">Lead Mobile Solutions Architect & PDF Systems Expert</div>
    <div class="contact">jane.sterling@email.com | +1 (555) 019-2834 | San Francisco, CA</div>
  </div>
  
  <div class="section-title">Professional Summary</div>
  <p style="margin: 0; font-size: 14px; text-align: justify;">
    Innovative Software Engineer with over 8 years of specialized experience in high-performance Android mobile systems and document processing components. Proven record of designing and deploying offline-first vector PDF rendering solutions, custom hardware-accelerated drawing pipelines, and complex on-device database architectures.
  </p>

  <div class="section-title">Work Experience</div>
  <div>
    <span class="date">2023 - Present</span>
    <div class="job-title">Principal Mobile Systems Engineer - <span class="company">Quantum Tech Corp</span></div>
    <ul class="bullet-list" style="font-size: 14px;">
      <li>Engineered a fully local on-device PDF annotation framework utilizing PDFBox, driving relative touch coordinate mapping and lowering runtime memory footprint by 45%.</li>
      <li>Implemented an offscreen headless WebView vector conversion engine mapping dynamic HTML/CSS invoices directly to paper-ready PDFs.</li>
    </ul>
  </div>
  
  <div style="margin-top: 15px;">
    <span class="date">2019 - 2023</span>
    <div class="job-title">Senior Android Developer - <span class="company">Apex Mobile Lab</span></div>
    <ul class="bullet-list" style="font-size: 14px;">
      <li>Led core architectural rewrite of enterprise document editor from legacy Java to modern Jetpack Compose and Kotlin coroutines.</li>
      <li>Implemented stateful layering systems for on-page text and vector graphic stamps.</li>
    </ul>
  </div>

  <div class="section-title">Skills</div>
  <div class="skills-container">
    <span class="skill-pill">Kotlin / Java</span>
    <span class="skill-pill">Jetpack Compose</span>
    <span class="skill-pill">Android Print Manager</span>
    <span class="skill-pill">PDFBox Internals</span>
    <span class="skill-pill">WebView Engine Rendering</span>
    <span class="skill-pill">Robolectric Testing</span>
  </div>
</body>
</html>"""
                                    "report" -> """<!DOCTYPE html>
<html>
<head>
<style>
  body { font-family: Georgia, serif; color: #2C3E50; margin: 50px; line-height: 1.7; font-size: 15px; }
  .report-title { font-size: 34px; font-weight: bold; color: #2C3E50; margin-bottom: 5px; text-align: center; }
  .report-subtitle { font-size: 18px; color: #7F8C8D; text-align: center; margin-bottom: 30px; font-style: italic; }
  .meta-box { background-color: #F8F9F9; border-left: 4px solid #34495E; padding: 15px; margin-bottom: 40px; font-family: sans-serif; font-size: 13px; }
  h2 { font-family: sans-serif; font-size: 20px; color: #2C3E50; border-bottom: 2px solid #BDC3C7; padding-bottom: 5px; margin-top: 30px; }
  .highlight-card { background-color: #EBF5FB; border-radius: 12px; padding: 20px; margin: 20px 0; border: 1px dashed #3498DB; font-family: sans-serif; font-size: 14px; }
  p { text-align: justify; }
</style>
</head>
<body>
  <div class="report-title">Q2 Technical Innovation Audit</div>
  <div class="report-subtitle">On-Device PDF Processing Engines and Local Performance Benchmarks</div>
  
  <div class="meta-box">
    <strong>Author:</strong> Lead Architect Team<br>
    <strong>Department:</strong> Mobile & Document Core Platform<br>
    <strong>Date:</strong> May 21, 2026<br>
    <strong>Classification:</strong> Internal Enterprise Report
  </div>
  
  <h2>1. Executive Summary</h2>
  <p>
    This report outlines the technical findings of our Q2 migration of heavy document operations to local, offline-first execution environments on mobile nodes. Historically, operations such as HTML vector conversions and interactive document annotations were routed through remote print relays, adding significant latency and data transfer costs.
  </p>
  
  <div class="highlight-card">
    <strong>Core Breakthrough:</strong> By leveraging on-device offscreen WebView layouts driven programmatically by the Android Print Document Adapter, we successfully reduced average document conversion times from 4.2 seconds to <strong>380 milliseconds</strong>, guaranteeing 100% data privacy.
  </div>

  <h2>2. Performance & Memory Profiles</h2>
  <p>
    Stamping visual annotations to existing PDF streams was implemented with coordinate normalization. The coordinates mapped directly from Compose's relative bounding boxes to PDFBox's mathematical bottom-left matrices. By safely managing bitmap recycling streams immediately after graphic operations, garbage collection pressure remained stable, presenting zero Out Of Memory (OOM) heap exceptions during high-load tests.
  </p>
</body>
</html>"""
                                    else -> ""
                                }
                                viewModel.htmlConfig.value = config.copy(htmlContent = templateContent)
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.tool_html_custom_source),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = config.htmlContent,
            onValueChange = { viewModel.htmlConfig.value = config.copy(htmlContent = it) },
            placeholder = { Text(stringResource(R.string.tool_html_placeholder)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            shape = RoundedCornerShape(16.dp),
            maxLines = 15,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontSize = 12.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )

        Text(
            text = stringResource(R.string.tool_html_help),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
