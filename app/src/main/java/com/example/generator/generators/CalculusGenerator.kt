package com.example.generator.generators

import com.example.generator.core.AnswerChoiceGenerator
import com.example.generator.core.ProblemGenerator
import com.example.generator.core.ProblemValidator
import com.example.model.Difficulty
import com.example.model.Problem
import kotlin.random.Random

class CalculusGenerator(private val random: Random = Random.Default) : ProblemGenerator {

    override val category: String = "Calculus"
    override val generatorId: String = "CalculusGenerator"
    override val supportedTopics: List<String> = listOf("calc_derivatives", "calc_integrals")

    override fun generate(difficulty: Difficulty, topicId: String?): Problem? {
        val topic = topicId ?: if (random.nextBoolean()) "calc_derivatives" else "calc_integrals"
        return when (topic) {
            "calc_derivatives" -> generateDerivatives(difficulty)
            "calc_integrals" -> generateIntegrals(difficulty)
            else -> generateDerivatives(difficulty)
        }
    }

    private fun generateDerivatives(difficulty: Difficulty): Problem? {
        return when (difficulty) {
            Difficulty.EASY -> {
                // Power rule: d/dx (a * x^n)
                val a = random.nextInt(2, 6)
                val n = random.nextInt(2, 5)
                val coeff = a * n
                val exp = n - 1
                val expStr = if (exp == 1) "x" else "x^$exp"
                val question = "Find the derivative of f(x) = $a·x^$n with respect to x."
                val correct = "$coeff$expStr"
                val distractors = listOf(
                    "$a$expStr",
                    "${coeff + 1}$expStr",
                    "$coeff x^$n",
                    "${a * (n + 1)}x^$n"
                )
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                createProblem(
                    topicId = "calc_derivatives",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = "Using the power rule: d/dx (a · x^n) = a · n · x^(n - 1) = $a × $n · x^$exp = $coeff$expStr.",
                    explanation = "Power rule for differentiation.",
                    formula = "d/dx (x^n) = n · x^(n-1)",
                    tags = "calculus,derivatives,power_rule"
                )
            }
            else -> {
                // Slope of tangent line at x = x0
                // f(x) = a*x² + b*x + c at x = x0
                val a = random.nextInt(1, 4)
                val b = random.nextInt(-4, 5)
                val x0 = random.nextInt(1, 4)
                val slope = 2 * a * x0 + b
                val bStr = if (b >= 0) "+ $b*x" else "- ${-b}*x"
                val question = "Find the slope of the tangent line to f(x) = $a*x² $bStr + 5 at the point where x = $x0."
                val correct = slope.toString()
                val distractors = listOf((slope + 2).toString(), (slope - 2).toString(), (slope * 2).toString(), (a * x0 + b).toString())
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                createProblem(
                    topicId = "calc_derivatives",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = "The derivative is f'(x) = ${2 * a}x $bStr. Evaluating at x = $x0 gives f'($x0) = ${2 * a}($x0) + ($b) = $slope.",
                    explanation = "The slope of the tangent line equals the derivative evaluated at the given point.",
                    formula = "m = f'(x_0)",
                    tags = "calculus,tangent,slope"
                )
            }
        }
    }

    private fun generateIntegrals(difficulty: Difficulty): Problem? {
        // Definite integral: ∫[0 to b] k*x dx = (k/2)*b²
        val k = listOf(2, 4, 6).random(random)
        val upper = random.nextInt(2, 5)
        val ans = (k / 2) * upper * upper
        val question = "Evaluate the definite integral:\n∫ from 0 to $upper of ($k·x) dx"
        val correct = ans.toString()
        val distractors = listOf((ans * 2).toString(), (ans + upper).toString(), (ans - 2).coerceAtLeast(1).toString(), (k * upper).toString())
        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        return createProblem(
            topicId = "calc_integrals",
            difficulty = difficulty,
            question = question,
            choices = choices,
            solution = "Antiderivative of $k·x is (${k}/2)·x² = ${k/2}·x². Evaluating from 0 to $upper: ${k/2}($upper²) - 0 = ${k/2}($ {upper * upper}) = $ans.",
            explanation = "Fundamental Theorem of Calculus applied to power function.",
            formula = "∫ x dx = (x²)/2 + C",
            tags = "calculus,integrals,definite_integral"
        )
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
            id = "gen_calc_${hash.take(12)}",
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
