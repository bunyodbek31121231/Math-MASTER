package com.example.data.database.entity.maxsus

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class MaxsusContentType {
    THEORY, FORMULA, EXAMPLE, EXERCISE
}

@Serializable
@Entity(
    tableName = "maxsus_contents",
    foreignKeys = [
        ForeignKey(
            entity = MaxsusTopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["topicId"])]
)
data class MaxsusContentEntity(
    @PrimaryKey
    val id: String,
    val topicId: String,
    val type: MaxsusContentType,
    val title: String? = null,
    val body: String,
    val formula: String? = null,
    val solution: String? = null,
    val explanation: String? = null,
    val orderIndex: Int = 0,
    val importId: String? = null, // Link to import history
    val createdAt: Long = System.currentTimeMillis()
)
