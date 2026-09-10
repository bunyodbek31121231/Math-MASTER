package com.example.util.admin

import com.example.BuildConfig
import com.example.data.database.entity.maxsus.MaxsusContentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@Serializable
data class AnalyzedMaterial(
    val type: String, // SECTION, TOPIC, THEORY, FORMULA, EXAMPLE, EXERCISE, TEST
    val title: String? = null,
    val body: String? = null,
    val formula: String? = null,
    val question: String? = null,
    val optionA: String? = null,
    val optionB: String? = null,
    val optionC: String? = null,
    val optionD: String? = null,
    val correctAnswer: String? = null,
    val solution: String? = null,
    val explanation: String? = null,
    val sectionTitle: String? = null,
    val topicTitle: String? = null,
    val isUncertain: Boolean = false
)

@Serializable
data class AnalyzerResult(
    val materials: List<AnalyzedMaterial>
)

class GeminiMaterialAnalyzer {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun analyzeText(text: String): List<AnalyzedMaterial> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "GEMINI_API_KEY_DEFAULT_VALUE") {
            return@withContext RuleBasedMaterialAnalyzer.analyzeText(text)
        }

        val prompt = """
            Analyze the following mathematical text and extract structured materials.
            Identify:
            - Sections (Major chapters)
            - Topics (Sub-chapters)
            - Theory (Descriptive text, definitions)
            - Formulas
            - Examples (Problem + Solution)
            - Exercises (Problem only or Problem + Solution)
            - Tests (Multiple choice questions with options A, B, C, D and correct answer)

            Text:
            $text

            Return ONLY a JSON object with a 'materials' array.
            Each item in 'materials' should have:
            - 'type': one of "SECTION", "TOPIC", "THEORY", "FORMULA", "EXAMPLE", "EXERCISE", "TEST"
            - 'title': for sections, topics, or specific headers
            - 'body': the main text/theory/question
            - 'formula': if it's a formula or has one
            - 'question': if it's a test or example
            - 'optionA', 'optionB', 'optionC', 'optionD': for tests
            - 'correctAnswer': 'A', 'B', 'C', or 'D' for tests
            - 'solution': step-by-step solution
            - 'explanation': additional context
            - 'sectionTitle': current section it belongs to
            - 'topicTitle': current topic it belongs to
            - 'isUncertain': true if the text was unclear or possibly misidentified

            If something is unclear, mark it as 'isUncertain': true. Do NOT invent data.
        """.trimIndent()

        val requestBody = buildJsonObject {
            putJsonArray("contents") {
                add(buildJsonObject {
                    putJsonArray("parts") {
                        add(buildJsonObject { put("text", prompt) })
                    }
                })
            }
            putJsonObject("generationConfig") {
                put("responseMimeType", "application/json")
            }
        }.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-pro-preview:generateContent?key=$apiKey")
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext emptyList()
            
            // Basic parsing of Gemini response structure
            val responseJson = Json.parseToJsonElement(responseBody).let { it as? JsonObject }
            val candidates = responseJson?.get("candidates")?.let { it as? kotlinx.serialization.json.JsonArray }
            val content = candidates?.firstOrNull()?.let { it as? JsonObject }?.get("content")?.let { it as? JsonObject }
            val parts = content?.get("parts")?.let { it as? kotlinx.serialization.json.JsonArray }
            val resultText = parts?.firstOrNull()?.let { it as? JsonObject }?.get("text")?.let { it as? kotlinx.serialization.json.JsonPrimitive }?.content
            
            if (resultText != null) {
                val result = json.decodeFromString<AnalyzerResult>(resultText)
                result.materials
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            RuleBasedMaterialAnalyzer.analyzeText(text)
        }
    }
}
