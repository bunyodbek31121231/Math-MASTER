package com.example.generator.pipeline

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GenerationJobDao {

    @Query("SELECT * FROM generation_jobs ORDER BY updatedAt DESC")
    fun getAllJobs(): Flow<List<GenerationJobEntity>>

    @Query("SELECT * FROM generation_jobs WHERE jobId = :jobId")
    suspend fun getJobById(jobId: String): GenerationJobEntity?

    @Query("SELECT * FROM generation_jobs WHERE status = 'RUNNING' OR status = 'PAUSED' ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getActiveOrPausedJob(): GenerationJobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: GenerationJobEntity)

    @Update
    suspend fun updateJob(job: GenerationJobEntity)

    @Query("UPDATE generation_jobs SET completedCount = :completedCount, updatedAt = :updatedAt WHERE jobId = :jobId")
    suspend fun updateProgress(jobId: String, completedCount: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE generation_jobs SET status = :status, updatedAt = :updatedAt, errorDetails = :errorDetails WHERE jobId = :jobId")
    suspend fun updateStatus(jobId: String, status: String, errorDetails: String? = null, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM generation_jobs WHERE jobId = :jobId")
    suspend fun deleteJob(jobId: String)
}
