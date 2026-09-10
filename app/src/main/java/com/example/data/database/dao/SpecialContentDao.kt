package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.database.entity.SpecialContentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpecialContentDao {
    @Query("SELECT * FROM special_content WHERE topicId = :topicId ORDER BY orderIndex ASC")
    fun getContentForTopic(topicId: String): Flow<List<SpecialContentEntity>>

    @Query("SELECT * FROM special_content WHERE id = :id LIMIT 1")
    suspend fun getContentById(id: String): SpecialContentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpecialContents(contents: List<SpecialContentEntity>)

    @Query("SELECT COUNT(*) FROM special_content")
    suspend fun getSpecialContentCount(): Int
}
