package com.example.generator

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.database.dao.ProblemDao
import com.example.generator.core.ProblemGenerator
import com.example.generator.core.ProblemValidator
import com.example.generator.generators.AlgebraGenerator
import com.example.generator.generators.CalculusGenerator
import com.example.generator.generators.CombinatoricsGenerator
import com.example.generator.generators.GeometryGenerator
import com.example.generator.generators.LogicGenerator
import com.example.generator.generators.MixedGenerator
import com.example.generator.generators.NumberTheoryGenerator
import com.example.generator.generators.OlympiadGenerator
import com.example.generator.generators.ProbabilityGenerator
import com.example.generator.generators.StatisticsGenerator
import com.example.generator.generators.TrigonometryGenerator
import com.example.generator.pipeline.GenerationJobDao
import com.example.generator.pipeline.QuotaManager
import com.example.generator.service.DatabaseIntegrityChecker
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
class PipelineProductionTest {

    private lateinit var database: AppDatabase
    private lateinit var problemDao: ProblemDao
    private lateinit var jobDao: GenerationJobDao
    private lateinit var generationService: ProblemGenerationService
    private lateinit var integrityChecker: DatabaseIntegrityChecker

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        problemDao = database.problemDao()
        jobDao = database.generationJobDao()
        generationService = ProblemGenerationService(
            problemDao = problemDao,
            jobDao = jobDao,
            random = Random(42)
        )
        integrityChecker = DatabaseIntegrityChecker(problemDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `test Olympiad and Mixed generators produce valid problems`() {
        val oly = OlympiadGenerator(Random(123))
        val problem = oly.generate(Difficulty.HARD)
        assertNotNull(problem)
        assertNull(ProblemValidator.validate(problem!!))

        val mixed = MixedGenerator(listOf(oly), Random(124))
        val mixedProblem = mixed.generate(Difficulty.HARD)
        assertNotNull(mixedProblem)
        assertEquals("Mixed", mixedProblem!!.category)
        assertNull(ProblemValidator.validate(mixedProblem))
    }

    @Test
    fun `test QuotaManager computes category deficits correctly`() {
        val currentCounts = mapOf(
            "Algebra" to 3000,
            "Trigonometry" to 1000
        )
        val deficits = QuotaManager.computeDeficit(currentCounts, 20620)
        assertTrue(deficits.containsKey("Algebra"))
        assertTrue(deficits.containsKey("Trigonometry"))
        assertTrue(deficits["Algebra"]!! > 0)
        assertTrue(deficits["Trigonometry"]!! > 0)
    }

    @Test
    fun `test batch generation commits in safe batches and updates job entity`() = runBlocking {
        val jobId = "job_test_batch_1"
        val stats = generationService.executeJob(
            jobId = jobId,
            targetTotal = 150,
            batchSize = 50,
            categories = listOf("Algebra", "Geometry", "Calculus"),
            difficulties = listOf(Difficulty.EASY, Difficulty.MEDIUM)
        )

        assertEquals(150, stats.requested)
        assertEquals(150, stats.inserted)

        val job = jobDao.getJobById(jobId)
        assertNotNull(job)
        assertEquals(150, job!!.completedCount)
        assertEquals("COMPLETED", job.status)

        val countInDb = problemDao.getProblemCountDirect()
        assertEquals(150, countInDb)
    }

    @Test
    fun `test pause and resume of generation job`() = runBlocking {
        val jobId = "job_resumable_1"

        // 1. Run first part with pause triggered
        generationService.pauseActiveJob()
        val firstStats = generationService.executeJob(
            jobId = jobId,
            targetTotal = 100,
            batchSize = 50
        )
        // With pause active from start, it breaks after 0 or 1 batch
        val jobAfterPause = jobDao.getJobById(jobId)
        assertNotNull(jobAfterPause)

        // 2. Resume the job with resetControls
        generationService.resetControls()
        val resumeStats = generationService.executeJob(
            jobId = jobId,
            targetTotal = 100,
            batchSize = 50
        )

        val finalJob = jobDao.getJobById(jobId)
        assertNotNull(finalJob)
        assertEquals(100, finalJob!!.completedCount)
        assertEquals("COMPLETED", finalJob.status)
    }

    @Test
    fun `test database integrity check reports 100 percent healthy`() = runBlocking {
        // Populate 100 problems
        generationService.generate(count = 100)

        val report = integrityChecker.checkIntegrity(maxCheck = 200)
        assertEquals(100, report.totalChecked)
        assertEquals(100, report.healthyCount)
        assertEquals(0, report.malformedCount)
        assertEquals(0, report.duplicateHashCount)
        assertTrue(report.isValid)
        assertTrue(report.issues.isEmpty())
    }
}
