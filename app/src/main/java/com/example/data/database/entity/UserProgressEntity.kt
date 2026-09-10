package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_progress",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["topicId"]),
        Index(value = ["userId", "topicId"], unique = true)
    ]
)
data class UserProgressEntity(
    @PrimaryKey
    val id: String, // format: "${userId}_${topicId}"
    val userId: String,
    val topicId: String,
    val category: String,
    val problemsSolved: Int = 0,
    val correctCount: Int = 0,
    val masteryPercentage: Double = 0.0,
    val lastPracticedAt: Long = System.currentTimeMillis()
)
