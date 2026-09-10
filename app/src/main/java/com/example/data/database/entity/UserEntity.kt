package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val username: String,
    val passwordHash: String,
    val passwordSalt: String,
    val currentLevel: Int = 1,
    val xp: Long = 0L,
    val problemsSolved: Int = 0,
    val correctAnswers: Int = 0,
    val incorrectAnswers: Int = 0,
    val testsCompleted: Int = 0,
    val averageScore: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis()
)
