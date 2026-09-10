package com.example.generator.generators

import com.example.generator.core.AnswerChoiceGenerator
import com.example.generator.core.ProblemGenerator
import com.example.generator.core.ProblemValidator
import com.example.model.Difficulty
import com.example.model.Problem
import kotlin.random.Random

class TrigonometryGenerator(private val random: Random = Random.Default) : ProblemGenerator {

    override val category: String = "Trigonometry"
    override val generatorId: String = "TrigonometryGenerator"
    override val supportedTopics: List<String> = listOf(
        "trig_identities",
        "trig_equations",
        "trig_triangles"
    )

    override fun generate(difficulty: Difficulty, topicId: String?): Problem? {
        val topic = if (topicId != null && supportedTopics.contains(topicId)) {
            topicId
        } else {
            when (difficulty) {
                Difficulty.EASY -> "trig_identities"
                Difficulty.MEDIUM -> listOf("trig_identities", "trig_triangles").random(random)
                Difficulty.HARD -> listOf("trig_equations", "trig_triangles").random(random)
                Difficulty.EXPERT -> "trig_equations"
            }
        }

        return when (topic) {
            "trig_identities" -> generateIdentities(difficulty)
            "trig_triangles" -> generateTriangles(difficulty)
            "trig_equations" -> generateEquations(difficulty)
            else -> generateIdentities(difficulty)
        }
    }

    private fun generateIdentities(difficulty: Difficulty): Problem? {
        return when (difficulty) {
            Difficulty.EASY -> {
                // Special angle exact evaluation
                val specialAngles = listOf(
                    Triple("sin(30°)", "1/2", listOf("√3/2", "√2/2", "1")),
                    Triple("cos(60°)", "1/2", listOf("√3/2", "0", "1")),
                    Triple("sin(60°)", "√3/2", listOf("1/2", "√2/2", "1")),
                    Triple("cos(30°)", "√3/2", listOf("1/2", "√2/2", "0")),
                    Triple("tan(45°)", "1", listOf("0", "√3", "1/√3")),
                    Triple("sin(90°)", "1", listOf("0", "-1", "1/2")),
                    Triple("cos(90°)", "0", listOf("1", "-1", "1/2"))
                )
                val item = specialAngles.random(random)
                val question = "Find the exact mathematical value of:\n${item.first}"
                val choices = AnswerChoiceGenerator.buildChoices(item.second, item.third, random) ?: return null
                createProblem(
                    topicId = "trig_identities",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = "From standard reference angles on the unit circle, ${item.first} = ${item.second}.",
                    explanation = "Standard trigonometric values of fundamental special angles.",
                    formula = "sin(30°) = 1/2, cos(60°) = 1/2, sin(60°) = √3/2",
                    tags = "trig,special_angles"
                )
            }
            Difficulty.MEDIUM -> {
                // sin²(x) + cos²(x) identity or 1 - cos²(x)
                val angle = listOf(15, 27, 34, 42, 68, 73).random(random)
                val question = "Simplify the expression:\nsin²($angle°) + cos²($angle°)"
                val correct = "1"
                val distractors = listOf("0", "2", "sin($angle°)", "cos($angle°)")
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                createProblem(
                    topicId = "trig_identities",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = "By the fundamental Pythagorean trigonometric identity, sin²(θ) + cos²(θ) = 1 for any angle θ. Here θ = $angle°, so the result is 1.",
                    explanation = "Pythagorean identity holds universally for all real angles.",
                    formula = "sin²(θ) + cos²(θ) = 1",
                    tags = "trig,pythagorean_identity"
                )
            }
            Difficulty.HARD -> {
                // Complementary angles: sin²(θ) + sin²(90° - θ) = 1
                val theta = random.nextInt(10, 40)
                val comp = 90 - theta
                val question = "Compute the exact value of:\nsin²($theta°) + sin²($comp°)"
                val correct = "1"
                val distractors = listOf("0", "1/2", "2", "√3/2")
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                createProblem(
                    topicId = "trig_identities",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = "Since $comp° = 90° - $theta°, sin($comp°) = cos($theta°). Therefore, sin²($theta°) + sin²($comp°) = sin²($theta°) + cos²($theta°) = 1.",
                    explanation = "Use complementary angle identity sin(90° - θ) = cos(θ).",
                    formula = "sin(90° - θ) = cos(θ), sin²(θ) + cos²(θ) = 1",
                    tags = "trig,complementary_angles"
                )
            }
            Difficulty.EXPERT -> {
                // Double angle identity: 2 sin(x) cos(x) = sin(2x)
                val x = listOf(15, 22.5.toInt(), 30).random(random)
                val doubleX = x * 2
                val question = "Simplify the expression into a single trigonometric ratio:\n2 · sin($x°) · cos($x°)"
                val correct = "sin($doubleX°)"
                val distractors = listOf("cos($doubleX°)", "tan($doubleX°)", "2 sin($x°)", "sin²($x°)")
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                createProblem(
                    topicId = "trig_identities",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = "Using the double-angle formula for sine: 2 sin(θ) cos(θ) = sin(2θ). With θ = $x°, 2 sin($x°) cos($x°) = sin($doubleX°).",
                    explanation = "Double-angle sine expansion.",
                    formula = "sin(2θ) = 2 sin(θ) cos(θ)",
                    tags = "trig,double_angle"
                )
            }
        }
    }

