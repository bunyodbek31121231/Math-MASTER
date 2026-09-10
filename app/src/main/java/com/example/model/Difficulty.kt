package com.example.model

enum class Difficulty(val displayName: String, val baseXP: Int) {
    EASY("Easy", 10),
    MEDIUM("Medium", 25),
    HARD("Hard", 50),
    EXPERT("Expert", 100);

    companion object {
        fun fromString(value: String): Difficulty {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: EASY
        }
    }
}
