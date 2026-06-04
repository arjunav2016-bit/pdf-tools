package com.example.pdftools.data

import android.content.Context
import com.example.pdftools.data.db.RecentFileDao
import com.example.pdftools.data.db.RecentFileEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray

data class RecentFile(
    val id: String,
    val fileName: String,
    val toolId: String,
    val filePath: String, // URI string
    val timestamp: Long
)

@Singleton
class RecentFilesRepository(
    private val context: Context,
    private val recentFileDao: RecentFileDao,
    private val coroutineScope: CoroutineScope
) {
    @Inject
    constructor(
        @ApplicationContext context: Context,
        recentFileDao: RecentFileDao
    ) : this(context, recentFileDao, CoroutineScope(SupervisorJob() + Dispatchers.IO))

    val recents: StateFlow<List<RecentFile>> = recentFileDao.getRecentsFlow()
        .map { list ->
            list.map {
                RecentFile(
                    id = it.id,
                    fileName = it.fileName,
                    toolId = it.toolId,
                    filePath = it.filePath,
                    timestamp = it.timestamp
                )
            }
        }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    init {
        // Silent migration from old SharedPreferences JSON
        coroutineScope.launch {
            val prefs = context.getSharedPreferences("pdf_tools_recents", Context.MODE_PRIVATE)
            val jsonStr = prefs.getString("recents", null)
            if (jsonStr != null) {
                try {
                    val array = JSONArray(jsonStr)
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        recentFileDao.insert(
                            RecentFileEntity(
                                id = obj.getString("id"),
                                fileName = obj.getString("fileName"),
                                toolId = obj.getString("toolId"),
                                filePath = obj.getString("filePath"),
                                timestamp = obj.getLong("timestamp")
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Ignore decoding errors
                }
                // Clear old preferences
                prefs.edit().clear().apply()
            }

            // Clean up any existing mock files that might have been seeded previously
            recentFileDao.deleteById("mock_doc_1")
            recentFileDao.deleteById("mock_doc_2")
            recentFileDao.deleteById("mock_doc_3")
        }
    }

    fun addRecent(fileName: String, toolId: String, filePath: String) {
        coroutineScope.launch {
            recentFileDao.deleteByPath(filePath)
            val newEntity = RecentFileEntity(
                id = System.currentTimeMillis().toString(),
                fileName = fileName,
                toolId = toolId,
                filePath = filePath,
                timestamp = System.currentTimeMillis()
            )
            recentFileDao.insert(newEntity)
            recentFileDao.trim(30)
        }
    }

    fun deleteRecent(id: String) {
        coroutineScope.launch {
            recentFileDao.deleteById(id)
        }
    }

    fun insertRecent(recent: RecentFile) {
        coroutineScope.launch {
            recentFileDao.insert(
                RecentFileEntity(
                    id = recent.id,
                    fileName = recent.fileName,
                    toolId = recent.toolId,
                    filePath = recent.filePath,
                    timestamp = recent.timestamp
                )
            )
        }
    }

    fun clear() {
        coroutineScope.launch {
            recentFileDao.clearAll()
        }
    }
}
