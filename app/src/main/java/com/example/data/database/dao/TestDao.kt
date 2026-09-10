package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.database.entity.TestEntity
import com.example.data.database.entity.TestQuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: TestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestQuestions(questions: List<TestQuestionEntity>)

    @Query("SELECT * FROM tests WHERE userId = :userId ORDER BY completedAt DESC")
    fun getTestsForUser(userId: String): Flow<List<TestEntity>>

    @Query("SELECT * FROM tests WHERE id = :testId LIMIT 1")
    suspend fun getTestById(testId: String): TestEntity?

    @Query("SELECT * FROM test_questions WHERE testId = :testId ORDER BY questionOrder ASC")
    suspend fun getQuestionsForTest(testId: String): List<TestQuestionEntity>

    @Query("SELECT COUNT(*) FROM tests WHERE userId = :userId")
    suspend fun getCompletedTestCount(userId: String): Int

    @Query("SELECT AVG(scorePercentage) FROM tests WHERE userId = :userId")
    suspend fun getAverageScore(userId: String): Double?

    @Query("SELECT COUNT(*) FROM tests")
    suspend fun getTestCount(): Int
}
