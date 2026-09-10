package com.example.security

interface IMaxsusAuthenticator {
    suspend fun verifyGateCode(code: String): Boolean
}

class LocalMaxsusAuthenticator : IMaxsusAuthenticator {
    // Hashed locally to avoid plain-text storage
    // Code "103202" hashed with SHA-256
    private val hashedExpectedCode = "c3aad334ea3ad17e2b6d86c99cdd0ed9245082c538cd80c06e35eba27eb5c40a"

    override suspend fun verifyGateCode(code: String): Boolean {
        val hashed = java.security.MessageDigest.getInstance("SHA-256")
            .digest(code.trim().toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        return hashed == hashedExpectedCode
    }
}

object MaxsusGate {
    private var authenticator: IMaxsusAuthenticator = LocalMaxsusAuthenticator()

    fun setAuthenticator(custom: IMaxsusAuthenticator) {
        authenticator = custom
    }

    suspend fun verify(code: String): Boolean {
        return authenticator.verifyGateCode(code)
    }
}
