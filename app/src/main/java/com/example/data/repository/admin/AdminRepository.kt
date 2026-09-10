package com.example.data.repository.admin

import com.example.data.database.dao.*
import com.example.data.database.entity.admin.ImportHistoryEntity
import com.example.data.database.entity.maxsus.MaxsusContentEntity
import com.example.data.database.entity.maxsus.MaxsusProblemEntity
import com.example.data.database.entity.maxsus.MaxsusSectionEntity
import com.example.data.database.entity.maxsus.MaxsusTopicEntity
import kotlinx.coroutines.flow.Flow

class AdminRepository(
    private val userDao: UserDao,
    private val problemDao: ProblemDao,
    private val topicDao: TopicDao,
    private val testDao: TestDao,
    private val maxsusDao: MaxsusDao,
    private val adminDao: AdminDao
) {
    suspend fun getDashboardStats(): Map<String, Int> {
        return mapOf(
            "users" to userDao.getUserCount(),
            "ordinary_problems" to problemDao.getProblemCountDirect(),
            "maxsus_materials" to maxsusDao.getContentCount(),
            "maxsus_problems" to maxsusDao.getProblemCount(),
            "topics" to topicDao.getTopicCount(),
            "tests" to testDao.getTestCount(),
            "imports" to adminDao.getImportCount()
        )
    }

    fun getImportHistory(): Flow<List<ImportHistoryEntity>> = adminDao.getAllImportHistory()

    suspend fun undoLastImport() {
        val lastImport = adminDao.getLastImport()
        if (lastImport != null) {
            maxsusDao.undoImport(lastImport.id)
            adminDao.deleteImportHistory(lastImport.id)
        }
    }

    suspend fun recordImport(history: ImportHistoryEntity) {
        adminDao.insertImportHistory(history)
    }

    suspend fun recordSection(section: MaxsusSectionEntity) {
        maxsusDao.insertSection(section)
    }

    suspend fun recordTopic(topic: MaxsusTopicEntity) {
        maxsusDao.insertTopic(topic)
    }

    suspend fun recordContent(content: MaxsusContentEntity) {
        maxsusDao.insertContent(content)
    }

    suspend fun recordProblem(problem: MaxsusProblemEntity) {
        recordProblemWithResult(problem)
    }

    suspend fun recordProblemWithResult(problem: MaxsusProblemEntity): Boolean {
        val existing = maxsusDao.getProblemByHash(problem.questionHash)
        return if (existing == null) {
            maxsusDao.insertProblem(problem)
            true
        } else {
            false
        }
    }
}
