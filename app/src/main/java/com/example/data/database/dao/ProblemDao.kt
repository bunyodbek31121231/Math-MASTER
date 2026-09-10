package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.database.entity.ProblemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProblemDao {
    @Query("SELECT * FROM problems WHERE id = :id LIMIT 1")
    suspend fun getProblemById(id: String): ProblemEntity?

    @Query("SELECT * FROM problems WHERE questionHash = :hash LIMIT 1")
    suspend fun getProblemByHash(hash: String): ProblemEntity?

    @Query("""
        SELECT * FROM problems
        WHERE (:topicId IS NULL OR topicId = :topicId)
          AND (:category IS NULL OR category = :category)
          AND (:difficulty IS NULL OR difficulty = :difficulty)
          AND id NOT IN (SELECT problemId FROM question_history WHERE userId = :userId)
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getUnseenProblems(
        userId: String,
        topicId: String? = null,
        category: String? = null,
        difficulty: String? = null,
        limit: Int = 1
    ): List<ProblemEntity>

    @Query("""
        SELECT * FROM problems
        WHERE (:topicId IS NULL OR topicId = :topicId)
          AND (:category IS NULL OR category = :category)
          AND (:difficulty IS NULL OR difficulty = :difficulty)
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getRandomProblems(
        topicId: String? = null,
        category: String? = null,
        difficulty: String? = null,
        limit: Int = 1
    ): List<ProblemEntity>

    @Query("SELECT COUNT(*) FROM problems")
    fun getTotalProblemCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM problems")
    suspend fun getProblemCountDirect(): Int

    @Query("SELECT COUNT(*) FROM problems WHERE topicId = :topicId")
    suspend fun getCountByTopic(topicId: String): Int

    @Query("SELECT COUNT(*) FROM problems WHERE category = :category")
    suspend fun getCountByCategory(category: String): Int

    @Query("SELECT category, COUNT(*) as count FROM problems GROUP BY category")
    suspend fun getCategoryCounts(): List<CategoryCountTuple>

    @Query("SELECT difficulty, COUNT(*) as count FROM problems GROUP BY difficulty")
    suspend fun getDifficultyCounts(): List<DifficultyCountTuple>

    @Query("SELECT topicId, COUNT(*) as count FROM problems GROUP BY topicId")
    suspend fun getTopicCounts(): List<TopicCountTuple>

    @Query("SELECT * FROM problems LIMIT :limit OFFSET :offset")
    suspend fun getProblemsPaged(limit: Int, offset: Int): List<ProblemEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProblem(problem: ProblemEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProblems(problems: List<ProblemEntity>): List<Long>
}

data class CategoryCountTuple(
    val category: String,
    val count: Int
)

data class DifficultyCountTuple(
    val difficulty: String,
    val count: Int
)

data class TopicCountTuple(
    val topicId: String,
    val count: Int
)

