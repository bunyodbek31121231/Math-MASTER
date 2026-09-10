package com.example.generator.generators

import com.example.generator.core.AnswerChoiceGenerator
import com.example.generator.core.ProblemGenerator
import com.example.generator.core.ProblemValidator
import com.example.model.Difficulty
import com.example.model.Problem
import kotlin.random.Random

class GeometryGenerator(private val random: Random = Random.Default) : ProblemGenerator {

    override val category: String = "Geometry"
    override val generatorId: String = "GeometryGenerator"
    override val supportedTopics: List<String> = listOf(
        "geom_triangles",
        "geom_circles",
        "geom_polygons",
        "geom_3d"
    )

    override fun generate(difficulty: Difficulty, topicId: String?): Problem? {
        val topic = if (topicId != null && supportedTopics.contains(topicId)) {
            topicId
        } else {
            when (difficulty) {
                Difficulty.EASY -> listOf("geom_triangles", "geom_polygons").random(random)
                Difficulty.MEDIUM -> listOf("geom_triangles", "geom_circles", "geom_polygons").random(random)
                Difficulty.HARD -> listOf("geom_triangles", "geom_circles", "geom_3d").random(random)
                Difficulty.EXPERT -> listOf("geom_circles", "geom_3d").random(random)
            }
        }

        return when (topic) {
            "geom_triangles" -> generateTriangles(difficulty)
            "geom_circles" -> generateCircles(difficulty)
            "geom_polygons" -> generatePolygons(difficulty)
            "geom_3d" -> generate3DGeometry(difficulty)
            else -> generateTriangles(difficulty)
        }
    }

    private fun generateTriangles(difficulty: Difficulty): Problem? {
        return when (difficulty) {
            Difficulty.EASY -> {
                // Pythagorean theorem with integer triples: (3,4,5), (5,12,13), (6,8,10), (8,15,17), (7,24,25)
                val triples = listOf(
                    Triple(3, 4, 5),
                    Triple(5, 12, 13),
                    Triple(6, 8, 10),
                    Triple(8, 15, 17),
                    Triple(7, 24, 25),
                    Triple(9, 12, 15)
                )
                val t = triples.random(random)
                val question = "In a right-angled triangle, the lengths of the two legs are ${t.first} cm and ${t.second} cm. Find the length of the hypotenuse."
                val correct = "${t.third} cm"
                val distractors = listOf(
                    "${t.third + 2} cm",
                    "${t.third - 1} cm",
                    "${t.first + t.second} cm",
                    "${t.third * 2} cm"
                )
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                createProblem(
                    topicId = "geom_triangles",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = "By the Pythagorean theorem: c² = a² + b² = ${t.first}² + ${t.second}² = ${t.first * t.first} + ${t.second * t.second} = ${t.third * t.third}. Taking the square root gives c = ${t.third} cm.",
                    explanation = "Pythagorean theorem: sum of squares of legs equals square of hypotenuse.",
                    formula = "c = √(a² + b²)",
                    tags = "geometry,pythagoras,triangles"
                )
            }
            Difficulty.MEDIUM -> {
                // Triangle area: base * height / 2 or equilateral triangle area: (s² * √3)/4
                val base = random.nextInt(6, 18).let { if (it % 2 != 0) it + 1 else it }
                val height = random.nextInt(5, 15)
                val area = (base * height) / 2
                val question = "Find the area of a triangle with a base of $base cm and a corresponding height of $height cm."
                val correct = "$area cm²"
                val distractors = listOf(
                    "${area * 2} cm²",
                    "${area + base} cm²",
                    "${area - 4} cm²",
                    "${base * height} cm²"
                )
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                createProblem(
                    topicId = "geom_triangles",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = "Area = (1/2) · base · height = (1/2) · $base · $height = $area cm².",
                    explanation = "Basic triangle area formula.",
                    formula = "A = (b · h) / 2",
                    tags = "geometry,triangle_area"
                )
            }
            Difficulty.HARD -> {
                // Altitude to hypotenuse: h = (a * b) / c
                val triples = listOf(
                    Triple(9, 12, 15), // h = 108 / 15 = 7.2
                    Triple(6, 8, 10),  // h = 48 / 10 = 4.8
                    Triple(15, 20, 25), // h = 300 / 25 = 12
                    Triple(5, 12, 13) // h = 60 / 13
                )
                val t = triples.take(3).random(random) // clean decimals
                val h = (t.first * t.second).toDouble() / t.third
                val hStr = if (h == h.toLong().toDouble()) "${h.toLong()}" else "$h"
                val question = "In a right triangle with legs ${t.first} and ${t.second}, find the length of the altitude drawn to the hypotenuse."
                val correct = hStr
                val distractors = listOf(
                    "${(h + 1.2)}",
                    "${(h - 1.0).coerceAtLeast(1.0)}",
                    "${t.third / 2.0}",
                    "${(h * 1.5)}"
                )
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                createProblem(
                    topicId = "geom_triangles",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = "Hypotenuse c = √(${t.first}² + ${t.second}²) = ${t.third}. The area is (1/2) · ${t.first} · ${t.second} = ${(t.first * t.second) / 2}. Also Area = (1/2) · c · h => h = (${t.first} · ${t.second}) / ${t.third} = $hStr.",
                    explanation = "Altitude to hypotenuse equals product of legs divided by hypotenuse.",
                    formula = "h = (a · b) / c",
                    tags = "geometry,altitude,triangles"
                )
            }
            Difficulty.EXPERT -> {
                // Similar triangles ratio: Area ratio is k²
                val k = random.nextInt(2, 5)
                val a1 = random.nextInt(8, 25)
                val a2 = a1 * k * k
                val question = "Two similar triangles have a linear ratio of similitude of 1 : $k. If the area of the smaller triangle is $a1 cm², what is the area of the larger triangle?"
                val correct = "$a2 cm²"
                val distractors = listOf(
                    "${a1 * k} cm²",
                    "${a2 + a1} cm²",
                    "${a2 * 2} cm²",
                    "${a1 * (k + 1)} cm²"
                )
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                createProblem(
                    topicId = "geom_triangles",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = "The ratio of areas of two similar figures is the square of the ratio of their corresponding linear dimensions: Area_large / Area_small = ($k / 1)² = ${k * k}. Therefore, Area_large = $a1 × ${k * k} = $a2 cm².",
                    explanation = "Ratio of areas is proportional to the square of the similarity ratio k².",
                    formula = "A2 / A1 = k²",
                    tags = "geometry,similarity,area_ratios"
                )
            }
        }
    }

