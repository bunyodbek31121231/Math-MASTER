package com.example.generator.generators

import com.example.generator.core.AnswerChoiceGenerator
import com.example.generator.core.ProblemGenerator
import com.example.generator.core.ProblemValidator
import com.example.model.Difficulty
import com.example.model.Problem
import kotlin.random.Random

class OlympiadGenerator(private val random: Random = Random.Default) : ProblemGenerator {

    override val category: String = "Olympiad"
    override val generatorId: String = "OlympiadGenerator"
    override val supportedTopics: List<String> = listOf("olympiad_inequalities")

    override fun generate(difficulty: Difficulty, topicId: String?): Problem? {
        val type = listOf("am_gm", "cauchy_schwarz", "polynomial_roots", "integer_extremum").random(random)
        return when (type) {
            "am_gm" -> generateAmGm(difficulty)
            "cauchy_schwarz" -> generateCauchySchwarz(difficulty)
            "polynomial_roots" -> generatePolynomialRoots(difficulty)
            "integer_extremum" -> generateIntegerExtremum(difficulty)
            else -> generateAmGm(difficulty)
        }
    }

    private fun generateAmGm(difficulty: Difficulty): Problem? {
        // e.g. For positive real numbers x, y such that x + y = S, what is the maximum value of x * y?
        val s = random.nextInt(4, 20).let { if (it % 2 != 0) it + 1 else it }
        val maxProduct = (s / 2) * (s / 2)
        val question = "If x and y are positive real numbers such that x + y = $s, find the maximum possible value of their product x·y."
        val correct = "$maxProduct"
        val distractors = listOf(
            "${maxProduct - 1}",
            "${maxProduct + s}",
            "${s * 2}",
            "${(s * s)}"
        ).filter { it != correct }.distinct().take(3)

        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        return createProblem(
            topicId = "olympiad_inequalities",
            difficulty = difficulty,
            question = question,
            choices = choices,
            solution = "By the AM-GM Inequality: (x + y)/2 >= √(x·y) => √(x·y) <= $s/2 = ${s/2}. Squaring both sides yields x·y <= ${(s/2) * (s/2)} = $maxProduct, with equality when x = y = ${s/2}.",
            explanation = "AM-GM inequality states arithmetic mean is always at least geometric mean for non-negative reals.",
            formula = "(a + b)/2 >= √(ab)",
            tags = "olympiad,am_gm,inequalities"
        )
    }

    private fun generateCauchySchwarz(difficulty: Difficulty): Problem? {
        // e.g. Min value of a² + b² given a + 2b = c
        val c = random.nextInt(5, 15)
        // (1*a + 2*b)² <= (1² + 2²)(a² + b²) => c² <= 5(a² + b²) => a² + b² >= c²/5
        // Let's use clean numbers: x + y = s, min x² + y² = s²/2
        val s = random.nextInt(4, 16).let { if (it % 2 != 0) it + 1 else it }
        val minSum = (s * s) / 2
        val question = "If real numbers a and b satisfy a + b = $s, what is the minimum value of a² + b²?"
        val correct = "$minSum"
        val distractors = listOf(
            "${s * s}",
            "${minSum - 2}",
            "${minSum + 4}",
            "${s * 2}"
        ).filter { it != correct }.distinct().take(3)

        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        return createProblem(
            topicId = "olympiad_inequalities",
            difficulty = difficulty,
            question = question,
            choices = choices,
            solution = "By Cauchy-Schwarz Inequality: (1² + 1²)(a² + b²) >= (1·a + 1·b)² => 2(a² + b²) >= $s² = ${s*s}. Thus a² + b² >= $minSum, with equality when a = b = ${s/2}.",
            explanation = "Cauchy-Schwarz inequality: (Σ u_i²)(Σ v_i²) >= (Σ u_i v_i)².",
            formula = "(Σ a_i²)(Σ b_i²) >= (Σ a_i b_i)²",
            tags = "olympiad,cauchy_schwarz,extrema"
        )
    }

    private fun generatePolynomialRoots(difficulty: Difficulty): Problem? {
        // Vieta's formulas for cubic: x³ - (r1+r2+r3)x² + ...
        val r1 = random.nextInt(1, 5)
        val r2 = random.nextInt(1, 5)
        val r3 = random.nextInt(1, 5)
        val sum = r1 + r2 + r3
        val prod = r1 * r2 * r3
        val pairSum = r1 * r2 + r2 * r3 + r3 * r1

        val question = "Let r₁, r₂, r₃ be the roots of the cubic polynomial P(x) = x³ - ${sum}x² + ${pairSum}x - $prod = 0. Find the exact value of r₁² + r₂² + r₃²."
        val ans = sum * sum - 2 * pairSum
        val correct = "$ans"
        val distractors = listOf(
            "${sum * sum}",
            "${ans + 2}",
            "${ans - 4}",
            "${pairSum * 2}"
        ).filter { it != correct }.distinct().take(3)

        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        return createProblem(
            topicId = "olympiad_inequalities",
            difficulty = difficulty,
            question = question,
            choices = choices,
            solution = "By algebraic expansion: (r₁ + r₂ + r₃)² = r₁² + r₂² + r₃² + 2(r₁r₂ + r₂r₃ + r₃r₁). By Vieta's formulas, r₁ + r₂ + r₃ = $sum and r₁r₂ + r₂r₃ + r₃r₁ = $pairSum. Therefore, r₁² + r₂² + r₃² = $sum² - 2($pairSum) = ${sum * sum} - ${2 * pairSum} = $ans.",
            explanation = "Vieta's formulas relating polynomial coefficients to symmetric root polynomials.",
            formula = "Σ r_i² = (Σ r_i)² - 2(Σ r_i r_j)",
            tags = "olympiad,vieta,polynomials"
        )
    }

    private fun generateIntegerExtremum(difficulty: Difficulty): Problem? {
        // e.g. Number of integer pairs (x, y) satisfying 1/x + 1/y = 1/p for prime p
        val p = listOf(2, 3, 5, 7).random(random)
        // (x - p)(y - p) = p² => number of divisors of p² is 3 positive, so total integer pairs = 2*3 - 1 = 5? Or positive integers: 3
        val question = "How many ordered pairs of positive integers (x, y) satisfy the equation: 1/x + 1/y = 1/$p?"
        val correct = "3"
        val distractors = listOf("1", "2", "4")
        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        return createProblem(
            topicId = "olympiad_inequalities",
            difficulty = difficulty,
            question = question,
            choices = choices,
            solution = "Rearranging 1/x + 1/y = 1/$p yields xy - $p·x - $p·y = 0 => (x - $p)(y - $p) = $p² = ${p * p}. Since $p is prime, $p² has exactly 3 positive divisors: 1, $p, and $p². Each divisor d corresponds to an ordered positive pair (x, y) = ($p + d, $p + $p²/d). Thus there are exactly 3 pairs.",
            explanation = "Simon's Favorite Factoring Trick applied to Diophantine reciprocals.",
            formula = "(x - p)(y - p) = p²",
            tags = "olympiad,diophantine,algebra"
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
            id = "gen_oly_${hash.take(12)}",
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
