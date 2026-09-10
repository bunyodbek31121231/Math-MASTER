package com.example.data.repository

import com.example.data.database.dao.MaxsusDao
import com.example.data.database.dao.ProblemDao
import com.example.data.database.dao.SpecialContentDao
import com.example.data.database.dao.TopicDao
import com.example.data.database.entity.ProblemEntity
import com.example.data.database.entity.SpecialContentEntity
import com.example.data.database.entity.TopicEntity
import com.example.data.database.entity.maxsus.MaxsusContentEntity
import com.example.data.database.entity.maxsus.MaxsusProblemEntity
import com.example.data.database.entity.maxsus.MaxsusSectionEntity
import com.example.data.database.entity.maxsus.MaxsusTopicEntity
import com.example.data.preferences.UserPreferences
import com.example.security.MaxsusGate
import kotlinx.coroutines.flow.Flow

class MaxsusRepository(
    private val topicDao: TopicDao,
    private val specialContentDao: SpecialContentDao,
    private val problemDao: ProblemDao,
    private val maxsusDao: MaxsusDao,
    private val userPreferences: UserPreferences
) {
    val isUnlocked: Flow<Boolean> = userPreferences.maxsusUnlocked
    val maxsusTopics: Flow<List<TopicEntity>> = topicDao.getAllMaxsusTopics()

    // New Multi-level structure
    fun getAllSections(): Flow<List<MaxsusSectionEntity>> = maxsusDao.getAllSections()
    fun getTopicsForSection(sectionId: String): Flow<List<MaxsusTopicEntity>> = maxsusDao.getTopicsForSection(sectionId)
    fun getNewContentForTopic(topicId: String): Flow<List<MaxsusContentEntity>> = maxsusDao.getContentForTopic(topicId)
    fun getNewProblemsForTopic(topicId: String): Flow<List<MaxsusProblemEntity>> = maxsusDao.getProblemsForTopic(topicId)

    suspend fun verifyAndUnlock(code: String): Boolean {
        val success = MaxsusGate.verify(code)
        if (success) {
            userPreferences.setMaxsusUnlocked(true)
        }
        return success
    }

    suspend fun lockSection() {
        userPreferences.setMaxsusUnlocked(false)
    }

    fun getContentForTopic(topicId: String): Flow<List<SpecialContentEntity>> {
        return specialContentDao.getContentForTopic(topicId)
    }

    suspend fun getProblemsForMaxsusTopic(topicId: String, limit: Int = 10): List<ProblemEntity> {
        val problems = problemDao.getRandomProblems(topicId = topicId, limit = limit)
        if (problems.isEmpty()) {
            return problemDao.getRandomProblems(category = "MAXSUS", limit = limit)
        }
        return problems
    }
}