    private fun generateCircles(difficulty: Difficulty): Problem? {
        val r = random.nextInt(3, 12)
        val d = 2 * r
        val circ = 2 * r
        val area = r * r

        return when (difficulty) {
            Difficulty.EASY -> {
                val question = "Find the circumference of a circle with a radius of $r cm. (Express answer in terms of π)"
                val correct = "${circ}π cm"
                val distractors = listOf("${area}π cm", "${r}π cm", "${circ * 2}π cm", "${circ + 2}π cm")
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                createProblem(
                    topicId = "geom_circles",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = "Circumference C = 2πr = 2π($r) = ${circ}π cm.",
                    explanation = "Standard circumference formula.",
                    formula = "C = 2πr",
                    tags = "geometry,circles,circumference"
                )
            }
            Difficulty.MEDIUM, Difficulty.HARD, Difficulty.EXPERT -> {
                val question = "Find the area of a circle whose diameter is $d cm. (Express answer in terms of π)"
                val correct = "${area}π cm²"
                val distractors = listOf("${circ}π cm²", "${d * d}π cm²", "${area * 2}π cm²", "${(area / 2)}π cm²")
                val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
                createProblem(
                    topicId = "geom_circles",
                    difficulty = difficulty,
                    question = question,
                    choices = choices,
                    solution = "Radius r = d / 2 = $d / 2 = $r cm. Area A = πr² = π($r)² = ${area}π cm².",
                    explanation = "Area of circle formula with diameter.",
                    formula = "A = πr² = π(d/2)²",
                    tags = "geometry,circles,area"
                )
            }
        }
    }

    private fun generatePolygons(difficulty: Difficulty): Problem? {
        // Parallelogram or Trapezoid
        val b1 = random.nextInt(5, 12)
        val b2 = random.nextInt(3, b1)
        val h = random.nextInt(4, 10)
        val area = ((b1 + b2) * h) / 2.0
        val areaStr = if (area == area.toLong().toDouble()) "${area.toLong()} cm²" else "$area cm²"
        val question = "Find the area of a trapezoid with parallel bases $b1 cm and $b2 cm and height $h cm."
        val correct = areaStr
        val distractors = listOf(
            "${(b1 + b2) * h} cm²",
            "${area * 2} cm²",
            "${(area - 3).coerceAtLeast(1.0)} cm²",
            "${(b1 * h)} cm²"
        )
        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        return createProblem(
            topicId = "geom_polygons",
            difficulty = difficulty,
            question = question,
            choices = choices,
            solution = "Area of trapezoid = ((b1 + b2) / 2) · h = (($b1 + $b2) / 2) · $h = $areaStr.",
            explanation = "Trapezoid area is average base times height.",
            formula = "A = ((a + b) / 2) · h",
            tags = "geometry,trapezoid,polygons"
        )
    }

    private fun generate3DGeometry(difficulty: Difficulty): Problem? {
        // Rectangular prism or cylinder volume
        val l = random.nextInt(3, 8)
        val w = random.nextInt(2, 6)
        val h = random.nextInt(2, 5)
        val vol = l * w * h
        val sa = 2 * (l * w + w * h + h * l)
        val question = "Find the total volume of a rectangular prism with length $l cm, width $w cm, and height $h cm."
        val correct = "$vol cm³"
        val distractors = listOf(
            "$sa cm³",
            "${vol * 2} cm³",
            "${vol + l + w + h} cm³",
            "${(l + w + h) * 2} cm³"
        )
        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        return createProblem(
            topicId = "geom_3d",
            difficulty = difficulty,
            question = question,
            choices = choices,
            solution = "Volume V = length × width × height = $l × $w × $h = $vol cm³.",
            explanation = "Volume of a cuboid / rectangular prism.",
            formula = "V = l · w · h",
            tags = "geometry,volume,3d"
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
            id = "gen_geom_${hash.take(12)}",
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
