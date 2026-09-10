package com.example.di

import android.content.Context
import com.example.data.database.AppDatabase
import com.example.data.importer.ProblemImporter
import com.example.data.preferences.UserPreferences
import com.example.data.repository.MathRepository
import com.example.data.repository.MaxsusRepository
import com.example.data.repository.TestRepository
import com.example.data.repository.UserRepository
import com.example.data.repository.admin.AdminAuthRepository
import com.example.data.repository.admin.AdminAuthRepositoryImpl
import com.example.data.repository.admin.AdminRepository
import com.example.data.sample.SampleMathData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppContainer(private val context: Context) {

    val userPreferences = UserPreferences(context)

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context) { db ->
            // Pre-populate initial sample data on database first creation
            db.topicDao().insertTopics(SampleMathData.topics)
            db.problemDao().insertProblems(SampleMathData.problems)
            db.specialContentDao().insertSpecialContents(SampleMathData.specialContent)
        }
    }

    val userRepository: UserRepository by lazy {
        UserRepository(database.userDao(), userPreferences)
    }

    val mathRepository: MathRepository by lazy {
        MathRepository(
            database.problemDao(),
            database.questionHistoryDao(),
            database.topicDao(),
            database.userProgressDao(),
            userRepository
        )
    }

    val testRepository: TestRepository by lazy {
        TestRepository(database.testDao(), database.problemDao(), userRepository)
    }

    val maxsusRepository: MaxsusRepository by lazy {
        MaxsusRepository(
            database.topicDao(),
            database.specialContentDao(),
            database.problemDao(),
            database.maxsusDao(),
            userPreferences
        )
    }

    val problemImporter: ProblemImporter by lazy {
        ProblemImporter(database.problemDao())
    }

    val problemGenerationService: com.example.generator.service.ProblemGenerationService by lazy {
        com.example.generator.service.ProblemGenerationService(
            problemDao = database.problemDao(),
            jobDao = database.generationJobDao()
        )
    }

    val adminAuthRepository: AdminAuthRepository by lazy {
        AdminAuthRepositoryImpl()
    }

    val adminRepository: AdminRepository by lazy {
        AdminRepository(
            database.userDao(),
            database.problemDao(),
            database.topicDao(),
            database.testDao(),
            database.maxsusDao(),
            database.adminDao()
        )
    }

    init {
        // Ensure initial topics and problems exist (in case database already existed without them)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (database.topicDao().getTopicCount() == 0) {
                    database.topicDao().insertTopics(SampleMathData.topics)
                }
                database.problemDao().insertProblems(SampleMathData.problems)
                if (database.specialContentDao().getSpecialContentCount() == 0) {
                    database.specialContentDao().insertSpecialContents(SampleMathData.specialContent)
                }
            } catch (e: Exception) {
                // Database ready
            }
        }
    }
}
