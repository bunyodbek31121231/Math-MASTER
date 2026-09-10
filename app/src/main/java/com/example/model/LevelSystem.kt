package com.example.model

object LevelSystem {
    // Configurable formula: XP required for level N:
    // Level 1: 0 to 99 XP
    // Level 2: 100 to 299 XP
    // Level N: 100 * N * (N - 1) / 2
    fun getLevelForXp(xp: Long): Int {
        if (xp <= 0) return 1
        var level = 1
        while (getXpRequiredForLevel(level + 1) <= xp) {
            level++
        }
        return level
    }

    fun getXpRequiredForLevel(level: Int): Long {
        if (level <= 1) return 0L
        return (50L * (level - 1) * level)
    }

    fun getXpProgressInCurrentLevel(xp: Long): Pair<Long, Long> {
        val currentLevel = getLevelForXp(xp)
        val currentLevelBaseXp = getXpRequiredForLevel(currentLevel)
        val nextLevelXp = getXpRequiredForLevel(currentLevel + 1)
        val xpInLevel = (xp - currentLevelBaseXp).coerceAtLeast(0L)
        val xpSpan = (nextLevelXp - currentLevelBaseXp).coerceAtLeast(1L)
        return Pair(xpInLevel, xpSpan)
    }

    fun getTitleForLevel(level: Int): String {
        return when {
            level < 3 -> "Novice Mathematician"
            level < 6 -> "Diligent Student"
            level < 10 -> "Problem Solver"
            level < 15 -> "Analytical Thinker"
            level < 20 -> "Master of Proofs"
            level < 30 -> "Grand Calculator"
            else -> "Math MASTER"
        }
    }

    const val PRACTICE_BONUS_XP = 15
    const val TEST_COMPLETION_BASE_XP = 50
}
