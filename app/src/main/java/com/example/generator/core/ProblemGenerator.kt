package com.example.generator.core

import com.example.model.Difficulty
import com.example.model.Problem

/**
 * Common interface for all Math MASTER problem generators.
 * Each generator produces verified, mathematically sound Problem instances.
 */
interface ProblemGenerator {
    /**
     * The primary category this generator belongs to (e.g., "Algebra", "Geometry").
     */
    val category: String

    /**
     * Unique identifier for the generator (e.g., "AlgebraLinearGenerator").
     */
    val generatorId: String

    /**
     * List of topic IDs supported by this generator.
     */
    val supportedTopics: List<String>

    /**
     * Generates a complete mathematical Problem for the given difficulty and optional topic.
     * Returns null if valid parameters cannot be formed after reasonable attempts.
     */
    fun generate(difficulty: Difficulty, topicId: String? = null): Problem?
}
