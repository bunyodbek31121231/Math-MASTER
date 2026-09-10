package com.example.data.database.entity.maxsus

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "maxsus_problems",
    foreignKeys = [
        ForeignKey(
            entity = MaxsusTopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["topicId"]),
        Index(value = ["questionHash"], unique = true)
    ]
)
data class MaxsusProblemEntity(
    @PrimaryKey
    val id: String,
    val topicId: String,
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String, // A, B, C, or D
    val solution: String? = null,
    val explanation: String? = null,
    val formula: String? = null,
    val questionHash: String,
    val importId: String? = null,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
