package com.example.pdftools.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONArray
import org.json.JSONObject

data class RecentFile(
    val id: String,
    val fileName: String,
    val toolId: String,
    val filePath: String, // URI string
    val timestamp: Long
)

@Singleton
class RecentFilesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "pdf_tools_recents"
        private const val KEY_RECENTS = "recents"
        private const val MAX_ITEMS = 30
    }
    
    private val recents = mutableStateListOf<RecentFile>()

    init {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_RECENTS, null)
        recents.clear()
        if (jsonStr != null) {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<RecentFile>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        RecentFile(
                            id = obj.getString("id"),
                            fileName = obj.getString("fileName"),
                            toolId = obj.getString("toolId"),
                            filePath = obj.getString("filePath"),
                            timestamp = obj.getLong("timestamp")
                        )
                    )
                }
                recents.addAll(list)
            } catch (e: Exception) {
                // Ignore decoding errors
            }
        }
    }

    fun getRecents(): SnapshotStateList<RecentFile> = recents

    fun addRecent(fileName: String, toolId: String, filePath: String) {
        // Remove existing item with same path if any
        recents.removeAll { it.filePath == filePath }
        
        val newRecent = RecentFile(
            id = System.currentTimeMillis().toString(),
            fileName = fileName,
            toolId = toolId,
            filePath = filePath,
            timestamp = System.currentTimeMillis()
        )
        // Add at the beginning of the list
        recents.add(0, newRecent)
        
        // Trim if exceeds max items
        if (recents.size > MAX_ITEMS) {
            recents.removeRange(MAX_ITEMS, recents.size)
        }
        
        save()
    }

    fun clear() {
        recents.clear()
        save()
    }

    private fun save() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        try {
            val array = JSONArray()
            for (item in recents) {
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("fileName", item.fileName)
                obj.put("toolId", item.toolId)
                obj.put("filePath", item.filePath)
                obj.put("timestamp", item.timestamp)
                array.put(obj)
            }
            prefs.edit().putString(KEY_RECENTS, array.toString()).apply()
        } catch (e: Exception) {
            // Ignore saving errors
        }
    }
}
