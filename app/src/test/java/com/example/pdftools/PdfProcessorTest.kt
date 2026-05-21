package com.example.pdftools

import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.pdftools.data.PdfProcessor
import com.example.pdftools.data.FormFieldInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import java.io.File
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PdfProcessorTest {

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        PDFBoxResourceLoader.init(context)
    }

    @Test
    fun testParsePageRanges_validRanges() {
        val totalPages = 10
        assertEquals(listOf(0, 1, 2, 4), PdfProcessor.parsePageRanges("1-3, 5", totalPages))
        assertEquals(listOf(0, 1, 4, 5, 6), PdfProcessor.parsePageRanges("1-2, 5-7", totalPages))
        assertEquals(listOf(0, 1, 2, 3, 4), PdfProcessor.parsePageRanges("5-1", totalPages))
        assertEquals(listOf(1, 2, 3), PdfProcessor.parsePageRanges("2-4, 3, 2", totalPages))
    }

    @Test
    fun testParsePageRanges_outOfBounds() {
        val totalPages = 5
        assertEquals(listOf(0, 1, 4), PdfProcessor.parsePageRanges("1, 2, 5, 6", totalPages))
        assertEquals(listOf(2, 3, 4), PdfProcessor.parsePageRanges("3-8", totalPages))
        assertEquals(emptyList<Int>(), PdfProcessor.parsePageRanges("10-12, 15", totalPages))
    }

    @Test
    fun testParsePageRanges_invalidInput() {
        val totalPages = 10
        assertEquals(listOf(0, 1), PdfProcessor.parsePageRanges("1, abc, 2, -", totalPages))
        assertEquals(emptyList<Int>(), PdfProcessor.parsePageRanges("1-2-3, 5-", totalPages))
    }

    @Test
    fun testProtectAndUnlockPdf() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_${System.currentTimeMillis()}.pdf")
        
        com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
            doc.save(dummyPdfFile)
        }

        val dummyUri = Uri.fromFile(dummyPdfFile)

        try {
            // Call protectPdf
            val password = "securepassword123"
            val protectedUri = PdfProcessor.protectPdf(context, dummyUri, password)
            val protectedFile = File(protectedUri.path ?: "")

            // Verify protected PDF is encrypted and requires password
            assertTrue(protectedFile.exists())
            
            // Loading without password should throw standard PDFBox exception (IOException)
            var loadFailed = false
            try {
                com.tom_roush.pdfbox.pdmodel.PDDocument.load(protectedFile)
            } catch (e: IOException) {
                loadFailed = true
            }
            assertTrue("Loading encrypted PDF without password should fail", loadFailed)

            // Loading with correct password should succeed
            val loadedDoc = com.tom_roush.pdfbox.pdmodel.PDDocument.load(protectedFile, password)
            assertTrue(loadedDoc.isEncrypted)
            loadedDoc.close()

            // Test unlocking the protected PDF
            val protectedUriFromFile = Uri.fromFile(protectedFile)
            val unlockedUri = PdfProcessor.unlockPdf(context, protectedUriFromFile, password)
            val unlockedFile = File(unlockedUri.path ?: "")

            assertTrue(unlockedFile.exists())

            // Loading unlocked PDF without password should succeed
            val unlockedDoc = com.tom_roush.pdfbox.pdmodel.PDDocument.load(unlockedFile)
            assertFalse(unlockedDoc.isEncrypted)
            unlockedDoc.close()

            // Cleanup temp files
            protectedFile.delete()
            unlockedFile.delete()
        } finally {
            dummyPdfFile.delete()
        }
    }

    @Test
    fun testRotatePdf() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_rotate_${System.currentTimeMillis()}.pdf")
        
        com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
            doc.save(dummyPdfFile)
        }

        val dummyUri = Uri.fromFile(dummyPdfFile)

        try {
            // Test 1: Rotate all pages by 90 degrees (empty range)
            val rotatedAllUri = PdfProcessor.rotatePdf(context, dummyUri, 90, "")
            val rotatedAllFile = File(rotatedAllUri.path ?: "")
            assertTrue(rotatedAllFile.exists())

            com.tom_roush.pdfbox.pdmodel.PDDocument.load(rotatedAllFile).use { doc ->
                assertEquals(3, doc.numberOfPages)
                assertEquals(90, doc.getPage(0).rotation)
                assertEquals(90, doc.getPage(1).rotation)
                assertEquals(90, doc.getPage(2).rotation)
            }

            // Test 2: Rotate specific pages (e.g., page 2 by 180 degrees -> total 270 degrees rotation)
            val rotatedSpecificUri = PdfProcessor.rotatePdf(context, Uri.fromFile(rotatedAllFile), 180, "2")
            val rotatedSpecificFile = File(rotatedSpecificUri.path ?: "")
            assertTrue(rotatedSpecificFile.exists())

            com.tom_roush.pdfbox.pdmodel.PDDocument.load(rotatedSpecificFile).use { doc ->
                assertEquals(3, doc.numberOfPages)
                assertEquals(90, doc.getPage(0).rotation)    // Unchanged
                assertEquals(270, doc.getPage(1).rotation)   // 90 + 180 = 270
                assertEquals(90, doc.getPage(2).rotation)    // Unchanged
            }

            rotatedAllFile.delete()
            rotatedSpecificFile.delete()
        } finally {
            dummyPdfFile.delete()
        }
    }

    @Test
    fun testExtractPages() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_extract_${System.currentTimeMillis()}.pdf")
        
        com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
            for (i in 1..5) {
                doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
            }
            doc.save(dummyPdfFile)
        }

        val dummyUri = Uri.fromFile(dummyPdfFile)

        try {
            // Extract pages 1, 2, and 5
            val extractedUri = PdfProcessor.extractPages(context, dummyUri, "1-2, 5")
            val extractedFile = File(extractedUri.path ?: "")
            assertTrue(extractedFile.exists())

            com.tom_roush.pdfbox.pdmodel.PDDocument.load(extractedFile).use { doc ->
                assertEquals(3, doc.numberOfPages)
            }

            extractedFile.delete()
        } finally {
            dummyPdfFile.delete()
        }
    }

    @Test
    fun testAddWatermark() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_watermark_${System.currentTimeMillis()}.pdf")
        
        com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
            doc.save(dummyPdfFile)
        }

        val dummyUri = Uri.fromFile(dummyPdfFile)

        try {
            val watermarkUri = PdfProcessor.addWatermark(
                context = context,
                uri = dummyUri,
                text = "CONFIDENTIAL",
                colorHex = "#E74C3C",
                fontSize = 40f,
                rotation = 45f,
                opacity = 0.3f,
                pageRange = ""
            )
            val watermarkFile = File(watermarkUri.path ?: "")
            assertTrue(watermarkFile.exists())

            com.tom_roush.pdfbox.pdmodel.PDDocument.load(watermarkFile).use { doc ->
                assertEquals(1, doc.numberOfPages)
            }
            watermarkFile.delete()
        } finally {
            dummyPdfFile.delete()
        }
    }

    @Test
    fun testAddPageNumbers() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_numbers_${System.currentTimeMillis()}.pdf")
        
        com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
            doc.save(dummyPdfFile)
        }

        val dummyUri = Uri.fromFile(dummyPdfFile)

        try {
            val numberedUri = PdfProcessor.addPageNumbers(
                context = context,
                uri = dummyUri,
                format = "detailed",
                position = "bottom_right",
                fontSize = 12f,
                pageRange = "1"
            )
            val numberedFile = File(numberedUri.path ?: "")
            assertTrue(numberedFile.exists())

            com.tom_roush.pdfbox.pdmodel.PDDocument.load(numberedFile).use { doc ->
                assertEquals(2, doc.numberOfPages)
            }
            numberedFile.delete()
        } finally {
            dummyPdfFile.delete()
        }
    }

    @Test
    fun testCropPdf() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_crop_${System.currentTimeMillis()}.pdf")
        
        com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
            val page = com.tom_roush.pdfbox.pdmodel.PDPage()
            doc.addPage(page)
            doc.save(dummyPdfFile)
        }

        val dummyUri = Uri.fromFile(dummyPdfFile)

        try {
            val croppedUri = PdfProcessor.cropPdf(
                context = context,
                uri = dummyUri,
                marginPercentage = 0.10f,
                pageRange = ""
            )
            val croppedFile = File(croppedUri.path ?: "")
            assertTrue(croppedFile.exists())

            com.tom_roush.pdfbox.pdmodel.PDDocument.load(croppedFile).use { doc ->
                assertEquals(1, doc.numberOfPages)
                val page = doc.getPage(0)
                val cropBox = page.cropBox
                
                // standard Letter mediaBox is 612x792
                // margin is 10%, so 61.2pt horiz and 79.2pt vert crop from each side
                // new width = 612 - 2 * 61.2 = 489.6
                // new height = 792 - 2 * 79.2 = 633.6
                assertEquals(489.6f, cropBox.width, 0.1f)
                assertEquals(633.6f, cropBox.height, 0.1f)
            }
            croppedFile.delete()
        } finally {
            dummyPdfFile.delete()
        }
    }

    @Test
    fun testOrganizePdf() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_organize_${System.currentTimeMillis()}.pdf")
        
        com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage()) // Page 0
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage()) // Page 1
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage()) // Page 2
            doc.save(dummyPdfFile)
        }

        val dummyUri = Uri.fromFile(dummyPdfFile)

        try {
            val transforms = listOf(
                PdfProcessor.PageTransform(originalIndex = 0, rotation = 90),
                PdfProcessor.PageTransform(originalIndex = 2, rotation = 180),
                PdfProcessor.PageTransform(originalIndex = 0, rotation = 0)
            )

            val organizedUri = PdfProcessor.organizePdf(context, dummyUri, transforms)
            val organizedFile = File(organizedUri.path ?: "")
            assertTrue(organizedFile.exists())

            com.tom_roush.pdfbox.pdmodel.PDDocument.load(organizedFile).use { doc ->
                assertEquals(3, doc.numberOfPages)
                assertEquals(90, doc.getPage(0).rotation)
                assertEquals(180, doc.getPage(1).rotation)
                assertEquals(0, doc.getPage(2).rotation)
            }

            organizedFile.delete()
        } finally {
            dummyPdfFile.delete()
        }
    }

    @Test
    fun testRepairPdf() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_repair_${System.currentTimeMillis()}.pdf")
        
        com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
            doc.save(dummyPdfFile)
        }

        val dummyUri = Uri.fromFile(dummyPdfFile)

        try {
            val repairedUri = PdfProcessor.repairPdf(context, dummyUri)
            val repairedFile = File(repairedUri.path ?: "")
            assertTrue(repairedFile.exists())

            com.tom_roush.pdfbox.pdmodel.PDDocument.load(repairedFile).use { doc ->
                assertEquals(1, doc.numberOfPages)
            }

            repairedFile.delete()
        } finally {
            dummyPdfFile.delete()
        }
    }

    @Test
    fun testConvertToPdfA() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_pdfa_${System.currentTimeMillis()}.pdf")
        
        com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
            doc.save(dummyPdfFile)
        }

        val dummyUri = Uri.fromFile(dummyPdfFile)

        try {
            val pdfaUri = PdfProcessor.convertToPdfA(context, dummyUri, "pdfa_1b")
            val pdfaFile = File(pdfaUri.path ?: "")
            assertTrue(pdfaFile.exists())

            com.tom_roush.pdfbox.pdmodel.PDDocument.load(pdfaFile).use { doc ->
                assertEquals(1, doc.numberOfPages)
                val catalog = doc.documentCatalog
                
                // Assert that Output Intent is set
                val outputIntents = catalog.outputIntents
                assertEquals(1, outputIntents.size)
                assertEquals("sRGB IEC61966-2.1", outputIntents[0].outputConditionIdentifier)
                
                // Assert that metadata block is set and contains compliance info
                val metadata = catalog.metadata
                assertTrue(metadata != null)
                val xmpText = metadata.exportXMPMetadata().use { stream ->
                    stream.readBytes().toString(Charsets.UTF_8)
                }
                assertTrue(xmpText.contains("<pdfaid:part>1</pdfaid:part>"))
                assertTrue(xmpText.contains("<pdfaid:conformance>B</pdfaid:conformance>"))
            }

            pdfaFile.delete()
        } finally {
            dummyPdfFile.delete()
        }
    }

    @Test
    fun testSignPdf() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_sign_${System.currentTimeMillis()}.pdf")
        val dummySigFile = File(tempDir, "dummy_sig_${System.currentTimeMillis()}.png")
        
        try {
            com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
                doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
                doc.save(dummyPdfFile)
            }
            
            val signatureBitmap = Bitmap.createBitmap(100, 50, Bitmap.Config.ARGB_8888)
            dummySigFile.outputStream().use { outStream ->
                signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            }
            signatureBitmap.recycle()
            
            val dummyUri = Uri.fromFile(dummyPdfFile)
            val sigUri = Uri.fromFile(dummySigFile)
            
            val signedUri = PdfProcessor.signPdf(
                context = context,
                uri = dummyUri,
                signatureUri = sigUri,
                pageIndex = 0,
                x = 100f,
                y = 100f,
                width = 150f,
                height = 75f
            )
            
            val signedFile = File(signedUri.path ?: "")
            assertTrue(signedFile.exists())
            
            com.tom_roush.pdfbox.pdmodel.PDDocument.load(signedFile).use { doc ->
                assertEquals(1, doc.numberOfPages)
                val page = doc.getPage(0)
                assertTrue(page != null)
            }
            
            signedFile.delete()
        } finally {
            dummyPdfFile.delete()
            dummySigFile.delete()
        }
    }

    @Test
    fun testRedactPdf() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_redact_${System.currentTimeMillis()}.pdf")
        
        try {
            com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
                doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
                doc.save(dummyPdfFile)
            }
            
            val dummyUri = Uri.fromFile(dummyPdfFile)
            val redactedUri = PdfProcessor.redactPdf(
                context = context,
                uri = dummyUri,
                pageIndex = 0,
                x = 50f,
                y = 50f,
                width = 200f,
                height = 100f,
                textToRedact = "CONFIDENTIAL"
            )
            
            val redactedFile = File(redactedUri.path ?: "")
            assertTrue(redactedFile.exists())
            
            com.tom_roush.pdfbox.pdmodel.PDDocument.load(redactedFile).use { doc ->
                assertEquals(1, doc.numberOfPages)
                val page = doc.getPage(0)
                assertTrue(page != null)
            }
            
            redactedFile.delete()
        } finally {
            dummyPdfFile.delete()
        }
    }

    @Test
    fun testGetFormFieldsAndFillPdfFields() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_forms_${System.currentTimeMillis()}.pdf")
        
        try {
            com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
                val page = com.tom_roush.pdfbox.pdmodel.PDPage()
                doc.addPage(page)
                
                val acroForm = com.tom_roush.pdfbox.pdmodel.interactive.form.PDAcroForm(doc)
                doc.documentCatalog.acroForm = acroForm
                
                val textField = com.tom_roush.pdfbox.pdmodel.interactive.form.PDTextField(acroForm)
                textField.partialName = "TestTextField"
                acroForm.fields.add(textField)
                
                val checkBox = com.tom_roush.pdfbox.pdmodel.interactive.form.PDCheckBox(acroForm)
                checkBox.partialName = "TestCheckBox"
                acroForm.fields.add(checkBox)
                
                val choiceField = com.tom_roush.pdfbox.pdmodel.interactive.form.PDComboBox(acroForm)
                choiceField.partialName = "TestChoiceField"
                choiceField.setOptions(listOf("Option 1", "Option 2"))
                acroForm.fields.add(choiceField)
                
                doc.save(dummyPdfFile)
            }
            
            val dummyUri = Uri.fromFile(dummyPdfFile)
            
            val fields = PdfProcessor.getFormFields(context, dummyUri)
            assertEquals(3, fields.size)
            
            val textSpec = fields.first { it.name == "TestTextField" }
            assertEquals("text", textSpec.type)
            
            val checkSpec = fields.first { it.name == "TestCheckBox" }
            assertEquals("checkbox", checkSpec.type)
            
            val choiceSpec = fields.first { it.name == "TestChoiceField" }
            assertEquals("choice", choiceSpec.type)
            assertEquals(listOf("Option 1", "Option 2"), choiceSpec.options)
            
            val valuesToFill = mapOf(
                "TestTextField" to "Hello World",
                "TestCheckBox" to "true",
                "TestChoiceField" to "Option 2"
            )
            
            val filledUri = PdfProcessor.fillPdfFields(context, dummyUri, valuesToFill)
            val filledFile = File(filledUri.path ?: "")
            assertTrue(filledFile.exists())
            
            com.tom_roush.pdfbox.pdmodel.PDDocument.load(filledFile).use { doc ->
                val acroForm = doc.documentCatalog.acroForm
                assertTrue(acroForm != null)
                val tf = acroForm!!.getField("TestTextField") as com.tom_roush.pdfbox.pdmodel.interactive.form.PDTextField
                assertEquals("Hello World", tf.value)
                
                val cb = acroForm.getField("TestCheckBox") as com.tom_roush.pdfbox.pdmodel.interactive.form.PDCheckBox
                assertTrue(cb.isChecked)
                
                val choice = acroForm.getField("TestChoiceField") as com.tom_roush.pdfbox.pdmodel.interactive.form.PDChoice
                assertEquals("Option 2", choice.value[0])
            }
            
            filledFile.delete()
        } finally {
            dummyPdfFile.delete()
        }
    }
}
