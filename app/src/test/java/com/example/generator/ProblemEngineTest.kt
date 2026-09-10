package com.example.generator

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.database.dao.ProblemDao
import com.example.generator.core.AnswerChoiceGenerator
import com.example.generator.core.ProblemGenerator
import com.example.generator.core.ProblemValidator
import com.example.generator.generators.AlgebraGenerator
import com.example.generator.generators.CalculusGenerator
import com.example.generator.generators.CombinatoricsGenerator
import com.example.generator.generators.GeometryGenerator
import com.example.generator.generators.LogicGenerator
import com.example.generator.generators.NumberTheoryGenerator
import com.example.generator.generators.ProbabilityGenerator
import com.example.generator.generators.StatisticsGenerator
import com.example.generator.generators.TrigonometryGenerator
import com.example.generator.service.ProblemGenerationService
import com.example.model.Difficulty
import com.example.model.Problem
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProblemEngineTest {

    private lateinit var database: AppDatabase
    private lateinit var problemDao: ProblemDao
    private lateinit var generationService: ProblemGenerationService

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        problemDao = database.problemDao()
        generationService = ProblemGenerationService(problemDao = problemDao, random = Random(42))
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `test all 9 generators produce valid problems across difficulties`() {
        val generators: List<ProblemGenerator> = listOf(
            AlgebraGenerator(Random(101)),
            TrigonometryGenerator(Random(102)),
            GeometryGenerator(Random(103)),
            NumberTheoryGenerator(Random(104)),
            ProbabilityGenerator(Random(105)),
            CombinatoricsGenerator(Random(106)),
            StatisticsGenerator(Random(107)),
            CalculusGenerator(Random(108)),
            LogicGenerator(Random(109))
        )

        for (gen in generators) {
            for (diff in Difficulty.entries) {
                val problem = gen.generate(diff)
                assertNotNull("Generator ${gen.generatorId} should produce problem for $diff", problem)
                val validationErr = ProblemValidator.validate(problem!!)
                assertNull("Problem from ${gen.generatorId} failed validation: $validationErr", validationErr)
            }
        }
    }

    @Test
    fun `test AnswerChoiceGenerator builds exactly 4 distinct options with one correct answer`() {
        val result = AnswerChoiceGenerator.buildChoices("42", listOf("41", "43", "40"))
        assertNotNull(result)
        val opts = setOf(result!!.optionA, result.optionB, result.optionC, result.optionD)
        assertEquals(4, opts.size)

        val selectedOption = when (result.correctAnswerLetter) {
            "A" -> result.optionA
            "B" -> result.optionB
            "C" -> result.optionC
            "D" -> result.optionD
            else -> ""
        }
        assertEquals("42", selectedOption)
    }

    @Test
    fun `test ProblemValidator rejects malformed or duplicate choices`() {
        val badProblem = Problem(
            id = "bad_1",
            category = "Algebra",
            topicId = "alg_linear",
            difficulty = "EASY",
            question = "Solve x: x + 2 = 4",
            optionA = "2",
            optionB = "2", // duplicate choice
            optionC = "3",
            optionD = "4",
            correctAnswer = "A",
            solution = "x = 4 - 2 = 2.",
            explanation = "Subtraction property.",
            generator = "TestGen",
            questionHash = "dummy_hash"
        )
        val error = ProblemValidator.validate(badProblem)
        assertNotNull("Validator must reject duplicate options", error)
    }

    @Test
    fun `test batch generation of 100 original problems with duplicate detection and database insertion`() = runBlocking {
        val stats = generationService.generate(
            count = 100,
            categories = null,
            difficulties = null
        )

        // 1. Verify 100 problems were successfully inserted
        assertEquals("Expected 100 problems requested", 100, stats.requested)
        assertEquals("Expected 100 valid problems inserted into database", 100, stats.inserted)
        assertEquals("Invalid rejected count should be 0", 0, stats.invalid)

        // 2. Verify all categories received questions
        assertTrue("Should generate across categories", stats.byCategory.size >= 5)

        // 3. Verify no duplicate hashes in database
        val allProblemsInDb = problemDao.getRandomProblems(limit = 200)
        assertEquals(100, allProblemsInDb.size)
        val uniqueHashes = allProblemsInDb.map { it.questionHash }.toSet()
        assertEquals("All 100 generated problems must have unique question hashes", 100, uniqueHashes.size)

        // 4. Test re-running generation skips duplicates
        val duplicateRunStats = generationService.generate(count = 10)
        assertEquals(10, duplicateRunStats.inserted)
    }

    @Test
    fun `test safe generation limit prevents infinite loop when generator exhausts`() = runBlocking {
        // Mock a generator that always fails
        val brokenGenerator = object : ProblemGenerator {
            override val category: String = "Broken"
            override val generatorId: String = "BrokenGenerator"
            override val supportedTopics: List<String> = listOf("broken_topic")
            override fun generate(difficulty: Difficulty, topicId: String?): Problem? = null
        }

        val brokenService = ProblemGenerationService(
            problemDao = problemDao,
            baseGenerators = listOf(brokenGenerator)
        )

        val stats = brokenService.generate(count = 5)
        assertEquals(0, stats.inserted)
        assertTrue("Failed attempts must be recorded", stats.failedAttempts > 0)
    }
}
