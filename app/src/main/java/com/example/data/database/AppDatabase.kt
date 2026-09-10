package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.database.dao.AdminDao
import com.example.data.database.dao.MaxsusDao
import com.example.data.database.dao.ProblemDao
import com.example.data.database.dao.QuestionHistoryDao
import com.example.data.database.dao.SpecialContentDao
import com.example.data.database.dao.TestDao
import com.example.data.database.dao.TopicDao
import com.example.data.database.dao.UserDao
import com.example.data.database.dao.UserProgressDao
import com.example.data.database.entity.ProblemEntity
import com.example.data.database.entity.QuestionHistoryEntity
import com.example.data.database.entity.SpecialContentEntity
import com.example.data.database.entity.TestEntity
import com.example.data.database.entity.TestQuestionEntity
import com.example.data.database.entity.TopicEntity
import com.example.data.database.entity.UserEntity
import com.example.data.database.entity.UserProgressEntity
import com.example.data.database.entity.admin.ImportHistoryEntity
import com.example.data.database.entity.maxsus.MaxsusContentEntity
import com.example.data.database.entity.maxsus.MaxsusProblemEntity
import com.example.data.database.entity.maxsus.MaxsusSectionEntity
import com.example.data.database.entity.maxsus.MaxsusTopicEntity
import com.example.generator.pipeline.GenerationJobDao
import com.example.generator.pipeline.GenerationJobEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        ProblemEntity::class,
        QuestionHistoryEntity::class,
        TestEntity::class,
        TestQuestionEntity::class,
        UserProgressEntity::class,
        TopicEntity::class,
        SpecialContentEntity::class,
        GenerationJobEntity::class,
        MaxsusSectionEntity::class,
        MaxsusTopicEntity::class,
        MaxsusContentEntity::class,
        MaxsusProblemEntity::class,
        ImportHistoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun problemDao(): ProblemDao
    abstract fun questionHistoryDao(): QuestionHistoryDao
    abstract fun testDao(): TestDao
    abstract fun topicDao(): TopicDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun specialContentDao(): SpecialContentDao
    abstract fun generationJobDao(): GenerationJobDao
    abstract fun maxsusDao(): MaxsusDao
    abstract fun adminDao(): AdminDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, onFirstCreate: (suspend (AppDatabase) -> Unit)? = null): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "math_master_database.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Note: Using a separate CoroutineScope to avoid blocking the main thread
                        // during initial data population if needed.
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
