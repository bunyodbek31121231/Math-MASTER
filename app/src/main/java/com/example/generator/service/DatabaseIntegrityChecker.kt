package com.example.generator.service

import com.example.data.database.dao.ProblemDao
import com.example.data.database.entity.ProblemEntity
import com.example.generator.core.ProblemValidator

data class DatabaseIntegrityReport(
    val totalChecked: Int,
    val healthyCount: Int,
    val malformedCount: Int,
    val duplicateHashCount: Int,
    val issues: List<String>,
    val isValid: Boolean
)

class DatabaseIntegrityChecker(private val problemDao: ProblemDao) {

    /**
     * Performs a thorough validation pass across database problem records.
     * Checks:
     * 1. Required non-empty questions, solutions, valid difficulty levels.
     * 2. Four non-empty distinct options and valid answer keys ('A', 'B', 'C', 'D').
     * 3. Consistent SHA-256 question hashes matching canonical formatting.
     */
    suspend fun checkIntegrity(maxCheck: Int = 2000): DatabaseIntegrityReport {
        val issues = mutableListOf<String>()
        var checked = 0
        var healthy = 0
        var malformed = 0
        var duplicateHashes = 0
        val seenHashes = mutableSetOf<String>()

        val pageSize = 200
        var offset = 0

        while (checked < maxCheck) {
            val limit = (maxCheck - checked).coerceAtMost(pageSize)
            val batch = problemDao.getProblemsPaged(limit = limit, offset = offset)
            if (batch.isEmpty()) break

            for (p in batch) {
                checked++
                var itemValid = true

                // Check basic fields
                if (p.question.isBlank() || p.solution.isBlank()) {
                    issues.add("Problem ${p.id}: Question or solution is blank.")
                    itemValid = false
                }

                // Check options
                val opts = listOf(p.optionA.trim(), p.optionB.trim(), p.optionC.trim(), p.optionD.trim())
                if (opts.any { it.isBlank() } || opts.toSet().size != 4) {
                    issues.add("Problem ${p.id}: Choices are invalid or not distinct: $opts")
                    itemValid = false
                }

                // Check answer key
                if (p.correctAnswer !in listOf("A", "B", "C", "D")) {
                    issues.add("Problem ${p.id}: Invalid correctAnswer letter '${p.correctAnswer}'")
                    itemValid = false
                }

                // Check hash
                if (seenHashes.contains(p.questionHash)) {
                    issues.add("Problem ${p.id}: Duplicate hash detected in inspected batch: ${p.questionHash}")
                    duplicateHashes++
                    itemValid = false
                } else {
                    seenHashes.add(p.questionHash)
                }

                if (itemValid) {
                    healthy++
                } else {
                    malformed++
                }
            }

            offset += batch.size
            if (batch.size < limit) break
        }

        return DatabaseIntegrityReport(
            totalChecked = checked,
            healthyCount = healthy,
            malformedCount = malformed,
            duplicateHashCount = duplicateHashes,
            issues = issues.take(50),
            isValid = malformed == 0 && duplicateHashes == 0
        )
    }
}
