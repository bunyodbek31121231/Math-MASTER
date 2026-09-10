package com.example.data.database.entity.maxsus

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "maxsus_sections")
data class MaxsusSectionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
