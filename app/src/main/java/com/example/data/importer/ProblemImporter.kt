package com.example.data.importer

import com.example.data.database.dao.ProblemDao
import com.example.data.database.entity.ProblemEntity
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

data class ImportResult(
    val totalProcessed: Int,
    val insertedCount: Int,
    val duplicateCount: Int,
    val malformedCount: Int,
    val errors: List<String>
)

class ProblemImporter(private val problemDao: ProblemDao) {

    /**
     * Imports problems from a JSON array string.
     * Validates required fields, checks for duplicate hashes, and skips malformed records safely.
     */
    suspend fun importFromJson(jsonString: String): ImportResult {
        var total = 0
        var inserted = 0
        var duplicates = 0
        var malformed = 0
        val errorMessages = mutableListOf<String>()
        val validBatch = mutableListOf<ProblemEntity>()

        try {
            val jsonArray = JSONArray(jsonString)
            total = jsonArray.length()

            for (i in 0 until total) {
                val obj = jsonArray.optJSONObject(i)
                if (obj == null) {
                    malformed++
                    errorMessages.add("Item at index $i is not a valid JSON object.")
                    continue
                }

                val validated = parseProblemObject(obj, i)
                if (validated != null) {
                    validBatch.add(validated)
                } else {
                    malformed++
                    errorMessages.add("Item at index $i failed validation (missing required fields).")
                }
            }

            if (validBatch.isNotEmpty()) {
                val insertResult = problemDao.insertProblems(validBatch)
                for (rowId in insertResult) {
                    if (rowId != -1L) {
                        inserted++
                    } else {
                        duplicates++
                    }
                }
            }
        } catch (e: Exception) {
            errorMessages.add("Fatal JSON parsing error: ${e.localizedMessage}")
        }

        return ImportResult(
            totalProcessed = total,
            insertedCount = inserted,
            duplicateCount = duplicates,
            malformedCount = malformed,
            errors = errorMessages.take(20)
        )
    }

    private fun parseProblemObject(obj: JSONObject, index: Int): ProblemEntity? {
        val question = obj.optString("question").trim()
        if (question.isEmpty()) return null

        val category = obj.optString("category", "General").trim().ifEmpty { "Algebra" }
        val topic = (if (obj.has("topic")) obj.optString("topic") else obj.optString("topicId", "algebra_linear")).trim()
        val difficulty = obj.optString("difficulty", "MEDIUM").uppercase().trim()

        var optA = obj.optString("optionA").trim()
        var optB = obj.optString("optionB").trim()
        var optC = obj.optString("optionC").trim()
        var optD = obj.optString("optionD").trim()

        // Also support "options" array if present
        if (obj.has("options")) {
            val arr = obj.optJSONArray("options")
            if (arr != null && arr.length() >= 4) {
                optA = arr.optString(0).trim()
                optB = arr.optString(1).trim()
                optC = arr.optString(2).trim()
                optD = arr.optString(3).trim()
            }
        }

        if (optA.isEmpty() || optB.isEmpty() || optC.isEmpty() || optD.isEmpty()) {
            return null
        }

        val answer = (if (obj.has("answer")) obj.optString("answer") else obj.optString("correctAnswer", "A"))
            .trim().uppercase()
        val normalizedAnswer = when {
            answer.startsWith("A") -> "A"
            answer.startsWith("B") -> "B"
            answer.startsWith("C") -> "C"
            answer.startsWith("D") -> "D"
            else -> "A"
        }

        val solution = obj.optString("solution").trim().ifEmpty { "Step-by-step mathematical reasoning verified." }
        val explanation = obj.optString("explanation").trim().ifEmpty { solution }
        val formula = obj.optString("formula").trim().ifEmpty { null }
        val tags = obj.optString("tags").trim()
        val generator = obj.optString("generator").trim().ifEmpty { "MathMaster-CoreGen" }

        val hash = if (obj.has("question_hash")) {
            obj.optString("question_hash").trim()
        } else if (obj.has("questionHash")) {
            obj.optString("questionHash").trim()
        } else {
            computeHash(question + optA + optB + optC + optD)
        }

        val id = if (obj.has("id") && obj.optString("id").isNotEmpty()) {
            obj.optString("id")
        } else {
            "mm_${hash.take(12)}"
        }

        return ProblemEntity(
            id = id,
            category = category,
            topicId = topic,
            difficulty = difficulty,
            question = question,
            optionA = optA,
            optionB = optB,
            optionC = optC,
            optionD = optD,
            correctAnswer = normalizedAnswer,
            solution = solution,
            explanation = explanation,
            formula = formula,
            tags = tags,
            generator = generator,
            questionHash = hash
        )
    }

    private fun computeHash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
