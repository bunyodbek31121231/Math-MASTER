package com.example.data.repository

import com.example.data.database.dao.UserDao
import com.example.data.database.entity.UserEntity
import com.example.data.preferences.UserPreferences
import com.example.model.LevelSystem
import com.example.security.PasswordHasher
import kotlinx.coroutines.flow.Flow
import java.util.UUID

sealed class AuthResult {
    data class Success(val user: UserEntity) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class UserRepository(
    private val userDao: UserDao,
    private val userPreferences: UserPreferences
) {
    val activeUserId: Flow<String?> = userPreferences.activeUserId

    fun getUser(userId: String): Flow<UserEntity?> {
        return userDao.getUserById(userId)
    }

    suspend fun getUserOnce(userId: String): UserEntity? {
        return userDao.getUserByIdOnce(userId)
    }

    suspend fun register(
        name: String,
        username: String,
        password: String,
        confirmPassword: String
    ): AuthResult {
        val trimmedName = name.trim()
        val trimmedUsername = username.trim().lowercase()
        val trimmedPassword = password.trim()

        if (trimmedName.isEmpty()) {
            return AuthResult.Error("Name cannot be empty.")
        }
        if (trimmedUsername.length < 3) {
            return AuthResult.Error("Username must be at least 3 characters.")
        }
        if (trimmedPassword.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters.")
        }
        if (trimmedPassword != confirmPassword.trim()) {
            return AuthResult.Error("Passwords do not match.")
        }

        val existing = userDao.getUserByUsername(trimmedUsername)
        if (existing != null) {
            return AuthResult.Error("Username '$trimmedUsername' is already taken.")
        }

        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hashPassword(trimmedPassword, salt)
        val newId = UUID.randomUUID().toString()

        val newUser = UserEntity(
            id = newId,
            name = trimmedName,
            username = trimmedUsername,
            passwordHash = hash,
            passwordSalt = salt,
            currentLevel = 1,
            xp = 0L,
            problemsSolved = 0,
            correctAnswers = 0,
            incorrectAnswers = 0,
            testsCompleted = 0,
            averageScore = 0.0
        )

        userDao.insertUser(newUser)
        userPreferences.setActiveUserId(newId)
        return AuthResult.Success(newUser)
    }

    suspend fun login(username: String, password: String): AuthResult {
        val trimmedUsername = username.trim().lowercase()
        val trimmedPassword = password.trim()

        if (trimmedUsername.isEmpty() || trimmedPassword.isEmpty()) {
            return AuthResult.Error("Please enter both username and password.")
        }

        val user = userDao.getUserByUsername(trimmedUsername)
            ?: return AuthResult.Error("No user found with username '$trimmedUsername'.")

        val isValid = PasswordHasher.verifyPassword(trimmedPassword, user.passwordSalt, user.passwordHash)
        if (!isValid) {
            return AuthResult.Error("Incorrect password. Please try again.")
        }

        userPreferences.setActiveUserId(user.id)
        return AuthResult.Success(user)
    }

    suspend fun logout() {
        userPreferences.setActiveUserId(null)
    }

    suspend fun addXpAndRecordProblem(
        userId: String,
        xpGained: Long,
        isCorrect: Boolean
    ) {
        val user = userDao.getUserByIdOnce(userId) ?: return
        val newXp = user.xp + xpGained
        val newLevel = LevelSystem.getLevelForXp(newXp)
        userDao.recordProblemResult(
            userId = userId,
            xpDelta = xpGained,
            isCorrect = if (isCorrect) 1 else 0,
            isIncorrect = if (!isCorrect) 1 else 0,
            newLevel = newLevel
        )
    }

    suspend fun addXpAndRecordTest(
        userId: String,
        xpGained: Long,
        scorePercentage: Double
    ) {
        val user = userDao.getUserByIdOnce(userId) ?: return
        val newXp = user.xp + xpGained
        val newLevel = LevelSystem.getLevelForXp(newXp)
        val newTestsCount = user.testsCompleted + 1
        val newAvg = ((user.averageScore * user.testsCompleted) + scorePercentage) / newTestsCount

        val updatedUser = user.copy(
            xp = newXp,
            currentLevel = newLevel,
            testsCompleted = newTestsCount,
            averageScore = newAvg,
            lastActiveAt = System.currentTimeMillis()
        )
        userDao.updateUser(updatedUser)
    }
}
