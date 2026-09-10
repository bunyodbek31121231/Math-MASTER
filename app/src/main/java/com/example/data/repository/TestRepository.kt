package com.example.data.repository

import com.example.data.database.dao.ProblemDao
import com.example.data.database.dao.TestDao
import com.example.data.database.entity.ProblemEntity
import com.example.data.database.entity.TestEntity
import com.example.data.database.entity.TestQuestionEntity
import com.example.model.Difficulty
import com.example.model.LevelSystem
import kotlinx.coroutines.flow.Flow
import java.util.UUID

data class CompletedTestSummary(
    val testId: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val scorePercentage: Double,
    val xpEarned: Int,
    val durationSeconds: Int
)

class TestRepository(
    private val testDao: TestDao,
    private val problemDao: ProblemDao,
    private val userRepository: UserRepository
) {
    fun getTestHistory(userId: String): Flow<List<TestEntity>> {
        return testDao.getTestsForUser(userId)
    }

    suspend fun getTestById(testId: String): TestEntity? {
        return testDao.getTestById(testId)
    }

    suspend fun getQuestionsForTest(testId: String): List<TestQuestionEntity> {
        return testDao.getQuestionsForTest(testId)
    }

    /**
     * Loads problem questions for test generation according to parameters.
     */
    suspend fun generateTestProblems(
        topicId: String?,
        difficulty: String?,
        count: Int
    ): List<ProblemEntity> {
        val problems = problemDao.getRandomProblems(
            topicId = if (topicId.isNullOrEmpty()) null else topicId,
            difficulty = if (difficulty.isNullOrEmpty()) null else difficulty,
            limit = count
        )
        // If not enough problems with exact filters, expand pool
        if (problems.size < count) {
            val supplemental = problemDao.getRandomProblems(
                topicId = null,
                difficulty = null,
                limit = count - problems.size
            )
            val combined = (problems + supplemental).distinctBy { it.id }
            return combined.take(count)
        }
        return problems
    }

    suspend fun saveCompletedTest(
        userId: String,
        title: String,
        topicId: String?,
        difficulty: String,
        problems: List<ProblemEntity>,
        userAnswers: Map<String, String>, // problemId -> selectedOption
        durationSeconds: Int,
        isMaxsus: Boolean = false
    ): CompletedTestSummary {
        val testId = UUID.randomUUID().toString()
        var correctCount = 0

        val testQuestionEntities = problems.mapIndexed { index, problem ->
            val selected = userAnswers[problem.id]
            val isCorrect = selected.equals(problem.correctAnswer, ignoreCase = true)
            if (isCorrect) correctCount++

            TestQuestionEntity(
                testId = testId,
                problemId = problem.id,
                questionOrder = index + 1,
                selectedAnswer = selected,
                correctAnswer = problem.correctAnswer,
                isCorrect = isCorrect
            )
        }

        val total = problems.size
        val percentage = if (total > 0) (correctCount.toDouble() / total.toDouble()) * 100.0 else 0.0

        val diffMultiplier = when (Difficulty.fromString(difficulty)) {
            Difficulty.EASY -> 1.0
            Difficulty.MEDIUM -> 1.5
            Difficulty.HARD -> 2.0
            Difficulty.EXPERT -> 2.5
        }
        val xpEarned = (LevelSystem.TEST_COMPLETION_BASE_XP + (correctCount * 15 * diffMultiplier)).toInt()

        val testEntity = TestEntity(
            id = testId,
            userId = userId,
            title = title,
            topicId = topicId,
            difficulty = difficulty,
            totalQuestions = total,
            correctCount = correctCount,
            scorePercentage = percentage,
            xpEarned = xpEarned,
            durationSeconds = durationSeconds,
            completedAt = System.currentTimeMillis(),
            isMaxsus = isMaxsus
        )

        testDao.insertTest(testEntity)
        testDao.insertTestQuestions(testQuestionEntities)

        userRepository.addXpAndRecordTest(userId, xpEarned.toLong(), percentage)

        return CompletedTestSummary(
            testId = testId,
            totalQuestions = total,
            correctCount = correctCount,
            scorePercentage = percentage,
            xpEarned = xpEarned,
            durationSeconds = durationSeconds
        )
    }
}
