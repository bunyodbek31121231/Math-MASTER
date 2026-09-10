package com.example.generator.generators

import com.example.generator.core.AnswerChoiceGenerator
import com.example.generator.core.ProblemGenerator
import com.example.generator.core.ProblemValidator
import com.example.model.Difficulty
import com.example.model.Problem
import kotlin.random.Random

class AlgebraGenerator(private val random: Random = Random.Default) : ProblemGenerator {

    override val category: String = "Algebra"
    override val generatorId: String = "AlgebraGenerator"
    override val supportedTopics: List<String> = listOf(
        "alg_linear",
        "alg_quadratic",
        "alg_polynomials",
        "alg_powers_roots",
        "alg_sequences"
    )

    override fun generate(difficulty: Difficulty, topicId: String?): Problem? {
        val topic = if (topicId != null && supportedTopics.contains(topicId)) {
            topicId
        } else {
            when (difficulty) {
                Difficulty.EASY -> listOf("alg_linear", "alg_powers_roots").random(random)
                Difficulty.MEDIUM -> listOf("alg_linear", "alg_quadratic", "alg_sequences").random(random)
                Difficulty.HARD -> listOf("alg_quadratic", "alg_polynomials", "alg_sequences").random(random)
                Difficulty.EXPERT -> listOf("alg_quadratic", "alg_polynomials").random(random)
            }
        }

        return when (topic) {
            "alg_linear" -> generateLinear(difficulty)
            "alg_quadratic" -> generateQuadratic(difficulty)
            "alg_polynomials" -> generateFactoringAndPolynomials(difficulty)
            "alg_powers_roots" -> generatePowersAndRoots(difficulty)
            "alg_sequences" -> generateSequences(difficulty)
            else -> generateLinear(difficulty)
        }
    }

