package com.example.ui.admin

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.admin.ImportHistoryEntity
import com.example.data.database.entity.maxsus.*
import com.example.data.repository.admin.AdminAuthRepository
import com.example.data.repository.admin.AdminRepository
import com.example.util.admin.AnalyzedMaterial
import com.example.util.admin.DatabaseBackupManager
import com.example.util.admin.GeminiMaterialAnalyzer
import com.example.util.admin.PdfTextExtractor
import com.example.data.database.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class AdminUiState {
    object Idle : AdminUiState()
    object Loading : AdminUiState()
    data class Success(val message: String) : AdminUiState()
    data class Error(val message: String) : AdminUiState()
}

class AdminViewModel(
    private val authRepository: AdminAuthRepository,
    private val adminRepository: AdminRepository,
    private val database: AppDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Idle)
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private val _dashboardStats = MutableStateFlow<Map<String, Int>>(emptyMap())
    val dashboardStats: StateFlow<Map<String, Int>> = _dashboardStats.asStateFlow()

    private val _previewMaterials = MutableStateFlow<List<AnalyzedMaterial>>(emptyList())
    val previewMaterials: StateFlow<List<AnalyzedMaterial>> = _previewMaterials.asStateFlow()

    private val _selectedIndices = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIndices: StateFlow<Set<Int>> = _selectedIndices.asStateFlow()

    private val _importHistory = MutableStateFlow<List<ImportHistoryEntity>>(emptyList())
    val importHistory: StateFlow<List<ImportHistoryEntity>> = _importHistory.asStateFlow()

    private val analyzer = GeminiMaterialAnalyzer()

    init {
        loadStats()
        loadHistory()
    }

    fun loadStats() {
        viewModelScope.launch {
            _dashboardStats.value = adminRepository.getDashboardStats()
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            adminRepository.getImportHistory().collect {
                _importHistory.value = it
            }
        }
    }

    fun login(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            val result = authRepository.login(username, password)
            result.onSuccess {
                _uiState.value = AdminUiState.Idle
                onResult(true)
            }.onFailure {
                _uiState.value = AdminUiState.Error(it.message ?: "Xatolik")
                onResult(false)
            }
        }
    }

    fun handleFileImport(context: Context, uri: Uri, fileName: String) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                val textResult = if (fileName.lowercase().endsWith(".pdf") || context.contentResolver.getType(uri)?.contains("pdf") == true) {
                    PdfTextExtractor.extractText(context, uri)
                } else {
                    val rawText = context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() } ?: ""
                    if (rawText.isBlank()) Result.failure(IllegalArgumentException("Fayl bo'sh yoki o'qib bo'lmadi"))
                    else Result.success(rawText)
                }

                textResult.onSuccess { text ->
                    val materials = analyzer.analyzeText(text)
                    if (materials.isEmpty()) {
                        _uiState.value = AdminUiState.Error("Fayldan mos materiallar topilmadi")
                        return@launch
                    }
                    _previewMaterials.value = materials
                    _selectedIndices.value = materials.indices.toSet()
                    _uiState.value = AdminUiState.Success("Fayl tahlil qilindi")
                }.onFailure { error ->
                    _uiState.value = AdminUiState.Error(error.message ?: "PDF o'qishda xatolik")
                }
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error("Import xatoligi: ${e.localizedMessage}")
            }
        }
    }

    fun toggleSelection(index: Int) {
        val current = _selectedIndices.value.toMutableSet()
        if (current.contains(index)) current.remove(index) else current.add(index)
        _selectedIndices.value = current
    }

    fun toggleSelectionByType(type: String) {
        val materials = _previewMaterials.value
        val current = _selectedIndices.value.toMutableSet()
        val matchingIndices = materials.indices.filter { materials[it].type == type || (type == "THEORY_FORMULA" && (materials[it].type == "THEORY" || materials[it].type == "FORMULA")) }
        
        val allSelected = matchingIndices.all { current.contains(it) }
        if (allSelected) {
            current.removeAll(matchingIndices.toSet())
        } else {
            current.addAll(matchingIndices)
        }
        _selectedIndices.value = current
    }

    fun updateMaterial(index: Int, updated: AnalyzedMaterial) {
        val current = _previewMaterials.value.toMutableList()
        if (index in current.indices) {
            current[index] = updated
            _previewMaterials.value = current
        }
    }

    private fun calculateHash(input: String): String {
        val normalized = input.lowercase().trim().replace(Regex("\\s+"), " ")
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val digest = md.digest(normalized.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun finalizeImport(fileName: String) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                val selected = _previewMaterials.value.filterIndexed { index, _ -> _selectedIndices.value.contains(index) }
                val importId = UUID.randomUUID().toString()
                
                var importedCount = 0
                var duplicateCount = 0
                var lastSectionId: String? = null
                var lastTopicId: String? = null

                selected.forEach { mat ->
                    when (mat.type) {
                        "SECTION" -> {
                            val section = MaxsusSectionEntity(
                                id = UUID.randomUUID().toString(),
                                title = mat.title ?: "Bo'lim"
                            )
                            adminRepository.recordSection(section)
                            lastSectionId = section.id
                            importedCount++
                        }
                        "TOPIC" -> {
                            val targetSectionId = lastSectionId ?: "default_section"
                            val topic = MaxsusTopicEntity(
                                id = UUID.randomUUID().toString(),
                                sectionId = targetSectionId,
                                title = mat.title ?: "Mavzu"
                            )
                            adminRepository.recordTopic(topic)
                            lastTopicId = topic.id
                            importedCount++
                        }
                        "THEORY", "FORMULA", "EXAMPLE", "EXERCISE" -> {
                            val targetTopicId = lastTopicId ?: "default_topic"
                            val content = MaxsusContentEntity(
                                id = UUID.randomUUID().toString(),
                                topicId = targetTopicId,
                                type = try { MaxsusContentType.valueOf(mat.type) } catch (_: Exception) { MaxsusContentType.THEORY },
                                title = mat.title,
                                body = mat.body ?: mat.question ?: "",
                                formula = mat.formula,
                                solution = mat.solution,
                                explanation = mat.explanation,
                                importId = importId
                            )
                            adminRepository.recordContent(content)
                            importedCount++
                        }
                        "TEST" -> {
                            val targetTopicId = lastTopicId ?: "default_topic"
                            val qText = mat.question ?: mat.body ?: ""
                            val hash = calculateHash("$qText|${mat.optionA}|${mat.optionB}|${mat.optionC}|${mat.optionD}")

                            val problem = MaxsusProblemEntity(
                                id = UUID.randomUUID().toString(),
                                topicId = targetTopicId,
                                question = qText,
                                optionA = mat.optionA ?: "",
                                optionB = mat.optionB ?: "",
                                optionC = mat.optionC ?: "",
                                optionD = mat.optionD ?: "",
                                correctAnswer = mat.correctAnswer ?: "A",
                                solution = mat.solution,
                                explanation = mat.explanation,
                                formula = mat.formula,
                                questionHash = hash,
                                importId = importId
                            )
                            val inserted = adminRepository.recordProblemWithResult(problem)
                            if (inserted) {
                                importedCount++
                            } else {
                                duplicateCount++
                            }
                        }
                    }
                }

                val history = ImportHistoryEntity(
                    id = importId,
                    fileName = fileName,
                    fileType = if (fileName.contains(".")) fileName.substringAfterLast(".") else "PDF/TXT",
                    totalFound = _previewMaterials.value.size,
                    selectedCount = selected.size,
                    importedCount = importedCount,
                    errorCount = duplicateCount
                )
                adminRepository.recordImport(history)
                
                val msg = if (duplicateCount > 0) {
                    "$importedCount ta material import qilindi ($duplicateCount ta takroriy o'tkazib yuborildi)"
                } else {
                    "$importedCount ta material import qilindi"
                }
                
                _uiState.value = AdminUiState.Success(msg)
                _previewMaterials.value = emptyList()
                loadStats()
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error("Xatolik: ${e.localizedMessage}")
            }
        }
    }

    fun undoLastImport() {
        viewModelScope.launch {
            adminRepository.undoLastImport()
            loadStats()
        }
    }

    fun createBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            DatabaseBackupManager.createBackup(context, database, uri)
                .onSuccess { _uiState.value = AdminUiState.Success("Zaxira nusxa yaratildi") }
                .onFailure { _uiState.value = AdminUiState.Error("Backup xatoligi: ${it.message}") }
        }
    }

    fun restoreBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            DatabaseBackupManager.restoreBackup(context, database, uri)
                .onSuccess { 
                    _uiState.value = AdminUiState.Success("Ma'lumotlar tiklandi")
                    loadStats()
                }
                .onFailure { _uiState.value = AdminUiState.Error("Restore xatoligi: ${it.message}") }
        }
    }

    companion object {
        fun provideFactory(
            authRepository: AdminAuthRepository,
            adminRepository: AdminRepository,
            database: AppDatabase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AdminViewModel(authRepository, adminRepository, database) as T
            }
        }
    }
}
