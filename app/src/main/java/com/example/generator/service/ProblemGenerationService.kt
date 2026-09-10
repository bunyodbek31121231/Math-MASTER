package com.example.generator.service

import com.example.data.database.dao.ProblemDao
import com.example.data.database.entity.ProblemEntity
import com.example.generator.core.ProblemGenerator
import com.example.generator.core.ProblemValidator
import com.example.generator.generators.AlgebraGenerator
import com.example.generator.generators.CalculusGenerator
import com.example.generator.generators.CombinatoricsGenerator
import com.example.generator.generators.GeometryGenerator
import com.example.generator.generators.LogicGenerator
import com.example.generator.generators.MixedGenerator
import com.example.generator.generators.NumberTheoryGenerator
import com.example.generator.generators.OlympiadGenerator
import com.example.generator.generators.ProbabilityGenerator
import com.example.generator.generators.StatisticsGenerator
import com.example.generator.generators.TrigonometryGenerator
import com.example.generator.pipeline.GenerationJobDao
import com.example.generator.pipeline.GenerationJobEntity
import com.example.generator.pipeline.QuotaManager
import com.example.model.Difficulty
import com.example.model.Problem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

class ProblemGenerationService(
    private val problemDao: ProblemDao,
    private val jobDao: GenerationJobDao? = null,
    baseGenerators: List<ProblemGenerator>? = null,
    private val random: Random = Random.Default
) {
    private val generators: List<ProblemGenerator>

    init {
        val base = baseGenerators ?: listOf(
            AlgebraGenerator(random),
            TrigonometryGenerator(random),
            GeometryGenerator(random),
            NumberTheoryGenerator(random),
            ProbabilityGenerator(random),
            CombinatoricsGenerator(random),
            StatisticsGenerator(random),
            CalculusGenerator(random),
            LogicGenerator(random),
            OlympiadGenerator(random)
        )
        // Add Mixed generator containing all base generators
        generators = base + MixedGenerator(base, random)
    }

    private val isCancelled = AtomicBoolean(false)
    private val isPaused = AtomicBoolean(false)

    fun cancelActiveJob() {
        isCancelled.set(true)
    }

    fun pauseActiveJob() {
        isPaused.set(true)
    }

    fun resetControls() {
        isCancelled.set(false)
        isPaused.set(false)
    }

    /**
     * Executes a resumable generation job in controlled batches.
     * Batches of 100-1000 problems are generated and committed in individual Room transactions.
     * Progress is persisted to SQLite after each batch commit so that even if the app stops or crashes,
     * all already-committed problems are completely safe and progress resumes cleanly.
     */
    suspend fun executeJob(
        jobId: String,
        targetTotal: Int,
        batchSize: Int = 500,
        categories: List<String>? = null,
        difficulties: List<Difficulty>? = null,
        onProgress: ((currentCompleted: Int, targetTotal: Int, currentBatchInserted: Int) -> Unit)? = null
    ): GenerationStats = withContext(Dispatchers.IO) {
        resetControls()

        // 1. Check if job exists in DB to resume
        var existingJob = jobDao?.getJobById(jobId)
        if (existingJob == null) {
            existingJob = GenerationJobEntity(
                jobId = jobId,
                targetCount = targetTotal,
                completedCount = 0,
                batchSize = batchSize,
                status = "RUNNING",
                categoriesJson = categories?.joinToString(",") ?: "",
                difficultiesJson = difficulties?.joinToString(",") { it.name } ?: ""
            )
            jobDao?.insertJob(existingJob)
        } else {
            jobDao?.updateStatus(jobId, "RUNNING")
        }

        var completedCount = existingJob.completedCount
        val targetCategories = if (existingJob.categoriesJson.isNotBlank()) {
            existingJob.categoriesJson.split(",").filter { it.isNotBlank() }
        } else {
            categories?.ifEmpty { null } ?: generators.map { it.category }.distinct()
        }

        val targetDifficulties = if (existingJob.difficultiesJson.isNotBlank()) {
            existingJob.difficultiesJson.split(",").mapNotNull {
                try { Difficulty.valueOf(it) } catch (e: Exception) { null }
            }.ifEmpty { Difficulty.entries.toList() }
        } else {
            difficulties?.ifEmpty { null } ?: Difficulty.entries.toList()
        }

        var totalGenerated = 0
        var totalValid = 0
        var totalInvalid = 0
        var totalDuplicates = 0
        var totalInserted = 0
        var totalFailedAttempts = 0
        val categoryCounts = mutableMapOf<String, Int>()
        val difficultyCounts = mutableMapOf<String, Int>()
        val errors = mutableListOf<String>()

        val effectiveBatchSize = batchSize.coerceIn(50, 1000)

        while (completedCount < targetTotal && !isCancelled.get() && !isPaused.get()) {
            val remaining = targetTotal - completedCount
            val currentBatchGoal = remaining.coerceAtMost(effectiveBatchSize)

            val batchStats = generateSingleBatch(
                batchGoal = currentBatchGoal,
                targetCategories = targetCategories,
                targetDifficulties = targetDifficulties,
                onBatchProgress = { cur, tot ->
                    onProgress?.invoke(completedCount + cur, targetTotal, cur)
                }
            )

            totalGenerated += batchStats.generated
            totalValid += batchStats.valid
            totalInvalid += batchStats.invalid
            totalDuplicates += batchStats.duplicates
            totalInserted += batchStats.inserted
            totalFailedAttempts += batchStats.failedAttempts
            errors.addAll(batchStats.errors)

            for ((k, v) in batchStats.byCategory) {
                categoryCounts[k] = (categoryCounts[k] ?: 0) + v
            }
            for ((k, v) in batchStats.byDifficulty) {
                difficultyCounts[k] = (difficultyCounts[k] ?: 0) + v
            }

            // Advance completed count only by successfully inserted records
            completedCount += batchStats.inserted
            jobDao?.updateProgress(jobId, completedCount)

            onProgress?.invoke(completedCount, targetTotal, batchStats.inserted)

            if (batchStats.inserted == 0 && batchStats.failedAttempts > 50) {
                // Cannot generate more unique items in this category set
                errors.add("Terminating batch loop: Generator unable to find new unique items.")
                break
            }
        }

        val finalStatus = when {
            isCancelled.get() -> "CANCELLED"
            isPaused.get() -> "PAUSED"
            completedCount >= targetTotal -> "COMPLETED"
            else -> "PAUSED"
        }
        jobDao?.updateStatus(jobId, finalStatus)

        return@withContext GenerationStats(
            requested = targetTotal,
            generated = totalGenerated,
            valid = totalValid,
            invalid = totalInvalid,
            duplicates = totalDuplicates,
            inserted = totalInserted,
            failedAttempts = totalFailedAttempts,
            byCategory = categoryCounts,
            byDifficulty = difficultyCounts,
            errors = errors.take(20)
        )
    }

    /**
     * Generates a single atomic batch and commits it to the database.
     */
    private suspend fun generateSingleBatch(
        batchGoal: Int,
        targetCategories: List<String>,
        targetDifficulties: List<Difficulty>,
        onBatchProgress: ((current: Int, total: Int) -> Unit)? = null
    ): GenerationStats {
        val matchingGenerators = generators.filter { targetCategories.contains(it.category) }
        if (matchingGenerators.isEmpty()) {
            return GenerationStats(
                requested = batchGoal,
                generated = 0,
                valid = 0,
                invalid = 0,
                duplicates = 0,
                inserted = 0,
                failedAttempts = 0,
                errors = listOf("No active generators found matching categories: $targetCategories")
            )
        }

        // Get current counts to balance quotas
        val currentCategoryDbCounts = try {
            problemDao.getCategoryCounts().associate { it.category to it.count }
        } catch (e: Exception) {
            emptyMap()
        }

        val deficits = QuotaManager.computeDeficit(currentCategoryDbCounts, 20620)

        val seenHashes = mutableSetOf<String>()
        val batchEntities = mutableListOf<ProblemEntity>()
        val catCounts = mutableMapOf<String, Int>()
        val diffCounts = mutableMapOf<String, Int>()
        val errorLogs = mutableListOf<String>()

        var generatedAttempts = 0
        var validCount = 0
        var invalidCount = 0
        var duplicatesCount = 0
        var failedAttempts = 0
        val maxFailedAttempts = (batchGoal * 20).coerceAtLeast(200)

        while (batchEntities.size < batchGoal && failedAttempts < maxFailedAttempts && !isCancelled.get() && !isPaused.get()) {
            generatedAttempts++

            // Select category using quota deficits, then pick matching generator
            val chosenCategory = QuotaManager.selectCategoryByDeficit(deficits, targetCategories, random)
            val generator = matchingGenerators.filter { it.category == chosenCategory }.ifEmpty { matchingGenerators }.random(random)
            val difficulty = targetDifficulties.random(random)

            val problem: Problem? = try {
                generator.generate(difficulty)
            } catch (e: Exception) {
                errorLogs.add("Generator ${generator.generatorId} error: ${e.localizedMessage}")
                null
            }

            if (problem == null) {
                failedAttempts++
                continue
            }

            // Strict mathematical & structural validation
            val validationError = ProblemValidator.validate(problem)
            if (validationError != null) {
                invalidCount++
                failedAttempts++
                if (errorLogs.size < 10) {
                    errorLogs.add("Validation error [${generator.generatorId}]: $validationError")
                }
                continue
            }
            validCount++

            // Hash deduplication in current batch
            val hash = problem.questionHash
            if (seenHashes.contains(hash)) {
                duplicatesCount++
                failedAttempts++
                continue
            }

            // Hash deduplication against Room database
            val existingInDb = problemDao.getProblemByHash(hash)
            if (existingInDb != null) {
                duplicatesCount++
                failedAttempts++
                seenHashes.add(hash)
                continue
            }

            // Accept problem
            seenHashes.add(hash)
            val entity = ProblemEntity(
                id = problem.id,
                category = problem.category,
                topicId = problem.topicId,
                difficulty = problem.difficulty,
                question = problem.question,
                optionA = problem.optionA,
                optionB = problem.optionB,
                optionC = problem.optionC,
                optionD = problem.optionD,
                correctAnswer = problem.correctAnswer,
                solution = problem.solution,
                explanation = problem.explanation,
                formula = problem.formula,
                tags = problem.tags,
                generator = problem.generator,
                questionHash = hash
            )
            batchEntities.add(entity)
            catCounts[problem.category] = (catCounts[problem.category] ?: 0) + 1
            diffCounts[problem.difficulty] = (diffCounts[problem.difficulty] ?: 0) + 1

            onBatchProgress?.invoke(batchEntities.size, batchGoal)
        }

        // Room safe chunked insertion (chunks of 100)
        var insertedCount = 0
        for (chunk in batchEntities.chunked(100)) {
            val insertResults = problemDao.insertProblems(chunk)
            for (res in insertResults) {
                if (res != -1L) {
                    insertedCount++
                } else {
                    duplicatesCount++
                }
            }
        }

        return GenerationStats(
            requested = batchGoal,
            generated = generatedAttempts,
            valid = validCount,
            invalid = invalidCount,
            duplicates = duplicatesCount,
            inserted = insertedCount,
            failedAttempts = failedAttempts,
            byCategory = catCounts,
            byDifficulty = diffCounts,
            errors = errorLogs.take(10)
        )
    }

    /**
     * Backward-compatible simple generate call.
     */
    suspend fun generate(
        count: Int,
        categories: List<String>? = null,
        difficulties: List<Difficulty>? = null,
        onProgress: ((current: Int, total: Int) -> Unit)? = null
    ): GenerationStats {
        val tempJobId = "job_quick_${UUID.randomUUID().toString().take(8)}"
        return executeJob(
            jobId = tempJobId,
            targetTotal = count,
            batchSize = count.coerceAtMost(500),
            categories = categories,
            difficulties = difficulties,
            onProgress = { cur, tot, _ -> onProgress?.invoke(cur, tot) }
        )
    }
}
