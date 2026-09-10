package com.example.generator.pipeline

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted state of a generation job in the Room database, enabling safe
 * pauses, background resumes, crash recovery, and multi-session completion
 * towards the 20,620+ problem milestone.
 */
@Entity(tableName = "generation_jobs")
data class GenerationJobEntity(
    @PrimaryKey
    val jobId: String,
    val targetCount: Int,
    val completedCount: Int,
    val batchSize: Int,
    val status: String, // "PENDING", "RUNNING", "PAUSED", "COMPLETED", "FAILED", "CANCELLED"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val categoriesJson: String = "", // Comma-separated or JSON list of categories
    val difficultiesJson: String = "", // Comma-separated list of difficulties
    val errorDetails: String? = null
)
