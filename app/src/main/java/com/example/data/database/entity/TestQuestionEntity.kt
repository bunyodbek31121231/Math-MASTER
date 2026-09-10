package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "test_questions",
    indices = [
        Index(value = ["testId"]),
        Index(value = ["problemId"])
    ]
)
data class TestQuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val testId: String,
    val problemId: String,
    val questionOrder: Int,
    val selectedAnswer: String?,
    val correctAnswer: String,
    val isCorrect: Boolean
)