    private fun generateLinear(difficulty: Difficulty): Problem? {
        return when (difficulty) {
            Difficulty.EASY -> {
                // ax + b = c, with integers
                val x = random.nextInt(-10, 15).let { if (it == 0) 2 else it }
                val a = random.nextInt(2, 8)
                val b = random.nextInt(-15, 20)
                val c = a * x + b
                val bStr = if (b >= 0) "+ $b" else "- ${-b}"
                val question = "Solve for x in the linear equation:\n$a*x $bStr = $c"
                val correct = "x = $x"
                val distractors = listOf("x = ${x + 1}", "x = ${x - 1}", "x = ${-x}", "x = ${x + 2}")
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                val solution = "Subtract $b from both sides to get $a*x = ${c - b}. Then divide by $a: x = $x."
                createProblem(
                    topicId = "alg_linear",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = solution,
                    explanation = "Isolate the variable term on one side of the equation.",
                    formula = "ax + b = c => x = (c - b) / a",
                    tags = "linear,equations,algebra"
                )
            }
            Difficulty.MEDIUM -> {
                // ax + b = cx + d with a != c
                val x = random.nextInt(-12, 16).let { if (it == 0) 3 else it }
                var a = random.nextInt(3, 9)
                var c = random.nextInt(1, 5)
                if (a == c) a += 2
                val b = random.nextInt(-20, 20)
                val d = (a - c) * x + b
                val bStr = if (b >= 0) "+ $b" else "- ${-b}"
                val dStr = if (d >= 0) "+ $d" else "- ${-d}"
                val question = "Solve for x:\n$a*x $bStr = $c*x $dStr"
                val correct = "x = $x"
                val distractors = listOf("x = ${x + 1}", "x = ${x - 2}", "x = ${-x}", "x = ${x + 3}")
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                val solution = "Subtract $c*x from both sides: ${a - c}*x $bStr = $d. Subtract $b: ${a - c}*x = ${d - b}. Divide by ${a - c}: x = $x."
                createProblem(
                    topicId = "alg_linear",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = solution,
                    explanation = "Collect variable terms on the left side and constant terms on the right side.",
                    formula = "ax + b = cx + d => (a - c)x = d - b",
                    tags = "linear,algebra"
                )
            }
            Difficulty.HARD -> {
                // System of 2 linear equations:
                // a1*x + b1*y = c1
                // a2*x + b2*y = c2
                val x = random.nextInt(-5, 8).let { if (it == 0) 2 else it }
                val y = random.nextInt(-5, 8).let { if (it == 0) -1 else it }
                val a1 = random.nextInt(1, 5)
                val b1 = random.nextInt(1, 4)
                val c1 = a1 * x + b1 * y

                val a2 = random.nextInt(1, 4)
                var b2 = random.nextInt(-4, 4).let { if (it == 0) -2 else it }
                if (a1 * b2 == a2 * b1) b2 += 1 // ensure non-zero determinant
                val c2 = a2 * x + b2 * y

                val b1Str = if (b1 >= 0) "+ ${b1}y" else "- ${-b1}y"
                val b2Str = if (b2 >= 0) "+ ${b2}y" else "- ${-b2}y"
                val question = "Find the ordered pair (x, y) that satisfies the system:\n${a1}x $b1Str = $c1\n${a2}x $b2Str = $c2"
                val correct = "($x, $y)"
                val distractors = listOf("($y, $x)", "(${x + 1}, ${y - 1})", "(${-x}, $y)", "(${x - 1}, ${y + 2})")
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                val solution = "Using substitution or elimination gives determinant D = ${a1 * b2 - a2 * b1}. Solving yields x = $x and y = $y."
                createProblem(
                    topicId = "alg_linear",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = solution,
                    explanation = "Solve the 2x2 system via linear combination or Cramer's rule.",
                    formula = "x = (c1*b2 - c2*b1) / (a1*b2 - a2*b1)",
                    tags = "systems,linear,algebra"
                )
            }
            Difficulty.EXPERT -> {
                // Linear inequality with absolute value: |ax + b| <= c (or >=)
                val a = random.nextInt(2, 5)
                val b = random.nextInt(-8, 8)
                val k = random.nextInt(2, 8)
                val c = a * k
                val bStr = if (b >= 0) "+ $b" else "- ${-b}"
                val question = "Find the interval of all real solutions for the inequality:\n|$a*x $bStr| <= $c"
                val low = (-c - b) / a
                val high = (c - b) / a
                val correct = "[$low, $high]"
                val distractors = listOf(
                    "($low, $high)",
                    "[${low - 1}, ${high + 1}]",
                    "(-infinity, $low] U [$high, infinity)",
                    "[${-high}, ${-low}]"
                )
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                val solution = "-$c <= $a*x $bStr <= $c. Subtract $b: ${-c - b} <= $a*x <= ${c - b}. Divide by $a: $low <= x <= $high."
                createProblem(
                    topicId = "alg_linear",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = solution,
                    explanation = "|u| <= c is equivalent to -c <= u <= c.",
                    formula = "|u| <= c <=> -c <= u <= c",
                    tags = "inequality,absolute_value,algebra"
                )
            }
        }
    }

