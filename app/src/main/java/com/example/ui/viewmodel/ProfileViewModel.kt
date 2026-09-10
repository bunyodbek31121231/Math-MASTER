package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.TestEntity
import com.example.data.database.entity.UserEntity
import com.example.data.database.entity.UserProgressEntity
import com.example.data.repository.MathRepository
import com.example.data.repository.TestRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val mathRepository: MathRepository,
    private val testRepository: TestRepository
) : ViewModel() {

    val activeUser: StateFlow<UserEntity?> = userRepository.activeUserId
        .flatMapLatest { userId ->
            if (userId != null) userRepository.getUser(userId) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val progressList: StateFlow<List<UserProgressEntity>> = userRepository.activeUserId
        .flatMapLatest { userId ->
            if (userId != null) mathRepository.getUserProgressList(userId) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val testHistory: StateFlow<List<TestEntity>> = userRepository.activeUserId
        .flatMapLatest { userId ->
            if (userId != null) testRepository.getTestHistory(userId) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    companion object {
        fun provideFactory(
            userRepository: UserRepository,
            mathRepository: MathRepository,
            testRepository: TestRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileViewModel(userRepository, mathRepository, testRepository) as T
                }
            }
    }
}
