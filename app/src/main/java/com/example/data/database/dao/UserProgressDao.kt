package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.database.entity.UserProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE userId = :userId ORDER BY lastPracticedAt DESC")
    fun getProgressForUser(userId: String): Flow<List<UserProgressEntity>>

    @Query("SELECT * FROM user_progress WHERE userId = :userId AND topicId = :topicId LIMIT 1")
    suspend fun getProgressForTopic(userId: String, topicId: String): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: UserProgressEntity)

    @Query("SELECT COUNT(DISTINCT topicId) FROM user_progress WHERE userId = :userId AND problemsSolved > 0")
    suspend fun getTopicsStudiedCount(userId: String): Int
}
