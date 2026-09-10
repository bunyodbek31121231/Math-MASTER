package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "topics",
    indices = [
        Index(value = ["category"]),
        Index(value = ["isMaxsus"])
    ]
)
data class TopicEntity(
    @PrimaryKey
    val id: String,
    val category: String, // Algebra, Trigonometry, Geometry, Number Theory, Probability, Combinatorics, Statistics, Calculus, Logic, Olympiad
    val name: String,
    val description: String,
    val orderIndex: Int = 0,
    val iconName: String = "function",
    val isMaxsus: Boolean = false,
    val problemCountEstimate: Int = 0
)
