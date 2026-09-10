package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tests",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["topicId"]),
        Index(value = ["completedAt"])
    ]
)
data class TestEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val category: String? = null,
    val topicId: String? = null,
    val difficulty: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val scorePercentage: Double,
    val xpEarned: Int,
    val durationSeconds: Int,
    val completedAt: Long = System.currentTimeMillis(),
    val isMaxsus: Boolean = false
)
