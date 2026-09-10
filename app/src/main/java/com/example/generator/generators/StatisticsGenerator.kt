package com.example.generator.generators

import com.example.generator.core.AnswerChoiceGenerator
import com.example.generator.core.ProblemGenerator
import com.example.generator.core.ProblemValidator
import com.example.model.Difficulty
import com.example.model.Problem
import kotlin.random.Random

class StatisticsGenerator(private val random: Random = Random.Default) : ProblemGenerator {

    override val category: String = "Statistics"
    override val generatorId: String = "StatisticsGenerator"
    override val supportedTopics: List<String> = listOf("stat_analysis")

    override fun generate(difficulty: Difficulty, topicId: String?): Problem? {
        val metricChoice = listOf("mean", "median", "mode", "range").random(random)
        return when (metricChoice) {
            "mean" -> generateMean(difficulty)
            "median" -> generateMedian(difficulty)
            "mode" -> generateMode(difficulty)
            "range" -> generateRange(difficulty)
            else -> generateMean(difficulty)
        }
    }

    private fun generateMean(difficulty: Difficulty): Problem? {
        val count = 5
        val baseMean = random.nextInt(10, 30)
        // generate 5 numbers whose sum is baseMean * 5
        val offsets = listOf(-4, -2, 0, 1, 5)
        val numbers = offsets.map { baseMean + it }
        val sum = numbers.sum()
        val mean = sum / count

        val question = "Find the arithmetic mean of the dataset: ${numbers.joinToString(", ")}"
        val correct = mean.toString()
        val distractors = listOf((mean + 1).toString(), (mean - 1).toString(), (mean + 2).toString(), (mean * 2).toString())
        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        return createProblem(
            topicId = "stat_analysis",
            difficulty = difficulty,
            question = question,
            choices = choices,
            solution = "Sum = ${numbers.joinToString(" + ")} = $sum. Mean = Sum / $count = $sum / $count = $mean.",
            explanation = "Arithmetic mean is the sum of observations divided by the total count.",
            formula = "Mean = (Σ x) / n",
            tags = "statistics,mean,average"
        )
    }

    private fun generateMedian(difficulty: Difficulty): Problem? {
        val sortedList = listOf(
            random.nextInt(3, 10),
            random.nextInt(11, 15),
            random.nextInt(16, 22),
            random.nextInt(23, 28),
            random.nextInt(29, 35)
        )
        val median = sortedList[2]
        val shuffledList = sortedList.shuffled(random)
        val question = "Find the median of the dataset: ${shuffledList.joinToString(", ")}"
        val correct = median.toString()
        val distractors = listOf(
            sortedList[1].toString(),
            sortedList[3].toString(),
            ((sortedList[1] + sortedList[3]) / 2).toString(),
            (median + 3).toString()
        ).filter { it != correct }.distinct().take(3)

        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        return createProblem(
            topicId = "stat_analysis",
            difficulty = difficulty,
            question = question,
            choices = choices,
            solution = "Arrange the values in ascending order: ${sortedList.joinToString(", ")}. The middle value (3rd element) is $median.",
            explanation = "Median is the middle value separating the higher half from the lower half.",
            formula = "Median = x_((n+1)/2)",
            tags = "statistics,median"
        )
    }

    private fun generateMode(difficulty: Difficulty): Problem? {
        val modalVal = random.nextInt(12, 25)
        val list = mutableListOf(modalVal, modalVal, modalVal)
        list.add(modalVal + 2)
        list.add(modalVal - 3)
        list.add(modalVal + 5)
        val shuffled = list.shuffled(random)

        val question = "Find the mode of the dataset: ${shuffled.joinToString(", ")}"
        val correct = modalVal.toString()
        val distractors = listOf((modalVal + 2).toString(), (modalVal - 3).toString(), (modalVal + 5).toString())
        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        return createProblem(
            topicId = "stat_analysis",
            difficulty = difficulty,
            question = question,
            choices = choices,
            solution = "The value $modalVal appears 3 times, which is more frequent than any other number. Thus the mode is $modalVal.",
            explanation = "Mode is the value that appears most frequently in a dataset.",
            formula = "Mode = argmax(freq(x))",
            tags = "statistics,mode"
        )
    }

    private fun generateRange(difficulty: Difficulty): Problem? {
        val minVal = random.nextInt(5, 15)
        val maxVal = random.nextInt(35, 55)
        val numbers = listOf(minVal, minVal + 5, minVal + 12, maxVal - 6, maxVal).shuffled(random)
        val range = maxVal - minVal

        val question = "What is the statistical range of the dataset: ${numbers.joinToString(", ")}?"
        val correct = range.toString()
        val distractors = listOf((range + 2).toString(), (range - 3).toString(), maxVal.toString(), (range / 2).toString())
        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        return createProblem(
            topicId = "stat_analysis",
            difficulty = difficulty,
            question = question,
            choices = choices,
            solution = "Range = Maximum - Minimum = $maxVal - $minVal = $range.",
            explanation = "Range is the difference between the highest and lowest values.",
            formula = "Range = Max - Min",
            tags = "statistics,range"
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
            id = "gen_stat_${hash.take(12)}",
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
