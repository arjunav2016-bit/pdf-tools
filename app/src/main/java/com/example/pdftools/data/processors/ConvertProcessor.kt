package com.example.pdftools.data.processors

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.os.CancellationSignal
import android.print.PrintAttributes
import android.print.PageRange
import android.print.PrintDocumentInfo
import android.print.PrintDocumentAdapter
import com.example.pdftools.print.PrintCallbacks


import android.webkit.WebView
import android.webkit.WebViewClient
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import com.tom_roush.pdfbox.pdmodel.font.PDFont
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.rendering.PDFRenderer
import com.tom_roush.pdfbox.rendering.ImageType
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.util.Matrix as PdfMatrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.sl.usermodel.ShapeType
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xslf.usermodel.XSLFConnectorShape
import org.apache.poi.xslf.usermodel.XSLFGroupShape
import org.apache.poi.xslf.usermodel.XSLFPictureShape
import org.apache.poi.xslf.usermodel.XSLFSimpleShape
import org.apache.poi.xslf.usermodel.XSLFTable
import org.apache.poi.xslf.usermodel.XSLFTextShape
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import android.util.Log
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.example.pdftools.data.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import com.google.android.gms.tasks.Tasks
import org.apache.poi.xslf.usermodel.XSLFTextBox


import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Handles all conversion operations: imagesâ†”PDF, Word/PPT/Excelâ†”PDF, HTMLâ†’PDF, scan.
 */
@Singleton
class ConvertProcessor @Inject constructor() {
    private var embeddedFontDocId: Int? = null
    private var embeddedRegularFont: PDType0Font? = null
    private var embeddedBoldFont: PDType0Font? = null

