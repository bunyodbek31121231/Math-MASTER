package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.database.dao.ProblemDao
import com.example.data.database.dao.QuestionHistoryDao
import com.example.data.database.dao.SpecialContentDao
import com.example.data.database.dao.TestDao
import com.example.data.database.dao.TopicDao
import com.example.data.database.dao.UserDao
import com.example.data.database.dao.UserProgressDao
import com.example.data.database.entity.ProblemEntity
import com.example.data.database.entity.SpecialContentEntity
import com.example.data.database.entity.TopicEntity
import com.example.data.preferences.UserPreferences
import com.example.data.repository.AuthResult
import com.example.data.repository.MathRepository
import com.example.data.repository.MaxsusRepository
import com.example.data.repository.TestRepository
import com.example.data.repository.UserRepository
import com.example.model.LevelSystem
import com.example.security.MaxsusGate
import com.example.ui.viewmodel.TestViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MathMasterComprehensiveTest {

    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var userProgressDao: UserProgressDao
    private lateinit var topicDao: TopicDao
    private lateinit var problemDao: ProblemDao
    private lateinit var testDao: TestDao
    private lateinit var questionHistoryDao: QuestionHistoryDao
    private lateinit var specialContentDao: SpecialContentDao

    private lateinit var userPreferences: UserPreferences
    private lateinit var userRepository: UserRepository
    private lateinit var mathRepository: MathRepository
    private lateinit var testRepository: TestRepository
    private lateinit var maxsusRepository: MaxsusRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        userDao = database.userDao()
        userProgressDao = database.userProgressDao()
        topicDao = database.topicDao()
        problemDao = database.problemDao()
        testDao = database.testDao()
        questionHistoryDao = database.questionHistoryDao()
        specialContentDao = database.specialContentDao()

        userPreferences = UserPreferences(context)
        userRepository = UserRepository(userDao, userPreferences)
        mathRepository = MathRepository(
            problemDao = problemDao,
            questionHistoryDao = questionHistoryDao,
            topicDao = topicDao,
            userProgressDao = userProgressDao,
            userRepository = userRepository
        )
        testRepository = TestRepository(
            testDao = testDao,
            problemDao = problemDao,
            userRepository = userRepository
        )
        maxsusRepository = MaxsusRepository(topicDao, specialContentDao, problemDao, database.maxsusDao(), userPreferences)
    }

    @After
    fun tearDown() {
        database.close()
    }

    // 1. MAXSUS Password Gate Verification
    @Test
    fun `test MAXSUS password gate accepts 103202 and rejects wrong passwords`() = runBlocking {
        // Correct passcode
        assertTrue("Passcode 103202 must unlock MAXSUS", MaxsusGate.verify("103202"))
        assertTrue("Passcode with leading/trailing whitespace must be trimmed and accepted", MaxsusGate.verify("  103202  "))

        // Wrong passcode
        assertFalse("Arbitrary code must be rejected", MaxsusGate.verify("000000"))
        assertFalse("Empty string must be rejected", MaxsusGate.verify(""))
    }

    // 2. Special Content Architecture Ready for Usmonov Book
    @Test
    fun `test special content database model supports topic info examples and practice`() = runBlocking {
        val specialTopic = TopicEntity(
            id = "maxsus_usmonov_ch1",
            category = "MAXSUS",
            name = "1-Bob: Haqiqiy Sonlar va Arifmetika",
            description = "Usmonov Matematika qo'llanmasi asosida tayyorlangan mavzu",
            iconName = "menu_book",
            isMaxsus = true,
            orderIndex = 1
        )
        topicDao.insertTopics(listOf(specialTopic))

        val specialContent = SpecialContentEntity(
            id = "usmonov_sec_1_1",
            topicId = "maxsus_usmonov_ch1",
            title = "1.1. Kasrlar va Ratsional Sonlar ustida amallar",
            subtitle = "Nazariya va Namunaviy Misollar",
            contentBody = "Ratsional sonlar deb p/q ko'rinishida yozish mumkin bo'lgan sonlarga aytiladi.",
            formulaSheet = "a/b + c/d = (ad + bc) / bd",
            exampleCount = 5,
            orderIndex = 1
        )
        specialContentDao.insertSpecialContents(listOf(specialContent))

        val fetched = specialContentDao.getContentForTopic("maxsus_usmonov_ch1").first()
        assertEquals(1, fetched.size)
        assertEquals("1.1. Kasrlar va Ratsional Sonlar ustida amallar", fetched[0].title)
        assertEquals("a/b + c/d = (ad + bc) / bd", fetched[0].formulaSheet)
    }

    // 3. Complete Test Flow, Scoring, Timer, and History Persistence
    @Test
    fun `test complete test flow calculates score percentage XP and saves history`() = runBlocking {
        val registerRes = userRepository.register("Test User", "tester1", "pass123", "pass123")
        assertTrue(registerRes is AuthResult.Success)
        val user = (registerRes as AuthResult.Success).user

        // Seed 3 test problems
        val testProblems = listOf(
            ProblemEntity(
                id = "t_p1",
                category = "Algebra",
                topicId = "alg_linear",
                difficulty = "MEDIUM",
                question = "2x = 10, x = ?",
                optionA = "5",
                optionB = "4",
                optionC = "6",
                optionD = "2",
                correctAnswer = "A",
                solution = "x = 5",
                explanation = "Divide by 2",
                questionHash = "hash_tp1"
            ),
            ProblemEntity(
                id = "t_p2",
                category = "Algebra",
                topicId = "alg_linear",
                difficulty = "MEDIUM",
                question = "3x = 12, x = ?",
                optionA = "3",
                optionB = "4",
                optionC = "5",
                optionD = "6",
                correctAnswer = "B",
                solution = "x = 4",
                explanation = "Divide by 3",
                questionHash = "hash_tp2"
            ),
            ProblemEntity(
                id = "t_p3",
                category = "Algebra",
                topicId = "alg_linear",
                difficulty = "MEDIUM",
                question = "x + 5 = 7, x = ?",
                optionA = "1",
                optionB = "3",
                optionC = "2",
                optionD = "4",
                correctAnswer = "C",
                solution = "x = 2",
                explanation = "Subtract 5",
                questionHash = "hash_tp3"
            )
        )

        // User answers: 2 correct (p1=A, p2=B), 1 incorrect (p3=D)
        val userAnswers = mapOf(
            "t_p1" to "A",
            "t_p2" to "B",
            "t_p3" to "D"
        )

        val summary = testRepository.saveCompletedTest(
            userId = user.id,
            title = "Test: Algebra Medium",
            topicId = "alg_linear",
            difficulty = "MEDIUM",
            problems = testProblems,
            userAnswers = userAnswers,
            durationSeconds = 125,
            isMaxsus = false
        )

        assertEquals(3, summary.totalQuestions)
        assertEquals(2, summary.correctCount)
        assertEquals(66.66, summary.scorePercentage, 0.5)
        assertTrue("XP earned must be greater than 0", summary.xpEarned > 0)
        assertEquals(125, summary.durationSeconds)

        // Verify test record exists in user's test history
        val history = testRepository.getTestHistory(user.id).first()
        assertEquals(1, history.size)
        assertEquals(summary.testId, history[0].id)
        assertEquals(2, history[0].correctCount)

        // Verify individual question breakdown was saved
        val questions = testRepository.getQuestionsForTest(summary.testId)
        assertEquals(3, questions.size)
        assertTrue(questions[0].isCorrect)
        assertTrue(questions[1].isCorrect)
        assertFalse(questions[2].isCorrect)
    }

    // 4. Progression System: XP, Level Calculation, and Accrual
    @Test
    fun `test level and XP calculation formula`() {
        assertEquals(1, LevelSystem.getLevelForXp(0))
        assertEquals(1, LevelSystem.getLevelForXp(50))
        assertEquals(1, LevelSystem.getLevelForXp(99))
        assertEquals(2, LevelSystem.getLevelForXp(100))
        assertEquals(2, LevelSystem.getLevelForXp(250))
        assertEquals(3, LevelSystem.getLevelForXp(300))

        val (xpInLevel, span) = LevelSystem.getXpProgressInCurrentLevel(150)
        assertEquals(50L, xpInLevel) // In level 2 (100 to 300), 150 - 100 = 50
        assertEquals(200L, span)     // 300 - 100 = 200

        assertEquals("Novice Mathematician", LevelSystem.getTitleForLevel(1))
        assertEquals("Diligent Student", LevelSystem.getTitleForLevel(4))
        assertEquals("Problem Solver", LevelSystem.getTitleForLevel(8))
    }

    // 5. User Data Separation: One user's history never appears for another user
    @Test
    fun `test user separation ensures isolated test history and question records`() = runBlocking {
        val user1 = (userRepository.register("User One", "user_one", "pass123", "pass123") as AuthResult.Success).user
        val user2 = (userRepository.register("User Two", "user_two", "pass123", "pass123") as AuthResult.Success).user

        // Save a test for user 1
        testRepository.saveCompletedTest(
            userId = user1.id,
            title = "User 1 Exam",
            topicId = null,
            difficulty = "EASY",
            problems = emptyList(),
            userAnswers = emptyMap(),
            durationSeconds = 60,
            isMaxsus = false
        )

        // Verify user 1 has 1 test, user 2 has 0 tests
        val user1History = testRepository.getTestHistory(user1.id).first()
        val user2History = testRepository.getTestHistory(user2.id).first()

        assertEquals(1, user1History.size)
        assertEquals(0, user2History.size)

        // Verify user 1 and user 2 progress isolation
        val problem = ProblemEntity(
            id = "sep_p1",
            category = "Geometry",
            topicId = "geom_triangles",
            difficulty = "EASY",
            question = "Angle sum of a triangle?",
            optionA = "180",
            optionB = "360",
            optionC = "90",
            optionD = "270",
            correctAnswer = "A",
            solution = "Sum is 180",
            explanation = "Triangle postulate",
            questionHash = "sep_hash_1"
        )
        problemDao.insertProblems(listOf(problem))

        mathRepository.submitProblemAnswer(user1.id, problem, "A", 15)

        val user1Seen = questionHistoryDao.hasUserSeenProblem(user1.id, problem.id)
        val user2Seen = questionHistoryDao.hasUserSeenProblem(user2.id, problem.id)

        assertTrue("User 1 has answered problem", user1Seen)
        assertFalse("User 2 must not inherit User 1 answer history", user2Seen)
    }

    // 6. Active Test Exit Guard: Prevents accidental loss without locking device
    @Test
    fun `test active test view model back navigation request displays confirmation dialog`() = runBlocking {
        val registerRes = userRepository.register("Nav User", "nav_tester", "pass123", "pass123")
        assertTrue(registerRes is AuthResult.Success)

        val vm = TestViewModel(
            testRepository = testRepository,
            userRepository = userRepository,
            topicId = null,
            difficulty = "EASY",
            questionCount = 5,
            isMaxsus = false
        )

        // Request exit triggers confirmation dialog, doesn't immediately exit or lose state
        assertFalse(vm.uiState.value.showExitDialog)
        vm.requestExit()
        assertTrue("Exit request must prompt confirmation dialog", vm.uiState.value.showExitDialog)

        // Dismiss dialog returns user safely to the test
        vm.dismissExitDialog()
        assertFalse("Dismissing dialog returns safely to test", vm.uiState.value.showExitDialog)
    }

    // 7. REAL Database Count and Duplicate Protection Verification
    @Test
    fun `test real database count and duplicate protection`() = runBlocking {
        // Seed SampleMathData into DB
        database.topicDao().insertTopics(com.example.data.sample.SampleMathData.topics)
        database.problemDao().insertProblems(com.example.data.sample.SampleMathData.problems)
        database.specialContentDao().insertSpecialContents(com.example.data.sample.SampleMathData.specialContent)

        val totalProblems = problemDao.getProblemCountDirect()
        val totalTopics = topicDao.getTopicCount()
        val totalSpecialContent = specialContentDao.getSpecialContentCount()
        val totalMaxsusProblems = database.maxsusDao().getProblemCount()

        assertEquals("Problem count must match sample size", com.example.data.sample.SampleMathData.problems.size, totalProblems)
        assertEquals("Topics count must match sample", com.example.data.sample.SampleMathData.topics.size, totalTopics)

        // Duplicate check on questionHash
        val allProblems = problemDao.getProblemsPaged(1000, 0)
        val hashes = allProblems.map { it.questionHash }
        val duplicateHashesCount = hashes.size - hashes.toSet().size
        assertEquals("There must be 0 duplicate question hashes in problems table", 0, duplicateHashesCount)
    }
}
