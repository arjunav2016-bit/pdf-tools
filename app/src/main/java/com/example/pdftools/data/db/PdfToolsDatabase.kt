package com.example.pdftools.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteEntity::class, RecentFileEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PdfToolsDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentFileDao(): RecentFileDao
}
