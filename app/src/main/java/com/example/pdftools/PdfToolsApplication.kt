package com.example.pdftools

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dagger.hilt.android.HiltAndroidApp
import java.io.File

@HiltAndroidApp
class PdfToolsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(this)
        deleteExpiredCacheFiles()
    }

    private fun deleteExpiredCacheFiles() {
        val oldestAllowedTimestamp = System.currentTimeMillis() - CACHE_RETENTION_MILLIS
        cacheDir.walkBottomUp()
            .filter { file -> file != cacheDir && file.lastModified() < oldestAllowedTimestamp }
            .forEach(File::delete)
    }

    private companion object {
        const val CACHE_RETENTION_MILLIS = 7L * 24L * 60L * 60L * 1000L
    }
}
