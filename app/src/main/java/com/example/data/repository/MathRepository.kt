package com.example.data.repository

import com.example.data.database.dao.ProblemDao
import com.example.data.database.dao.QuestionHistoryDao
import com.example.data.database.dao.TopicDao
import com.example.data.database.dao.UserProgressDao
import com.example.data.database.entity.ProblemEntity
import com.example.data.database.entity.QuestionHistoryEntity
import com.example.data.database.entity.TopicEntity
import com.example.data.database.entity.UserProgressEntity
import com.example.model.Difficulty
import com.example.model.LevelSystem
import kotlinx.coroutines.flow.Flow

class MathRepository(
    private val problemDao: ProblemDao,
    private val questionHistoryDao: QuestionHistoryDao,
    private val topicDao: TopicDao,
    private val userProgressDao: UserProgressDao,
    private val userRepository: UserRepository
) {
    val allTopics: Flow<List<TopicEntity>> = topicDao.getAllRegularTopics()
    val allCategories: Flow<List<String>> = topicDao.getAllCategories()
    val totalProblemCount: Flow<Int> = problemDao.getTotalProblemCount()

    fun getTopicsByCategory(category: String): Flow<List<TopicEntity>> {
        return topicDao.getTopicsByCategory(category)
    }

    suspend fun getTopicById(id: String): TopicEntity? {
        return topicDao.getTopicById(id)
    }

    /**
     * Retrieves an unseen problem for the specified user.
     * Efficiently queries the database excluding previously answered problems.
     * If all problems matching the criteria have been seen, safely falls back to any matching problem.
     */
    suspend fun getNextPracticeProblem(
        userId: String,
        topicId: String? = null,
        category: String? = null,
        difficulty: String? = null
    ): ProblemEntity? {
        val unseen = problemDao.getUnseenProblems(
            userId = userId,
            topicId = topicId,
            category = category,
            difficulty = difficulty,
            limit = 1
        )
        if (unseen.isNotEmpty()) {
            return unseen.first()
        }

        // If all unseen problems are exhausted, gracefully return random problem
        val randomFallback = problemDao.getRandomProblems(
            topicId = topicId,
            category = category,
            difficulty = difficulty,
            limit = 1
        )
        return randomFallback.firstOrNull()
    }

    /**
     * Records problem answer, updates question history (no-repeat system),
     * updates user progress on the specific topic, and awards XP.
     */
    suspend fun submitProblemAnswer(
        userId: String,
        problem: ProblemEntity,
        selectedAnswer: String,
        timeSpentSeconds: Int,
        mode: String = "PRACTICE"
    ): Boolean {
        val isCorrect = selectedAnswer.equals(problem.correctAnswer, ignoreCase = true)

        // 1. Record in question history
        val historyEntry = QuestionHistoryEntity(
            userId = userId,
            problemId = problem.id,
            questionHash = problem.questionHash,
            selectedAnswer = selectedAnswer,
            isCorrect = isCorrect,
            timeSpentSeconds = timeSpentSeconds,
            mode = mode
        )
        questionHistoryDao.insertHistory(historyEntry)

        // 2. Award XP
        val difficulty = Difficulty.fromString(problem.difficulty)
        val xpGain = if (isCorrect) {
            difficulty.baseXP.toLong() + LevelSystem.PRACTICE_BONUS_XP
        } else {
            // Small participation XP
            3L
        }
        userRepository.addXpAndRecordProblem(userId, xpGain, isCorrect)

        // 3. Update User Progress on topic
        updateUserProgress(userId, problem.topicId, problem.category, isCorrect)

        return isCorrect
    }

    private suspend fun updateUserProgress(
        userId: String,
        topicId: String,
        category: String,
        isCorrect: Boolean
    ) {
        val compositeId = "${userId}_${topicId}"
        val existing = userProgressDao.getProgressForTopic(userId, topicId)
        if (existing != null) {
            val newTotal = existing.problemsSolved + 1
            val newCorrect = existing.correctCount + (if (isCorrect) 1 else 0)
            val newMastery = (newCorrect.toDouble() / newTotal.toDouble()) * 100.0
            userProgressDao.saveProgress(
                existing.copy(
                    problemsSolved = newTotal,
                    correctCount = newCorrect,
                    masteryPercentage = newMastery,
                    lastPracticedAt = System.currentTimeMillis()
                )
            )
        } else {
            userProgressDao.saveProgress(
                UserProgressEntity(
                    id = compositeId,
                    userId = userId,
                    topicId = topicId,
                    category = category,
                    problemsSolved = 1,
                    correctCount = if (isCorrect) 1 else 0,
                    masteryPercentage = if (isCorrect) 100.0 else 0.0,
                    lastPracticedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun getUserProgressList(userId: String): Flow<List<UserProgressEntity>> {
        return userProgressDao.getProgressForUser(userId)
    }

    fun getRecentHistory(userId: String, limit: Int = 10): Flow<List<QuestionHistoryEntity>> {
        return questionHistoryDao.getRecentHistory(userId, limit)
    }
}
