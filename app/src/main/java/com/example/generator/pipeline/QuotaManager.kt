package com.example.generator.pipeline

data class CategoryQuota(
    val category: String,
    val targetPercentage: Double, // e.g. 0.18 for 18%
    val minimumTarget: Int = 0
)

data class DistributionProfile(
    val profileName: String,
    val quotas: List<CategoryQuota>
)

object QuotaManager {

    /**
     * Standard balanced distribution targeting the 20,620+ problem milestone.
     * Guaranteed distribution ensures every core math branch has significant breadth.
     */
    val DEFAULT_BALANCED_DISTRIBUTION = listOf(
        CategoryQuota("Algebra", 0.18, 3700),
        CategoryQuota("Trigonometry", 0.12, 2400),
        CategoryQuota("Geometry", 0.14, 2900),
        CategoryQuota("Number Theory", 0.11, 2300),
        CategoryQuota("Probability", 0.09, 1850),
        CategoryQuota("Combinatorics", 0.09, 1850),
        CategoryQuota("Statistics", 0.08, 1650),
        CategoryQuota("Calculus", 0.11, 2300),
        CategoryQuota("Logic", 0.05, 1050),
        CategoryQuota("Olympiad", 0.02, 420),
        CategoryQuota("Mixed", 0.01, 200)
    )

    /**
     * Calculates the remaining quota needed for each category based on current database counts.
     */
    fun computeDeficit(
        currentCounts: Map<String, Int>,
        targetTotal: Int,
        quotas: List<CategoryQuota> = DEFAULT_BALANCED_DISTRIBUTION
    ): Map<String, Int> {
        val deficits = mutableMapOf<String, Int>()
        for (quota in quotas) {
            val targetForCategory = (targetTotal * quota.targetPercentage).toInt().coerceAtLeast(quota.minimumTarget)
            val current = currentCounts[quota.category] ?: 0
            val needed = (targetForCategory - current).coerceAtLeast(0)
            deficits[quota.category] = needed
        }
        return deficits
    }

    /**
     * Selects the next best category to generate, prioritizing categories with the highest deficit.
     */
    fun selectCategoryByDeficit(
        deficits: Map<String, Int>,
        availableCategories: List<String>,
        random: kotlin.random.Random = kotlin.random.Random.Default
    ): String {
        val availableDeficits = deficits.filterKeys { availableCategories.contains(it) }
        val positiveDeficits = availableDeficits.filterValues { it > 0 }

        if (positiveDeficits.isNotEmpty()) {
            // Weighted selection according to needed count
            val totalWeight = positiveDeficits.values.sum()
            if (totalWeight > 0) {
                var pick = random.nextInt(totalWeight)
                for ((cat, weight) in positiveDeficits) {
                    pick -= weight
                    if (pick < 0) return cat
                }
            }
            return positiveDeficits.keys.random(random)
        }

        // If all quotas satisfied or none found, pick uniformly from available
        return availableCategories.random(random)
    }
}
