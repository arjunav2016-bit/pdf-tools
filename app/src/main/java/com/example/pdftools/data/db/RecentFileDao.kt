package com.example.pdftools.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentFileDao {
    @Query("SELECT * FROM recent_files ORDER BY timestamp DESC")
    fun getRecentsFlow(): Flow<List<RecentFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: RecentFileEntity)

    @Query("DELETE FROM recent_files WHERE filePath = :path")
    suspend fun deleteByPath(path: String)

    @Query("DELETE FROM recent_files WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM recent_files")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM recent_files")
    suspend fun getCount(): Int

    @Query("DELETE FROM recent_files WHERE id NOT IN (SELECT id FROM recent_files ORDER BY timestamp DESC LIMIT :limit)")
    suspend fun trim(limit: Int)
}
