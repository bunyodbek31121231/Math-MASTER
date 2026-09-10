package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.database.entity.QuestionHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: QuestionHistoryEntity): Long

    @Query("SELECT COUNT(*) FROM question_history WHERE userId = :userId AND isCorrect = 1")
    suspend fun getCorrectCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM question_history WHERE userId = :userId")
    suspend fun getTotalAnsweredCount(userId: String): Int

    @Query("SELECT * FROM question_history WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistory(userId: String, limit: Int = 10): Flow<List<QuestionHistoryEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM question_history WHERE userId = :userId AND problemId = :problemId)")
    suspend fun hasUserSeenProblem(userId: String, problemId: String): Boolean

    @Query("DELETE FROM question_history WHERE userId = :userId")
    suspend fun clearUserHistory(userId: String)
}
