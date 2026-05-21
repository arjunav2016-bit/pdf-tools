package com.example.pdftools

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.pdftools.data.FavoritesRepository
import com.example.pdftools.data.RecentFilesRepository
import com.example.pdftools.theme.PDFToolsTheme
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    PDFBoxResourceLoader.init(applicationContext)
    FavoritesRepository.init(applicationContext)
    RecentFilesRepository.init(applicationContext)

    enableEdgeToEdge()
    setContent {
      PDFToolsTheme { Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainNavigation() } }
    }
  }
}
