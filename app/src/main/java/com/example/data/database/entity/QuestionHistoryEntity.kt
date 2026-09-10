package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "question_history",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["problemId"]),
        Index(value = ["userId", "problemId"]),
        Index(value = ["userId", "questionHash"])
    ]
)
data class QuestionHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: String,
    val problemId: String,
    val questionHash: String,
    val selectedAnswer: String,
    val isCorrect: Boolean,
    val timeSpentSeconds: Int = 0,
    val mode: String = "PRACTICE", // PRACTICE, TEST, MAXSUS
    val timestamp: Long = System.currentTimeMillis()
)
