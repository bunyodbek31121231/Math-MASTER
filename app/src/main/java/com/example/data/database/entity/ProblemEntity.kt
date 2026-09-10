package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "problems",
    indices = [
        Index(value = ["topicId"]),
        Index(value = ["category"]),
        Index(value = ["difficulty"]),
        Index(value = ["questionHash"], unique = true)
    ]
)
data class ProblemEntity(
    @PrimaryKey
    val id: String,
    val category: String,
    val topicId: String,
    val difficulty: String, // EASY, MEDIUM, HARD, EXPERT
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String, // "A", "B", "C", or "D"
    val solution: String,
    val explanation: String,
    val formula: String? = null,
    val tags: String = "",
    val generator: String? = null,
    val questionHash: String
)
