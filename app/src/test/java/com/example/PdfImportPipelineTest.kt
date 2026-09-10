package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.database.entity.maxsus.MaxsusContentType
import com.example.data.database.entity.maxsus.MaxsusProblemEntity
import com.example.data.database.entity.maxsus.MaxsusSectionEntity
import com.example.data.database.entity.maxsus.MaxsusTopicEntity
import com.example.data.repository.admin.AdminRepository
import com.example.util.admin.PdfTextExtractor
import com.example.util.admin.RuleBasedMaterialAnalyzer
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
import java.io.ByteArrayOutputStream
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PdfImportPipelineTest {

    private lateinit var database: AppDatabase
    private lateinit var adminRepository: AdminRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        adminRepository = AdminRepository(
            userDao = database.userDao(),
            problemDao = database.problemDao(),
            topicDao = database.topicDao(),
            testDao = database.testDao(),
            maxsusDao = database.maxsusDao(),
            adminDao = database.adminDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testPdfTextExtractorWithInvalidHeader() {
        val invalidBytes = "Hello World, not a PDF".toByteArray()
        val result = PdfTextExtractor.extractTextFromBytes(invalidBytes)

        assertTrue(result.isFailure)
        assertEquals("Fayl PDF formatida emas yoki buzilgan", result.exceptionOrNull()?.message)
    }

    @Test
    fun testPdfTextExtractorWithEmptyBytes() {
        val emptyBytes = ByteArray(0)
        val result = PdfTextExtractor.extractTextFromBytes(emptyBytes)

        assertTrue(result.isFailure)
        assertEquals("PDF fayli bo'sh", result.exceptionOrNull()?.message)
    }

    @Test
    fun testPdfTextExtractorWithScannedImagePdf() {
        val scannedPdfDummy = ("%PDF-1.4\n" +
                "1 0 obj\n<</Type /Page /Contents 2 0 R>>\nendobj\n" +
                "2 0 obj\n<</Filter /FlateDecode /Length 10>>\nstream\n/Subtype /Image\nendstream\nendobj\n" +
                "%%EOF").toByteArray()

        val result = PdfTextExtractor.extractTextFromBytes(scannedPdfDummy)

        assertTrue(result.isFailure)
        assertEquals("Bu PDF skanerlangan rasm ko'rinishida. OCR kerak.", result.exceptionOrNull()?.message)
    }

    @Test
    fun testPdfTextExtractorWithValidTextPdfStream() {
        val validPdfContent = ("%PDF-1.4\n" +
                "1 0 obj\n<</Type /Page /Contents 2 0 R>>\nendobj\n" +
                "2 0 obj\n<</Length 40>>\nstream\n" +
                "BT\n(1-BO'LIM) Tj\nT*\n(1-Mavzu. Ratsional tenglamalar) Tj\nET\n" +
                "endstream\nendobj\n" +
                "%%EOF").toByteArray()

        val result = PdfTextExtractor.extractTextFromBytes(validPdfContent)

        assertTrue(result.isSuccess)
        val extracted = result.getOrNull()
        assertNotNull(extracted)
        assertTrue(extracted!!.contains("1-BO'LIM"))
        assertTrue(extracted.contains("Ratsional tenglamalar"))
    }

    @Test
    fun testRuleBasedMaterialAnalyzerParsing() {
        val sampleText = """
            1-BO'LIM. ALGEBRA
            1-Mavzu. Kvadrat tenglamalar
            
            Kvadrat tenglama ax^2 + bx + c = 0 ko'rinishida bo'ladi.
            
            1-misol. x^2 - 5x + 6 = 0 tenglamani yeching.
            
            1. x^2 - 4 = 0 tenglamaning ildizlarini toping.
            A) -2, 2
            B) 0, 4
            C) 1, 3
            D) -1, 1
            Javob: A
        """.trimIndent()

        val materials = RuleBasedMaterialAnalyzer.analyzeText(sampleText)

        assertTrue(materials.isNotEmpty())
        assertTrue(materials.any { it.type == "SECTION" && it.title!!.contains("1-BO'LIM") })
        assertTrue(materials.any { it.type == "TOPIC" && it.title!!.contains("1-Mavzu") })
        assertTrue(materials.any { it.type == "EXAMPLE" })
        assertTrue(materials.any { it.type == "TEST" && it.question!!.contains("tenglamaning ildizlarini toping") })
    }

    @Test
    fun testMaxsusImportAndDuplicateProtection() = runBlocking {
        val section = MaxsusSectionEntity(id = "sec1", title = "Algebra")
        val topic = MaxsusTopicEntity(id = "top1", sectionId = "sec1", title = "Tenglamalar")
        adminRepository.recordSection(section)
        adminRepository.recordTopic(topic)

        val hash = "test_hash_unique_123"
        val problem1 = MaxsusProblemEntity(
            id = "prob1",
            topicId = "top1",
            question = "x + 2 = 5 tenglamani yeching",
            optionA = "3",
            optionB = "4",
            optionC = "5",
            optionD = "6",
            correctAnswer = "A",
            questionHash = hash,
            importId = "imp1"
        )

        val inserted1 = adminRepository.recordProblemWithResult(problem1)
        assertTrue(inserted1)

        // Attempting to insert duplicate with same hash
        val problemDuplicate = problem1.copy(id = "prob2", importId = "imp2")
        val inserted2 = adminRepository.recordProblemWithResult(problemDuplicate)
        assertFalse(inserted2)

        val problemCount = database.maxsusDao().getProblemCount()
        assertEquals(1, problemCount)
    }
}
