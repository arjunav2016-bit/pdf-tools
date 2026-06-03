package com.example.pdftools

import android.app.Application
import com.example.pdftools.data.PdfPreviewRepository
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
class PdfToolsApplication : Application() {

    @Inject
    lateinit var pdfPreviewRepository: PdfPreviewRepository

    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(this)
        deleteExpiredCacheFiles()
    }

    private fun deleteExpiredCacheFiles() {
        val oldestAllowedTimestamp = System.currentTimeMillis() - CACHE_RETENTION_MILLIS
        var deletedAny = false
        cacheDir.walkBottomUp()
            .filter { file -> file != cacheDir && file.lastModified() < oldestAllowedTimestamp }
            .forEach { file ->
                if (file.delete()) {
                    deletedAny = true
                }
            }
        if (deletedAny) {
            pdfPreviewRepository.evictAll()
        }
    }

    private companion object {
        const val CACHE_RETENTION_MILLIS = 7L * 24L * 60L * 60L * 1000L
    }
}
