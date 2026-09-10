package com.example.generator.generators

import com.example.generator.core.AnswerChoiceGenerator
import com.example.generator.core.ProblemGenerator
import com.example.generator.core.ProblemValidator
import com.example.model.Difficulty
import com.example.model.Problem
import kotlin.random.Random

class MixedGenerator(
    private val subGenerators: List<ProblemGenerator>,
    private val random: Random = Random.Default
) : ProblemGenerator {

    override val category: String = "Mixed"
    override val generatorId: String = "MixedGenerator"
    override val supportedTopics: List<String> = listOf("mixed_comprehensive")

    override fun generate(difficulty: Difficulty, topicId: String?): Problem? {
        if (subGenerators.isEmpty()) return null
        val chosenGenerator = subGenerators.random(random)
        val candidate = chosenGenerator.generate(difficulty) ?: return null

        // Rebrand under Mixed category for comprehensive cross-disciplinary test coverage
        val mixedId = "gen_mix_${candidate.questionHash.take(12)}"
        return candidate.copy(
            id = mixedId,
            category = "Mixed",
            topicId = "mixed_comprehensive",
            generator = generatorId,
            tags = "${candidate.tags},mixed"
        )
    }
}
