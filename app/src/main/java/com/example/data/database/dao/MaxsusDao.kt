package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.database.entity.maxsus.MaxsusContentEntity
import com.example.data.database.entity.maxsus.MaxsusProblemEntity
import com.example.data.database.entity.maxsus.MaxsusSectionEntity
import com.example.data.database.entity.maxsus.MaxsusTopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaxsusDao {
    // Sections
    @Query("SELECT * FROM maxsus_sections ORDER BY orderIndex ASC")
    fun getAllSections(): Flow<List<MaxsusSectionEntity>>

    @Query("SELECT * FROM maxsus_sections ORDER BY orderIndex ASC")
    suspend fun getAllSectionsOnce(): List<MaxsusSectionEntity>

    @Query("SELECT * FROM maxsus_topics ORDER BY orderIndex ASC")
    suspend fun getAllTopicsOnce(): List<MaxsusTopicEntity>

    @Query("SELECT * FROM maxsus_contents ORDER BY orderIndex ASC")
    suspend fun getAllContentsOnce(): List<MaxsusContentEntity>

    @Query("SELECT * FROM maxsus_problems ORDER BY orderIndex ASC")
    suspend fun getAllProblemsOnce(): List<MaxsusProblemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: MaxsusSectionEntity)

    @Query("SELECT COUNT(*) FROM maxsus_sections")
    suspend fun getSectionCount(): Int

    // Topics
    @Query("SELECT * FROM maxsus_topics WHERE sectionId = :sectionId ORDER BY orderIndex ASC")
    fun getTopicsForSection(sectionId: String): Flow<List<MaxsusTopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: MaxsusTopicEntity)

    @Query("SELECT COUNT(*) FROM maxsus_topics")
    suspend fun getTopicCount(): Int

    // Contents (Theory, Formula, Example, Exercise)
    @Query("SELECT * FROM maxsus_contents WHERE topicId = :topicId ORDER BY orderIndex ASC")
    fun getContentForTopic(topicId: String): Flow<List<MaxsusContentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(content: MaxsusContentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContents(contents: List<MaxsusContentEntity>)

    @Query("SELECT COUNT(*) FROM maxsus_contents")
    suspend fun getContentCount(): Int

    // Problems (Tests)
    @Query("SELECT * FROM maxsus_problems WHERE topicId = :topicId ORDER BY orderIndex ASC")
    fun getProblemsForTopic(topicId: String): Flow<List<MaxsusProblemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblem(problem: MaxsusProblemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblems(problems: List<MaxsusProblemEntity>)

    @Query("SELECT COUNT(*) FROM maxsus_problems")
    suspend fun getProblemCount(): Int

    @Query("SELECT * FROM maxsus_problems WHERE questionHash = :hash LIMIT 1")
    suspend fun getProblemByHash(hash: String): MaxsusProblemEntity?

    // Search
    @Query("SELECT * FROM maxsus_contents WHERE body LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%'")
    fun searchContent(query: String): Flow<List<MaxsusContentEntity>>

    @Query("SELECT * FROM maxsus_problems WHERE question LIKE '%' || :query || '%'")
    fun searchProblems(query: String): Flow<List<MaxsusProblemEntity>>

    // Undo / Delete by import
    @Query("DELETE FROM maxsus_contents WHERE importId = :importId")
    suspend fun deleteContentByImport(importId: String)

    @Query("DELETE FROM maxsus_problems WHERE importId = :importId")
    suspend fun deleteProblemsByImport(importId: String)

    @Transaction
    suspend fun undoImport(importId: String) {
        deleteContentByImport(importId)
        deleteProblemsByImport(importId)
    }

    @Update
    suspend fun updateContent(content: MaxsusContentEntity)

    @Update
    suspend fun updateProblem(problem: MaxsusProblemEntity)

    @Delete
    suspend fun deleteContent(content: MaxsusContentEntity)

    @Delete
    suspend fun deleteProblem(problem: MaxsusProblemEntity)
}
