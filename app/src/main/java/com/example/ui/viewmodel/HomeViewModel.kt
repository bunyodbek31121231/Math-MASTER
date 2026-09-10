package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.QuestionHistoryEntity
import com.example.data.database.entity.TopicEntity
import com.example.data.database.entity.UserEntity
import com.example.data.repository.MathRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val user: UserEntity? = null,
    val topics: List<TopicEntity> = emptyList(),
    val recentHistory: List<QuestionHistoryEntity> = emptyList(),
    val totalProblemCount: Int = 0,
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val userRepository: UserRepository,
    private val mathRepository: MathRepository
) : ViewModel() {

    val activeUser: StateFlow<UserEntity?> = userRepository.activeUserId
        .flatMapLatest { userId ->
            if (userId != null) userRepository.getUser(userId) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val topics: StateFlow<List<TopicEntity>> = mathRepository.allTopics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalProblems: StateFlow<Int> = mathRepository.totalProblemCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentHistory: StateFlow<List<QuestionHistoryEntity>> = userRepository.activeUserId
        .flatMapLatest { userId ->
            if (userId != null) mathRepository.getRecentHistory(userId, 5) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    companion object {
        fun provideFactory(
            userRepository: UserRepository,
            mathRepository: MathRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(userRepository, mathRepository) as T
                }
            }
    }
}
