package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.ProblemEntity
import com.example.data.repository.MathRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class PracticeUiState(
    val currentProblem: ProblemEntity? = null,
    val selectedOption: String? = null,
    val isAnswerRevealed: Boolean = false,
    val isAnswerCorrect: Boolean = false,
    val secondsElapsed: Int = 0,
    val solvedInSession: Int = 0,
    val correctInSession: Int = 0,
    val isLoading: Boolean = true,
    val isSessionFinished: Boolean = false,
    val errorMessage: String? = null
)

class PracticeViewModel(
    private val mathRepository: MathRepository,
    private val userRepository: UserRepository,
    private val initialTopicId: String?,
    private val initialDifficulty: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadNextProblem()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_uiState.value.isAnswerRevealed) {
                    _uiState.value = _uiState.value.copy(
                        secondsElapsed = _uiState.value.secondsElapsed + 1
                    )
                }
            }
        }
    }

    fun selectOption(option: String) {
        if (!_uiState.value.isAnswerRevealed) {
            _uiState.value = _uiState.value.copy(selectedOption = option)
        }
    }

    fun submitAnswer() {
        val state = _uiState.value
        val problem = state.currentProblem ?: return
        val selected = state.selectedOption ?: return

        viewModelScope.launch {
            val userId = userRepository.activeUserId.firstOrNull() ?: "guest"
            val isCorrect = mathRepository.submitProblemAnswer(
                userId = userId,
                problem = problem,
                selectedAnswer = selected,
                timeSpentSeconds = state.secondsElapsed,
                mode = "PRACTICE"
            )

            _uiState.value = state.copy(
                isAnswerRevealed = true,
                isAnswerCorrect = isCorrect,
                solvedInSession = state.solvedInSession + 1,
                correctInSession = state.correctInSession + (if (isCorrect) 1 else 0)
            )
        }
    }

    fun loadNextProblem() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            selectedOption = null,
            isAnswerRevealed = false,
            secondsElapsed = 0,
            errorMessage = null
        )

        viewModelScope.launch {
            val userId = userRepository.activeUserId.firstOrNull() ?: "guest"
            val problem = mathRepository.getNextPracticeProblem(
                userId = userId,
                topicId = initialTopicId?.ifEmpty { null },
                difficulty = initialDifficulty?.ifEmpty { null }
            )

            if (problem != null) {
                _uiState.value = _uiState.value.copy(
                    currentProblem = problem,
                    isLoading = false
                )
                startTimer()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No practice problems available for this selection."
                )
            }
        }
    }

    fun finishSession() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(isSessionFinished = true)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    companion object {
        fun provideFactory(
            mathRepository: MathRepository,
            userRepository: UserRepository,
            topicId: String?,
            difficulty: String?
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PracticeViewModel(mathRepository, userRepository, topicId, difficulty) as T
                }
            }
    }
}
