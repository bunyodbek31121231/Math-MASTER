package com.example.generator.generators

import com.example.generator.core.AnswerChoiceGenerator
import com.example.generator.core.ProblemGenerator
import com.example.generator.core.ProblemValidator
import com.example.model.Difficulty
import com.example.model.Problem
import kotlin.random.Random

class ProbabilityGenerator(private val random: Random = Random.Default) : ProblemGenerator {

    override val category: String = "Probability"
    override val generatorId: String = "ProbabilityGenerator"
    override val supportedTopics: List<String> = listOf("prob_classical", "prob_independent")

    override fun generate(difficulty: Difficulty, topicId: String?): Problem? {
        val topic = topicId ?: "prob_classical"
        return when (topic) {
            "prob_classical" -> generateClassical(difficulty)
            "prob_independent" -> generateIndependent(difficulty)
            else -> generateClassical(difficulty)
        }
    }

    private fun generateClassical(difficulty: Difficulty): Problem? {
        val scenarios = listOf("dice", "coins", "marbles")
        val scenario = scenarios.random(random)

        return when (scenario) {
            "dice" -> {
                // Single die or two dice sum
                if (difficulty == Difficulty.EASY) {
                    val question = "A standard fair 6-sided die is rolled once. What is the probability of rolling an even number?"
                    val correct = "1/2"
                    val distractors = listOf("1/3", "1/6", "2/3")
                    val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                    createProblem(
                        topicId = "prob_classical",
                        difficulty = difficulty,
                        question = question,
                        choices = choices,
                        solution = "Even numbers on a die are {2, 4, 6} (3 favorable outcomes). Total outcomes = 6. Probability = 3/6 = 1/2.",
                        explanation = "Classical probability definition: P(E) = favorable / total.",
                        formula = "P(E) = n(E) / n(S)",
                        tags = "probability,dice"
                    )
                } else {
                    // Two dice sum
                    val targetSum = listOf(7, 8, 9, 10).random(random)
                    val favorable = when (targetSum) {
                        7 -> 6
                        8 -> 5
                        9 -> 4
                        10 -> 3
                        else -> 6
                    }
                    val g = gcd(favorable.toLong(), 36L).toInt()
                    val fracStr = "${favorable / g}/${36 / g}"
                    val question = "Two standard fair 6-sided dice are rolled simultaneously. What is the probability that the sum of the two numbers equals $targetSum?"
                    val correct = fracStr
                    val distractors = listOf(
                        "${(favorable + 1) / g}/${36 / g}",
                        "${(favorable - 1).coerceAtLeast(1) / g}/${36 / g}",
                        "1/6",
                        "1/4"
                    ).filter { it != fracStr }.distinct().take(3)

                    val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                    createProblem(
                        topicId = "prob_classical",
                        difficulty = difficulty,
                        question = question,
                        choices = choices,
                        solution = "There are 36 total outcomes. There are $favorable pairs yielding a sum of $targetSum. Probability = $favorable/36 = $fracStr.",
                        explanation = "Sample space size for two 6-sided dice is 6 × 6 = 36.",
                        formula = "P(Sum = $targetSum) = favorable / 36",
                        tags = "probability,dice,sums"
                    )
                }
            }
            "coins" -> {
                // Flipping n fair coins
                val n = random.nextInt(2, 4)
                val totalOutcomes = 1 shl n
                val question = "A fair coin is tossed $n times in a row. What is the probability of getting heads on all $n tosses?"
                val correct = "1/$totalOutcomes"
                val distractors = listOf("1/${totalOutcomes / 2}", "1/${totalOutcomes * 2}", "1/2")
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                createProblem(
                    topicId = "prob_classical",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = "Each toss has independent probability 1/2. For $n consecutive heads: (1/2)^$n = 1/$totalOutcomes.",
                    explanation = "Multiplication rule for independent events.",
                    formula = "P(A ∩ B) = P(A) · P(B)",
                    tags = "probability,coins"
                )
            }
            else -> {
                // Marbles in an urn
                val red = random.nextInt(3, 8)
                val blue = random.nextInt(4, 9)
                val total = red + blue
                val g = gcd(red.toLong(), total.toLong()).toInt()
                val fracStr = "${red / g}/${total / g}"
                val question = "A bag contains $red red marbles and $blue blue marbles. If one marble is drawn at random, what is the probability that it is red?"
                val correct = fracStr
                val distractors = listOf(
                    "${blue / g}/${total / g}",
                    "${(red + 1) / g}/${total / g}",
                    "1/2",
                    "${red}/${total + 2}"
                ).filter { it != fracStr }.distinct().take(3)
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                createProblem(
                    topicId = "prob_classical",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = "Total marbles = $red + $blue = $total. Probability of drawing a red marble = $red / $total = $fracStr.",
                    explanation = "Direct ratio of favorable outcomes to total sample space.",
                    formula = "P(Red) = Red / Total",
                    tags = "probability,urn"
                )
            }
        }
    }

    private fun generateIndependent(difficulty: Difficulty): Problem? {
        val pA = 0.4
        val pB = 0.5
        val pBoth = 0.20
        val question = "If events A and B are independent with P(A) = 0.40 and P(B) = 0.50, find the joint probability P(A ∩ B)."
        val correct = "0.20"
        val distractors = listOf("0.90", "0.10", "0.70")
        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        return createProblem(
            topicId = "prob_independent",
            difficulty = difficulty,
            question = question,
            choices = choices,
            solution = "For independent events: P(A ∩ B) = P(A) · P(B) = 0.40 × 0.50 = 0.20.",
            explanation = "Definition of independent probability multiplication.",
            formula = "P(A ∩ B) = P(A)P(B)",
            tags = "probability,independence"
        )
    }

    private fun gcd(a: Long, b: Long): Long {
        var x = kotlin.math.abs(a)
        var y = kotlin.math.abs(b)
        while (y != 0L) {
            val t = y
            y = x % y
            x = t
        }
        return x
    }

    private fun createProblem(
        topicId: String,
        difficulty: Difficulty,
        question: String,
        choices: com.example.generator.core.ChoicesResult,
        solution: String,
        explanation: String,
        formula: String,
        tags: String
    ): Problem {
        val hash = ProblemValidator.computeHash(question, choices.optionA, choices.optionB, choices.optionC, choices.optionD)
        return Problem(
            id = "gen_prob_${hash.take(12)}",
            category = category,
            topicId = topicId,
            difficulty = difficulty.name,
            question = question,
            optionA = choices.optionA,
            optionB = choices.optionB,
            optionC = choices.optionC,
            optionD = choices.optionD,
            correctAnswer = choices.correctAnswerLetter,
            solution = solution,
            explanation = explanation,
            formula = formula,
            tags = tags,
            generator = generatorId,
            questionHash = hash
        )
    }
}
