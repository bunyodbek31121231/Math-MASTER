package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.database.entity.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics WHERE isMaxsus = 0 ORDER BY orderIndex ASC")
    fun getAllRegularTopics(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE isMaxsus = 1 ORDER BY orderIndex ASC")
    fun getAllMaxsusTopics(): Flow<List<TopicEntity>>

    @Query("SELECT DISTINCT category FROM topics WHERE isMaxsus = 0 ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM topics WHERE category = :category AND isMaxsus = 0 ORDER BY orderIndex ASC")
    fun getTopicsByCategory(category: String): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE id = :id LIMIT 1")
    suspend fun getTopicById(id: String): TopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicEntity>)

    @Query("SELECT COUNT(*) FROM topics")
    suspend fun getTopicCount(): Int

    @Query("SELECT * FROM topics ORDER BY orderIndex ASC")
    suspend fun getAllTopicsOnce(): List<TopicEntity>
}
