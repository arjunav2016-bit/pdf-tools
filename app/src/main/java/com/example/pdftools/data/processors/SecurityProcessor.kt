package com.example.pdftools.data.processors

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles PDF security operations: protect, unlock, redact.
 */
@Singleton
class SecurityProcessor @Inject constructor() {

    /**
     * Encrypts/Protects a PDF document using standard 128-bit security.
     */
    suspend fun protectPdf(context: Context, uri: Uri, password: String): Uri = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("protect_input_", ".pdf", context.cacheDir)
        val outputFile = File(context.cacheDir, "Protected_${System.currentTimeMillis()}.pdf")
        
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            PDDocument.load(tempInputFile).use { doc ->
                val ap = AccessPermission()
                // Set the StandardProtectionPolicy: user password, owner password (same), access permission
                val spp = StandardProtectionPolicy(password, password, ap)
                spp.encryptionKeyLength = 128
                doc.protect(spp)
                doc.save(outputFile)
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
     * Decrypts/Unlocks a password-protected PDF document.
     */
    suspend fun unlockPdf(context: Context, uri: Uri, password: String): Uri = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("unlock_input_", ".pdf", context.cacheDir)
        val outputFile = File(context.cacheDir, "Unlocked_${System.currentTimeMillis()}.pdf")
        
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            // Attempt to load the document with the password.
            // If the password is wrong, this will throw an InvalidPasswordException (IOException).
            PDDocument.load(tempInputFile, password).use { doc ->
                if (doc.isEncrypted) {
                    doc.setAllSecurityToBeRemoved(true)
                }
                doc.save(outputFile)
            }
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            outputFile.delete()
            // Throw a cleaner message on password error
            if (e.message?.contains("password", ignoreCase = true) == true || 
                e.javaClass.simpleName.contains("Password", ignoreCase = true)) {
                throw IllegalArgumentException("Invalid password. Please enter the correct password.")
            }
            throw e
        } finally {
            tempInputFile.delete()
        }
    }

    /**
     * Redacts a specific rectangular coordinate region by permanently drawing a solid black bounding box.
     */
    suspend fun redactPdf(
        context: Context,
        uri: Uri,
        pageIndex: Int,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        textToRedact: String?
    ): Uri = withContext(Dispatchers.IO) {
        val tempInputFile = File.createTempFile("redact_input_", ".pdf", context.cacheDir)
        val outputFile = File(context.cacheDir, "Redacted_${System.currentTimeMillis()}.pdf")
        
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempInputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            PDDocument.load(tempInputFile).use { doc ->
                val totalPages = doc.numberOfPages
                val targetPageIndex = if (pageIndex in 0 until totalPages) pageIndex else 0
                val page = doc.getPage(targetPageIndex)
                
                PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true).use { contentStream ->
                    contentStream.setNonStrokingColor(0f, 0f, 0f) // solid black
                    contentStream.addRect(x, y, width, height)
                    contentStream.fill()
                }
                
                doc.save(outputFile)
            }
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            outputFile.delete()
            throw e
        } finally {
            tempInputFile.delete()
        }
    }
}
