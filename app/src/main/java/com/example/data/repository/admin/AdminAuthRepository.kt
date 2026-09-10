package com.example.data.repository.admin

import java.security.MessageDigest

interface AdminAuthRepository {
    suspend fun login(username: String, password: String): Result<Boolean>
    fun isLoggedIn(): Boolean
    fun logout()
}

class AdminAuthRepositoryImpl : AdminAuthRepository {
    // Hardcoded for now as requested, but with a structure ready for backend
    private val ADMIN_USERNAME = "sapayev1231"
    // SHA-256 hash of "sapayev3112"
    private val ADMIN_PASSWORD_HASH = "80f1e8a834468f7663e26f1947b9605d8f28469e6b4e0586e9297693d258416d"

    private var loggedIn = false

    override suspend fun login(username: String, password: String): Result<Boolean> {
        val hashedInput = hashPassword(password)
        return if (username == ADMIN_USERNAME && hashedInput == ADMIN_PASSWORD_HASH) {
            loggedIn = true
            Result.success(true)
        } else {
            Result.failure(Exception("Noto'g'ri login yoki parol"))
        }
    }

    override fun isLoggedIn(): Boolean = loggedIn

    override fun logout() {
        loggedIn = false
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
