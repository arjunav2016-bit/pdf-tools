package com.example.pdftools.data

import android.content.Context
import com.google.android.gms.common.moduleinstall.InstallStatusListener
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate.InstallState
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

sealed interface OcrModuleStatus {
    object Ready : OcrModuleStatus
    object NotDownloaded : OcrModuleStatus
    data class Downloading(val progress: Float) : OcrModuleStatus
    data class Error(val message: String) : OcrModuleStatus
}

@Singleton
open class OcrModelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val moduleInstallClient = ModuleInstall.getClient(context)

    private val clients = mapOf(
        "chinese" to TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build()),
        "devanagari" to TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build()),
        "japanese" to TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build()),
        "korean" to TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
    )

    private val _statuses = MutableStateFlow<Map<String, OcrModuleStatus>>(
        mapOf(
            "latin" to OcrModuleStatus.Ready,
            "chinese" to OcrModuleStatus.NotDownloaded,
            "devanagari" to OcrModuleStatus.NotDownloaded,
            "japanese" to OcrModuleStatus.NotDownloaded,
            "korean" to OcrModuleStatus.NotDownloaded
        )
    )
    val statuses: StateFlow<Map<String, OcrModuleStatus>> = _statuses.asStateFlow()

    init {
        checkAllStatuses()
    }

    open fun checkAllStatuses() {
        scope.launch {
            val updated = _statuses.value.toMutableMap()
            clients.forEach { (lang, client) ->
                try {
                    moduleInstallClient.areModulesAvailable(client)
                        .addOnSuccessListener { response ->
                            if (response.areModulesAvailable()) {
                                updated[lang] = OcrModuleStatus.Ready
                            } else {
                                updated[lang] = OcrModuleStatus.NotDownloaded
                            }
                            _statuses.value = updated.toMap()
                        }
                        .addOnFailureListener { e ->
                            updated[lang] = OcrModuleStatus.Error(e.message ?: "Failed to check status")
                            _statuses.value = updated.toMap()
                        }
                } catch (e: Exception) {
                    updated[lang] = OcrModuleStatus.Error(e.message ?: "Error checking status")
                    _statuses.value = updated.toMap()
                }
            }
        }
    }

    open fun downloadLanguage(languageCode: String) {
        val client = clients[languageCode] ?: return

        scope.launch {
            var listener: InstallStatusListener? = null
            listener = InstallStatusListener { update ->
                val progressInfo = update.progressInfo
                val progress = if (progressInfo != null) {
                    if (progressInfo.totalBytesToDownload > 0) {
                        progressInfo.bytesDownloaded.toFloat() / progressInfo.totalBytesToDownload
                    } else 0f
                } else 0f

                val updated = _statuses.value.toMutableMap()
                when (update.installState) {
                    InstallState.STATE_DOWNLOADING -> {
                        updated[languageCode] = OcrModuleStatus.Downloading(progress)
                    }
                    InstallState.STATE_INSTALLING, InstallState.STATE_COMPLETED -> {
                        updated[languageCode] = OcrModuleStatus.Ready
                        listener?.let { moduleInstallClient.unregisterListener(it) }
                    }
                    InstallState.STATE_FAILED -> {
                        updated[languageCode] = OcrModuleStatus.Error("Installation failed")
                        listener?.let { moduleInstallClient.unregisterListener(it) }
                    }
                    InstallState.STATE_CANCELED -> {
                        updated[languageCode] = OcrModuleStatus.NotDownloaded
                        listener?.let { moduleInstallClient.unregisterListener(it) }
                    }
                }
                _statuses.value = updated.toMap()
            }

            val request = ModuleInstallRequest.newBuilder()
                .addApi(client)
                .setListener(listener)
                .build()

            val updated = _statuses.value.toMutableMap()
            updated[languageCode] = OcrModuleStatus.Downloading(0f)
            _statuses.value = updated.toMap()

            moduleInstallClient.installModules(request)
                .addOnSuccessListener { response ->
                    if (response.areModulesAlreadyInstalled()) {
                        val finalMap = _statuses.value.toMutableMap()
                        finalMap[languageCode] = OcrModuleStatus.Ready
                        _statuses.value = finalMap.toMap()
                    }
                }
                .addOnFailureListener { e ->
                    val finalMap = _statuses.value.toMutableMap()
                    finalMap[languageCode] = OcrModuleStatus.Error(e.message ?: "Launch failed")
                    _statuses.value = finalMap.toMap()
                    listener?.let { moduleInstallClient.unregisterListener(it) }
                }
        }
    }
}