    private fun generateQuadratic(difficulty: Difficulty): Problem? {
        return when (difficulty) {
            Difficulty.EASY -> {
                // (x - r1)(x - r2) = 0 with small positive integer roots
                val r1 = random.nextInt(1, 6)
                val r2 = random.nextInt(r1 + 1, 9)
                val b = -(r1 + r2)
                val c = r1 * r2
                val bStr = if (b >= 0) "+ $b" else "- ${-b}"
                val question = "Find the roots of the quadratic equation:\nx² $bStr*x + $c = 0"
                val correct = "x = $r1 or x = $r2"
                val distractors = listOf(
                    "x = ${-r1} or x = ${-r2}",
                    "x = ${r1 + 1} or x = ${r2 - 1}",
                    "x = $r1 or x = ${-r2}",
                    "x = ${r1 * 2} or x = $r2"
                )
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                val solution = "Factor the quadratic into (x - $r1)(x - $r2) = 0. Setting each factor to 0 gives x = $r1 and x = $r2."
                createProblem(
                    topicId = "alg_quadratic",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = solution,
                    explanation = "Factor by finding two numbers whose sum is ${-b} and product is $c.",
                    formula = "x² - (r1 + r2)x + r1*r2 = 0",
                    tags = "quadratic,roots,factoring"
                )
            }
            Difficulty.MEDIUM -> {
                // Vieta's formulas: sum of squares of roots r1² + r2² = (r1+r2)² - 2*r1*r2
                val r1 = random.nextInt(1, 7)
                val r2 = random.nextInt(-6, 0)
                val sum = r1 + r2
                val prod = r1 * r2
                val sumSq = r1 * r1 + r2 * r2
                val bStr = if (-sum >= 0) "+ ${-sum}" else "- ${sum}"
                val cStr = if (prod >= 0) "+ $prod" else "- ${-prod}"
                val question = "Let r and s be the roots of x² $bStr*x $cStr = 0. What is the value of r² + s²?"
                val correct = sumSq.toString()
                val distractors = listOf(
                    (sumSq + 2 * kotlin.math.abs(prod)).toString(),
                    (sumSq - 4).toString(),
                    (sum * sum).toString(),
                    (sumSq + 6).toString()
                )
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                val solution = "By Vieta's formulas, r + s = $sum and r*s = $prod. Then r² + s² = (r + s)² - 2rs = ($sum)² - 2($prod) = ${sum * sum} - ${2 * prod} = $sumSq."
                createProblem(
                    topicId = "alg_quadratic",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = solution,
                    explanation = "Express r² + s² algebraically using the fundamental symmetric polynomials.",
                    formula = "r² + s² = (r + s)² - 2rs",
                    tags = "vieta,quadratics,symmetric_polynomials"
                )
            }
            Difficulty.HARD -> {
                // Discriminant condition for equal or real roots
                // 4x² + kx + 9 = 0 has exactly one real solution
                val a = listOf(1, 4, 9).random(random)
                val c = listOf(1, 4, 9, 16).random(random)
                val kAbs = 2 * kotlin.math.sqrt((a * c).toDouble()).toLong()
                val question = "For what positive value of k does the quadratic equation $a*x² + k*x + $c = 0 have exactly one real solution (a double root)?"
                val correct = "k = $kAbs"
                val distractors = listOf(
                    "k = ${kAbs * 2}",
                    "k = ${kAbs / 2}",
                    "k = ${kAbs + 4}",
                    "k = ${kAbs - 2}"
                )
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                val solution = "A quadratic equation has exactly one real solution when its discriminant is zero: D = k² - 4ac = 0. Thus k² = 4($a)($c) = ${4 * a * c}, so k = $kAbs."
                createProblem(
                    topicId = "alg_quadratic",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = solution,
                    explanation = "The discriminant D = b² - 4ac determines root multiplicity.",
                    formula = "D = b² - 4ac = 0",
                    tags = "discriminant,quadratics"
                )
            }
            Difficulty.EXPERT -> {
                // Reciprocal sum of roots or higher symmetric power: (r/s) + (s/r)
                val r = random.nextInt(2, 6)
                val s = random.nextInt(1, 4)
                val sum = r + s
                val prod = r * s
                val num = (r * r + s * s)
                val den = prod
                // simplify fraction
                val g = gcd(num.toLong(), den.toLong()).toInt()
                val fracStr = if (den / g == 1) "${num / g}" else "${num / g}/${den / g}"
                val bStr = if (-sum >= 0) "+ ${-sum}" else "- $sum"
                val question = "If u and v are roots of x² $bStr*x + $prod = 0, evaluate (u/v) + (v/u)."
                val correct = fracStr
                val distractors = listOf(
                    "${(num + g) / g}/${den / g}",
                    "${(num - g).coerceAtLeast(1) / g}/${den / g}",
                    "${sum}/${prod}",
                    "${num * 2 / g}/${den / g}"
                )
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                val solution = "(u/v) + (v/u) = (u² + v²) / (uv) = ((u + v)² - 2uv) / uv = ($sum² - 2($prod)) / $prod = $num / $den = $fracStr."
                createProblem(
                    topicId = "alg_quadratic",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = solution,
                    explanation = "Combine the fractions over the common denominator uv.",
                    formula = "(u/v) + (v/u) = ((u + v)² - 2uv) / uv",
                    tags = "vieta,quadratics,advanced"
                )
            }
        }
    }

