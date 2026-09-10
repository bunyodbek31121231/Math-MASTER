package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserByIdOnce(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET xp = xp + :xpDelta, problemsSolved = problemsSolved + 1, correctAnswers = correctAnswers + :isCorrect, incorrectAnswers = incorrectAnswers + :isIncorrect, currentLevel = :newLevel, lastActiveAt = :now WHERE id = :userId")
    suspend fun recordProblemResult(
        userId: String,
        xpDelta: Long,
        isCorrect: Int,
        isIncorrect: Int,
        newLevel: Int,
        now: Long = System.currentTimeMillis()
    )

    @Query("UPDATE users SET xp = xp + :xpDelta, testsCompleted = testsCompleted + 1, currentLevel = :newLevel, lastActiveAt = :now WHERE id = :userId")
    suspend fun recordTestResult(
        userId: String,
        xpDelta: Long,
        newLevel: Int,
        now: Long = System.currentTimeMillis()
    )

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}
