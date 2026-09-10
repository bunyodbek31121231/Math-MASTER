package com.example.generator.core

import java.util.Locale
import java.util.UUID
import kotlin.random.Random

data class ChoicesResult(
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswerLetter: String // "A", "B", "C", or "D"
)

object AnswerChoiceGenerator {

    /**
     * Builds four distinct answer choices with exactly one correct answer.
     * Shuffles the choices so that the correct answer is uniformly distributed among A, B, C, and D.
     * Generates plausible distractors using provided distractor candidates and mathematical perturbations.
     */
    fun buildChoices(
        correctAnswer: String,
        distractors: List<String> = emptyList(),
        random: Random = Random.Default
    ): ChoicesResult? {
        val trimmedCorrect = correctAnswer.trim()
        if (trimmedCorrect.isEmpty()) return null

        val uniqueDistractors = linkedSetOf<String>()

        // 1. Add provided distractors first (normalized)
        for (d in distractors) {
            val clean = d.trim()
            if (clean.isNotEmpty() && !areEquivalent(clean, trimmedCorrect)) {
                uniqueDistractors.add(clean)
                if (uniqueDistractors.size >= 3) break
            }
        }

        // 2. If we need more distractors, synthesize plausible mathematical variations
        if (uniqueDistractors.size < 3) {
            val synthesized = generateSynthesizedDistractors(trimmedCorrect, random)
            for (s in synthesized) {
                if (!areEquivalent(s, trimmedCorrect) && !uniqueDistractors.contains(s)) {
                    uniqueDistractors.add(s)
                    if (uniqueDistractors.size >= 3) break
                }
            }
        }

        // If we still cannot produce 3 distinct distractors, return null
        if (uniqueDistractors.size < 3) {
            return null
        }

        val chosenDistractors = uniqueDistractors.take(3)
        val allOptions = mutableListOf(trimmedCorrect, chosenDistractors[0], chosenDistractors[1], chosenDistractors[2])
        allOptions.shuffle(random)

        val correctIndex = allOptions.indexOf(trimmedCorrect)
        val letter = when (correctIndex) {
            0 -> "A"
            1 -> "B"
            2 -> "C"
            3 -> "D"
            else -> "A"
        }

        return ChoicesResult(
            optionA = allOptions[0],
            optionB = allOptions[1],
            optionC = allOptions[2],
            optionD = allOptions[3],
            correctAnswerLetter = letter
        )
    }

    /**
     * Numeric helper to create choices from a numerical value (Int or Double).
     */
    fun buildNumericChoices(
        correctValue: Long,
        unit: String = "",
        customDistractors: List<Long> = emptyList(),
        random: Random = Random.Default
    ): ChoicesResult? {
        val list = mutableListOf<String>()
        for (cd in customDistractors) {
            if (cd != correctValue) {
                list.add(formatValue(cd, unit))
            }
        }

        // Add standard cognitive mistake variations
        val offsets = listOf(1L, -1L, 2L, -2L, correctValue * 2, (correctValue / 2).coerceAtLeast(1), -correctValue)
        for (offset in offsets) {
            val candidate = if (offset == -correctValue || offset == correctValue * 2 || offset == (correctValue / 2).coerceAtLeast(1)) {
                offset
            } else {
                correctValue + offset
            }
            if (candidate != correctValue && !list.contains(formatValue(candidate, unit))) {
                list.add(formatValue(candidate, unit))
            }
        }

        return buildChoices(formatValue(correctValue, unit), list, random)
    }

    fun buildDoubleChoices(
        correctValue: Double,
        unit: String = "",
        random: Random = Random.Default
    ): ChoicesResult? {
        val formattedCorrect = formatDouble(correctValue) + (if (unit.isNotEmpty()) " $unit" else "")
        val distractors = mutableListOf<String>()

        val candidateVals = listOf(
            correctValue + 1.0,
            correctValue - 1.0,
            correctValue * 2.0,
            correctValue / 2.0,
            -correctValue,
            correctValue + 0.5,
            correctValue - 0.5
        )

        for (cv in candidateVals) {
            val s = formatDouble(cv) + (if (unit.isNotEmpty()) " $unit" else "")
            if (s != formattedCorrect) {
                distractors.add(s)
            }
        }

        return buildChoices(formattedCorrect, distractors, random)
    }

    private fun formatValue(v: Long, unit: String): String {
        return if (unit.isNotEmpty()) "$v $unit" else v.toString()
    }

    private fun formatDouble(d: Double): String {
        return if (d == d.toLong().toDouble()) {
            d.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", d)
        }
    }

    private fun areEquivalent(a: String, b: String): Boolean {
        if (a.equals(b, ignoreCase = true)) return true
        val numA = a.toDoubleOrNull()
        val numB = b.toDoubleOrNull()
        if (numA != null && numB != null) {
            return kotlin.math.abs(numA - numB) < 1e-9
        }
        return false
    }

    private fun generateSynthesizedDistractors(correct: String, random: Random): List<String> {
        val list = mutableListOf<String>()
        val intVal = correct.toLongOrNull()
        if (intVal != null) {
            list.add((intVal + 1).toString())
            list.add((intVal - 1).toString())
            list.add((intVal + 2).toString())
            list.add((-intVal).toString())
            list.add((intVal * 2).toString())
            return list
        }

        val doubleVal = correct.toDoubleOrNull()
        if (doubleVal != null) {
            list.add(formatDouble(doubleVal + 1.0))
            list.add(formatDouble(doubleVal - 1.0))
            list.add(formatDouble(doubleVal * 2.0))
            list.add(formatDouble(-doubleVal))
            return list
        }

        // If algebraic like "x = 4"
        if (correct.contains("=")) {
            val parts = correct.split("=")
            if (parts.size == 2) {
                val varName = parts[0].trim()
                val valPart = parts[1].trim().toLongOrNull()
                if (valPart != null) {
                    list.add("$varName = ${valPart + 1}")
                    list.add("$varName = ${valPart - 1}")
                    list.add("$varName = ${-valPart}")
                    list.add("$varName = ${valPart * 2}")
                    return list
                }
            }
        }

        // Generic fallback variations
        list.add("$correct + 1")
        list.add("$correct - 1")
        list.add("-$correct")
        list.add("2($correct)")
        return list
    }
}
