package com.example.pdftools.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_files")
data class RecentFileEntity(
    @PrimaryKey val id: String,
    val fileName: String,
    val toolId: String,
    val filePath: String,
    val timestamp: Long
)
