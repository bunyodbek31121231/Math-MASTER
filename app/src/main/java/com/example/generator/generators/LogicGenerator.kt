package com.example.generator.generators

import com.example.generator.core.AnswerChoiceGenerator
import com.example.generator.core.ProblemGenerator
import com.example.generator.core.ProblemValidator
import com.example.model.Difficulty
import com.example.model.Problem
import kotlin.random.Random

class LogicGenerator(private val random: Random = Random.Default) : ProblemGenerator {

    override val category: String = "Logic"
    override val generatorId: String = "LogicGenerator"
    override val supportedTopics: List<String> = listOf("logic_boolean")

    override fun generate(difficulty: Difficulty, topicId: String?): Problem? {
        val type = listOf("truth_table", "implication", "negation", "sequence").random(random)
        return when (type) {
            "truth_table" -> generateTruthTable(difficulty)
            "implication" -> generateImplication(difficulty)
            "negation" -> generateNegation(difficulty)
            "sequence" -> generateSequenceReasoning(difficulty)
            else -> generateTruthTable(difficulty)
        }
    }

    private fun generateTruthTable(difficulty: Difficulty): Problem? {
        val p = random.nextBoolean()
        val q = random.nextBoolean()
        val op = listOf("AND", "OR", "XOR").random(random)
        val pStr = if (p) "True" else "False"
        val qStr = if (q) "True" else "False"

        val result = when (op) {
            "AND" -> p && q
            "OR" -> p || q
            "XOR" -> p xor q
            else -> p && q
        }
        val correct = if (result) "True" else "False"
        val distractors = listOf(
            if (!result) "True" else "False",
            "Indeterminate",
            "Contradiction"
        )
        val question = "Given propositions P = $pStr and Q = $qStr, what is the truth value of: P $op Q?"
        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        return createProblem(
            topicId = "logic_boolean",
            difficulty = difficulty,
            question = question,
            choices = choices,
            solution = "P is $pStr and Q is $qStr. Under the logical $op operator, ($pStr $op $qStr) evaluates to $correct.",
            explanation = "Truth table evaluation for Boolean operations.",
            formula = "Truth table definition of $op",
            tags = "logic,boolean,truth_tables"
        )
    }

    private fun generateImplication(difficulty: Difficulty): Problem? {
        val question = "Under classical mathematical logic, the implication statement (P => Q) is logically equivalent to which of the following?"
        val correct = "NOT P OR Q"
        val distractors = listOf("P AND Q", "NOT P AND NOT Q", "Q => P")
        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        return createProblem(
            topicId = "logic_boolean",
            difficulty = difficulty,
            question = question,
            choices = choices,
            solution = "By material implication in propositional calculus, (P => Q) ≡ (¬P ∨ Q). It is false only when P is True and Q is False.",
            explanation = "Material implication rule of equivalence.",
            formula = "P => Q ≡ ¬P ∨ Q",
            tags = "logic,implication,equivalence"
        )
    }

    private fun generateNegation(difficulty: Difficulty): Problem? {
        val question = "What is the logical negation of the statement: 'All prime numbers are odd'?"
        val correct = "There exists at least one prime number that is not odd"
        val distractors = listOf(
            "No prime numbers are odd",
            "All prime numbers are even",
            "Some prime numbers are odd"
        )
        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        return createProblem(
            topicId = "logic_boolean",
            difficulty = difficulty,
            question = question,
            choices = choices,
            solution = "The negation of a universal statement '∀x P(x)' is the existential statement '∃x ¬P(x)'. Therefore: 'There exists at least one prime that is not odd' (namely 2).",
            explanation = "De Morgan's laws for quantifiers: ¬(∀x P(x)) ≡ ∃x ¬P(x).",
            formula = "¬(∀x P(x)) ≡ ∃x ¬P(x)",
            tags = "logic,quantifiers,negation"
        )
    }

    private fun generateSequenceReasoning(difficulty: Difficulty): Problem? {
        val base = random.nextInt(2, 5)
        val mult = random.nextInt(2, 4)
        val s1 = base
        val s2 = s1 * mult
        val s3 = s2 * mult
        val s4 = s3 * mult
        val s5 = s4 * mult

        val question = "Identify the next number in the logical mathematical sequence:\n$s1, $s2, $s3, $s4, ?"
        val correct = s5.toString()
        val distractors = listOf((s4 + mult).toString(), (s5 + mult).toString(), (s5 - base).toString(), (s4 * (mult + 1)).toString())
        val choices = AnswerChoiceGenerator.buildChoices(correct, distractors, random) ?: return null
        return createProblem(
            topicId = "logic_boolean",
            difficulty = difficulty,
            question = question,
            choices = choices,
            solution = "Each term is obtained by multiplying the previous term by $mult. The next term is $s4 × $mult = $s5.",
            explanation = "Geometric sequence inductive pattern recognition.",
            formula = "a_n = a_(n-1) · r",
            tags = "logic,sequences,patterns"
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
            id = "gen_log_${hash.take(12)}",
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
