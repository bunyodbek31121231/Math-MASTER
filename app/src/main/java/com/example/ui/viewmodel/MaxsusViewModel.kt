package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.SpecialContentEntity
import com.example.data.database.entity.TopicEntity
import com.example.data.repository.MaxsusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MaxsusGateUiState(
    val passcodeInput: String = "",
    val isUnlocked: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null
)

class MaxsusViewModel(
    private val maxsusRepository: MaxsusRepository
) : ViewModel() {

    private val _gateState = MutableStateFlow(MaxsusGateUiState())
    val gateState: StateFlow<MaxsusGateUiState> = _gateState.asStateFlow()

    val isSectionUnlocked: StateFlow<Boolean> = maxsusRepository.isUnlocked
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val maxsusTopics: StateFlow<List<TopicEntity>> = maxsusRepository.maxsusTopics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onPasscodeChange(code: String) {
        _gateState.value = _gateState.value.copy(passcodeInput = code, isError = false, errorMessage = null)
    }

    fun verifyPasscode(onSuccess: () -> Unit) {
        val input = _gateState.value.passcodeInput
        viewModelScope.launch {
            val verified = maxsusRepository.verifyAndUnlock(input)
            if (verified) {
                _gateState.value = _gateState.value.copy(
                    isUnlocked = true,
                    isError = false,
                    errorMessage = null
                )
                onSuccess()
            } else {
                _gateState.value = _gateState.value.copy(
                    isError = true,
                    errorMessage = "Invalid passcode. Please enter the authorized access code."
                )
            }
        }
    }

    fun getContentForTopic(topicId: String): StateFlow<List<SpecialContentEntity>> {
        return maxsusRepository.getContentForTopic(topicId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    companion object {
        fun provideFactory(maxsusRepository: MaxsusRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MaxsusViewModel(maxsusRepository) as T
                }
            }
    }
}