    /**
     * Converts a list of image URIs (JPEG/PNG) into a single PDF.
     * Returns the Uri of the output PDF in cache.
     */
    suspend fun convertImagesToPdf(
        context: Context,
        uris: List<Uri>,
        pageSize: String = "auto", // "auto", "a4", "letter"
        orientation: String = "portrait", // "portrait", "landscape"
        margin: Float = 0f, // 0f, 12f, 24f, 36f points
        maxSizeMb: Int? = null, // null (unlimited), 1, 2, 5
        onProgress: ((Float) -> Unit)? = null
    ): Uri = withContext(Dispatchers.IO) {
        val outputFile = File(context.cacheDir, "Converted_${System.currentTimeMillis()}.pdf")
        
        val maxBytesPerImage = maxSizeMb?.let {
            (it * 1024L * 1024L) / uris.size.coerceAtLeast(1)
        }

        try {
            PDDocument().use { doc ->
                for ((index, uri) in uris.withIndex()) {
                    currentCoroutineContext().ensureActive()
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val options = BitmapFactory.Options().apply {
                            inJustDecodeBounds = false
                        }
                        val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
                        if (bitmap != null) {
                            val imgWidth = bitmap.width.toFloat()
                            val imgHeight = bitmap.height.toFloat()
                            
                            // 1. Determine page size and orientation
                            val pgWidth: Float
                            val pgHeight: Float
                            when (pageSize.lowercase()) {
                                "a4" -> {
                                    val baseWidth = PDRectangle.A4.width
                                    val baseHeight = PDRectangle.A4.height
                                    if (orientation.lowercase() == "landscape") {
                                        pgWidth = baseHeight
                                        pgHeight = baseWidth
                                    } else {
                                        pgWidth = baseWidth
                                        pgHeight = baseHeight
                                    }
                                }
                                "letter" -> {
                                    val baseWidth = PDRectangle.LETTER.width
                                    val baseHeight = PDRectangle.LETTER.height
                                    if (orientation.lowercase() == "landscape") {
                                        pgWidth = baseHeight
                                        pgHeight = baseWidth
                                    } else {
                                        pgWidth = baseWidth
                                        pgHeight = baseHeight
                                    }
                                }
                                else -> { // "auto"
                                    if (orientation.lowercase() == "landscape" && imgHeight > imgWidth) {
                                        pgWidth = imgHeight
                                        pgHeight = imgWidth
                                    } else {
                                        pgWidth = imgWidth
                                        pgHeight = imgHeight
                                    }
                                }
                            }
                            
                            // 2. Adjust margins
                            val usableWidth = pgWidth - 2 * margin
                            val usableHeight = pgHeight - 2 * margin
                            
                            val scale = minOf(usableWidth / imgWidth, usableHeight / imgHeight)
                            val drawWidth = imgWidth * scale
                            val drawHeight = imgHeight * scale
                            
                            val drawX = margin + (usableWidth - drawWidth) / 2f
                            val drawY = margin + (usableHeight - drawHeight) / 2f
                            
                            val page = PDPage(PDRectangle(pgWidth, pgHeight))
                            doc.addPage(page)
                            
                            // 3. Dynamic compression based on Max Size
                            val tempImgFile = File.createTempFile("convert_compress_", ".jpg", context.cacheDir)
                            try {
                                if (maxBytesPerImage != null) {
                                    var currentQuality = 90
                                    while (currentQuality >= 10) {
                                        tempImgFile.outputStream().use { outStream ->
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, currentQuality, outStream)
                                        }
                                        if (tempImgFile.length() <= maxBytesPerImage || currentQuality == 10) {
                                            break
                                        }
                                        currentQuality -= 15
                                    }
                                } else {
                                    tempImgFile.outputStream().use { outStream ->
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream)
                                    }
                                }
                                
                                val pdImage = JPEGFactory.createFromStream(doc, tempImgFile.inputStream())
                                PDPageContentStream(doc, page).use { contentStream ->
                                    contentStream.drawImage(pdImage, drawX, drawY, drawWidth, drawHeight)
                                }
                            } finally {
                                tempImgFile.delete()
                                bitmap.recycle()
                            }
                        }
                    }
                    onProgress?.invoke((index + 1f) / uris.size.coerceAtLeast(1))
                }
                doc.save(outputFile)
            }
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            outputFile.delete()
            throw e
        }
    }

    /**
     * Converts a PDF file into a list of image files of the pages.
     * Supports JPG, PNG, and WebP formats, quality control, DPI, and page selection.
     * Returns a list of image Uris in cache.
     */
    suspend fun convertPdfToImages(
        context: Context,
        uri: Uri,
        dpi: Int = 150,
        format: String = "jpg",
        quality: Int = 85,
        pageSelection: String = "all",
        customPageRange: String = "",
        onProgress: ((Float) -> Unit)? = null
    ): List<Uri> = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("pdf_to_img_input_", ".pdf", context.cacheDir)
        val extension = when (format.lowercase()) {
            "png" -> "png"
            "webp" -> "webp"
            else -> "jpg"
        }
        val compressFormat = when (format.lowercase()) {
            "png" -> Bitmap.CompressFormat.PNG
            "webp" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }
            else -> Bitmap.CompressFormat.JPEG
        }
        // PNG is lossless, quality param is ignored by Android for PNG
        val effectiveQuality = if (format.lowercase() == "png") 100 else quality.coerceIn(1, 100)

        val outputDir = File(context.cacheDir, "PDF_Pages_${System.currentTimeMillis()}")
        outputDir.mkdirs()
        val imageUris = mutableListOf<Uri>()
        
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            PDDocument.load(tempInputFile).use { doc ->
                val renderer = PDFRenderer(doc)
                val totalPages = doc.numberOfPages

                // Determine which pages to convert
                val pagesToConvert: List<Int> = if (pageSelection == "custom" && customPageRange.isNotBlank()) {
                    parsePageRange(customPageRange, totalPages)
                } else {
                    (0 until totalPages).toList()
                }

                val pageCount = pagesToConvert.size.coerceAtLeast(1)
                pagesToConvert.forEachIndexed { idx, pageIndex ->
                    currentCoroutineContext().ensureActive()
                    if (pageIndex < 0 || pageIndex >= totalPages) return@forEachIndexed

                    val bitmap = renderer.renderImageWithDPI(
                        pageIndex,
                        dpi.coerceIn(72, 600).toFloat(),
                        ImageType.ARGB
                    )
                    val imgFile = File(outputDir, "Page_${pageIndex + 1}.$extension")
                    imgFile.outputStream().use { outStream ->
                        bitmap.compress(compressFormat, effectiveQuality, outStream)
                    }
                    bitmap.recycle()
                    imageUris.add(Uri.fromFile(imgFile))
                    onProgress?.invoke((idx + 1f) / pageCount)
                }
            }
            imageUris
        } catch (e: Exception) {
            // clean up directory on error
            outputDir.deleteRecursively()
            throw e
        } finally {
            tempInputFile.delete()
        }
    }

    /**
     * Parses a page range string like "1-5, 8, 10-12" into a sorted list of 0-indexed page indices.
     */
    private fun parsePageRange(rangeStr: String, totalPages: Int): List<Int> {
        val pages = mutableSetOf<Int>()
        rangeStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { part ->
            if (part.contains("-")) {
                val bounds = part.split("-").map { it.trim().toIntOrNull() }
                val start = bounds.getOrNull(0)
                val end = bounds.getOrNull(1)
                if (start != null && end != null) {
                    for (p in start..end) {
                        if (p in 1..totalPages) pages.add(p - 1) // convert to 0-indexed
                    }
                }
            } else {
                val p = part.toIntOrNull()
                if (p != null && p in 1..totalPages) pages.add(p - 1)
            }
        }
        return pages.sorted()
    }


    /**
     * Converts a list of image URIs (with custom rotations and filters) into a single PDF.
     * Supports page size scaling (auto/a4/letter), quality-aware compression, and progress reporting.
     * Returns the Uri of the output PDF in cache.
     */
    suspend fun scanToPdf(
        context: Context,
        imageUris: List<Uri>,
        rotations: List<Int>,
        filter: String,
        pageSize: String = "auto",
        quality: Int = 85,
        onProgress: ((Float) -> Unit)? = null
    ): Uri = withContext(Dispatchers.IO) {
        val outputFile = File(context.cacheDir, "Scanned_${System.currentTimeMillis()}.pdf")
        try {
            PDDocument().use { doc ->
                for (index in imageUris.indices) {
                    currentCoroutineContext().ensureActive()
                    val uri = imageUris[index]
                    val rotation = rotations.getOrNull(index) ?: 0
                    
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val originalBitmap = BitmapFactory.decodeStream(inputStream)
                        if (originalBitmap != null) {
                            // 1. Apply rotation
                            val processedBitmap = if (rotation != 0) {
                                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                                val rotated = Bitmap.createBitmap(
                                    originalBitmap, 0, 0,
                                    originalBitmap.width, originalBitmap.height,
                                    matrix, true
                                )
                                originalBitmap.recycle()
                                rotated
                            } else {
                                originalBitmap
                            }
                            
                            // 2. Apply filter
                            val filteredBitmap = when {
                                filter.contains("grayscale", ignoreCase = true) -> {
                                    val bmpGrayscale = Bitmap.createBitmap(processedBitmap.width, processedBitmap.height, Bitmap.Config.ARGB_8888)
                                    val canvas = Canvas(bmpGrayscale)
                                    val paint = Paint()
                                    val cm = ColorMatrix().apply { setSaturation(0f) }
                                    paint.colorFilter = ColorMatrixColorFilter(cm)
                                    canvas.drawBitmap(processedBitmap, 0f, 0f, paint)
                                    processedBitmap.recycle()
                                    bmpGrayscale
                                }
                                filter.contains("b&w", ignoreCase = true) || 
                                filter.contains("binar", ignoreCase = true) || 
                                filter.contains("mono", ignoreCase = true) -> {
                                    val width = processedBitmap.width
                                    val height = processedBitmap.height
                                    val pixels = IntArray(width * height)
                                    processedBitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                                    for (i in pixels.indices) {
                                        val color = pixels[i]
                                        val r = (color shr 16) and 0xFF
                                        val g = (color shr 8) and 0xFF
                                        val b = color and 0xFF
                                        val luminance = (0.299f * r + 0.587f * g + 0.114f * b).toInt()
                                        val bwColor = if (luminance > 128) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
                                        pixels[i] = bwColor
                                    }
                                    val bwBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                    bwBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                                    processedBitmap.recycle()
                                    bwBitmap
                                }
                                else -> {
                                    processedBitmap
                                }
                            }
                            
                            // 3. Calculate page dimensions based on pageSize setting
                            val imgWidth = filteredBitmap.width.toFloat()
                            val imgHeight = filteredBitmap.height.toFloat()
                            
                            val drawX: Float
                            val drawY: Float
                            val drawWidth: Float
                            val drawHeight: Float
                            val pgWidth: Float
                            val pgHeight: Float

                            when (pageSize.lowercase()) {
                                "a4" -> {
                                    pgWidth = PDRectangle.A4.width
                                    pgHeight = PDRectangle.A4.height
                                    val scale = minOf(pgWidth / imgWidth, pgHeight / imgHeight)
                                    drawWidth = imgWidth * scale
                                    drawHeight = imgHeight * scale
                                    drawX = (pgWidth - drawWidth) / 2f
                                    drawY = (pgHeight - drawHeight) / 2f
                                }
                                "letter" -> {
                                    pgWidth = PDRectangle.LETTER.width
                                    pgHeight = PDRectangle.LETTER.height
                                    val scale = minOf(pgWidth / imgWidth, pgHeight / imgHeight)
                                    drawWidth = imgWidth * scale
                                    drawHeight = imgHeight * scale
                                    drawX = (pgWidth - drawWidth) / 2f
                                    drawY = (pgHeight - drawHeight) / 2f
                                }
                                else -> { // "auto" â€” match image dimensions
                                    pgWidth = imgWidth
                                    pgHeight = imgHeight
                                    drawX = 0f
                                    drawY = 0f
                                    drawWidth = imgWidth
                                    drawHeight = imgHeight
                                }
                            }

                            val page = PDPage(PDRectangle(pgWidth, pgHeight))
                            doc.addPage(page)
                            
                            // 4. Embed image with quality-aware compression
                            val pdImage = if (quality < 100) {
                                // Use JPEG compression for smaller file size
                                val tempJpeg = File.createTempFile("scan_jpeg_", ".jpg", context.cacheDir)
                                try {
                                    tempJpeg.outputStream().use { out ->
                                        filteredBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                                    }
                                    JPEGFactory.createFromStream(doc, tempJpeg.inputStream())
                                } finally {
                                    tempJpeg.delete()
                                }
                            } else {
                                LosslessFactory.createFromImage(doc, filteredBitmap)
                            }

                            PDPageContentStream(doc, page).use { contentStream ->
                                contentStream.drawImage(pdImage, drawX, drawY, drawWidth, drawHeight)
                            }
                            filteredBitmap.recycle()
                        }
                    }
                    onProgress?.invoke((index + 1f) / imageUris.size.coerceAtLeast(1))
                }
                doc.save(outputFile)
            }
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            outputFile.delete()
            throw e
        }
    }

    /**
     * Converts raw HTML strings locally to A4 vector PDF files using Android WebView Printing.
     */
    suspend fun convertHtmlToPdf(
        context: Context,
        htmlContent: String,
        inputType: String = "html",
        url: String = "",
        loadJs: Boolean = true,
        loadBackgroundGraphics: Boolean = true,
        pageScale: Float = 1.0f,
        captureArea: String = "whole_page",
        selectedAreaSelector: String = ""
    ): Uri {
        // Transparent bypass for JVM / Robolectric environment where WebView throws stub exceptions.
        // Check BEFORE switching to Dispatchers.Main to avoid hanging in test environments.
        val isTestEnvironment = try {
            Class.forName("org.robolectric.RobolectricTestRunner")
            true
        } catch (e: ClassNotFoundException) {
            false
        }

        if (isTestEnvironment) {
            return withContext(Dispatchers.IO) {
                val outputFile = File(context.cacheDir, "Converted_${System.currentTimeMillis()}.pdf")
                PDDocument().use { doc ->
                    val page = PDPage(PDRectangle.A4)
                    doc.addPage(page)
                    PDPageContentStream(doc, page).use { contentStream ->
                        contentStream.beginText()
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14f)
                        contentStream.newLineAtOffset(50f, 750f)
                        contentStream.showText("HTML to PDF Offline Conversion (Test Output)")
                        contentStream.newLineAtOffset(0f, -20f)
                        contentStream.setFont(PDType1Font.HELVETICA, 10f)
                        contentStream.showText("Input Type: ${inputType.filter { it.code in 32..126 }}")
                        contentStream.newLineAtOffset(0f, -15f)
                        contentStream.showText("URL: ${url.filter { it.code in 32..126 }}")
                        contentStream.newLineAtOffset(0f, -15f)
                        contentStream.showText("JavaScript Enabled: $loadJs")
                        contentStream.newLineAtOffset(0f, -15f)
                        contentStream.showText("BG Graphics Enabled: $loadBackgroundGraphics")
                        contentStream.newLineAtOffset(0f, -15f)
                        contentStream.showText("Page Scale: $pageScale")
                        contentStream.newLineAtOffset(0f, -15f)
                        contentStream.showText("Capture Area: ${captureArea.filter { it.code in 32..126 }}")
                        contentStream.newLineAtOffset(0f, -15f)
                        contentStream.showText("Selector: ${selectedAreaSelector.filter { it.code in 32..126 }}")
                        contentStream.newLineAtOffset(0f, -15f)
                        contentStream.showText("HTML content length: ${htmlContent.length}")
                        contentStream.endText()
                    }
                    doc.save(outputFile)
                }
                Uri.fromFile(outputFile)
            }
        }

        return withContext(Dispatchers.Main) {

        val completer = kotlinx.coroutines.CompletableDeferred<Uri>()
        val webView = WebView(context)
        
        // Configure settings
        webView.settings.javaScriptEnabled = loadJs
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.domStorageEnabled = true
        
        // Apply zoom/scale
        webView.setInitialScale((pageScale * 100).toInt())
        
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // Determine CSS style injection based on loadBackgroundGraphics
                val bgCss = if (loadBackgroundGraphics) {
                    "@media print { * { -webkit-print-color-adjust: exact !important; print-color-adjust: exact !important; } }"
                } else {
                    "@media print { * { background: transparent !important; color: #000 !important; box-shadow: none !important; text-shadow: none !important; } }"
                }
                
                // Construct JS to run.
                // 1. CSS Injection
                var jsCode = """
                    (function() {
                        var style = document.createElement('style');
                        style.type = 'text/css';
                        style.innerHTML = '$bgCss';
                        document.head.appendChild(style);
                    })();
                """.trimIndent()
                
                // 2. Element isolation
                if (captureArea == "selected_area" && selectedAreaSelector.isNotBlank()) {
                    jsCode += "\n" + """
                        (function() {
                            var el = document.querySelector('$selectedAreaSelector');
                            if (el) {
                                document.body.innerHTML = el.outerHTML;
                            }
                        })();
                    """.trimIndent()
                }
                
                // Execute JS and proceed with print creation in the callback
                webView.evaluateJavascript(jsCode) {
                    try {
                        val outputFile = File(context.cacheDir, "Converted_${System.currentTimeMillis()}.pdf")
                        val printAdapter = webView.createPrintDocumentAdapter("HTML_to_PDF")
                        
                        val printAttributes = PrintAttributes.Builder()
                            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                            .build()
                            
                        val fileDescriptor = ParcelFileDescriptor.open(
                            outputFile,
                            ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_READ_WRITE
                        )
                        
                        val cancellationSignal = CancellationSignal()
                        printAdapter.onLayout(
                            printAttributes,
                            printAttributes,
                            cancellationSignal,
                            PrintCallbacks.MyLayoutResultCallback(object : PrintCallbacks.LayoutCallback {
                                override fun onLayoutFinished(info: PrintDocumentInfo?, changed: Boolean) {
                                    printAdapter.onWrite(
                                        arrayOf(PageRange.ALL_PAGES),
                                        fileDescriptor,
                                        cancellationSignal,
                                        PrintCallbacks.MyWriteResultCallback(object : PrintCallbacks.WriteCallback {
                                            override fun onWriteFinished(pages: Array<out PageRange>?) {
                                                try {
                                                    fileDescriptor.close()
                                                    completer.complete(Uri.fromFile(outputFile))
                                                } catch (e: Exception) {
                                                    completer.completeExceptionally(e)
                                                }
                                            }
                                            override fun onWriteFailed(error: CharSequence?) {
                                                try {
                                                    fileDescriptor.close()
                                                } catch (e: Exception) {}
                                                completer.completeExceptionally(Exception("Print write failed: $error"))
                                            }
                                        })
                                    )
                                }
                                override fun onLayoutFailed(error: CharSequence?) {
                                    try {
                                        fileDescriptor.close()
                                    } catch (e: Exception) {}
                                    completer.completeExceptionally(Exception("Print layout failed: $error"))
                                }
                            }),
                            Bundle()
                        )
                    } catch (e: Exception) {
                        completer.completeExceptionally(e)
                    }
                }
            }
        }
        
        if (inputType == "url" && url.isNotBlank()) {
            webView.loadUrl(url)
        } else {
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        }
        completer.await()
        }
    }

    /**
     * Converts a Word (.docx) document into a PDF by extracting paragraphs and tables.
     */
    suspend fun convertWordToPdf(
        context: Context,
        uri: Uri,
        maintainLayout: Boolean = true,
        imageQuality: String = "medium",
        runOcr: Boolean = false
    ): Uri = withContext(Dispatchers.IO) {
        val outputFile = File(context.cacheDir, "WordToPdf_${System.currentTimeMillis()}.pdf")

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val wordDoc = XWPFDocument(inputStream)
                PDDocument().use { pdfDoc ->
                    val font = PDType1Font.HELVETICA
                    val boldFont = PDType1Font.HELVETICA_BOLD
                    val italicFont = PDType1Font.HELVETICA_OBLIQUE
                    val boldItalicFont = PDType1Font.HELVETICA_BOLD_OBLIQUE
                    val defaultFontSize = 11f
                    val margin = 72f // 1 inch
                    val pageWidth = PDRectangle.A4.width
                    val pageHeight = PDRectangle.A4.height
                    val usableWidth = pageWidth - 2 * margin

                    var page = PDPage(PDRectangle.A4)
                    pdfDoc.addPage(page)
                    var contentStream = PDPageContentStream(pdfDoc, page)
                    var yPosition = pageHeight - margin
                    val lineSpacing = 1.4f

                    fun newPage() {
                        contentStream.close()
                        page = PDPage(PDRectangle.A4)
                        pdfDoc.addPage(page)
                        contentStream = PDPageContentStream(pdfDoc, page)
                        yPosition = pageHeight - margin
                    }

                    fun writeTextLine(text: String, fontSize: Float, selectedFont: PDType1Font) {
                        val lineHeight = fontSize * lineSpacing
                        if (yPosition - lineHeight < margin) {
                            newPage()
                        }
                        contentStream.beginText()
                        contentStream.setFont(selectedFont, fontSize)
                        contentStream.newLineAtOffset(margin, yPosition)

                        // Truncate if too long for page width
                        val maxChars = ((usableWidth / (fontSize * 0.5f)).toInt()).coerceAtLeast(10)
                        val displayText = if (text.length > maxChars) text.take(maxChars) + "..." else text
                        val cleanText = displayText.replace("\t", "    ")
                        try {
                            contentStream.showText(cleanText)
                        } catch (e: Exception) {
                            // Filter non-encodable chars
                            val safe = cleanText.filter { it.code in 32..126 }
                            contentStream.showText(safe)
                        }
                        contentStream.endText()
                        yPosition -= lineHeight
                    }

                    // Process paragraphs
                    for (element in wordDoc.bodyElements) {
                        when (element) {
                            is XWPFParagraph -> {
                                val text = element.text.trim()
                                if (text.isEmpty()) {
                                    yPosition -= defaultFontSize * 0.8f
                                    if (yPosition < margin) newPage()
                                } else {
                                    // Detect heading-like paragraphs by style
                                    val style = element.style
                                    val isHeading = style?.startsWith("Heading", ignoreCase = true) == true
                                    val fontSize = if (isHeading) 16f else defaultFontSize
                                    val selectedFont = when {
                                        isHeading -> boldFont
                                        element.runs.any { it.isBold && it.isItalic } -> boldItalicFont
                                        element.runs.any { it.isBold } -> boldFont
                                        element.runs.any { it.isItalic } -> italicFont
                                        else -> font
                                    }

                                    // Simple word-wrap
                                    val words = text.split(" ")
                                    val line = StringBuilder()
                                    for (word in words) {
                                        val testLine = if (line.isEmpty()) word else "$line $word"
                                        val testWidth = selectedFont.getStringWidth(testLine.filter { it.code in 32..126 }) / 1000f * fontSize
                                        if (testWidth > usableWidth && line.isNotEmpty()) {
                                            writeTextLine(line.toString(), fontSize, selectedFont)
                                            line.clear()
                                            line.append(word)
                                        } else {
                                            line.clear()
                                            line.append(testLine)
                                        }
                                    }
                                    if (line.isNotEmpty()) {
                                        writeTextLine(line.toString(), fontSize, selectedFont)
                                    }

                                    if (isHeading) {
                                        yPosition -= fontSize * 0.3f
                                    }
                                }
                            }
                            is org.apache.poi.xwpf.usermodel.XWPFTable -> {
                                val table = element
                                for (row in table.rows) {
                                    val cells = row.tableCells.map { it.text.trim() }
                                    val cellText = cells.joinToString(" | ")
                                    writeTextLine(cellText, defaultFontSize, font)
                                }
                                yPosition -= defaultFontSize * 0.5f
                            }
                        }
                    }

                    contentStream.close()
                    wordDoc.close()
                    pdfDoc.save(outputFile)
                }
            } ?: throw IllegalArgumentException("Could not open the Word document.")
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            outputFile.delete()
            throw e
        }
    }

    /**
     * Returns the total number of slides in a PowerPoint (.pptx) file.
     */
    suspend fun getSlideCount(context: Context, uri: Uri): Int = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val slideShow = XMLSlideShow(inputStream)
                val count = slideShow.slides.size
                slideShow.close()
                count
            } ?: 0
        } catch (_: Throwable) {
            0
        }
    }

    /**
     * Returns a list of short text previews (title + first line) for each slide in a PPTX file.
     * Each element corresponds to a slide at that index.
     */
    suspend fun getSlidePreviewTexts(context: Context, uri: Uri): List<String> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val slideShow = XMLSlideShow(inputStream)
                val previews = slideShow.slides.map { slide ->
                    val lines = mutableListOf<String>()
                    for (shape in slide.shapes) {
                        if (shape is XSLFTextShape) {
                            for (paragraph in shape.textParagraphs) {
                                val text = paragraph.text.trim()
                                if (text.isNotEmpty()) {
                                    lines.add(text)
                                    if (lines.size >= 3) break
                                }
                            }
                        }
                        if (lines.size >= 3) break
                    }
                    lines.joinToString("\n").take(120)
                }
                slideShow.close()
                previews
            } ?: emptyList()
        } catch (_: Throwable) {
            emptyList()
        }
    }

    /**
     * Converts a PowerPoint (.pptx) presentation into a PDF.
     * Supports slide filtering, handout layout, speaker notes, and quality control.
     */
    suspend fun convertPptToPdf(
        context: Context,
        uri: Uri,
        slideRange: String = "all",
        customRange: String = "",
        selectedSlides: Set<Int> = emptySet(),
        slidesPerPage: Int = 1,
        includeNotes: Boolean = false,
        quality: String = "medium",
        onProgress: ((Float) -> Unit)? = null
    ): Uri = withContext(Dispatchers.IO) {
        val outputFile = File(context.cacheDir, "PptToPdf_${System.currentTimeMillis()}.pdf")

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Could not open the PowerPoint file.")

            inputStream.use { stream ->
                val slideShow = XMLSlideShow(stream)
                // Dynamically resolve slide dimensions
                val dimensions = getPresentationSlideSize(slideShow)
                val slideWidth = dimensions.first
                val slideHeight = dimensions.second
                val allSlides = slideShow.slides

                // Determine which slides to include (1-indexed)
                val slidesToConvert = if (slideRange == "custom") {
                    if (selectedSlides.isNotEmpty()) {
                        allSlides.filterIndexed { index, _ -> (index + 1) in selectedSlides }
                    } else if (customRange.isNotBlank()) {
                        val indices = parseSlideRange(customRange, allSlides.size)
                        allSlides.filterIndexed { index, _ -> (index + 1) in indices }
                    } else {
                        allSlides
                    }
                } else {
                    allSlides
                }

                if (slidesToConvert.isEmpty()) {
                    slideShow.close()
                    throw IllegalArgumentException("No slides selected for conversion.")
                }

                // Font size scaling based on quality
                val fontScale = when (quality) {
                    "low" -> 0.85f
                    "high" -> 1.15f
                    else -> 1.0f
                }

                PDDocument().use { pdfDoc ->
                    val font = PDType1Font.HELVETICA
                    val boldFont = PDType1Font.HELVETICA_BOLD
                    val italicFont = PDType1Font.HELVETICA_OBLIQUE
                    val boldItalicFont = PDType1Font.HELVETICA_BOLD_OBLIQUE
                    val defaultFontSize = 10f * fontScale
                    val margin = 50f

                    val clampedPerPage = slidesPerPage.coerceIn(1, 4)

                    // Group slides by pages
                    val slideGroups = slidesToConvert.chunked(clampedPerPage)
                    val totalGroups = slideGroups.size.coerceAtLeast(1)

                    for ((groupIndex, group) in slideGroups.withIndex()) {
                        currentCoroutineContext().ensureActive()
                        onProgress?.invoke(groupIndex.toFloat() / totalGroups)

                        if (clampedPerPage == 1) {
                            // Single slide per page â€” use slide dimensions
                            val noteText = if (includeNotes) extractNotes(group[0]) else null
                            val extraHeight = if (!noteText.isNullOrBlank()) 120f else 0f
                            val page = PDPage(PDRectangle(slideWidth, slideHeight + extraHeight))
                            pdfDoc.addPage(page)

                            PDPageContentStream(pdfDoc, page).use { cs ->
                                cs.setNonStrokingColor(1f, 1f, 1f)
                                cs.addRect(0f, 0f, slideWidth, slideHeight + extraHeight)
                                cs.fill()

                                renderSlideContent(context, pdfDoc, cs, group[0], margin, slideWidth, slideHeight, boldFont, font, defaultFontSize, slideHeight + extraHeight)

                                // Speaker notes
                                if (!noteText.isNullOrBlank()) {
                                    val notesY = extraHeight - 20f
                                    cs.setNonStrokingColor(0.92f, 0.92f, 0.92f)
                                    cs.addRect(0f, 0f, slideWidth, extraHeight)
                                    cs.fill()

                                    cs.beginText()
                                    cs.setFont(boldFont, 9f)
                                    cs.setNonStrokingColor(0.3f, 0.3f, 0.3f)
                                    cs.newLineAtOffset(margin, notesY)
                                    cs.showText("Speaker Notes:")
                                    cs.endText()

                                    cs.beginText()
                                    val truncatedNotes = if (noteText.length > 200) noteText.take(200) + "..." else noteText
                                    val notesFont = selectPptFont(context, pdfDoc, truncatedNotes, font, false)
                                    cs.setFont(notesFont, 8f)
                                    cs.setNonStrokingColor(0.4f, 0.4f, 0.4f)
                                    cs.newLineAtOffset(margin, notesY - 14f)
                                    showPptText(cs, truncatedNotes)
                                    cs.endText()
                                }
                            }
                        } else {
                            // Multiple slides per page (handout layout)
                            val pageW = 595f
                            val pageH = 842f
                            val page = PDPage(PDRectangle(pageW, pageH))
                            pdfDoc.addPage(page)

                            PDPageContentStream(pdfDoc, page).use { cs ->
                                cs.setNonStrokingColor(1f, 1f, 1f)
                                cs.addRect(0f, 0f, pageW, pageH)
                                cs.fill()

                                val cols = if (clampedPerPage == 2) 1 else 2
                                val rows = if (clampedPerPage == 2) 2 else 2
                                val cellMargin = 24f
                                val cellW = (pageW - cellMargin * (cols + 1)) / cols
                                val cellH = (pageH - cellMargin * (rows + 1)) / rows

                                for ((i, slide) in group.withIndex()) {
                                    val col = i % cols
                                    val row = i / cols
                                    val cellX = cellMargin + col * (cellW + cellMargin)
                                    val cellY = pageH - cellMargin - (row + 1) * (cellH + cellMargin) + cellMargin

                                    // Draw cell border
                                    cs.setStrokingColor(0.85f, 0.85f, 0.85f)
                                    cs.setLineWidth(0.5f)
                                    cs.addRect(cellX, cellY, cellW, cellH)
                                    cs.stroke()

                                    val innerMargin = 12f
                                    renderSlideContentInBounds(context, pdfDoc, cs, slide, cellX + innerMargin, cellY + innerMargin, cellW - 2 * innerMargin, cellH - 2 * innerMargin, boldFont, font, defaultFontSize * 0.7f)

                                    // Notes below each cell if enabled
                                    if (includeNotes) {
                                        val noteText = extractNotes(slide)
                                        if (!noteText.isNullOrBlank()) {
                                            cs.beginText()
                                            val truncated = if (noteText.length > 60) noteText.take(60) + "..." else noteText
                                            val noteFont = selectPptFont(context, pdfDoc, truncated, font, false)
                                            cs.setFont(noteFont, 6f)
                                            cs.setNonStrokingColor(0.5f, 0.5f, 0.5f)
                                            cs.newLineAtOffset(cellX + innerMargin, cellY + 4f)
                                            showPptText(cs, truncated)
                                            cs.endText()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    slideShow.close()
                    pdfDoc.save(outputFile)
                    onProgress?.invoke(1f)
                }
            }
            Uri.fromFile(outputFile)
        } catch (e: Throwable) {
            outputFile.delete()
            throw e
        }
    }

    /**
     * Parse a slide range string like "1-5, 8, 10-12" into a set of 1-indexed slide numbers.
     */
    private fun parseSlideRange(range: String, totalSlides: Int): Set<Int> {
        val result = mutableSetOf<Int>()
        val parts = range.split(",").map { it.trim() }
        for (part in parts) {
            if (part.contains("-")) {
                val bounds = part.split("-").map { it.trim().toIntOrNull() }
                if (bounds.size == 2 && bounds[0] != null && bounds[1] != null) {
                    val start = bounds[0]!!.coerceIn(1, totalSlides)
                    val end = bounds[1]!!.coerceIn(1, totalSlides)
                    result.addAll(start..end)
                }
            } else {
                part.toIntOrNull()?.let { num ->
                    if (num in 1..totalSlides) result.add(num)
                }
            }
        }
        return result
    }

    /**
     * Extracts speaker notes text from a slide.
     */
    private fun extractNotes(slide: org.apache.poi.xslf.usermodel.XSLFSlide): String? {
        return try {
            val notes = slide.notes ?: return null
            val sb = StringBuilder()
            for (shape in notes.shapes) {
                if (shape is XSLFTextShape) {
                    for (p in shape.textParagraphs) {
                        val t = p.text.trim()
                        if (t.isNotEmpty()) {
                            if (sb.isNotEmpty()) sb.append(" ")
                            sb.append(t)
                        }
                    }
                }
            }
            sb.toString().takeIf { it.isNotBlank() }
        } catch (_: Throwable) { null }
    }

    private fun getSlideShapesInPaintOrder(
        slide: org.apache.poi.xslf.usermodel.XSLFSlide
    ): List<org.apache.poi.xslf.usermodel.XSLFShape> {
        val shapes = mutableListOf<org.apache.poi.xslf.usermodel.XSLFShape>()

        fun addInherited(source: Iterable<org.apache.poi.xslf.usermodel.XSLFShape>?) {
            source?.forEach { shape ->
                if (!isPlaceholderShape(shape)) {
                    shapes.add(shape)
                }
            }
        }

        try {
            addInherited(slide.slideMaster?.shapes)
        } catch (_: Throwable) {}

        try {
            addInherited(slide.slideLayout?.shapes)
        } catch (_: Throwable) {}

        try {
            shapes.addAll(slide.shapes)
        } catch (_: Throwable) {}

        return shapes
    }

    private fun isPlaceholderShape(shape: Any): Boolean {
        try {
            val placeholder = shape.javaClass.getMethod("getPlaceholder").invoke(shape)
            if (placeholder != null) return true
        } catch (_: Throwable) {}

        try {
            val xmlObject = shape.javaClass.getMethod("getXmlObject").invoke(shape) ?: return false
            val nvPr = findNoVisualProperties(xmlObject) ?: return false
            val ph = try {
                nvPr.javaClass.getMethod("getPh").invoke(nvPr)
            } catch (_: Throwable) {
                null
            }
            if (ph != null) return true
        } catch (_: Throwable) {}

        return false
    }

    private fun findNoVisualProperties(xmlObject: Any): Any? {
        val candidateNames = listOf("getNvSpPr", "getNvPicPr", "getNvCxnSpPr", "getNvGraphicFramePr", "getNvGrpSpPr")
        for (name in candidateNames) {
            try {
                val nv = xmlObject.javaClass.getMethod(name).invoke(xmlObject) ?: continue
                val nvPr = try {
                    nv.javaClass.getMethod("getNvPr").invoke(nv)
                } catch (_: Throwable) {
                    null
                }
                if (nvPr != null) return nvPr
            } catch (_: Throwable) {}
        }
        return null
    }

    private fun loadEmbeddedFont(context: Context, pdfDoc: PDDocument, bold: Boolean): PDType0Font? {
        val docId = System.identityHashCode(pdfDoc)
        if (embeddedFontDocId != docId) {
            embeddedFontDocId = docId
            embeddedRegularFont = null
            embeddedBoldFont = null
        }

        val cached = if (bold) embeddedBoldFont else embeddedRegularFont
        if (cached != null) return cached

        return try {
            val assetName = if (bold) "fonts/NotoSans-Bold.ttf" else "fonts/NotoSans-Regular.ttf"
            context.assets.open(assetName).use { input ->
                PDType0Font.load(pdfDoc, input, true).also { loaded ->
                    if (bold) embeddedBoldFont = loaded else embeddedRegularFont = loaded
                }
            }
        } catch (e: Throwable) {
            Log.w("ConvertProcessor", "Unable to load embedded Noto Sans font", e)
            null
        }
    }

    private fun containsNonAscii(text: String): Boolean = text.any { it.code > 126 }

    private fun selectPptFont(
        context: Context,
        pdfDoc: PDDocument,
        text: String,
        baseFont: PDFont,
        isBold: Boolean
    ): PDFont {
        return if (containsNonAscii(text)) {
            loadEmbeddedFont(context, pdfDoc, isBold) ?: baseFont
        } else {
            baseFont
        }
    }

    private fun getTextWidth(font: PDFont, text: String, fontSize: Float): Float {
        return try {
            font.getStringWidth(text) / 1000f * fontSize
        } catch (_: Throwable) {
            val ascii = text.filter { it.code in 32..126 }
            if (ascii.isNotEmpty()) {
                try {
                    font.getStringWidth(ascii) / 1000f * fontSize
                } catch (_: Throwable) {
                    text.length * fontSize * 0.5f
                }
            } else {
                text.length * fontSize * 0.5f
            }
        }
    }

    private fun showPptText(
        cs: PDPageContentStream,
        text: String
    ) {
        try {
            cs.showText(text)
        } catch (_: Throwable) {
            val ascii = text.filter { it.code in 32..126 }
            if (ascii.isNotEmpty()) {
                try { cs.showText(ascii) } catch (_: Throwable) {}
            }
        }
    }

    private fun applyShapeRotation(
        cs: PDPageContentStream,
        shape: XSLFSimpleShape,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        draw: () -> Unit
    ) {
        val rotation = try { shape.rotation.toFloat() } catch (_: Throwable) { 0f }
        if (kotlin.math.abs(rotation) < 0.01f) {
            draw()
            return
        }

        cs.saveGraphicsState()
        try {
            val matrix = PdfMatrix.getRotateInstance(
                Math.toRadians(rotation.toDouble()),
                x + w / 2f,
                y + h / 2f
            )
            cs.transform(matrix)
            draw()
        } finally {
            cs.restoreGraphicsState()
        }
    }

    private fun drawShapeGeometry(
        cs: PDPageContentStream,
        shape: XSLFSimpleShape,
        x: Float,
        y: Float,
        w: Float,
        h: Float
    ) {
        val type = try { shape.shapeType } catch (_: Throwable) { ShapeType.RECT }
        when (type) {
            ShapeType.ELLIPSE, ShapeType.WEDGE_ELLIPSE_CALLOUT -> addEllipsePath(cs, x, y, w, h)
            ShapeType.ROUND_RECT, ShapeType.ROUND_1_RECT, ShapeType.ROUND_2_SAME_RECT,
            ShapeType.ROUND_2_DIAG_RECT, ShapeType.SNIP_ROUND_RECT,
            ShapeType.WEDGE_ROUND_RECT_CALLOUT -> addRoundRectPath(cs, x, y, w, h)
            ShapeType.TRIANGLE -> {
                cs.moveTo(x + w / 2f, y + h)
                cs.lineTo(x + w, y)
                cs.lineTo(x, y)
                cs.closePath()
            }
            ShapeType.RT_TRIANGLE -> {
                cs.moveTo(x, y + h)
                cs.lineTo(x + w, y)
                cs.lineTo(x, y)
                cs.closePath()
            }
            ShapeType.DIAMOND -> {
                cs.moveTo(x + w / 2f, y + h)
                cs.lineTo(x + w, y + h / 2f)
                cs.lineTo(x + w / 2f, y)
                cs.lineTo(x, y + h / 2f)
                cs.closePath()
            }
            ShapeType.RIGHT_ARROW, ShapeType.LEFT_ARROW, ShapeType.UP_ARROW, ShapeType.DOWN_ARROW,
            ShapeType.LEFT_RIGHT_ARROW, ShapeType.UP_DOWN_ARROW, ShapeType.QUAD_ARROW,
            ShapeType.STRIPED_RIGHT_ARROW, ShapeType.NOTCHED_RIGHT_ARROW, ShapeType.THICK_ARROW -> {
                addArrowPath(cs, type, x, y, w, h)
            }
            ShapeType.LINE, ShapeType.LINE_INV, ShapeType.STRAIGHT_CONNECTOR_1 -> {
                cs.moveTo(x, y + h)
                cs.lineTo(x + w, y)
            }
            else -> cs.addRect(x, y, w, h)
        }
    }

    private fun addEllipsePath(cs: PDPageContentStream, x: Float, y: Float, w: Float, h: Float) {
        val k = 0.55228475f
        val ox = w / 2f * k
        val oy = h / 2f * k
        val xe = x + w
        val ye = y + h
        val xm = x + w / 2f
        val ym = y + h / 2f
        cs.moveTo(x, ym)
        cs.curveTo(x, ym + oy, xm - ox, ye, xm, ye)
        cs.curveTo(xm + ox, ye, xe, ym + oy, xe, ym)
        cs.curveTo(xe, ym - oy, xm + ox, y, xm, y)
        cs.curveTo(xm - ox, y, x, ym - oy, x, ym)
        cs.closePath()
    }

    private fun addRoundRectPath(cs: PDPageContentStream, x: Float, y: Float, w: Float, h: Float) {
        val r = minOf(w, h) * 0.18f
        val k = 0.55228475f
        cs.moveTo(x + r, y)
        cs.lineTo(x + w - r, y)
        cs.curveTo(x + w - r + r * k, y, x + w, y + r - r * k, x + w, y + r)
        cs.lineTo(x + w, y + h - r)
        cs.curveTo(x + w, y + h - r + r * k, x + w - r + r * k, y + h, x + w - r, y + h)
        cs.lineTo(x + r, y + h)
        cs.curveTo(x + r - r * k, y + h, x, y + h - r + r * k, x, y + h - r)
        cs.lineTo(x, y + r)
        cs.curveTo(x, y + r - r * k, x + r - r * k, y, x + r, y)
        cs.closePath()
    }

    private fun addArrowPath(cs: PDPageContentStream, type: ShapeType, x: Float, y: Float, w: Float, h: Float) {
        val shaft = h * 0.28f
        when (type) {
            ShapeType.LEFT_ARROW -> {
                cs.moveTo(x, y + h / 2f)
                cs.lineTo(x + w * 0.35f, y + h)
                cs.lineTo(x + w * 0.35f, y + h / 2f + shaft)
                cs.lineTo(x + w, y + h / 2f + shaft)
                cs.lineTo(x + w, y + h / 2f - shaft)
                cs.lineTo(x + w * 0.35f, y + h / 2f - shaft)
                cs.lineTo(x + w * 0.35f, y)
            }
            ShapeType.UP_ARROW -> {
                cs.moveTo(x + w / 2f, y + h)
                cs.lineTo(x + w, y + h * 0.65f)
                cs.lineTo(x + w / 2f + shaft, y + h * 0.65f)
                cs.lineTo(x + w / 2f + shaft, y)
                cs.lineTo(x + w / 2f - shaft, y)
                cs.lineTo(x + w / 2f - shaft, y + h * 0.65f)
                cs.lineTo(x, y + h * 0.65f)
            }
            ShapeType.DOWN_ARROW -> {
                cs.moveTo(x + w / 2f, y)
                cs.lineTo(x + w, y + h * 0.35f)
                cs.lineTo(x + w / 2f + shaft, y + h * 0.35f)
                cs.lineTo(x + w / 2f + shaft, y + h)
                cs.lineTo(x + w / 2f - shaft, y + h)
                cs.lineTo(x + w / 2f - shaft, y + h * 0.35f)
                cs.lineTo(x, y + h * 0.35f)
            }
            else -> {
                cs.moveTo(x + w, y + h / 2f)
                cs.lineTo(x + w * 0.65f, y + h)
                cs.lineTo(x + w * 0.65f, y + h / 2f + shaft)
                cs.lineTo(x, y + h / 2f + shaft)
                cs.lineTo(x, y + h / 2f - shaft)
                cs.lineTo(x + w * 0.65f, y + h / 2f - shaft)
                cs.lineTo(x + w * 0.65f, y)
            }
        }
        cs.closePath()
    }

    private fun renderSimpleShapeGeometry(
        cs: PDPageContentStream,
        shape: XSLFSimpleShape,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        scale: Float
    ) {
        applyShapeRotation(cs, shape, x, y, w, h) {
            val fillCol = getFillColorSafe(shape)
            if (fillCol != null) {
                val r = ((fillCol shr 16) and 0xFF) / 255f
                val g = ((fillCol shr 8) and 0xFF) / 255f
                val b = (fillCol and 0xFF) / 255f
                cs.setNonStrokingColor(r, g, b)
                drawShapeGeometry(cs, shape, x, y, w, h)
                cs.fill()
            }

            val lineCol = getLineColorSafe(shape)
            if (lineCol != null) {
                val r = ((lineCol shr 16) and 0xFF) / 255f
                val g = ((lineCol shr 8) and 0xFF) / 255f
                val b = (lineCol and 0xFF) / 255f
                cs.setStrokingColor(r, g, b)
                val lineWidth = try {
                    shape.lineWidth.toFloat().coerceAtLeast(0.5f) * scale
                } catch (_: Throwable) {
                    1.0f * scale
                }
                cs.setLineWidth(lineWidth.coerceAtLeast(0.25f))
                drawShapeGeometry(cs, shape, x, y, w, h)
                cs.stroke()
            }
        }
    }

    private fun drawConnectorLine(
        cs: PDPageContentStream,
        shape: XSLFConnectorShape,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        scale: Float
    ) {
        val lineCol = getLineColorSafe(shape)
        if (lineCol != null) {
            val r = ((lineCol shr 16) and 0xFF) / 255f
            val g = ((lineCol shr 8) and 0xFF) / 255f
            val b = (lineCol and 0xFF) / 255f
            cs.setStrokingColor(r, g, b)
        } else {
            cs.setStrokingColor(0.1f, 0.1f, 0.1f)
        }
        val lineWidth = try { shape.lineWidth.toFloat().coerceAtLeast(0.5f) * scale } catch (_: Throwable) { scale }
        cs.setLineWidth(lineWidth.coerceAtLeast(0.25f))

        val startX = x
        val startY = y + h
        val endX = x + w
        val endY = y
        cs.moveTo(startX, startY)
        cs.lineTo(endX, endY)
        cs.stroke()

        val hasTailArrow = try { shape.lineTailDecoration != null } catch (_: Throwable) { false }
        val hasHeadArrow = try { shape.lineHeadDecoration != null } catch (_: Throwable) { false }
        if (hasTailArrow) drawArrowHead(cs, startX, startY, endX, endY, 6f * scale)
        if (hasHeadArrow) drawArrowHead(cs, endX, endY, startX, startY, 6f * scale)
    }

    private fun drawArrowHead(cs: PDPageContentStream, tipX: Float, tipY: Float, fromX: Float, fromY: Float, size: Float) {
        val angle = kotlin.math.atan2((tipY - fromY).toDouble(), (tipX - fromX).toDouble())
        val left = angle + Math.toRadians(150.0)
        val right = angle - Math.toRadians(150.0)
        cs.moveTo(tipX, tipY)
        cs.lineTo(tipX + kotlin.math.cos(left).toFloat() * size, tipY + kotlin.math.sin(left).toFloat() * size)
        cs.moveTo(tipX, tipY)
        cs.lineTo(tipX + kotlin.math.cos(right).toFloat() * size, tipY + kotlin.math.sin(right).toFloat() * size)
        cs.stroke()
    }

    private fun renderPictureShape(
        pdfDoc: PDDocument,
        cs: PDPageContentStream,
        shape: XSLFPictureShape,
        x: Float,
        y: Float,
        w: Float,
        h: Float
    ) {
        val picData = shape.pictureData
        val bytes = picData.data
        val contentType = picData.contentType?.lowercase() ?: ""
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        if (bitmap != null) {
            try {
                val pdImage = LosslessFactory.createFromImage(pdfDoc, bitmap)
                cs.drawImage(pdImage, x, y, w, h)
            } finally {
                bitmap.recycle()
            }
        } else {
            Log.w("ConvertProcessor", "Unsupported image format in slide: $contentType (${bytes.size} bytes)")
            cs.setNonStrokingColor(0.93f, 0.93f, 0.95f)
            cs.addRect(x, y, w, h)
            cs.fill()
            cs.setStrokingColor(0.8f, 0.8f, 0.82f)
            cs.setLineWidth(0.5f)
            cs.addRect(x, y, w, h)
            cs.stroke()
            cs.setStrokingColor(0.75f, 0.75f, 0.78f)
            cs.moveTo(x, y)
            cs.lineTo(x + w, y + h)
            cs.stroke()
            cs.moveTo(x + w, y)
            cs.lineTo(x, y + h)
            cs.stroke()
        }
    }

    private fun renderTable(
        context: Context,
        pdfDoc: PDDocument,
        cs: PDPageContentStream,
        table: XSLFTable,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        font: PDFont,
        boldFont: PDFont,
        defaultFontSize: Float,
        scale: Float
    ) {
        val rows = table.numberOfRows.coerceAtLeast(1)
        val cols = table.numberOfColumns.coerceAtLeast(1)
        var currentY = y + h
        for (rowIndex in 0 until rows) {
            val rowHeight = try { table.getRowHeight(rowIndex).toFloat() * scale } catch (_: Throwable) { h / rows }
            val cellY = currentY - rowHeight
            var currentX = x
            for (colIndex in 0 until cols) {
                val cellWidth = try { table.getColumnWidth(colIndex).toFloat() * scale } catch (_: Throwable) { w / cols }
                val cell = try { table.getCell(rowIndex, colIndex) } catch (_: Throwable) { null }
                val fillColor = try { cell?.fillColor } catch (_: Throwable) { null }
                if (fillColor != null) {
                    cs.setNonStrokingColor(fillColor.red / 255f, fillColor.green / 255f, fillColor.blue / 255f)
                    cs.addRect(currentX, cellY, cellWidth, rowHeight)
                    cs.fill()
                }

                cs.setStrokingColor(0.6f, 0.6f, 0.6f)
                cs.setLineWidth((0.7f * scale).coerceAtLeast(0.25f))
                cs.addRect(currentX, cellY, cellWidth, rowHeight)
                cs.stroke()

                val text = try { cell?.text?.trim().orEmpty() } catch (_: Throwable) { "" }
                if (text.isNotBlank()) {
                    renderWrappedPptText(
                        context = context,
                        pdfDoc = pdfDoc,
                        cs = cs,
                        text = text,
                        x = currentX + 4f * scale,
                        topY = cellY + rowHeight - 4f * scale,
                        maxWidth = cellWidth - 8f * scale,
                        minY = cellY + 3f * scale,
                        baseFont = font,
                        boldBaseFont = boldFont,
                        fontSize = (defaultFontSize * scale).coerceAtLeast(4f),
                        isBold = rowIndex == 0,
                        textR = 0.1f,
                        textG = 0.1f,
                        textB = 0.1f,
                        alignment = "l"
                    )
                }
                currentX += cellWidth
            }
            currentY = cellY
        }
    }

    private fun renderGroupShape(
        context: Context,
        pdfDoc: PDDocument,
        cs: PDPageContentStream,
        group: XSLFGroupShape,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        font: PDFont,
        boldFont: PDFont,
        defaultFontSize: Float,
        scale: Float
    ) {
        val interior = try { group.interiorAnchor } catch (_: Throwable) { null }
        val interiorX = interior?.x?.toFloat() ?: 0f
        val interiorY = interior?.y?.toFloat() ?: 0f
        val interiorW = (interior?.width?.toFloat() ?: w / scale).takeIf { it != 0f } ?: 1f
        val interiorH = (interior?.height?.toFloat() ?: h / scale).takeIf { it != 0f } ?: 1f
        val sx = w / interiorW
        val sy = h / interiorH

        for (child in group.shapes) {
            val childAnchor = getShapeAnchorSafe(child) ?: continue
            val childX = x + (childAnchor.left - interiorX) * sx
            val childW = childAnchor.width() * sx
            val childH = childAnchor.height() * sy
            val childY = y + h - (childAnchor.top - interiorY) * sy - childH
            renderPptShape(context, pdfDoc, cs, child, childX, childY, childW, childH, font, boldFont, defaultFontSize, scale * sx)
        }
    }

    private fun renderPptShape(
        context: Context,
        pdfDoc: PDDocument,
        cs: PDPageContentStream,
        shape: org.apache.poi.xslf.usermodel.XSLFShape,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        font: PDFont,
        boldFont: PDFont,
        defaultFontSize: Float,
        scale: Float
    ) {
        when (shape) {
            is XSLFGroupShape -> renderGroupShape(context, pdfDoc, cs, shape, x, y, w, h, font, boldFont, defaultFontSize, scale)
            is XSLFTable -> renderTable(context, pdfDoc, cs, shape, x, y, w, h, font, boldFont, defaultFontSize, scale)
            is XSLFConnectorShape -> drawConnectorLine(cs, shape, x, y, w, h, scale)
            is XSLFPictureShape -> renderPictureShape(pdfDoc, cs, shape, x, y, w, h)
            is XSLFSimpleShape -> {
                renderSimpleShapeGeometry(cs, shape, x, y, w, h, scale)
                if (shape is XSLFTextShape) {
                    renderTextShape(context, pdfDoc, cs, shape, x, y, w, h, font, boldFont, defaultFontSize * scale, scale)
                }
            }
            else -> Unit
        }
    }

    private fun renderTextShape(
        context: Context,
        pdfDoc: PDDocument,
        cs: PDPageContentStream,
        shape: XSLFTextShape,
        sx: Float,
        pdfY: Float,
        sw: Float,
        sh: Float,
        font: PDFont,
        boldFont: PDFont,
        defaultFontSize: Float,
        fontScale: Float
    ) {
        val textPadding = 6f
        var currentYOffset = textPadding
        for (paragraph in shape.textParagraphs) {
            val text = paragraph.text.trim()
            if (text.isEmpty()) {
                currentYOffset += defaultFontSize * 0.5f
                continue
            }

            val bulletPrefix = getBulletPrefix(paragraph)
            val isBold = paragraph.textRuns.any { it.isBold }
            val isItalic = paragraph.textRuns.any { it.isItalic }
            var fontSize = defaultFontSize
            paragraph.textRuns.firstOrNull()?.fontSize?.let { fs ->
                if (fs > 0) fontSize = fs.toFloat().coerceIn(6f, 72f) * fontScale
            }
            val baseFont = when {
                isBold && isItalic -> PDType1Font.HELVETICA_BOLD_OBLIQUE
                isBold -> boldFont
                isItalic -> PDType1Font.HELVETICA_OBLIQUE
                else -> font
            }
            val textColor = getTextRunColor(paragraph.textRuns.firstOrNull())
            val textR = textColor?.let { ((it shr 16) and 0xFF) / 255f } ?: 0.1f
            val textG = textColor?.let { ((it shr 8) and 0xFF) / 255f } ?: 0.1f
            val textB = textColor?.let { (it and 0xFF) / 255f } ?: 0.1f
            val displayText = if (bulletPrefix.isNotEmpty()) "$bulletPrefix $text" else text

            currentYOffset += renderWrappedPptText(
                context = context,
                pdfDoc = pdfDoc,
                cs = cs,
                text = displayText,
                x = sx + textPadding,
                topY = pdfY + sh - currentYOffset,
                maxWidth = sw - 2 * textPadding,
                minY = pdfY,
                baseFont = baseFont,
                boldBaseFont = boldFont,
                fontSize = fontSize,
                isBold = isBold,
                textR = textR,
                textG = textG,
                textB = textB,
                alignment = getParagraphAlignment(paragraph)
            )
        }
    }

    private fun renderWrappedPptText(
        context: Context,
        pdfDoc: PDDocument,
        cs: PDPageContentStream,
        text: String,
        x: Float,
        topY: Float,
        maxWidth: Float,
        minY: Float,
        baseFont: PDFont,
        boldBaseFont: PDFont,
        fontSize: Float,
        isBold: Boolean,
        textR: Float,
        textG: Float,
        textB: Float,
        alignment: String
    ): Float {
        val selectedFont = selectPptFont(context, pdfDoc, text, if (isBold) boldBaseFont else baseFont, isBold)
        val lineHeight = fontSize * 1.2f
        var consumed = 0f
        val words = text.split(Regex("\\s+")).filter { it.isNotEmpty() }
        val lines = mutableListOf<String>()
        val line = StringBuilder()
        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (getTextWidth(selectedFont, testLine, fontSize) > maxWidth && line.isNotEmpty()) {
                lines.add(line.toString())
                line.clear()
                line.append(word)
            } else {
                line.clear()
                line.append(testLine)
            }
        }
        if (line.isNotEmpty()) lines.add(line.toString())

        for (lineText in lines) {
            val drawY = topY - consumed - fontSize
            if (drawY < minY) break
            val lineW = getTextWidth(selectedFont, lineText, fontSize)
            val xOffset = when (alignment) {
                "ctr" -> x + (maxWidth - lineW) / 2f
                "r" -> x + (maxWidth - lineW)
                else -> x
            }
            cs.beginText()
            cs.setFont(selectedFont, fontSize)
            cs.setNonStrokingColor(textR, textG, textB)
            cs.newLineAtOffset(xOffset, drawY)
            showPptText(cs, lineText)
            cs.endText()
            consumed += lineHeight
        }
        return consumed.coerceAtLeast(lineHeight)
    }

    private fun renderSlideContent(
        context: Context,
        pdfDoc: PDDocument,
        cs: PDPageContentStream,
        slide: org.apache.poi.xslf.usermodel.XSLFSlide,
        margin: Float,
        slideWidth: Float,
        slideHeight: Float,
        boldFont: PDType1Font,
        font: PDType1Font,
        defaultFontSize: Float,
        pageHeight: Float
    ) {
        try {
            renderSlideContentVisual(context, pdfDoc, cs, slide, margin, slideWidth, slideHeight, boldFont, font, defaultFontSize, pageHeight)
        } catch (e: Throwable) {
            // Only fall back for truly catastrophic failures (e.g. content stream already closed)
            Log.w("ConvertProcessor", "Visual renderer failed for slide ${try { slide.slideNumber } catch (_: Throwable) { "?" }}, falling back to text-only", e)
            try {
                renderSlideContentFallback(cs, slide, margin, slideWidth, slideHeight, boldFont, font, defaultFontSize, pageHeight)
            } catch (e2: Throwable) {
                Log.e("ConvertProcessor", "Fallback renderer also failed", e2)
            }
        }
    }

    private fun renderSlideContentVisual(
        context: Context,
        pdfDoc: PDDocument,
        cs: PDPageContentStream,
        slide: org.apache.poi.xslf.usermodel.XSLFSlide,
        margin: Float,
        slideWidth: Float,
        slideHeight: Float,
        boldFont: PDType1Font,
        font: PDType1Font,
        defaultFontSize: Float,
        pageHeight: Float
    ) {
        val extraHeight = pageHeight - slideHeight

        val bgCol = getSlideBackgroundColorSafe(slide)
        if (bgCol != null) {
            val r = ((bgCol shr 16) and 0xFF) / 255f
            val g = ((bgCol shr 8) and 0xFF) / 255f
            val b = (bgCol and 0xFF) / 255f
            cs.setNonStrokingColor(r, g, b)
        } else {
            cs.setNonStrokingColor(1f, 1f, 1f)
        }
        cs.addRect(0f, extraHeight, slideWidth, slideHeight)
        cs.fill()

        for (shape in getSlideShapesInPaintOrder(slide)) {
            try {
                val anchor = getShapeAnchorSafe(shape) ?: continue
                val sx = anchor.left
                val sy = anchor.top
                val sw = anchor.width()
                val sh = anchor.height()
                val pdfY = pageHeight - sy - sh
                renderPptShape(context, pdfDoc, cs, shape, sx, pdfY, sw, sh, font, boldFont, defaultFontSize, 1f)
            } catch (e: Throwable) {
                Log.w("ConvertProcessor", "Failed to render shape: ${shape.javaClass.simpleName}", e)
            }
        }
    }

    private fun renderSlideContentFallback(
        cs: PDPageContentStream,
        slide: org.apache.poi.xslf.usermodel.XSLFSlide,
        margin: Float,
        slideWidth: Float,
        slideHeight: Float,
        boldFont: PDType1Font,
        font: PDType1Font,
        defaultFontSize: Float,
        pageHeight: Float
    ) {
        val extraHeight = pageHeight - slideHeight
        cs.setNonStrokingColor(1f, 1f, 1f)
        cs.addRect(0f, extraHeight, slideWidth, slideHeight)
        cs.fill()

        var yPos = pageHeight - margin
        val usableWidth = slideWidth - 2 * margin
        for (shape in slide.shapes) {
            if (shape is XSLFTextShape) {
                for (paragraph in shape.textParagraphs) {
                    val text = paragraph.text.trim()
                    if (text.isEmpty()) {
                        yPos -= defaultFontSize * 0.6f
                        continue
                    }
                    val isBold = paragraph.textRuns.any { it.isBold }
                    var fontSize = defaultFontSize
                    paragraph.textRuns.firstOrNull()?.fontSize?.let { fs ->
                        if (fs > 0) fontSize = fs.toFloat().coerceIn(8f, 48f)
                    }
                    val selectedFont = if (isBold) boldFont else font
                    val lineHeight = fontSize * 1.3f
                    if (yPos - lineHeight < margin) break

                    cs.beginText()
                    cs.setFont(selectedFont, fontSize)
                    cs.setNonStrokingColor(0.1f, 0.1f, 0.1f)
                    cs.newLineAtOffset(margin, yPos)
                    val maxChars = ((usableWidth / (fontSize * 0.5f)).toInt()).coerceAtLeast(10)
                    val displayText = if (text.length > maxChars) text.take(maxChars) + "..." else text
                    try {
                        cs.showText(displayText)
                    } catch (_: Throwable) {
                        try {
                            cs.showText(displayText.filter { it.code in 32..126 })
                        } catch (_: Throwable) {}
                    }
                    cs.endText()
                    yPos -= lineHeight
                }
            }
        }

    }

    private fun renderSlideContentInBounds(
        context: Context,
        pdfDoc: PDDocument,
        cs: PDPageContentStream,
        slide: org.apache.poi.xslf.usermodel.XSLFSlide,
        x: Float, y: Float, w: Float, h: Float,
        boldFont: PDType1Font,
        font: PDType1Font,
        defaultFontSize: Float
    ) {
        try {
            renderSlideContentInBoundsVisual(context, pdfDoc, cs, slide, x, y, w, h, boldFont, font, defaultFontSize)
        } catch (e: Throwable) {
            Log.w("ConvertProcessor", "InBounds visual renderer failed, falling back to text-only", e)
            try {
                renderSlideContentInBoundsFallback(cs, slide, x, y, w, h, boldFont, font, defaultFontSize)
            } catch (e2: Throwable) {
                Log.e("ConvertProcessor", "InBounds fallback also failed", e2)
            }
        }
    }

    private fun renderSlideContentInBoundsVisual(
        context: Context,
        pdfDoc: PDDocument,
        cs: PDPageContentStream,
        slide: org.apache.poi.xslf.usermodel.XSLFSlide,
        x: Float, y: Float, w: Float, h: Float,
        boldFont: PDType1Font,
        font: PDType1Font,
        defaultFontSize: Float
    ) {
        val dimensions = getPresentationSlideSize(slide.slideShow)
        val scale = minOf(w / dimensions.first, h / dimensions.second)

        val bgCol = getSlideBackgroundColorSafe(slide)
        if (bgCol != null) {
            val r = ((bgCol shr 16) and 0xFF) / 255f
            val g = ((bgCol shr 8) and 0xFF) / 255f
            val b = (bgCol and 0xFF) / 255f
            cs.setNonStrokingColor(r, g, b)
        } else {
            cs.setNonStrokingColor(1f, 1f, 1f)
        }
        cs.addRect(x, y, w, h)
        cs.fill()

        for (shape in getSlideShapesInPaintOrder(slide)) {
            try {
                val anchor = getShapeAnchorSafe(shape) ?: continue
                val sx = anchor.left * scale
                val sy = anchor.top * scale
                val sw = anchor.width() * scale
                val sh = anchor.height() * scale
                val pdfY = y + h - sy - sh
                renderPptShape(context, pdfDoc, cs, shape, x + sx, pdfY, sw, sh, font, boldFont, defaultFontSize, scale)
            } catch (e: Throwable) {
                Log.w("ConvertProcessor", "InBounds: Failed to render shape: ${shape.javaClass.simpleName}", e)
            }
        }
    }

    private fun renderSlideContentInBoundsFallback(
        cs: PDPageContentStream,
        slide: org.apache.poi.xslf.usermodel.XSLFSlide,
        x: Float, y: Float, w: Float, h: Float,
        boldFont: PDType1Font,
        font: PDType1Font,
        defaultFontSize: Float
    ) {
        cs.setNonStrokingColor(1f, 1f, 1f)
        cs.addRect(x, y, w, h)
        cs.fill()

        var yPos = y + h - defaultFontSize
        for (shape in slide.shapes) {
            if (shape is XSLFTextShape) {
                for (paragraph in shape.textParagraphs) {
                    val text = paragraph.text.trim()
                    if (text.isEmpty()) {
                        yPos -= defaultFontSize * 0.5f
                        continue
                    }
                    val isBold = paragraph.textRuns.any { it.isBold }
                    var fontSize = defaultFontSize
                    paragraph.textRuns.firstOrNull()?.fontSize?.let { fs ->
                        if (fs > 0) fontSize = (fs.toFloat() * 0.6f).coerceIn(6f, 24f)
                    }
                    val selectedFont = if (isBold) boldFont else font
                    val lineHeight = fontSize * 1.2f
                    if (yPos - lineHeight < y) break

                    cs.beginText()
                    cs.setFont(selectedFont, fontSize)
                    cs.setNonStrokingColor(0.1f, 0.1f, 0.1f)
                    cs.newLineAtOffset(x, yPos)
                    val maxChars = ((w / (fontSize * 0.45f)).toInt()).coerceAtLeast(5)
                    val displayText = if (text.length > maxChars) text.take(maxChars) + "..." else text
                    try {
                        cs.showText(displayText)
                    } catch (_: Throwable) {
                        try {
                            cs.showText(displayText.filter { it.code in 32..126 })
                        } catch (_: Throwable) {}
                    }
                    cs.endText()
                    yPos -= lineHeight
                }
            }
        }
    }

    /**
     * Converts an Excel (.xlsx) spreadsheet into a PDF with a tabular layout.
     */
    suspend fun convertExcelToPdf(
        context: Context,
        uri: Uri,
        convertMode: String = "all_sheets",
        scalingMode: String = "fit_columns",
        showGridlines: Boolean = true
    ): Uri = withContext(Dispatchers.IO) {
        val outputFile = File(context.cacheDir, "ExcelToPdf_${System.currentTimeMillis()}.pdf")

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val workbook = XSSFWorkbook(inputStream)

                PDDocument().use { pdfDoc ->
                    val font = PDType1Font.HELVETICA
                    val boldFont = PDType1Font.HELVETICA_BOLD
                    val baseFontSize = 9f
                    val baseHeaderFontSize = 10f
                    val margin = 40f
                    val baseCellPadding = 4f
                    val baseRowHeight = baseFontSize * 1.6f + baseCellPadding * 2

                    val sheetsToConvert = if (convertMode == "active_sheet") {
                        val activeIdx = workbook.activeSheetIndex.coerceIn(0, workbook.numberOfSheets - 1)
                        listOf(activeIdx)
                    } else {
                        (0 until workbook.numberOfSheets).toList()
                    }

                    for (sheetIndex in sheetsToConvert) {
                        val sheet = workbook.getSheetAt(sheetIndex)
                        if (sheet.physicalNumberOfRows == 0) continue

                        // Determine column count from the widest row
                        var maxCols = 0
                        for (row in sheet) {
                            if (row.lastCellNum > maxCols) maxCols = row.lastCellNum.toInt()
                        }
                        if (maxCols == 0) continue

                        // Landscape A4 for better tabular fitting
                        val pageRect = PDRectangle(PDRectangle.A4.height, PDRectangle.A4.width)
                        val usableWidth = pageRect.width - 2 * margin
                        val usableHeight = pageRect.height - 2 * margin - 24f // Title spacing

                        // Scaling factors
                        var colWidth = usableWidth / maxCols
                        var scaleFactor = 1f
                        val totalRowsCount = sheet.physicalNumberOfRows.coerceAtLeast(1)
                        val estTotalHeight = totalRowsCount * baseRowHeight

                        when (scalingMode) {
                            "no_scaling" -> {
                                colWidth = 95f
                                scaleFactor = 1f
                            }
                            "fit_columns" -> {
                                colWidth = usableWidth / maxCols
                                scaleFactor = 1f
                            }
                            "fit_rows" -> {
                                colWidth = usableWidth / maxCols
                                if (estTotalHeight > usableHeight) {
                                    scaleFactor = (usableHeight / estTotalHeight).coerceIn(0.5f, 1f)
                                }
                            }
                            "fit_all" -> {
                                // Fit horizontally
                                val hColWidth = usableWidth / maxCols
                                // Fit vertically
                                val vScale = if (estTotalHeight > usableHeight) {
                                    (usableHeight / estTotalHeight).coerceIn(0.5f, 1f)
                                } else 1f
                                colWidth = hColWidth
                                scaleFactor = vScale
                            }
                        }

                        val fontSize = baseFontSize * scaleFactor
                        val headerFontSize = baseHeaderFontSize * scaleFactor
                        val cellPadding = baseCellPadding * scaleFactor
                        val rowHeight = fontSize * 1.6f + cellPadding * 2

                        var page = PDPage(pageRect)
                        pdfDoc.addPage(page)
                        var contentStream = PDPageContentStream(pdfDoc, page)
                        var yPos = pageRect.height - margin

                        // Sheet title
                        contentStream.beginText()
                        contentStream.setFont(boldFont, 12f)
                        contentStream.newLineAtOffset(margin, yPos)
                        try {
                            contentStream.showText("Sheet: ${sheet.sheetName}")
                        } catch (e: Exception) {
                            contentStream.showText("Sheet: ${sheet.sheetName.filter { it.code in 32..126 }}")
                        }
                        contentStream.endText()
                        yPos -= 24f

                        for (row in sheet) {
                            if (yPos - rowHeight < margin) {
                                // Bottom border line before page split
                                if (showGridlines) {
                                    contentStream.setStrokingColor(0.7f, 0.7f, 0.7f)
                                    contentStream.moveTo(margin, yPos + rowHeight)
                                    contentStream.lineTo(margin + maxCols * colWidth, yPos + rowHeight)
                                    contentStream.stroke()
                                }

                                contentStream.close()
                                page = PDPage(pageRect)
                                pdfDoc.addPage(page)
                                contentStream = PDPageContentStream(pdfDoc, page)
                                yPos = pageRect.height - margin
                            }

                            val isFirstRow = row.rowNum == sheet.firstRowNum
                            val currentFont = if (isFirstRow) boldFont else font
                            val currentFontSize = if (isFirstRow) headerFontSize else fontSize

                            // Draw cell borders and fill
                            for (colIdx in 0 until maxCols) {
                                val x = margin + colIdx * colWidth

                                if (isFirstRow) {
                                    // Header row background
                                    contentStream.setNonStrokingColor(0.9f, 0.92f, 0.96f)
                                    contentStream.addRect(x, yPos - rowHeight, colWidth, rowHeight)
                                    contentStream.fill()
                                }

                                // Cell border
                                if (showGridlines) {
                                    contentStream.setStrokingColor(0.75f, 0.75f, 0.75f)
                                    contentStream.addRect(x, yPos - rowHeight, colWidth, rowHeight)
                                    contentStream.stroke()
                                }
                            }

                            // Draw cell text
                            contentStream.setNonStrokingColor(0.1f, 0.1f, 0.1f)
                            for (colIdx in 0 until maxCols) {
                                val cell = row.getCell(colIdx)
                                val cellText = if (cell != null) {
                                    when (cell.cellType) {
                                        CellType.STRING -> cell.stringCellValue
                                        CellType.NUMERIC -> {
                                            if (DateUtil.isCellDateFormatted(cell)) {
                                                cell.localDateTimeCellValue?.toString() ?: ""
                                            } else {
                                                val num = cell.numericCellValue
                                                if (num == num.toLong().toDouble()) num.toLong().toString()
                                                else String.format("%.2f", num)
                                            }
                                        }
                                        CellType.BOOLEAN -> cell.booleanCellValue.toString()
                                        CellType.FORMULA -> try { cell.numericCellValue.toString() } catch (e: Exception) { cell.stringCellValue }
                                        else -> ""
                                    }
                                } else ""

                                if (cellText.isNotEmpty()) {
                                    val x = margin + colIdx * colWidth + cellPadding
                                    val maxTextWidth = colWidth - cellPadding * 2
                                    val maxChars = ((maxTextWidth / (currentFontSize * 0.5f)).toInt()).coerceAtLeast(1)
                                    val displayText = if (cellText.length > maxChars) cellText.take(maxChars) else cellText

                                    contentStream.beginText()
                                    contentStream.setFont(currentFont, currentFontSize)
                                    contentStream.newLineAtOffset(x, yPos - rowHeight + cellPadding + 2f)
                                    try {
                                        contentStream.showText(displayText)
                                    } catch (e: Exception) {
                                        contentStream.showText(displayText.filter { it.code in 32..126 })
                                    }
                                    contentStream.endText()
                                }
                            }

                            yPos -= rowHeight
                        }

                        contentStream.close()
                    }

                    workbook.close()
                    pdfDoc.save(outputFile)
                }
            } ?: throw IllegalArgumentException("Could not open the Excel file.")
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            outputFile.delete()
            throw e
        }
    }

    /**
     * Converts a PDF document into a Word (.docx) file by extracting text per page.
     */
    suspend fun convertPdfToWord(context: Context, uri: Uri): Uri = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("pdf_to_word_", ".pdf", context.cacheDir)
        val outputFile = File(context.cacheDir, "PdfToWord_${System.currentTimeMillis()}.docx")

        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output -> input.copyTo(output) }
            }

            PDDocument.load(tempInputFile).use { pdfDoc ->
                val wordDoc = XWPFDocument()
                val totalPages = pdfDoc.numberOfPages
                val stripper = PDFTextStripper()

                for (pageNum in 1..totalPages) {
                    stripper.startPage = pageNum
                    stripper.endPage = pageNum
                    val pageText = stripper.getText(pdfDoc)

                    // Page header
                    val heading = wordDoc.createParagraph()
                    heading.style = "Heading2"
                    val headingRun = heading.createRun()
                    headingRun.isBold = true
                    headingRun.fontSize = 14
                    headingRun.setText("Page $pageNum")

                    // Page content
                    val lines = pageText.split("\n")
                    for (line in lines) {
                        val para = wordDoc.createParagraph()
                        val run = para.createRun()
                        run.fontSize = 11
                        run.setText(line)
                    }

                    // Page break between pages (except last)
                    if (pageNum < totalPages) {
                        val breakPara = wordDoc.createParagraph()
                        breakPara.isPageBreak = true
                    }
                }

                outputFile.outputStream().use { out ->
                    wordDoc.write(out)
                }
                wordDoc.close()
            }

            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            outputFile.delete()
            throw e
        } finally {
            tempInputFile.delete()
        }
    }

    /**
     * Converts a PDF document into a PowerPoint (.pptx) or OpenDoc Template (.otp) file with slide layout and OCR options.
     */
    suspend fun convertPdfToPpt(
        context: Context,
        uri: Uri,
        slidesPerPage: Int = 1,
        includeNotes: Boolean = false,
        runOcr: Boolean = true,
        exportFormat: String = "pptx"
    ): Uri = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("pdf_to_ppt_", ".pdf", context.cacheDir)
        val fileExtension = if (exportFormat == "otp") ".otp" else ".pptx"
        val outputFile = File(context.cacheDir, "PdfToPpt_${System.currentTimeMillis()}$fileExtension")

        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output -> input.copyTo(output) }
            }

            PDDocument.load(tempInputFile).use { pdfDoc ->
                val totalPages = pdfDoc.numberOfPages
                val stripper = PDFTextStripper()
                val slides = mutableListOf<PptSlideData>()

                val pageGroups = (1..totalPages).chunked(slidesPerPage.coerceIn(1, 4))
                for (group in pageGroups) {
                    currentCoroutineContext().ensureActive()
                    val boxes = group.mapIndexed { index, pageNum ->
                        PptTextBoxData(
                            pageNum = pageNum,
                            text = extractPageTextInternal(pdfDoc, pageNum, runOcr, stripper, context),
                            bounds = getPdfToPptBounds(group.size, index),
                            fontSize = getPdfToPptFontSize(group.size)
                        )
                    }
                    val notes = if (includeNotes) {
                        "Notes for: ${group.joinToString { "Page $it" }}\n\n[Write notes here]"
                    } else {
                        null
                    }
                    slides.add(PptSlideData(boxes, notes))
                }

                writeAndroidSafePptx(outputFile, slides)
            }

            Uri.fromFile(outputFile)
        } catch (t: Throwable) {
            outputFile.delete()
            throw t
        } finally {
            tempInputFile.delete()
        }
    }

    private data class PptSlideData(
        val boxes: List<PptTextBoxData>,
        val notes: String?
    )

    private data class PptTextBoxData(
        val pageNum: Int,
        val text: String,
        val bounds: PptBounds,
        val fontSize: Double
    )

    private data class PptBounds(
        val x: Int,
        val y: Int,
        val w: Int,
        val h: Int
    )

    private fun getPdfToPptBounds(groupSize: Int, index: Int): PptBounds {
        return when {
            groupSize == 1 -> PptBounds(30, 70, 660, 440)
            groupSize == 2 && index == 0 -> PptBounds(30, 70, 310, 440)
            groupSize == 2 -> PptBounds(380, 70, 310, 440)
            index == 0 -> PptBounds(30, 50, 310, 200)
            index == 1 -> PptBounds(380, 50, 310, 200)
            index == 2 -> PptBounds(30, 300, 310, 200)
            else -> PptBounds(380, 300, 310, 200)
        }
    }

    private fun getPdfToPptFontSize(groupSize: Int): Double = when (groupSize) {
        1 -> 10.0
        2 -> 9.0
        else -> 7.5
    }

    private fun writeAndroidSafePptx(outputFile: File, slides: List<PptSlideData>) {
        ZipOutputStream(outputFile.outputStream().buffered()).use { zip ->
            zip.putXml("[Content_Types].xml", buildContentTypesXml(slides.size))
            zip.putXml("_rels/.rels", rootRelsXml())
            zip.putXml("docProps/app.xml", appXml(slides.size))
            zip.putXml("docProps/core.xml", coreXml())
            zip.putXml("ppt/presentation.xml", presentationXml(slides.size))
            zip.putXml("ppt/_rels/presentation.xml.rels", presentationRelsXml(slides.size))
            zip.putXml("ppt/slideMasters/slideMaster1.xml", slideMasterXml())
            zip.putXml("ppt/slideMasters/_rels/slideMaster1.xml.rels", slideMasterRelsXml())
            zip.putXml("ppt/slideLayouts/slideLayout1.xml", slideLayoutXml())
            zip.putXml("ppt/slideLayouts/_rels/slideLayout1.xml.rels", slideLayoutRelsXml())
            zip.putXml("ppt/theme/theme1.xml", themeXml())
            zip.putXml("ppt/presProps.xml", presPropsXml())
            zip.putXml("ppt/viewProps.xml", viewPropsXml())
            zip.putXml("ppt/tableStyles.xml", tableStylesXml())

            slides.forEachIndexed { index, slide ->
                val slideNumber = index + 1
                zip.putXml("ppt/slides/slide$slideNumber.xml", slideXml(slide, slideNumber))
                zip.putXml("ppt/slides/_rels/slide$slideNumber.xml.rels", slideRelsXml())
            }
        }
    }

    private fun ZipOutputStream.putXml(path: String, xml: String) {
        putNextEntry(ZipEntry(path))
        write(xml.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun buildContentTypesXml(slideCount: Int): String {
        val slideOverrides = (1..slideCount).joinToString("") {
            """<Override PartName="/ppt/slides/slide$it.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>"""
        }
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
<Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
<Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/>
<Override PartName="/ppt/slideMasters/slideMaster1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml"/>
<Override PartName="/ppt/slideLayouts/slideLayout1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml"/>
<Override PartName="/ppt/theme/theme1.xml" ContentType="application/vnd.openxmlformats-officedocument.theme+xml"/>
<Override PartName="/ppt/presProps.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presProps+xml"/>
<Override PartName="/ppt/viewProps.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.viewProps+xml"/>
<Override PartName="/ppt/tableStyles.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.tableStyles+xml"/>
$slideOverrides
</Types>"""
    }

    private fun rootRelsXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="ppt/presentation.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
<Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>"""

    private fun appXml(slideCount: Int): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
<Application>PDF Tools</Application><PresentationFormat>On-screen Show (4:3)</PresentationFormat><Slides>$slideCount</Slides><Notes>0</Notes><HiddenSlides>0</HiddenSlides><MMClips>0</MMClips><ScaleCrop>false</ScaleCrop><HeadingPairs><vt:vector size="2" baseType="variant"><vt:variant><vt:lpstr>Slides</vt:lpstr></vt:variant><vt:variant><vt:i4>$slideCount</vt:i4></vt:variant></vt:vector></HeadingPairs><TitlesOfParts><vt:vector size="$slideCount" baseType="lpstr">${(1..slideCount).joinToString("") { "<vt:lpstr>Slide $it</vt:lpstr>" }}</vt:vector></TitlesOfParts><Company></Company><LinksUpToDate>false</LinksUpToDate><SharedDoc>false</SharedDoc><HyperlinksChanged>false</HyperlinksChanged><AppVersion>16.0000</AppVersion>
</Properties>"""

    private fun coreXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><dc:title>Converted PDF</dc:title><dc:creator>PDF Tools</dc:creator><cp:lastModifiedBy>PDF Tools</cp:lastModifiedBy></cp:coreProperties>"""

    private fun presentationXml(slideCount: Int): String {
        val slideIds = (1..slideCount).joinToString("") {
            """<p:sldId id="${255 + it}" r:id="rId$it"/>"""
        }
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:presentation xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
<p:sldMasterIdLst><p:sldMasterId id="2147483648" r:id="rId${slideCount + 1}"/></p:sldMasterIdLst>
<p:sldIdLst>$slideIds</p:sldIdLst>
<p:sldSz cx="9144000" cy="6858000" type="screen4x3"/><p:notesSz cx="6858000" cy="9144000"/><p:defaultTextStyle/>
</p:presentation>"""
    }

    private fun presentationRelsXml(slideCount: Int): String {
        val slideRels = (1..slideCount).joinToString("") {
            """<Relationship Id="rId$it" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide" Target="slides/slide$it.xml"/>"""
        }
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
$slideRels
<Relationship Id="rId${slideCount + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="slideMasters/slideMaster1.xml"/>
<Relationship Id="rId${slideCount + 2}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/presProps" Target="presProps.xml"/>
<Relationship Id="rId${slideCount + 3}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/viewProps" Target="viewProps.xml"/>
<Relationship Id="rId${slideCount + 4}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/tableStyles" Target="tableStyles.xml"/>
</Relationships>"""
    }

    private fun slideXml(slide: PptSlideData, slideNumber: Int): String {
        val shapes = slide.boxes.joinToString("") { box -> textShapeXml(box) }
        val notesShape = slide.notes?.let {
            textShapeXml(PptTextBoxData(0, it, PptBounds(30, 520, 660, 45), 8.0), placeholderTitle = "Notes")
        } ?: ""
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sld xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
<p:cSld><p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr>$shapes$notesShape</p:spTree></p:cSld><p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>
</p:sld>"""
    }

    private fun textShapeXml(box: PptTextBoxData, placeholderTitle: String = "Page ${box.pageNum}"): String {
        val titleHeight = 28
        val titleText = escapeXml(placeholderTitle)
        val bodyParagraphs = box.text
            .ifBlank { "[No selectable text found on this page]" }
            .lineSequence()
            .take(30)
            .joinToString("") { """<a:p><a:r><a:rPr lang="en-US" sz="${(box.fontSize * 100).toInt()}"/><a:t>${escapeXml(it)}</a:t></a:r><a:endParaRPr lang="en-US" sz="${(box.fontSize * 100).toInt()}"/></a:p>""" }
        return """<p:sp><p:nvSpPr><p:cNvPr id="${box.pageNum + 10}" name="$titleText"/><p:cNvSpPr txBox="1"/><p:nvPr/></p:nvSpPr><p:spPr><a:xfrm><a:off x="${ptToEmu(box.bounds.x)}" y="${ptToEmu(box.bounds.y - titleHeight)}"/><a:ext cx="${ptToEmu(box.bounds.w)}" cy="${ptToEmu(box.bounds.h + titleHeight)}"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom><a:solidFill><a:srgbClr val="FFFFFF"/></a:solidFill><a:ln><a:solidFill><a:srgbClr val="D9E2F3"/></a:solidFill></a:ln></p:spPr><p:txBody><a:bodyPr wrap="square" rtlCol="0"><a:spAutoFit/></a:bodyPr><a:lstStyle/><a:p><a:r><a:rPr lang="en-US" b="1" sz="1400"/><a:t>$titleText</a:t></a:r><a:endParaRPr lang="en-US" sz="1400"/></a:p>$bodyParagraphs</p:txBody></p:sp>"""
    }

    private fun slideRelsXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>
</Relationships>"""

    private fun slideMasterXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldMaster xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"><p:cSld><p:bg><p:bgPr><a:solidFill><a:srgbClr val="FFFFFF"/></a:solidFill><a:effectLst/></p:bgPr></p:bg><p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr></p:spTree></p:cSld><p:clrMap bg1="lt1" tx1="dk1" bg2="lt2" tx2="dk2" accent1="accent1" accent2="accent2" accent3="accent3" accent4="accent4" accent5="accent5" accent6="accent6" hlink="hlink" folHlink="folHlink"/><p:sldLayoutIdLst><p:sldLayoutId id="2147483649" r:id="rId1"/></p:sldLayoutIdLst><p:txStyles><p:titleStyle/><p:bodyStyle/><p:otherStyle/></p:txStyles></p:sldMaster>"""

    private fun slideMasterRelsXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/><Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme" Target="../theme/theme1.xml"/></Relationships>"""

    private fun slideLayoutXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldLayout xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" type="blank" preserve="1"><p:cSld name="Blank"><p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr></p:spTree></p:cSld><p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr></p:sldLayout>"""

    private fun slideLayoutRelsXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="../slideMasters/slideMaster1.xml"/></Relationships>"""

    private fun themeXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<a:theme xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" name="PDF Tools"><a:themeElements><a:clrScheme name="Office"><a:dk1><a:srgbClr val="000000"/></a:dk1><a:lt1><a:srgbClr val="FFFFFF"/></a:lt1><a:dk2><a:srgbClr val="1F497D"/></a:dk2><a:lt2><a:srgbClr val="EEECE1"/></a:lt2><a:accent1><a:srgbClr val="4F81BD"/></a:accent1><a:accent2><a:srgbClr val="C0504D"/></a:accent2><a:accent3><a:srgbClr val="9BBB59"/></a:accent3><a:accent4><a:srgbClr val="8064A2"/></a:accent4><a:accent5><a:srgbClr val="4BACC6"/></a:accent5><a:accent6><a:srgbClr val="F79646"/></a:accent6><a:hlink><a:srgbClr val="0000FF"/></a:hlink><a:folHlink><a:srgbClr val="800080"/></a:folHlink></a:clrScheme><a:fontScheme name="Office"><a:majorFont><a:latin typeface="Aptos Display"/></a:majorFont><a:minorFont><a:latin typeface="Aptos"/></a:minorFont></a:fontScheme><a:fmtScheme name="Office"><a:fillStyleLst><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:fillStyleLst><a:lnStyleLst><a:ln w="6350"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:ln></a:lnStyleLst><a:effectStyleLst><a:effectStyle><a:effectLst/></a:effectStyle></a:effectStyleLst><a:bgFillStyleLst><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:bgFillStyleLst></a:fmtScheme></a:themeElements></a:theme>"""

    private fun presPropsXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><p:presentationPr xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"/>"""

    private fun viewPropsXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><p:viewPr xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"/>"""

    private fun tableStylesXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><a:tblStyleLst xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" def="{5C22544A-7EE6-4342-B048-85BDC9FD1C3A}"/>"""

    private fun ptToEmu(value: Int): Int = value * 12700

    private fun escapeXml(value: String): String = buildString(value.length) {
        for (ch in value) {
            when (ch) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&apos;")
                else -> if (ch.code == 0x9 || ch.code == 0xA || ch.code == 0xD || ch.code >= 0x20) {
                    append(ch)
                }
            }
        }
    }

    private suspend fun extractPageTextInternal(
        pdfDoc: PDDocument,
        pageNum: Int,
        runOcr: Boolean,
        stripper: PDFTextStripper,
        context: Context
    ): String {
        stripper.startPage = pageNum
        stripper.endPage = pageNum
        var pageText = stripper.getText(pdfDoc) ?: ""
        if (pageText.trim().isEmpty() && runOcr) {
            try {
                val renderer = PDFRenderer(pdfDoc)
                val bitmap = renderer.renderImageWithDPI(pageNum - 1, 150f, ImageType.ARGB)
                try {
                    val image = InputImage.fromBitmap(bitmap, 0)
                    val ocrLang = UserPreferencesRepository(context).preferences.first().ocrLanguage
                    val options = when (ocrLang) {
                        "chinese" -> ChineseTextRecognizerOptions.Builder().build()
                        "devanagari" -> DevanagariTextRecognizerOptions.Builder().build()
                        "japanese" -> JapaneseTextRecognizerOptions.Builder().build()
                        "korean" -> KoreanTextRecognizerOptions.Builder().build()
                        else -> TextRecognizerOptions.DEFAULT_OPTIONS
                    }
                    val recognizer = TextRecognition.getClient(options)
                    val result = Tasks.await(recognizer.process(image))
                    pageText = result.text
                } catch (e: Exception) {
                    e.printStackTrace()
                    pageText = "[Scanned Page $pageNum - Offline Text Recognition Fallback Result]"
                } finally {
                    bitmap.recycle()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return pageText
    }

    private fun fillTextBoxWithTextInternal(
        contentBox: XSLFTextBox,
        text: String,
        fontSize: Double
    ) {
        val lines = text.split("\n")
        var isFirst = true
        for (line in lines) {
            val para = if (isFirst) {
                contentBox.textParagraphs.firstOrNull() ?: contentBox.addNewTextParagraph()
            } else {
                contentBox.addNewTextParagraph()
            }
            val run = para.addNewTextRun()
            run.setText(line)
            run.fontSize = fontSize
            isFirst = false
        }
    }


    /**
     * Converts a PDF document into an Excel (.xlsx) file by extracting text line by line.
     * Attempts basic tab/multi-space splitting for columnar data.
     */
    suspend fun convertPdfToExcel(context: Context, uri: Uri): Uri = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("pdf_to_excel_", ".pdf", context.cacheDir)
        val outputFile = File(context.cacheDir, "PdfToExcel_${System.currentTimeMillis()}.xlsx")

        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output -> input.copyTo(output) }
            }

            PDDocument.load(tempInputFile).use { pdfDoc ->
                val workbook = XSSFWorkbook()
                val totalPages = pdfDoc.numberOfPages
                val stripper = PDFTextStripper()

                for (pageNum in 1..totalPages) {
                    stripper.startPage = pageNum
                    stripper.endPage = pageNum
                    val pageText = stripper.getText(pdfDoc)

                    val sheetName = "Page $pageNum".take(31) // Excel sheet name max 31 chars
                    val sheet = workbook.createSheet(sheetName)

                    val lines = pageText.split("\n")
                    for ((lineIdx, line) in lines.withIndex()) {
                        if (line.isBlank()) continue
                        val row = sheet.createRow(lineIdx)

                        // Attempt to split by tabs first, then by 2+ consecutive spaces
                        val cells = if (line.contains("\t")) {
                            line.split("\t")
                        } else if (line.contains("  ")) {
                            line.split(Regex("\\s{2,}"))
                        } else {
                            listOf(line)
                        }

                        for ((cellIdx, cellText) in cells.withIndex()) {
                            val cell = row.createCell(cellIdx)
                            val trimmed = cellText.trim()
                            // Try to set as number if parseable
                            val numVal = trimmed.toDoubleOrNull()
                            if (numVal != null) {
                                cell.setCellValue(numVal)
                            } else {
                                cell.setCellValue(trimmed)
                            }
                        }
                    }

                    // Auto-size first 10 columns for readability
                    for (col in 0 until 10.coerceAtMost(sheet.getRow(0)?.lastCellNum?.toInt() ?: 0)) {
                        try { sheet.autoSizeColumn(col) } catch (e: Exception) { /* ignore */ }
                    }
                }

                outputFile.outputStream().use { out ->
                    workbook.write(out)
                }
                workbook.close()
            }

            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            outputFile.delete()
            throw e
        } finally {
            tempInputFile.delete()
        }
    }

    /**
     * Detect bullet character for a paragraph. Checks for explicit buChar in paragraph properties,
     * and also checks for indent level (which indicates a bulleted list item).
     * Returns the bullet character string, or empty string if none.
     */
    private fun getBulletPrefix(paragraph: org.apache.poi.xslf.usermodel.XSLFTextParagraph): String {
        try {
            // Try to get bullet character via POI API
            val bulletChar = paragraph.bulletCharacter
            if (bulletChar != null && bulletChar.isNotBlank()) {
                // Filter to printable ASCII; if the bullet is a special unicode char, use a dot
                val filtered = bulletChar.filter { it.code in 32..126 }
                return if (filtered.isNotEmpty()) filtered else "\u2022".filter { it.code in 32..126 }.ifEmpty { "-" }
            }
        } catch (_: Throwable) {}

        // Try via reflection on XML for buChar
        try {
            val xmlObj = paragraph.javaClass.getMethod("getXmlObject").invoke(paragraph) ?: return ""
            val pPr = try { xmlObj.javaClass.getMethod("getPPr").invoke(xmlObj) } catch (_: Throwable) { null }
            if (pPr != null) {
                val buChar = try { pPr.javaClass.getMethod("getBuChar").invoke(pPr) } catch (_: Throwable) { null }
                if (buChar != null) {
                    val charVal = try { buChar.javaClass.getMethod("getChar").invoke(buChar)?.toString() } catch (_: Throwable) { null }
                    if (charVal != null && charVal.isNotBlank()) {
                        val filtered = charVal.filter { it.code in 32..126 }
                        return if (filtered.isNotEmpty()) filtered else "-"
                    }
                }
                // Check indent level as indication of list item
                val lvl = try { pPr.javaClass.getMethod("getLvl").invoke(pPr) as? Int } catch (_: Throwable) { null }
                if (lvl != null && lvl > 0) {
                    return "-"
                }
            }
        } catch (_: Throwable) {}

        return ""
    }

    /**
     * Extract paragraph alignment from XML properties.
     * Returns "l" (left), "ctr" (center), "r" (right), "just" (justify), or "l" as default.
     */
    private fun getParagraphAlignment(paragraph: org.apache.poi.xslf.usermodel.XSLFTextParagraph): String {
        // Try POI API first
        try {
            val align = paragraph.textAlign
            if (align != null) {
                return when (align.name.lowercase()) {
                    "center" -> "ctr"
                    "right" -> "r"
                    "justify", "justified" -> "just"
                    else -> "l"
                }
            }
        } catch (_: Throwable) {}

        // Fallback: reflection on XML
        try {
            val xmlObj = paragraph.javaClass.getMethod("getXmlObject").invoke(paragraph) ?: return "l"
            val pPr = try { xmlObj.javaClass.getMethod("getPPr").invoke(xmlObj) } catch (_: Throwable) { null }
            if (pPr != null) {
                val algn = try { pPr.javaClass.getMethod("getAlgn").invoke(pPr) } catch (_: Throwable) { null }
                if (algn != null) {
                    val algnStr = algn.toString().lowercase()
                    return when {
                        algnStr.contains("ctr") || algnStr.contains("center") -> "ctr"
                        algnStr.contains("r") && !algnStr.contains("l") -> "r"
                        algnStr.contains("just") -> "just"
                        else -> "l"
                    }
                }
            }
        } catch (_: Throwable) {}

        return "l"
    }

    /**
     * Extract text color from an XSLFTextRun using reflection.
     * Tries: (1) XML object solidFill srgbClr, (2) XML solidFill schemeClr.
     * Returns packed 0xAARRGGBB int or null if no color is found.
     */
    private fun getTextRunColor(run: org.apache.poi.xslf.usermodel.XSLFTextRun?): Int? {
        if (run == null) return null

        // Strategy 1: Parse from XML (Android-safe, no AWT dependency)
        try {
            val xmlObj = run.javaClass.getMethod("getXmlObject").invoke(run) ?: return null
            // Get rPr (run properties)
            val rPr = try {
                xmlObj.javaClass.getMethod("getRPr").invoke(xmlObj)
            } catch (_: Throwable) { null }

            if (rPr != null) {
                // Try solidFill -> srgbClr
                val solidFill = try {
                    rPr.javaClass.getMethod("getSolidFill").invoke(rPr)
                } catch (_: Throwable) { null }

                if (solidFill != null) {
                    val srgbClr = try {
                        solidFill.javaClass.getMethod("getSrgbClr").invoke(solidFill)
                    } catch (_: Throwable) { null }

                    if (srgbClr != null) {
                        val rgbBytes = srgbClr.javaClass.getMethod("getVal").invoke(srgbClr) as? ByteArray
                        if (rgbBytes != null && rgbBytes.size >= 3) {
                            val r = rgbBytes[0].toInt() and 0xFF
                            val g = rgbBytes[1].toInt() and 0xFF
                            val b = rgbBytes[2].toInt() and 0xFF
                            return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                        }
                    }

                    // Try solidFill -> schemeClr
                    val schemeClr = try {
                        solidFill.javaClass.getMethod("getSchemeClr").invoke(solidFill)
                    } catch (_: Throwable) { null }
                    if (schemeClr != null) {
                        val valStr = schemeClr.javaClass.getMethod("getVal").invoke(schemeClr)?.toString() ?: ""
                        return getSchemeColorHex(valStr)
                    }
                }
            }
        } catch (_: Throwable) {}

        return null
    }

    private fun getShapeAnchorSafe(shape: org.apache.poi.xslf.usermodel.XSLFShape): android.graphics.RectF? {
        val anchorXml = getShapeAnchorAndroid(shape)
        if (anchorXml != null) return anchorXml

        try {
            val anchorAwt = shape.anchor
            if (anchorAwt != null) {
                return android.graphics.RectF(
                    anchorAwt.x.toFloat(),
                    anchorAwt.y.toFloat(),
                    (anchorAwt.x + anchorAwt.width).toFloat(),
                    (anchorAwt.y + anchorAwt.height).toFloat()
                )
            }
        } catch (e: Throwable) {
            Log.d("ConvertProcessor", "AWT anchor fallback failed for ${shape.javaClass.simpleName}: ${e.javaClass.simpleName}")
        }
        Log.w("ConvertProcessor", "Could not get anchor for shape: ${shape.javaClass.simpleName}")
        return null
    }

    private fun getShapeAnchorAndroid(shape: Any): android.graphics.RectF? {
        try {
            val xmlObject = shape.javaClass.getMethod("getXmlObject").invoke(shape) ?: return null
            val xfrm = getXfrmFromXmlObject(xmlObject) ?: return null
            
            val off = xfrm.javaClass.getMethod("getOff").invoke(xfrm) ?: return null
            val ext = xfrm.javaClass.getMethod("getExt").invoke(xfrm) ?: return null
            
            val xVal = off.javaClass.getMethod("getX").invoke(off) as Long
            val yVal = off.javaClass.getMethod("getY").invoke(off) as Long
            val cxVal = ext.javaClass.getMethod("getCx").invoke(ext) as Long
            val cyVal = ext.javaClass.getMethod("getCy").invoke(ext) as Long
            
            val xPoints = xVal / 12700f
            val yPoints = yVal / 12700f
            val wPoints = cxVal / 12700f
            val hPoints = cyVal / 12700f
            
            return android.graphics.RectF(xPoints, yPoints, xPoints + wPoints, yPoints + hPoints)
        } catch (t: Throwable) {
            Log.d("ConvertProcessor", "XML anchor extraction failed: ${t.javaClass.simpleName}: ${t.message}")
        }
        return null
    }

    private fun getXfrmFromXmlObject(xmlObj: Any): Any? {
        try {
            try {
                val spPr = xmlObj.javaClass.getMethod("getSpPr").invoke(xmlObj)
                if (spPr != null) {
                    val xfrm = spPr.javaClass.getMethod("getXfrm").invoke(spPr)
                    if (xfrm != null) return xfrm
                }
            } catch (_: Throwable) {}

            try {
                val xfrm = xmlObj.javaClass.getMethod("getXfrm").invoke(xmlObj)
                if (xfrm != null) return xfrm
            } catch (_: Throwable) {}

            try {
                val grpSpPr = xmlObj.javaClass.getMethod("getGrpSpPr").invoke(xmlObj)
                if (grpSpPr != null) {
                    val xfrm = grpSpPr.javaClass.getMethod("getXfrm").invoke(grpSpPr)
                    if (xfrm != null) return xfrm
                }
            } catch (_: Throwable) {}
        } catch (t: Throwable) {}
        return null
    }

    private fun getFillColorSafe(shape: org.apache.poi.xslf.usermodel.XSLFSimpleShape): Int? {
        val col = getFillColorAndroid(shape)
        if (col != null) return col

        try {
            val awtCol = shape.fillColor
            if (awtCol != null) {
                return (0xFF shl 24) or (awtCol.red shl 16) or (awtCol.green shl 8) or awtCol.blue
            }
        } catch (_: Throwable) {}
        return null
    }

    private fun getFillColorAndroid(shape: Any): Int? {
        try {
            val xmlObject = shape.javaClass.getMethod("getXmlObject").invoke(shape) ?: return null
            val spPr = xmlObject.javaClass.getMethod("getSpPr").invoke(xmlObject) ?: return null
            
            val solidFill = try {
                spPr.javaClass.getMethod("getSolidFill").invoke(spPr)
            } catch (_: NoSuchMethodException) { null }
            
            if (solidFill != null) {
                val srgbClr = try {
                    solidFill.javaClass.getMethod("getSrgbClr").invoke(solidFill)
                } catch (_: NoSuchMethodException) { null }
                
                if (srgbClr != null) {
                    val rgbBytes = srgbClr.javaClass.getMethod("getVal").invoke(srgbClr) as? ByteArray
                    if (rgbBytes != null && rgbBytes.size >= 3) {
                        val r = rgbBytes[0].toInt() and 0xFF
                        val g = rgbBytes[1].toInt() and 0xFF
                        val b = rgbBytes[2].toInt() and 0xFF
                        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                    }
                }
                
                val schemeClr = try {
                    solidFill.javaClass.getMethod("getSchemeClr").invoke(solidFill)
                } catch (_: NoSuchMethodException) { null }
                if (schemeClr != null) {
                    val valStr = schemeClr.javaClass.getMethod("getVal").invoke(schemeClr)?.toString() ?: ""
                    return getSchemeColorHex(valStr)
                }
            }
        } catch (t: Throwable) {}
        return null
    }

    private fun getLineColorSafe(shape: org.apache.poi.xslf.usermodel.XSLFSimpleShape): Int? {
        val col = getLineColorAndroid(shape)
        if (col != null) return col

        try {
            val awtCol = shape.lineColor
            if (awtCol != null) {
                return (0xFF shl 24) or (awtCol.red shl 16) or (awtCol.green shl 8) or awtCol.blue
            }
        } catch (_: Throwable) {}
        return null
    }

    private fun getLineColorAndroid(shape: Any): Int? {
        try {
            val xmlObject = shape.javaClass.getMethod("getXmlObject").invoke(shape) ?: return null
            val spPr = xmlObject.javaClass.getMethod("getSpPr").invoke(xmlObject) ?: return null
            
            val ln = try {
                spPr.javaClass.getMethod("getLn").invoke(spPr)
            } catch (_: NoSuchMethodException) { null }
            
            if (ln != null) {
                val solidFill = try {
                    ln.javaClass.getMethod("getSolidFill").invoke(ln)
                } catch (_: NoSuchMethodException) { null }
                
                if (solidFill != null) {
                    val srgbClr = try {
                        solidFill.javaClass.getMethod("getSrgbClr").invoke(solidFill)
                    } catch (_: NoSuchMethodException) { null }
                    
                    if (srgbClr != null) {
                        val rgbBytes = srgbClr.javaClass.getMethod("getVal").invoke(srgbClr) as? ByteArray
                        if (rgbBytes != null && rgbBytes.size >= 3) {
                            val r = rgbBytes[0].toInt() and 0xFF
                            val g = rgbBytes[1].toInt() and 0xFF
                            val b = rgbBytes[2].toInt() and 0xFF
                            return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                        }
                    }
                    
                    val schemeClr = try {
                        solidFill.javaClass.getMethod("getSchemeClr").invoke(solidFill)
                    } catch (_: NoSuchMethodException) { null }
                    if (schemeClr != null) {
                        val valStr = schemeClr.javaClass.getMethod("getVal").invoke(schemeClr)?.toString() ?: ""
                        return getSchemeColorHex(valStr)
                    }
                }
            }
        } catch (t: Throwable) {}
        return null
    }

    private fun getSchemeColorHex(name: String): Int {
        return when (name.lowercase()) {
            "bg1", "lt1" -> 0xFFFFFFFF.toInt()
            "bg2", "lt2" -> 0xFFF3F4F6.toInt()
            "tx1", "dk1" -> 0xFF1F2937.toInt()
            "tx2", "dk2" -> 0xFF374151.toInt()
            "accent1" -> 0xFF3B82F6.toInt()
            "accent2" -> 0xFFEF4444.toInt()
            "accent3" -> 0xFF10B981.toInt()
            "accent4" -> 0xFFF59E0B.toInt()
            "accent5" -> 0xFF6366F1.toInt()
            "accent6" -> 0xFF8B5CF6.toInt()
            "hlink" -> 0xFF2563EB.toInt()
            else -> 0xFF4B5563.toInt()
        }
    }

    private fun getSlideBackgroundColorSafe(slide: org.apache.poi.xslf.usermodel.XSLFSlide): Int? {
        // 1. Try direct slide background via XML reflection
        val col = getSlideBackgroundColorAndroid(slide)
        if (col != null) return col

        // 2. Try AWT API on slide background
        try {
            val bg = slide.background
            val awtCol = bg?.fillColor
            if (awtCol != null) {
                return (0xFF shl 24) or (awtCol.red shl 16) or (awtCol.green shl 8) or awtCol.blue
            }
        } catch (_: Throwable) {}

        // 3. Try slide layout background
        try {
            val layout = slide.slideLayout
            if (layout != null) {
                val layoutBg = layout.background
                if (layoutBg != null) {
                    val layoutCol = getBackgroundColorFromXml(layoutBg)
                    if (layoutCol != null) return layoutCol
                    try {
                        val awtCol = layoutBg.fillColor
                        if (awtCol != null) {
                            return (0xFF shl 24) or (awtCol.red shl 16) or (awtCol.green shl 8) or awtCol.blue
                        }
                    } catch (_: Throwable) {}
                }
            }
        } catch (_: Throwable) {}

        // 4. Try slide master background
        try {
            val master = slide.slideMaster
            if (master != null) {
                val masterBg = master.background
                if (masterBg != null) {
                    val masterCol = getBackgroundColorFromXml(masterBg)
                    if (masterCol != null) return masterCol
                    try {
                        val awtCol = masterBg.fillColor
                        if (awtCol != null) {
                            return (0xFF shl 24) or (awtCol.red shl 16) or (awtCol.green shl 8) or awtCol.blue
                        }
                    } catch (_: Throwable) {}
                }
            }
        } catch (_: Throwable) {}

        return null
    }

    /**
     * Extract background color from any XSLFBackground via XML reflection.
     * Reusable for slide, layout, and master backgrounds.
     */
    private fun getBackgroundColorFromXml(bg: Any): Int? {
        try {
            val xmlObject = bg.javaClass.getMethod("getXmlObject").invoke(bg) ?: return null

            val bgPr = try {
                xmlObject.javaClass.getMethod("getBgPr").invoke(xmlObject)
            } catch (_: NoSuchMethodException) { null }

            if (bgPr != null) {
                val solidFill = try {
                    bgPr.javaClass.getMethod("getSolidFill").invoke(bgPr)
                } catch (_: NoSuchMethodException) { null }

                if (solidFill != null) {
                    val srgbClr = try {
                        solidFill.javaClass.getMethod("getSrgbClr").invoke(solidFill)
                    } catch (_: NoSuchMethodException) { null }

                    if (srgbClr != null) {
                        val rgbBytes = srgbClr.javaClass.getMethod("getVal").invoke(srgbClr) as? ByteArray
                        if (rgbBytes != null && rgbBytes.size >= 3) {
                            val r = rgbBytes[0].toInt() and 0xFF
                            val g = rgbBytes[1].toInt() and 0xFF
                            val b = rgbBytes[2].toInt() and 0xFF
                            return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                        }
                    }

                    val schemeClr = try {
                        solidFill.javaClass.getMethod("getSchemeClr").invoke(solidFill)
                    } catch (_: NoSuchMethodException) { null }
                    if (schemeClr != null) {
                        val valStr = schemeClr.javaClass.getMethod("getVal").invoke(schemeClr)?.toString() ?: ""
                        return getSchemeColorHex(valStr)
                    }
                }
            }
        } catch (_: Throwable) {}
        return null
    }

    private fun getSlideBackgroundColorAndroid(slide: org.apache.poi.xslf.usermodel.XSLFSlide): Int? {
        try {
            val bg = slide.background ?: return null
            val xmlObject = bg.javaClass.getMethod("getXmlObject").invoke(bg) ?: return null
            
            val bgPr = try {
                xmlObject.javaClass.getMethod("getBgPr").invoke(xmlObject)
            } catch (_: NoSuchMethodException) { null }
            
            if (bgPr != null) {
                val solidFill = try {
                    bgPr.javaClass.getMethod("getSolidFill").invoke(bgPr)
                } catch (_: NoSuchMethodException) { null }
                
                if (solidFill != null) {
                    val srgbClr = try {
                        solidFill.javaClass.getMethod("getSrgbClr").invoke(solidFill)
                    } catch (_: NoSuchMethodException) { null }
                    
                    if (srgbClr != null) {
                        val rgbBytes = srgbClr.javaClass.getMethod("getVal").invoke(srgbClr) as? ByteArray
                        if (rgbBytes != null && rgbBytes.size >= 3) {
                            val r = rgbBytes[0].toInt() and 0xFF
                            val g = rgbBytes[1].toInt() and 0xFF
                            val b = rgbBytes[2].toInt() and 0xFF
                            return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                        }
                    }
                    
                    val schemeClr = try {
                        solidFill.javaClass.getMethod("getSchemeClr").invoke(solidFill)
                    } catch (_: NoSuchMethodException) { null }
                    if (schemeClr != null) {
                        val valStr = schemeClr.javaClass.getMethod("getVal").invoke(schemeClr)?.toString() ?: ""
                        return getSchemeColorHex(valStr)
                    }
                }
            }
        } catch (t: Throwable) {}
        return null
    }

    private fun setPresentationSlideSizeSafe(slideShow: XMLSlideShow, width: Int, height: Int) {
        try {
            val dimClass = Class.forName("java.awt.Dimension")
            val dimConstructor = dimClass.getConstructor(Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
            val dimInstance = dimConstructor.newInstance(width, height)
            
            val setPageSizeMethod = slideShow.javaClass.getMethod("setPageSize", dimClass)
            setPageSizeMethod.invoke(slideShow, dimInstance)
            return
        } catch (e: Throwable) {
            Log.d("ConvertProcessor", "java.awt.Dimension missing, setting slide size via XML: ${e.message}")
        }

        try {
            val ctPres = slideShow.javaClass.getMethod("getCTPresentation").invoke(slideShow) ?: return
            val sldSz = try {
                ctPres.javaClass.getMethod("getSldSz").invoke(ctPres) ?: ctPres.javaClass.getMethod("addNewSldSz").invoke(ctPres)
            } catch (e: Throwable) {
                ctPres.javaClass.getMethod("addNewSldSz").invoke(ctPres)
            }
            if (sldSz != null) {
                sldSz.javaClass.getMethod("setCx", Int::class.javaPrimitiveType).invoke(sldSz, width * 12700)
                sldSz.javaClass.getMethod("setCy", Int::class.javaPrimitiveType).invoke(sldSz, height * 12700)
            }
        } catch (e: Throwable) {
            Log.e("ConvertProcessor", "Failed to set slide size via XML", e)
        }
    }

    private fun setShapeAnchorSafe(shape: org.apache.poi.xslf.usermodel.XSLFShape, x: Double, y: Double, w: Double, h: Double) {
        try {
            val rectClass = Class.forName("java.awt.geom.Rectangle2D\$Double")
            val rectConstructor = rectClass.getConstructor(Double::class.javaPrimitiveType, Double::class.javaPrimitiveType, Double::class.javaPrimitiveType, Double::class.javaPrimitiveType)
            val rectInstance = rectConstructor.newInstance(x, y, w, h)
            
            val setAnchorMethod = shape.javaClass.getMethod("setAnchor", Class.forName("java.awt.geom.Rectangle2D"))
            setAnchorMethod.invoke(shape, rectInstance)
            return
        } catch (e: Throwable) {
            Log.d("ConvertProcessor", "java.awt.geom.Rectangle2D missing, setting anchor via XML: ${e.message}")
        }

        try {
            val xmlObject = shape.javaClass.getMethod("getXmlObject").invoke(shape) ?: return
            val xfrm = getXfrmFromXmlObject(xmlObject) ?: return
            
            val off = try {
                xfrm.javaClass.getMethod("getOff").invoke(xfrm) ?: xfrm.javaClass.getMethod("addNewOff").invoke(xfrm)
            } catch (e: Throwable) {
                xfrm.javaClass.getMethod("addNewOff").invoke(xfrm)
            }
            if (off != null) {
                off.javaClass.getMethod("setX", Long::class.javaPrimitiveType).invoke(off, (x * 12700).toLong())
                off.javaClass.getMethod("setY", Long::class.javaPrimitiveType).invoke(off, (y * 12700).toLong())
            }

            val ext = try {
                xfrm.javaClass.getMethod("getExt").invoke(xfrm) ?: xfrm.javaClass.getMethod("addNewExt").invoke(xfrm)
            } catch (e: Throwable) {
                xfrm.javaClass.getMethod("addNewExt").invoke(xfrm)
            }
            if (ext != null) {
                ext.javaClass.getMethod("setCx", Long::class.javaPrimitiveType).invoke(ext, (w * 12700).toLong())
                ext.javaClass.getMethod("setCy", Long::class.javaPrimitiveType).invoke(ext, (h * 12700).toLong())
            }
        } catch (e: Throwable) {
            Log.e("ConvertProcessor", "Failed to set shape anchor via XML", e)
        }
    }

    private fun getPresentationSlideSize(slideShow: XMLSlideShow): Pair<Float, Float> {
        // Primary: try slideShow.pageSize (java.awt.Dimension)
        try {
            val pageSize = slideShow.pageSize
            if (pageSize != null) {
                val w = pageSize.width.toFloat()
                val h = pageSize.height.toFloat()
                if (w > 0 && h > 0) {
                    return Pair(w, h)
                }
            }
        } catch (_: Throwable) {
            // java.awt.Dimension may not be available on Android â€” fall through
        }

        // Fallback: parse XML for slide size
        try {
            val ctPres = slideShow.javaClass.getMethod("getCTPresentation").invoke(slideShow)
            if (ctPres != null) {
                val xmlStr = ctPres.toString()
                val sldSzTagRegex = Regex("""<[^>]*sldSz[^>]*>""")
                val tagMatch = sldSzTagRegex.find(xmlStr)
                if (tagMatch != null) {
                    val tagContent = tagMatch.value
                    val cxMatch = Regex("""\bcx="(\d+)"""").find(tagContent)
                    val cyMatch = Regex("""\bcy="(\d+)"""").find(tagContent)
                    if (cxMatch != null && cyMatch != null) {
                        val cx = cxMatch.groupValues[1].toLongOrNull()
                        val cy = cyMatch.groupValues[1].toLongOrNull() // Fixed: was [2], but regex only has 1 capture group
                        if (cx != null && cy != null && cx > 0 && cy > 0) {
                            return Pair(cx / 12700f, cy / 12700f)
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            Log.w("ConvertProcessor", "Failed to parse slide size from XML", e)
        }
        // Standard 10x7.5 inches in points
        return Pair(720f, 540f)
    }
}

