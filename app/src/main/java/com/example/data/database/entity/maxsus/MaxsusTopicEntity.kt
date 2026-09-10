package com.example.data.database.entity.maxsus

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "maxsus_topics",
    foreignKeys = [
        ForeignKey(
            entity = MaxsusSectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sectionId"])]
)
data class MaxsusTopicEntity(
    @PrimaryKey
    val id: String,
    val sectionId: String,
    val title: String,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
