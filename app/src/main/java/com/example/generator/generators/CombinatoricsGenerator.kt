package com.example.generator.generators

import com.example.generator.core.AnswerChoiceGenerator
import com.example.generator.core.ProblemGenerator
import com.example.generator.core.ProblemValidator
import com.example.model.Difficulty
import com.example.model.Problem
import kotlin.random.Random

class CombinatoricsGenerator(private val random: Random = Random.Default) : ProblemGenerator {

    override val category: String = "Combinatorics"
    override val generatorId: String = "CombinatoricsGenerator"
    override val supportedTopics: List<String> = listOf("comb_permutations", "comb_combinations")

    override fun generate(difficulty: Difficulty, topicId: String?): Problem? {
        val topic = topicId ?: if (random.nextBoolean()) "comb_permutations" else "comb_combinations"
        return when (topic) {
            "comb_permutations" -> generatePermutations(difficulty)
            "comb_combinations" -> generateCombinations(difficulty)
            else -> generateCombinations(difficulty)
        }
    }

    private fun generatePermutations(difficulty: Difficulty): Problem? {
        return when (difficulty) {
            Difficulty.EASY -> {
                // n! basic factorial
                val n = random.nextInt(4, 7)
                val fact = factorial(n)
                val question = "In how many distinct ways can $n distinct books be arranged in a line on a single shelf?"
                val correct = fact.toString()
                val distractors = listOf((fact / 2).toString(), (fact * 2).toString(), factorial(n - 1).toString())
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                createProblem(
                    topicId = "comb_permutations",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = "The number of permutations of $n distinct items is $n! = $fact.",
                    explanation = "Factorial counting principle for linear arrangements.",
                    formula = "P(n) = n!",
                    tags = "combinatorics,factorials,permutations"
                )
            }
            else -> {
                // P(n, k)
                val n = random.nextInt(5, 8)
                val k = random.nextInt(2, 4)
                val p = perm(n, k)
                val question = "Compute the number of permutations P($n, $k) of $n items taken $k at a time."
                val correct = p.toString()
                val distractors = listOf(comb(n, k).toString(), (p / 2).toString(), (p + n).toString())
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                createProblem(
                    topicId = "comb_permutations",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = "P(n, k) = n! / (n - k)! = $n! / ${n - k}! = $p.",
                    explanation = "Ordered selection without replacement.",
                    formula = "P(n, k) = n! / (n - k)!",
                    tags = "combinatorics,permutations"
                )
            }
        }
    }

    private fun generateCombinations(difficulty: Difficulty): Problem? {
        val n = random.nextInt(5, 10)
        val k = random.nextInt(2, 4)
        val c = comb(n, k)
        val question = "A committee of $k members is to be chosen from a group of $n candidates. How many distinct committees can be formed?"
        val correct = c.toString()
        val distractors = listOf(perm(n, k).toString(), (c * 2).toString(), (c - 2).coerceAtLeast(1).toString(), (c + 5).toString())
        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        return createProblem(
            topicId = "comb_combinations",
            difficulty = difficulty,
            question = question,
            choices = choices,
            solution = "Number of combinations C($n, $k) = $n! / ($k! × ($n - $k)!) = $c.",
            explanation = "Unordered selection of k items from n available items.",
            formula = "C(n, k) = n! / (k!(n-k)!)",
            tags = "combinatorics,combinations,binomial"
        )
    }

    private fun factorial(n: Int): Long {
        var res = 1L
        for (i in 2..n) res *= i
        return res
    }

    private fun perm(n: Int, k: Int): Long {
        var res = 1L
        for (i in 0 until k) res *= (n - i)
        return res
    }

    private fun comb(n: Int, k: Int): Long {
        val kClamped = if (k > n - k) n - k else k
        var num = 1L
        var den = 1L
        for (i in 1..kClamped) {
            num *= (n - i + 1)
            den *= i
        }
        return num / den
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
            id = "gen_comb_${hash.take(12)}",
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