    private fun generateFactoringAndPolynomials(difficulty: Difficulty): Problem? {
        val a = random.nextInt(2, 8)
        val b = random.nextInt(2, 8)
        val diffOfSquares = a * a - b * b
        val question = "Evaluate without a calculator: $a²² - $b²²... Specifically: ($a + $b) · ($a - $b) = ?"
        val correct = diffOfSquares.toString()
        val distractors = listOf((diffOfSquares + 2).toString(), (diffOfSquares - 2).toString(), (a * a + b * b).toString(), (diffOfSquares * 2).toString())
        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        val solution = "Using the difference of squares identity: (a + b)(a - b) = a² - b² = ${a * a} - ${b * b} = $diffOfSquares."
        return createProblem(
            topicId = "alg_polynomials",
            difficulty = difficulty,
            question = "Compute the exact value of ($a + $b)($a - $b):",
            choices = choices,
            solution = solution,
            explanation = "Difference of two squares identity.",
            formula = "(a + b)(a - b) = a² - b²",
            tags = "factoring,polynomials,algebra"
        )
    }

    private fun generatePowersAndRoots(difficulty: Difficulty): Problem? {
        val base = listOf(2, 3, 5).random(random)
        val p1 = random.nextInt(2, 5)
        val p2 = random.nextInt(2, 4)
        val totalExp = p1 + p2
        val question = "Simplify the expression: $base^$p1 × $base^$p2"
        val correct = "$base^$totalExp"
        val distractors = listOf("$base^${p1 * p2}", "${base * 2}^$totalExp", "$base^${totalExp - 1}", "$base^${p1 - p2}")
        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        val solution = "When multiplying powers with identical bases, add the exponents: $base^$p1 × $base^$p2 = $base^($p1 + $p2) = $base^$totalExp."
        return createProblem(
            topicId = "alg_powers_roots",
            difficulty = difficulty,
            question = question,
            choices = choices,
            solution = solution,
            explanation = "Product rule for exponents: a^m × a^n = a^(m+n).",
            formula = "a^m × a^n = a^(m+n)",
            tags = "powers,roots,algebra"
        )
    }

    private fun generateSequences(difficulty: Difficulty): Problem? {
        val a1 = random.nextInt(1, 10)
        val d = random.nextInt(2, 6)
        val n = random.nextInt(10, 25)
        val an = a1 + (n - 1) * d
        val question = "In an arithmetic progression with first term a₁ = $a1 and common difference d = $d, find the ${n}th term (a_$n)."
        val correct = an.toString()
        val distractors = listOf((an + d).toString(), (an - d).toString(), (an + 2).toString(), (an * 2).toString())
        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        val solution = "Using the arithmetic sequence nth term formula: a_n = a₁ + (n - 1)d = $a1 + ($n - 1) × $d = $a1 + ${ (n - 1) * d } = $an."
        return createProblem(
            topicId = "alg_sequences",
            difficulty = difficulty,
            question = question,
            choices = choices,
            solution = solution,
            explanation = "Standard formula for the nth term of an arithmetic progression.",
            formula = "a_n = a_1 + (n - 1)d",
            tags = "sequences,progression,algebra"
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
            id = "gen_alg_${hash.take(12)}",
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
}
