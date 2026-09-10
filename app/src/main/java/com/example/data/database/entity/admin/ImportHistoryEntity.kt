package com.example.data.database.entity.admin

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "import_history")
data class ImportHistoryEntity(
    @PrimaryKey
    val id: String,
    val fileName: String,
    val fileType: String, // PDF, TXT, JSON
    val totalFound: Int,
    val selectedCount: Int,
    val importedCount: Int,
    val errorCount: Int,
    val createdAt: Long = System.currentTimeMillis()
)
