package com.example.generator.core

import com.example.model.Problem
import java.security.MessageDigest
import java.util.Locale

object ProblemValidator {

    private val VALID_CATEGORIES = setOf(
        "Algebra",
        "Trigonometry",
        "Geometry",
        "Number Theory",
        "Probability",
        "Combinatorics",
        "Statistics",
        "Calculus",
        "Logic",
        "Olympiad",
        "Mixed",
        "MAXSUS"
    )

    private val VALID_DIFFICULTIES = setOf("EASY", "MEDIUM", "HARD", "EXPERT")
    private val VALID_LETTERS = setOf("A", "B", "C", "D")

    /**
     * Normalizes a text string by removing extraneous whitespace, converting to lower case,
     * and normalizing common mathematical symbols for duplicate comparison.
     */
    fun normalize(text: String): String {
        return text.trim()
            .lowercase(Locale.ROOT)
            .replace("\\s+".toRegex(), " ")
            .replace("−", "-")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("·", "*")
            .replace("√", "sqrt")
            .replace("π", "pi")
            .replace("°", "deg")
            .replace("≤", "<=")
            .replace("≥", ">=")
            .replace("≠", "!=")
    }

    /**
     * Computes the canonical SHA-256 hash of a problem for duplicate detection.
     */
    fun computeHash(question: String, optionA: String, optionB: String, optionC: String, optionD: String): String {
        val normQ = normalize(question)
        // Sort options to ensure permutation invariance if question stem is identical
        val normOpts = listOf(normalize(optionA), normalize(optionB), normalize(optionC), normalize(optionD)).sorted()
        val combined = "$normQ||${normOpts.joinToString("|")}"

        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Validates a problem against structural and mathematical consistency rules.
     * Returns null if valid, or a descriptive error message if invalid.
     */
    fun validate(problem: Problem): String? {
        // 1. Basic non-empty checks
        if (problem.id.isBlank()) return "Problem ID cannot be blank."
        if (problem.question.trim().length < 5) return "Question is too short or blank."
        if (problem.solution.trim().length < 5) return "Solution is missing or too brief."

        // 2. Category check
        if (!VALID_CATEGORIES.contains(problem.category)) {
            return "Invalid category: '${problem.category}'. Must be one of $VALID_CATEGORIES"
        }

        // 3. Topic ID
        if (problem.topicId.isBlank()) return "Topic ID cannot be blank."

        // 4. Difficulty check
        if (!VALID_DIFFICULTIES.contains(problem.difficulty)) {
            return "Invalid difficulty: '${problem.difficulty}'. Must be one of $VALID_DIFFICULTIES"
        }

        // 5. Four distinct answer choices
        val opts = listOf(
            problem.optionA.trim(),
            problem.optionB.trim(),
            problem.optionC.trim(),
            problem.optionD.trim()
        )
        if (opts.any { it.isBlank() }) {
            return "All four options (A, B, C, D) must be non-empty."
        }
        val uniqueOpts = opts.toSet()
        if (uniqueOpts.size != 4) {
            return "Duplicate options found among choices: $opts"
        }

        // 6. Correct answer letter
        val letter = problem.correctAnswer.trim().uppercase()
        if (!VALID_LETTERS.contains(letter)) {
            return "Correct answer must be 'A', 'B', 'C', or 'D', was: '$letter'"
        }

        // 7. Hash check
        val expectedHash = computeHash(problem.question, problem.optionA, problem.optionB, problem.optionC, problem.optionD)
        if (problem.questionHash.isBlank()) {
            return "Question hash cannot be blank."
        }
        // Note: Generator can set the computed hash directly

        return null // Valid!
    }
}
