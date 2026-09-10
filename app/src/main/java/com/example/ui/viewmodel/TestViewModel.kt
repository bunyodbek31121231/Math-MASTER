package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.ProblemEntity
import com.example.data.repository.CompletedTestSummary
import com.example.data.repository.TestRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class ActiveTestUiState(
    val questions: List<ProblemEntity> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswers: Map<String, String> = emptyMap(), // problemId -> option
    val secondsElapsed: Int = 0,
    val isLoading: Boolean = true,
    val showExitDialog: Boolean = false,
    val isTestCompleted: Boolean = false,
    val completedSummary: CompletedTestSummary? = null,
    val errorMessage: String? = null
)

class TestViewModel(
    private val testRepository: TestRepository,
    private val userRepository: UserRepository,
    private val topicId: String?,
    private val difficulty: String,
    private val questionCount: Int,
    private val isMaxsus: Boolean
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveTestUiState())
    val uiState: StateFlow<ActiveTestUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadTestQuestions()
    }

    private fun loadTestQuestions() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val problems = testRepository.generateTestProblems(
                topicId = topicId?.ifEmpty { null },
                difficulty = difficulty,
                count = questionCount
            )

            if (problems.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    questions = problems,
                    isLoading = false
                )
                startTimer()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No questions found for the chosen criteria."
                )
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    secondsElapsed = _uiState.value.secondsElapsed + 1
                )
            }
        }
    }

    fun selectOption(option: String) {
        val currentQuestion = _uiState.value.questions.getOrNull(_uiState.value.currentIndex) ?: return
        val currentMap = _uiState.value.selectedAnswers.toMutableMap()
        currentMap[currentQuestion.id] = option
        _uiState.value = _uiState.value.copy(selectedAnswers = currentMap)
    }

    fun nextQuestion() {
        val next = _uiState.value.currentIndex + 1
        if (next < _uiState.value.questions.size) {
            _uiState.value = _uiState.value.copy(currentIndex = next)
        }
    }

    fun prevQuestion() {
        val prev = _uiState.value.currentIndex - 1
        if (prev >= 0) {
            _uiState.value = _uiState.value.copy(currentIndex = prev)
        }
    }

    fun goToQuestion(index: Int) {
        if (index in _uiState.value.questions.indices) {
            _uiState.value = _uiState.value.copy(currentIndex = index)
        }
    }

    fun requestExit() {
        _uiState.value = _uiState.value.copy(showExitDialog = true)
    }

    fun dismissExitDialog() {
        _uiState.value = _uiState.value.copy(showExitDialog = false)
    }

    fun finishTest() {
        timerJob?.cancel()
        val state = _uiState.value
        viewModelScope.launch {
            val userId = userRepository.activeUserId.firstOrNull() ?: "guest"
            val title = if (isMaxsus) "MAXSUS Math Test" else "Math MASTER Test: $difficulty"

            val summary = testRepository.saveCompletedTest(
                userId = userId,
                title = title,
                topicId = topicId,
                difficulty = difficulty,
                problems = state.questions,
                userAnswers = state.selectedAnswers,
                durationSeconds = state.secondsElapsed,
                isMaxsus = isMaxsus
            )

            _uiState.value = state.copy(
                isTestCompleted = true,
                completedSummary = summary,
                showExitDialog = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    companion object {
        fun provideFactory(
            testRepository: TestRepository,
            userRepository: UserRepository,
            topicId: String?,
            difficulty: String,
            questionCount: Int,
            isMaxsus: Boolean
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TestViewModel(
                        testRepository,
                        userRepository,
                        topicId,
                        difficulty,
                        questionCount,
                        isMaxsus
                    ) as T
                }
            }
    }
}