    private fun generateTriangles(difficulty: Difficulty): Problem? {
        // Area of triangle using sine: Area = (1/2) * a * b * sin(C)
        val a = random.nextInt(4, 12)
        val b = random.nextInt(4, 12)
        val angleChoice = listOf(30 to "1/2", 45 to "√2/2", 60 to "√3/2").random(random)
        val angle = angleChoice.first

        return if (angle == 30) {
            val area = (a * b) / 4.0
            val areaStr = if (area == area.toLong().toDouble()) area.toLong().toString() else area.toString()
            val question = "In triangle ABC, side a = $a, side b = $b, and the included angle C = 30°. Find the area of triangle ABC."
            val correct = areaStr
            val distractors = listOf(
                (area * 2).toString(),
                (area + 2).toString(),
                ((a * b) / 2.0).toString(),
                (area - 1).coerceAtLeast(1.0).toString()
            )
            val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
            createProblem(
                topicId = "trig_triangles",
                difficulty = difficulty,
                question = question,
                choices = choices,
                solution = "Area = (1/2) · a · b · sin(C) = (1/2) · $a · $b · sin(30°) = (1/2) · $a · $b · (1/2) = $areaStr.",
                explanation = "Standard trigonometric triangle area formula.",
                formula = "Area = (1/2)ab sin(C)",
                tags = "trig,triangle_area"
            )
        } else {
            // Right triangle hypotenuse / leg
            val leg = random.nextInt(3, 10)
            val question = "In a right triangle with acute angle 30°, the leg opposite to the 30° angle is $leg. Find the length of the hypotenuse."
            val hyp = leg * 2
            val correct = hyp.toString()
            val distractors = listOf(leg.toString(), (hyp + 2).toString(), (leg * 3).toString(), (hyp - 1).toString())
            val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
            createProblem(
                topicId = "trig_triangles",
                difficulty = difficulty,
                question = question,
                choices = choices,
                solution = "In a right triangle, sin(30°) = opposite / hypotenuse. Since sin(30°) = 1/2, hypotenuse = 2 × opposite = 2 × $leg = $hyp.",
                explanation = "Definition of sine in a right triangle.",
                formula = "sin(θ) = opp / hyp",
                tags = "trig,right_triangles"
            )
        }
    }

    private fun generateEquations(difficulty: Difficulty): Problem? {
        val question = "How many solutions does sin(x) = 1/2 have in the interval [0, 2π)?"
        val correct = "2"
        val distractors = listOf("1", "3", "4", "0")
        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        return createProblem(
            topicId = "trig_equations",
            difficulty = difficulty,
            question = question,
            choices = choices,
            solution = "sin(x) = 1/2 in [0, 2π) occurs in the first quadrant at x = π/6 and in the second quadrant at x = 5π/6. Total = 2 solutions.",
            explanation = "Sine is positive in Quadrants I and II.",
            formula = "sin(x) = 1/2 => x = π/6, 5π/6",
            tags = "trig,equations"
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
            id = "gen_trig_${hash.take(12)}",
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
