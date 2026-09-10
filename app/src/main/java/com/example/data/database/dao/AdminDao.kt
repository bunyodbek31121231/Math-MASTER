package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.database.entity.admin.ImportHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminDao {
    @Query("SELECT * FROM import_history ORDER BY createdAt DESC")
    fun getAllImportHistory(): Flow<List<ImportHistoryEntity>>

    @Query("SELECT * FROM import_history ORDER BY createdAt DESC")
    suspend fun getAllImportHistoryOnce(): List<ImportHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImportHistory(history: ImportHistoryEntity)

    @Query("SELECT * FROM import_history ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastImport(): ImportHistoryEntity?

    @Query("DELETE FROM import_history WHERE id = :id")
    suspend fun deleteImportHistory(id: String)

    @Query("SELECT COUNT(*) FROM import_history")
    suspend fun getImportCount(): Int
}
