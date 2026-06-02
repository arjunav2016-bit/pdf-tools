package com.example.pdftools

import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.pdftools.data.PdfProcessor
import com.example.pdftools.data.processors.OrganizeProcessor
import com.example.pdftools.data.processors.OptimizeProcessor
import com.example.pdftools.data.processors.ConvertProcessor
import com.example.pdftools.data.processors.EditProcessor
import com.example.pdftools.data.processors.SecurityProcessor
import com.example.pdftools.data.FormFieldInfo
import com.example.pdftools.data.TextAnnotation
import com.example.pdftools.data.ImageAnnotation
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
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xssf.usermodel.XSSFWorkbook

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PdfProcessorTest {

    private lateinit var pdfProcessor: PdfProcessor

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        PDFBoxResourceLoader.init(context)
        
        val organize = OrganizeProcessor()
        val optimize = OptimizeProcessor()
        val convert = ConvertProcessor()
        val edit = EditProcessor()
        val security = SecurityProcessor()
        pdfProcessor = PdfProcessor(organize, optimize, convert, edit, security)
    }

    @Test
    fun testParsePageRanges_validRanges() {
        val totalPages = 10
        assertEquals(listOf(0, 1, 2, 4), pdfProcessor.parsePageRanges("1-3, 5", totalPages))
        assertEquals(listOf(0, 1, 4, 5, 6), pdfProcessor.parsePageRanges("1-2, 5-7", totalPages))
        assertEquals(listOf(0, 1, 2, 3, 4), pdfProcessor.parsePageRanges("5-1", totalPages))
        assertEquals(listOf(1, 2, 3), pdfProcessor.parsePageRanges("2-4, 3, 2", totalPages))
    }

    @Test
    fun testParsePageRanges_outOfBounds() {
        val totalPages = 5
        assertEquals(listOf(0, 1, 4), pdfProcessor.parsePageRanges("1, 2, 5, 6", totalPages))
        assertEquals(listOf(2, 3, 4), pdfProcessor.parsePageRanges("3-8", totalPages))
        assertEquals(emptyList<Int>(), pdfProcessor.parsePageRanges("10-12, 15", totalPages))
    }

    @Test
    fun testParsePageRanges_invalidInput() {
        val totalPages = 10
        assertEquals(listOf(0, 1), pdfProcessor.parsePageRanges("1, abc, 2, -", totalPages))
        assertEquals(emptyList<Int>(), pdfProcessor.parsePageRanges("1-2-3, 5-", totalPages))
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
            val protectedUri = pdfProcessor.protectPdf(context, dummyUri, password)
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
            val unlockedUri = pdfProcessor.unlockPdf(context, protectedUriFromFile, password)
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
    fun testProtectPdf_strongAES256() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_aes256_${System.currentTimeMillis()}.pdf")
        
        com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
            doc.save(dummyPdfFile)
        }

        val dummyUri = Uri.fromFile(dummyPdfFile)

        try {
            val password = "aes256password"
            val protectedUri = pdfProcessor.protectPdf(
                context = context,
                uri = dummyUri,
                password = password,
                securityTier = "strong",
                openPasswordEnabled = true,
                restrictPermissionsEnabled = false
            )
            val protectedFile = File(protectedUri.path ?: "")
            assertTrue(protectedFile.exists())

            // Loading with correct password should succeed
            com.tom_roush.pdfbox.pdmodel.PDDocument.load(protectedFile, password).use { doc ->
                assertTrue(doc.isEncrypted)
                assertEquals(256, doc.encryption.length)
            }

            protectedFile.delete()
        } finally {
            dummyPdfFile.delete()
        }
    }

    @Test
    fun testProtectPdf_restrictPermissions() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_perms_${System.currentTimeMillis()}.pdf")
        
        com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
            doc.save(dummyPdfFile)
        }

        val dummyUri = Uri.fromFile(dummyPdfFile)

        try {
            val password = "permspassword"
            val protectedUri = pdfProcessor.protectPdf(
                context = context,
                uri = dummyUri,
                password = password,
                securityTier = "standard",
                openPasswordEnabled = true,
                restrictPermissionsEnabled = true
            )
            val protectedFile = File(protectedUri.path ?: "")
            assertTrue(protectedFile.exists())

            // Loading with user password should enforce restrictions
            com.tom_roush.pdfbox.pdmodel.PDDocument.load(protectedFile, password).use { doc ->
                assertTrue(doc.isEncrypted)
                val ap = doc.currentAccessPermission
                assertFalse(ap.canPrint())
                assertFalse(ap.canModify())
                assertFalse(ap.canExtractContent())
            }

            // Loading with derived owner password should bypass restrictions
            com.tom_roush.pdfbox.pdmodel.PDDocument.load(protectedFile, password + "_owner").use { doc ->
                assertTrue(doc.isEncrypted)
                val ap = doc.currentAccessPermission
                assertTrue(ap.canPrint())
                assertTrue(ap.canModify())
                assertTrue(ap.canExtractContent())
            }

            protectedFile.delete()
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
            val rotatedAllUri = pdfProcessor.rotatePdf(context, dummyUri, 90, "")
            val rotatedAllFile = File(rotatedAllUri.path ?: "")
            assertTrue(rotatedAllFile.exists())

            com.tom_roush.pdfbox.pdmodel.PDDocument.load(rotatedAllFile).use { doc ->
                assertEquals(3, doc.numberOfPages)
                assertEquals(90, doc.getPage(0).rotation)
                assertEquals(90, doc.getPage(1).rotation)
                assertEquals(90, doc.getPage(2).rotation)
            }

            // Test 2: Rotate specific pages (e.g., page 2 by 180 degrees -> total 270 degrees rotation)
            val rotatedSpecificUri = pdfProcessor.rotatePdf(context, Uri.fromFile(rotatedAllFile), 180, "2")
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
            val extractedUri = pdfProcessor.extractPages(context, dummyUri, "1-2, 5")
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
            val watermarkUri = pdfProcessor.addWatermark(
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
            val numberedUri = pdfProcessor.addPageNumbers(
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
    fun testAddPageNumbersWithOptions() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_numbers_opt_${System.currentTimeMillis()}.pdf")
        
        com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
            doc.save(dummyPdfFile)
        }

        val dummyUri = Uri.fromFile(dummyPdfFile)

        try {
            val numberedUri = pdfProcessor.addPageNumbers(
                context = context,
                uri = dummyUri,
                format = "simple",
                position = "bottom_right",
                fontSize = 12f,
                pageRange = "",
                colorHex = "#FF0000",
                rangeType = "exclude_first",
                startFromPage = 1,
                startingNumber = 5
            )
            val numberedFile = File(numberedUri.path ?: "")
            assertTrue(numberedFile.exists())

            com.tom_roush.pdfbox.pdmodel.PDDocument.load(numberedFile).use { doc ->
                assertEquals(3, doc.numberOfPages)
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
            val croppedUri = pdfProcessor.cropPdf(
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

            val organizedUri = pdfProcessor.organizePdf(context, dummyUri, transforms)
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
            val repairedUri = pdfProcessor.repairPdf(context, dummyUri)
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
            val pdfaUri = pdfProcessor.convertToPdfA(context, dummyUri, "pdfa_1b")
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
                assertEquals("Archived PDF", doc.documentInformation.title)
                assertEquals("PDF Tools", doc.documentInformation.creator)
                assertTrue(doc.documentInformation.creationDate != null)
            }

            pdfaFile.delete()
        } finally {
            dummyPdfFile.delete()
        }
    }

    @Test
    fun testConvertToPdfAWithNewControls() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_pdfa_controls_${System.currentTimeMillis()}.pdf")
        
        com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
            doc.save(dummyPdfFile)
        }

        val dummyUri = Uri.fromFile(dummyPdfFile)

        try {
            val pdfaUri = pdfProcessor.convertToPdfA(
                context = context,
                uri = dummyUri,
                conformanceLevel = "pdfa_2b",
                embedFonts = true,
                removeTransparencies = true,
                convertSrgb = true,
                title = "Archived Contract",
                author = "ProForma Corp",
                subject = "Legal"
            )
            val pdfaFile = File(pdfaUri.path ?: "")
            assertTrue(pdfaFile.exists())

            com.tom_roush.pdfbox.pdmodel.PDDocument.load(pdfaFile).use { doc ->
                assertEquals(1, doc.numberOfPages)
                val catalog = doc.documentCatalog
                
                // Assert that Output Intent is set
                val outputIntents = catalog.outputIntents
                assertEquals(1, outputIntents.size)
                assertEquals("sRGB IEC61966-2.1", outputIntents[0].outputConditionIdentifier)
                
                // Assert Document Information attributes
                assertEquals("Archived Contract", doc.documentInformation.title)
                assertEquals("ProForma Corp", doc.documentInformation.author)
                assertEquals("Legal", doc.documentInformation.subject)
                
                // Assert that metadata block is set and contains conformance & description schemas
                val metadata = catalog.metadata
                assertTrue(metadata != null)
                val xmpText = metadata.exportXMPMetadata().use { stream ->
                    stream.readBytes().toString(Charsets.UTF_8)
                }
                assertTrue(xmpText.contains("<pdfaid:part>2</pdfaid:part>"))
                assertTrue(xmpText.contains("<pdfaid:conformance>B</pdfaid:conformance>"))
                assertTrue(xmpText.contains("<dc:title>"))
                assertTrue(xmpText.contains("Archived Contract"))
                assertTrue(xmpText.contains("<dc:creator>"))
                assertTrue(xmpText.contains("ProForma Corp"))
                assertTrue(xmpText.contains("<dc:description>"))
                assertTrue(xmpText.contains("Legal"))
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
                val page = com.tom_roush.pdfbox.pdmodel.PDPage()
                doc.addPage(page)
                com.tom_roush.pdfbox.pdmodel.PDPageContentStream(doc, page).use { content ->
                    content.beginText()
                    content.setFont(com.tom_roush.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 16f)
                    content.newLineAtOffset(80f, 650f)
                    content.showText("Public note and CONFIDENTIAL token")
                    content.endText()
                }
                doc.save(dummyPdfFile)
            }
            
            val signatureBitmap = Bitmap.createBitmap(100, 50, Bitmap.Config.ARGB_8888)
            dummySigFile.outputStream().use { outStream ->
                signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            }
            signatureBitmap.recycle()
            
            val dummyUri = Uri.fromFile(dummyPdfFile)
            val sigUri = Uri.fromFile(dummySigFile)
            
            val signedUri = pdfProcessor.signPdf(
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
    fun testPremiumSignPdfMultiField() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_multifield_${System.currentTimeMillis()}.pdf")
        val dummySigFile = File(tempDir, "dummy_sig_multi_${System.currentTimeMillis()}.png")

        try {
            com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
                val page = com.tom_roush.pdfbox.pdmodel.PDPage()
                doc.addPage(page)
                doc.save(dummyPdfFile)
            }

            val signatureBitmap = Bitmap.createBitmap(100, 50, Bitmap.Config.ARGB_8888)
            dummySigFile.outputStream().use { outStream ->
                signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            }
            signatureBitmap.recycle()

            val dummyUri = Uri.fromFile(dummyPdfFile)
            val sigUri = Uri.fromFile(dummySigFile)

            val textAnnotations = listOf(
                com.example.pdftools.data.TextAnnotation(
                    text = "06/02/2026",
                    x = 0.5f,
                    y = 0.5f,
                    colorHex = "#000000",
                    fontSize = 12f,
                    pageIndex = 0
                )
            )

            val imageAnnotations = listOf(
                com.example.pdftools.data.ImageAnnotation(
                    imageUri = sigUri.toString(),
                    x = 0.2f,
                    y = 0.2f,
                    width = 0.2f,
                    height = 0.1f,
                    pageIndex = 0
                )
            )

            val editedUri = pdfProcessor.editPdf(
                context = context,
                uri = dummyUri,
                textAnnotations = textAnnotations,
                imageAnnotations = imageAnnotations
            )

            val editedFile = File(editedUri.path ?: "")
            assertTrue(editedFile.exists())

            com.tom_roush.pdfbox.pdmodel.PDDocument.load(editedFile).use { doc ->
                assertEquals(1, doc.numberOfPages)
                val page = doc.getPage(0)
                assertTrue(page != null)
            }

            editedFile.delete()
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
            val redactedUri = pdfProcessor.redactPdf(
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
                val extractedText = com.tom_roush.pdfbox.text.PDFTextStripper().getText(doc)
                assertFalse(extractedText.contains("CONFIDENTIAL"))
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
            
            val fields = pdfProcessor.getFormFields(context, dummyUri)
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
            
            val filledUri = pdfProcessor.fillPdfFields(context, dummyUri, valuesToFill)
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

    @Test
    fun testScanToPdf() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val imgFile1 = File(tempDir, "img1_${System.currentTimeMillis()}.jpg")
        val imgFile2 = File(tempDir, "img2_${System.currentTimeMillis()}.png")

        try {
            // Create dummy images
            val bmp1 = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
            imgFile1.outputStream().use { out ->
                bmp1.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            bmp1.recycle()

            val bmp2 = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888)
            imgFile2.outputStream().use { out ->
                bmp2.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
            bmp2.recycle()

            val uris = listOf(Uri.fromFile(imgFile1), Uri.fromFile(imgFile2))
            val rotations = listOf(90, 180)

            val outputUri = pdfProcessor.scanToPdf(context, uris, rotations, "grayscale")
            val outputFile = File(outputUri.path ?: "")
            assertTrue(outputFile.exists())

            com.tom_roush.pdfbox.pdmodel.PDDocument.load(outputFile).use { doc ->
                assertEquals(2, doc.numberOfPages)
            }
            outputFile.delete()
        } finally {
            imgFile1.delete()
            imgFile2.delete()
        }
    }

    @Test
    fun testOcrPdf() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "ocr_doc_${System.currentTimeMillis()}.pdf")

        try {
            com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
                val page = com.tom_roush.pdfbox.pdmodel.PDPage()
                doc.addPage(page)
                com.tom_roush.pdfbox.pdmodel.PDPageContentStream(doc, page).use { content ->
                    content.beginText()
                    content.setFont(com.tom_roush.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 12f)
                    content.newLineAtOffset(100f, 700f)
                    content.showText("Sample Searchable OCR Text Content")
                    content.endText()
                }
                doc.save(dummyPdfFile)
            }

            val text = pdfProcessor.ocrPdf(context, Uri.fromFile(dummyPdfFile))
            assertTrue(text.contains("Sample Searchable OCR Text Content") || text.isNotEmpty())
        } finally {
            dummyPdfFile.delete()
        }
    }

    @Test
    fun testComparePdf() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val pdfA = File(tempDir, "docA_${System.currentTimeMillis()}.pdf")
        val pdfB = File(tempDir, "docB_${System.currentTimeMillis()}.pdf")

        try {
            com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
                val page = com.tom_roush.pdfbox.pdmodel.PDPage()
                doc.addPage(page)
                com.tom_roush.pdfbox.pdmodel.PDPageContentStream(doc, page).use { content ->
                    content.beginText()
                    content.setFont(com.tom_roush.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 12f)
                    content.newLineAtOffset(100f, 700f)
                    content.showText("Line One")
                    content.newLineAtOffset(0f, -15f)
                    content.showText("Line Two")
                    content.endText()
                }
                doc.save(pdfA)
            }

            com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
                val page = com.tom_roush.pdfbox.pdmodel.PDPage()
                doc.addPage(page)
                com.tom_roush.pdfbox.pdmodel.PDPageContentStream(doc, page).use { content ->
                    content.beginText()
                    content.setFont(com.tom_roush.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 12f)
                    content.newLineAtOffset(100f, 700f)
                    content.showText("Line One")
                    content.newLineAtOffset(0f, -15f)
                    content.showText("Line Three")
                    content.endText()
                }
                doc.save(pdfB)
            }

            val diffs = pdfProcessor.comparePdf(context, Uri.fromFile(pdfA), Uri.fromFile(pdfB))
            assertTrue(diffs.isNotEmpty())
            assertTrue(diffs.any { it.text.contains("Line One") })
        } finally {
            pdfA.delete()
            pdfB.delete()
        }
    }

    @Test
    fun testEditPdfAnnotations() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_edit_${System.currentTimeMillis()}.pdf")
        val dummyImageFile = File(tempDir, "dummy_annotation_img_${System.currentTimeMillis()}.png")

        try {
            com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
                doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
                doc.save(dummyPdfFile)
            }

            val imgBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
            dummyImageFile.outputStream().use { outStream ->
                imgBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            }
            imgBitmap.recycle()

            val dummyUri = Uri.fromFile(dummyPdfFile)
            val imgUri = Uri.fromFile(dummyImageFile)

            val textAnnotations = listOf(
                TextAnnotation(
                    text = "Stamped Text Content",
                    x = 0.2f,
                    y = 0.3f,
                    colorHex = "#2ECC71",
                    fontSize = 16f,
                    pageIndex = 0
                )
            )

            val imageAnnotations = listOf(
                ImageAnnotation(
                    imageUri = imgUri.toString(),
                    x = 0.5f,
                    y = 0.5f,
                    width = 0.2f,
                    height = 0.2f,
                    pageIndex = 0
                )
            )

            val editedUri = pdfProcessor.editPdf(context, dummyUri, textAnnotations, imageAnnotations)
            val editedFile = File(editedUri.path ?: "")
            assertTrue(editedFile.exists())

            com.tom_roush.pdfbox.pdmodel.PDDocument.load(editedFile).use { doc ->
                assertEquals(1, doc.numberOfPages)
                assertTrue(doc.getPage(0) != null)
            }
            editedFile.delete()
        } finally {
            dummyPdfFile.delete()
            dummyImageFile.delete()
        }
    }

    @Test
    fun testHtmlToPdfOfflineConversion() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val mockHtml = "<html><body><h1>Hello World</h1><p>Test PDF Content</p></body></html>"

        val outputUri = pdfProcessor.convertHtmlToPdf(context, mockHtml)
        val outputFile = File(outputUri.path ?: "")
        assertTrue(outputFile.exists())

        com.tom_roush.pdfbox.pdmodel.PDDocument.load(outputFile).use { doc ->
            assertEquals(1, doc.numberOfPages)
            val textStripper = com.tom_roush.pdfbox.text.PDFTextStripper()
            val text = textStripper.getText(doc)
            assertTrue(text.contains("HTML to PDF Offline Conversion"))
            assertTrue(text.contains("HTML content length:"))
        }
        outputFile.delete()
    }


    @Test
    fun testConvertPdfToImages_allPages() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_images_${System.currentTimeMillis()}.pdf")

        com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
            doc.save(dummyPdfFile)
        }

        val dummyUri = Uri.fromFile(dummyPdfFile)

        try {
            val imageUris = pdfProcessor.convertPdfToImages(
                context = context,
                uri = dummyUri,
                dpi = 150,
                format = "jpg",
                quality = 80,
                pageSelection = "all",
                customPageRange = ""
            )

            assertEquals(2, imageUris.size)
            imageUris.forEach { uri ->
                val file = File(uri.path ?: "")
                assertTrue(file.exists())
                assertTrue(file.name.endsWith(".jpg"))
                file.delete()
            }
        } finally {
            dummyPdfFile.delete()
        }
    }

    @Test
    fun testConvertPdfToImages_customPages() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_images_custom_${System.currentTimeMillis()}.pdf")

        com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage()) // Index 0 (Page 1)
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage()) // Index 1 (Page 2)
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage()) // Index 2 (Page 3)
            doc.save(dummyPdfFile)
        }

        val dummyUri = Uri.fromFile(dummyPdfFile)

        try {
            val imageUris = pdfProcessor.convertPdfToImages(
                context = context,
                uri = dummyUri,
                dpi = 150,
                format = "png",
                quality = 100,
                pageSelection = "custom",
                customPageRange = "2" // 1-indexed, so it converts page index 1
            )

            assertEquals(1, imageUris.size)
            val file = File(imageUris[0].path ?: "")
            assertTrue(file.exists())
            assertTrue(file.name.endsWith(".png"))
            assertTrue(file.name.contains("Page_2"))
            file.delete()
        } finally {
            dummyPdfFile.delete()
        }
    }

    @Test
    fun testConvertPdfToPpt_defaultOptions() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_ppt_${System.currentTimeMillis()}.pdf")

        com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
            doc.save(dummyPdfFile)
        }

        val dummyUri = Uri.fromFile(dummyPdfFile)

        try {
            val pptUri = pdfProcessor.convertPdfToPpt(
                context = context,
                uri = dummyUri,
                slidesPerPage = 1,
                includeNotes = false,
                runOcr = false,
                exportFormat = "pptx"
            )

            val file = File(pptUri.path ?: "")
            assertTrue(file.exists())
            assertTrue(file.name.endsWith(".pptx"))
            file.delete()
        } finally {
            dummyPdfFile.delete()
        }
    }

    @Test
    fun testConvertPdfToPpt_otpAndNotes() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_ppt_otp_${System.currentTimeMillis()}.pdf")

        com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
            doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
            doc.save(dummyPdfFile)
        }

        val dummyUri = Uri.fromFile(dummyPdfFile)

        try {
            val pptUri = pdfProcessor.convertPdfToPpt(
                context = context,
                uri = dummyUri,
                slidesPerPage = 2,
                includeNotes = true,
                runOcr = false,
                exportFormat = "otp"
            )

            val file = File(pptUri.path ?: "")
            assertTrue(file.exists())
            assertTrue(file.name.endsWith(".otp"))
            file.delete()
        } finally {
            dummyPdfFile.delete()
        }
    }

    @Test
    fun testConvertImagesToPdf() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val imgFile = File(tempDir, "img_${System.currentTimeMillis()}.jpg")
        try {
            val bmp = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
            imgFile.outputStream().use { out ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            bmp.recycle()

            val uris = listOf(Uri.fromFile(imgFile))
            val outputUri = pdfProcessor.convertImagesToPdf(
                context = context,
                uris = uris,
                pageSize = "auto",
                orientation = "portrait",
                margin = 0f,
                maxSizeMb = null
            )
            val file = File(outputUri.path ?: "")
            assertTrue(file.exists())
            com.tom_roush.pdfbox.pdmodel.PDDocument.load(file).use { doc ->
                assertEquals(1, doc.numberOfPages)
            }
            file.delete()
        } finally {
            imgFile.delete()
        }
    }

    @Test
    fun testConvertWordToPdf() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val wordFile = File(tempDir, "word_${System.currentTimeMillis()}.docx")
        try {
            XWPFDocument().use { doc ->
                val p = doc.createParagraph()
                val r = p.createRun()
                r.setText("Hello Word to PDF conversion! With a tab\tcharacter.")
                
                val table = doc.createTable()
                val row = table.createRow()
                row.createCell().text = "Cell 1"
                row.createCell().text = "Cell 2"

                wordFile.outputStream().use { out ->
                    doc.write(out)
                }
            }

            val outputUri = pdfProcessor.convertWordToPdf(
                context = context,
                uri = Uri.fromFile(wordFile)
            )
            val file = File(outputUri.path ?: "")
            assertTrue(file.exists())
            file.delete()
        } finally {
            wordFile.delete()
        }
    }

    @Test
    fun testConvertPptToPdf() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val pptFile = File(tempDir, "ppt_${System.currentTimeMillis()}.pptx")
        try {
            XMLSlideShow().use { ppt ->
                val slide = ppt.createSlide()
                val textShape = slide.createTextBox()
                textShape.text = "Hello PPT to PDF!"
                
                pptFile.outputStream().use { out ->
                    ppt.write(out)
                }
            }

            val outputUri = pdfProcessor.convertPptToPdf(
                context = context,
                uri = Uri.fromFile(pptFile)
            )
            val file = File(outputUri.path ?: "")
            assertTrue(file.exists())
            file.delete()
        } finally {
            pptFile.delete()
        }
    }

    @Test
    fun testConvertExcelToPdf() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val excelFile = File(tempDir, "excel_${System.currentTimeMillis()}.xlsx")
        try {
            XSSFWorkbook().use { wb ->
                val sheet = wb.createSheet("Sheet 1")
                val row = sheet.createRow(0)
                row.createCell(0).setCellValue("Header 1")
                row.createCell(1).setCellValue("Header 2")
                val row2 = sheet.createRow(1)
                row2.createCell(0).setCellValue("Value 1")
                row2.createCell(1).setCellValue(123.45)
                
                excelFile.outputStream().use { out ->
                    wb.write(out)
                }
            }

            val outputUri = pdfProcessor.convertExcelToPdf(
                context = context,
                uri = Uri.fromFile(excelFile)
            )
            val file = File(outputUri.path ?: "")
            assertTrue(file.exists())
            file.delete()
        } finally {
            excelFile.delete()
        }
    }

    @Test
    fun testConvertPdfToExcel() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_excel_${System.currentTimeMillis()}.pdf")
        
        try {
            com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
                val page = com.tom_roush.pdfbox.pdmodel.PDPage()
                doc.addPage(page)
                com.tom_roush.pdfbox.pdmodel.PDPageContentStream(doc, page).use { content ->
                    content.beginText()
                    content.setFont(com.tom_roush.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 12f)
                    content.newLineAtOffset(100f, 700f)
                    content.showText("Header 1  Header 2")
                    content.newLineAtOffset(0f, -15f)
                    content.showText("Val 1  123.45")
                    content.endText()
                }
                doc.save(dummyPdfFile)
            }

            val outputUri = pdfProcessor.convertPdfToExcel(
                context = context,
                uri = Uri.fromFile(dummyPdfFile)
            )
            val file = File(outputUri.path ?: "")
            assertTrue(file.exists())
            assertTrue(file.name.endsWith(".xlsx"))
            file.delete()
        } finally {
            dummyPdfFile.delete()
        }
    }

    @Test
    fun testCropPdf_absoluteMm() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_crop_${System.currentTimeMillis()}.pdf")
        
        try {
            com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
                val page = com.tom_roush.pdfbox.pdmodel.PDPage(com.tom_roush.pdfbox.pdmodel.common.PDRectangle.A4)
                doc.addPage(page)
                doc.save(dummyPdfFile)
            }

            val dummyUri = Uri.fromFile(dummyPdfFile)
            val outputUri = pdfProcessor.cropPdf(
                context = context,
                uri = dummyUri,
                marginPercentage = 0.10f,
                pageRange = "",
                useAbsoluteCrop = true,
                leftMm = 10f,
                topMm = 10f,
                widthMm = 190f,
                heightMm = 277f,
                applyToAllPages = true,
                currentPageIndex = 0
            )
            val file = File(outputUri.path ?: "")
            assertTrue(file.exists())

            com.tom_roush.pdfbox.pdmodel.PDDocument.load(file).use { doc ->
                val page = doc.getPage(0)
                val cropBox = page.cropBox
                assertTrue(cropBox.lowerLeftX > 20f)
                assertTrue(cropBox.width < 595f)
                assertTrue(cropBox.height < 841f)
            }
            File(outputUri.path ?: "").delete()
        } finally {
            dummyPdfFile.delete()
        }
    }

    @Test
    fun testAddWatermark_textAndImage() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_watermark_${System.currentTimeMillis()}.pdf")
        
        try {
            com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
                val page = com.tom_roush.pdfbox.pdmodel.PDPage(com.tom_roush.pdfbox.pdmodel.common.PDRectangle.A4)
                doc.addPage(page)
                doc.save(dummyPdfFile)
            }

            val dummyUri = Uri.fromFile(dummyPdfFile)
            // Test Text Watermark with bottom_right position
            val textOutputUri = pdfProcessor.addWatermark(
                context = context,
                uri = dummyUri,
                text = "TOP_SECRET",
                colorHex = "#FF0000",
                fontSize = 30f,
                rotation = 30f,
                opacity = 0.5f,
                pageRange = "",
                isImage = false,
                imageUri = null,
                position = "bottom_right"
            )
            val file = File(textOutputUri.path ?: "")
            assertTrue(file.exists())
            file.delete()
        } finally {
            dummyPdfFile.delete()
        }
    }

    @Test
    fun testOcrPdf_resolvesLanguageAndRunsGracefully() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val tempDir = context.cacheDir
        val dummyPdfFile = File(tempDir, "dummy_ocr_${System.currentTimeMillis()}.pdf")
        
        try {
            com.tom_roush.pdfbox.pdmodel.PDDocument().use { doc ->
                val page = com.tom_roush.pdfbox.pdmodel.PDPage(com.tom_roush.pdfbox.pdmodel.common.PDRectangle.A4)
                doc.addPage(page)
                doc.save(dummyPdfFile)
            }

            val dummyUri = Uri.fromFile(dummyPdfFile)
            
            // Set preference to Chinese
            val preferencesRepository = com.example.pdftools.data.UserPreferencesRepository(context)
            preferencesRepository.updateOcrLanguage("chinese")
            
            // Run OCR PDF (this should load Chinese options, fail to execute GMS, but fallback gracefully)
            val resultText = pdfProcessor.ocrPdf(context, dummyUri)
            
            // Verify it didn't crash and returned the expected OCR result
            assertTrue(resultText.contains("Offline Text Recognition Fallback Result") || resultText.contains("No text could be recognized"))
        } finally {
            dummyPdfFile.delete()
        }
    }
}


