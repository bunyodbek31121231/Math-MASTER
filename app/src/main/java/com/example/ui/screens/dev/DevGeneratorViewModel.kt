package com.example.ui.screens.dev

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.dao.ProblemDao
import com.example.generator.pipeline.GenerationJobDao
import com.example.generator.pipeline.GenerationJobEntity
import com.example.generator.service.DatabaseIntegrityChecker
import com.example.generator.service.DatabaseIntegrityReport
import com.example.generator.service.GenerationStats
import com.example.generator.service.ProblemGenerationService
import com.example.model.Difficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class DevGeneratorUiState(
    val isGenerating: Boolean = false,
    val progressCurrent: Int = 0,
    val progressTotal: Int = 0,
    val currentBatchProgress: Int = 0,
    val lastStats: GenerationStats? = null,
    val totalInDb: Int = 0,
    val categoryBreakdown: Map<String, Int> = emptyMap(),
    val difficultyBreakdown: Map<String, Int> = emptyMap(),
    val activeJob: GenerationJobEntity? = null,
    val integrityReport: DatabaseIntegrityReport? = null,
    val isCheckingIntegrity: Boolean = false,
    val statusMessage: String = "Engine ready."
)

class DevGeneratorViewModel(
    private val generationService: ProblemGenerationService,
    private val problemDao: ProblemDao,
    private val jobDao: GenerationJobDao? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevGeneratorUiState())
    val uiState: StateFlow<DevGeneratorUiState> = _uiState.asStateFlow()

    private val integrityChecker = DatabaseIntegrityChecker(problemDao)

    init {
        refreshDatabaseStats()
        observeActiveJob()
    }

    fun refreshDatabaseStats() {
        viewModelScope.launch {
            try {
                val count = problemDao.getProblemCountDirect()
                val cats = problemDao.getCategoryCounts().associate { it.category to it.count }
                val diffs = problemDao.getDifficultyCounts().associate { it.difficulty to it.count }
                _uiState.value = _uiState.value.copy(
                    totalInDb = count,
                    categoryBreakdown = cats,
                    difficultyBreakdown = diffs
                )
            } catch (e: Exception) {
                // Ignore initial error
            }
        }
    }

    private fun observeActiveJob() {
        jobDao?.let { dao ->
            viewModelScope.launch {
                dao.getAllJobs().collectLatest { jobs ->
                    val runningOrPaused = jobs.firstOrNull { it.status == "RUNNING" || it.status == "PAUSED" }
                    _uiState.value = _uiState.value.copy(activeJob = runningOrPaused)
                }
            }
        }
    }

    fun startOrResumeJob(
        targetTotal: Int,
        batchSize: Int,
        categories: List<String>?,
        difficulties: List<Difficulty>?
    ) {
        if (_uiState.value.isGenerating) return

        val active = _uiState.value.activeJob
        val jobId = active?.jobId ?: "job_target_${System.currentTimeMillis()}"

        _uiState.value = _uiState.value.copy(
            isGenerating = true,
            progressCurrent = active?.completedCount ?: 0,
            progressTotal = if (active != null) active.targetCount else targetTotal,
            statusMessage = "Executing generation pipeline towards target..."
        )

        viewModelScope.launch {
            try {
                val stats = generationService.executeJob(
                    jobId = jobId,
                    targetTotal = if (active != null) active.targetCount else targetTotal,
                    batchSize = batchSize,
                    categories = categories,
                    difficulties = difficulties,
                    onProgress = { cur, tot, batchDone ->
                        _uiState.value = _uiState.value.copy(
                            progressCurrent = cur,
                            progressTotal = tot,
                            currentBatchProgress = batchDone
                        )
                    }
                )

                refreshDatabaseStats()

                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    lastStats = stats,
                    statusMessage = "Batch run complete: ${stats.inserted} inserted, ${stats.duplicates} duplicates, ${stats.invalid} invalid."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    statusMessage = "Pipeline interrupted: ${e.localizedMessage}"
                )
            }
        }
    }

    fun pauseGeneration() {
        generationService.pauseActiveJob()
        _uiState.value = _uiState.value.copy(
            isGenerating = false,
            statusMessage = "Pausing generation... Safe commit in progress."
        )
    }

    fun cancelGeneration() {
        generationService.cancelActiveJob()
        _uiState.value = _uiState.value.copy(
            isGenerating = false,
            statusMessage = "Cancelled generation."
        )
    }

    fun runIntegrityCheck() {
        if (_uiState.value.isCheckingIntegrity) return
        _uiState.value = _uiState.value.copy(isCheckingIntegrity = true, statusMessage = "Verifying database integrity...")
        viewModelScope.launch {
            try {
                val report = integrityChecker.checkIntegrity(maxCheck = 2000)
                _uiState.value = _uiState.value.copy(
                    isCheckingIntegrity = false,
                    integrityReport = report,
                    statusMessage = if (report.isValid) "Database integrity verified: 100% healthy." else "Integrity warnings detected."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCheckingIntegrity = false,
                    statusMessage = "Integrity check failed: ${e.localizedMessage}"
                )
            }
        }
    }

    companion object {
        fun provideFactory(
            generationService: ProblemGenerationService,
            problemDao: ProblemDao,
            jobDao: GenerationJobDao? = null
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DevGeneratorViewModel(generationService, problemDao, jobDao) as T
                }
            }
    }
}
