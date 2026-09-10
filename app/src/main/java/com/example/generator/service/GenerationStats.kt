package com.example.generator.service

data class GenerationStats(
    val requested: Int,
    val generated: Int,
    val valid: Int,
    val invalid: Int,
    val duplicates: Int,
    val inserted: Int,
    val failedAttempts: Int,
    val byCategory: Map<String, Int> = emptyMap(),
    val byDifficulty: Map<String, Int> = emptyMap(),
    val errors: List<String> = emptyList()
)
