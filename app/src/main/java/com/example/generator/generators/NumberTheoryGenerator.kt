package com.example.generator.generators

import com.example.generator.core.AnswerChoiceGenerator
import com.example.generator.core.ProblemGenerator
import com.example.generator.core.ProblemValidator
import com.example.model.Difficulty
import com.example.model.Problem
import kotlin.random.Random

class NumberTheoryGenerator(private val random: Random = Random.Default) : ProblemGenerator {

    override val category: String = "Number Theory"
    override val generatorId: String = "NumberTheoryGenerator"
    override val supportedTopics: List<String> = listOf(
        "num_divisibility",
        "num_primes",
        "num_gcd_lcm",
        "num_modular"
    )

    override fun generate(difficulty: Difficulty, topicId: String?): Problem? {
        val topic = if (topicId != null && supportedTopics.contains(topicId)) {
            topicId
        } else {
            when (difficulty) {
                Difficulty.EASY -> listOf("num_divisibility", "num_primes").random(random)
                Difficulty.MEDIUM -> listOf("num_gcd_lcm", "num_modular").random(random)
                Difficulty.HARD -> listOf("num_modular", "num_gcd_lcm").random(random)
                Difficulty.EXPERT -> "num_modular"
            }
        }

        return when (topic) {
            "num_divisibility" -> generateDivisibility(difficulty)
            "num_primes" -> generatePrimes(difficulty)
            "num_gcd_lcm" -> generateGcdLcm(difficulty)
            "num_modular" -> generateModular(difficulty)
            else -> generateDivisibility(difficulty)
        }
    }

    private fun generateDivisibility(difficulty: Difficulty): Problem? {
        val divisor = listOf(3, 4, 9, 11).random(random)
        val n = random.nextInt(100, 999)
        val remainder = n % divisor
        val question = "What is the remainder when the integer $n is divided by $divisor?"
        val correct = remainder.toString()
        val distractors = mutableListOf<String>()
        for (cand in 0 until divisor) {
            if (cand != remainder) {
                distractors.add(cand.toString())
            }
        }
        while (distractors.size < 3) {
            distractors.add((divisor + distractors.size).toString())
        }
        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        return createProblem(
            topicId = "num_divisibility",
            difficulty = difficulty,
            question = question,
            choices = choices,
            solution = "$n ÷ $divisor = ${n / divisor} with a remainder of $remainder (since $divisor × ${n / divisor} + $remainder = $n).",
            explanation = "Division algorithm: a = bq + r with 0 <= r < b.",
            formula = "a = bq + r",
            tags = "number_theory,divisibility,remainders"
        )
    }

    private fun generatePrimes(difficulty: Difficulty): Problem? {
        val primes = listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47)
        val targetPrime = primes.filter { it > 10 }.random(random)
        val composites = listOf(21, 27, 33, 35, 39, 45, 49, 51, 57).shuffled(random).take(3)

        val question = "Which of the following integers is a prime number?"
        val correct = targetPrime.toString()
        val distractors = composites.map { it.toString() }
        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        return createProblem(
            topicId = "num_primes",
            difficulty = difficulty,
            question = question,
            choices = choices,
            solution = "$targetPrime has only two positive divisors: 1 and $targetPrime itself. The other choices are composite.",
            explanation = "A prime number is greater than 1 with no positive divisors other than 1 and itself.",
            formula = "p is prime iff Div(p) = {1, p}",
            tags = "number_theory,primes"
        )
    }

    private fun generateGcdLcm(difficulty: Difficulty): Problem? {
        val isGcd = random.nextBoolean()
        val g = random.nextInt(2, 8)
        val aMult = random.nextInt(2, 6)
        var bMult = random.nextInt(2, 6)
        if (gcd(aMult.toLong(), bMult.toLong()) != 1L) bMult = 7
        val a = g * aMult
        val b = g * bMult

        return if (isGcd) {
            val correct = g.toString()
            val distractors = listOf((g * 2).toString(), (g + 1).toString(), (g - 1).coerceAtLeast(1).toString(), (g * aMult).toString())
            val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
            createProblem(
                topicId = "num_gcd_lcm",
                difficulty = difficulty,
                question = "Find the greatest common divisor: GCD($a, $b).",
                choices = choices,
                solution = "Factoring: $a = $g × $aMult and $b = $g × $bMult. Since $aMult and $bMult are coprime, the greatest common divisor is $g.",
                explanation = "Euclidean algorithm computes greatest common divisor.",
                formula = "gcd(a, b) = gcd(b, a mod b)",
                tags = "number_theory,gcd"
            )
        } else {
            val lcmVal = (a.toLong() * b.toLong()) / g
            val correct = lcmVal.toString()
            val distractors = listOf((lcmVal * 2).toString(), (lcmVal / 2).toString(), (a.toLong() * b.toLong()).toString(), (lcmVal + g).toString())
            val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
            createProblem(
                topicId = "num_gcd_lcm",
                difficulty = difficulty,
                question = "Find the least common multiple: LCM($a, $b).",
                choices = choices,
                solution = "LCM(a, b) = (a · b) / GCD(a, b) = ($a × $b) / $g = $lcmVal.",
                explanation = "Fundamental relationship between GCD and LCM.",
                formula = "LCM(a, b) = (a · b) / GCD(a, b)",
                tags = "number_theory,lcm"
            )
        }
    }

    private fun generateModular(difficulty: Difficulty): Problem? {
        // e.g. Evaluate (a^b) mod m using Euler / Fermat
        val m = listOf(5, 7, 11).random(random)
        val base = random.nextInt(2, 6)
        val exp = random.nextInt(10, 40)
        val phi = m - 1
        val reducedExp = exp % phi
        var ans = 1L
        for (i in 0 until reducedExp) {
            ans = (ans * base) % m
        }
        val correct = ans.toString()
        val distractors = mutableListOf<String>()
        for (cand in 0 until m) {
            if (cand.toLong() != ans) {
                distractors.add(cand.toString())
            }
        }
        while (distractors.size < 3) distractors.add((m + distractors.size).toString())
        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        return createProblem(
            topicId = "num_modular",
            difficulty = difficulty,
            question = "Compute the remainder of $base^$exp modulo $m.",
            choices = choices,
            solution = "By Fermat's Little Theorem, $base^($m-1) ≡ 1 (mod $m). Since $exp = $phi × ${exp / phi} + $reducedExp, $base^$exp ≡ $base^$reducedExp ≡ $ans (mod $m).",
            explanation = "Fermat's Little Theorem reduces exponents modulo p - 1.",
            formula = "a^(p-1) ≡ 1 (mod p)",
            tags = "number_theory,modular_arithmetic,fermat"
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
            id = "gen_num_${hash.take(12)}",
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
