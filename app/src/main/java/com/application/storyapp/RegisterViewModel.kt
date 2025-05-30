package com.application.storyapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.storyapp.model.RegisterUIState
import com.application.storyapp.network.AuthRepository
import com.application.storyapp.network.NetworkResult
import kotlinx.coroutines.launch


class RegisterViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(RegisterUIState())
    val uiState: LiveData<RegisterUIState> = _uiState

    // FIX: Add Event-based LiveData for dialogs
    private val _errorEvent = MutableLiveData<Event<String>>()
    val errorEvent: LiveData<Event<String>> = _errorEvent

    private val _successEvent = MutableLiveData<Event<Unit>>()
    val successEvent: LiveData<Event<Unit>> = _successEvent

    fun validateName(name: String) {
        val error = ValidationUtils.validateName(name)
        _uiState.value = _uiState.value?.copy(nameError = error)
    }

    fun validateEmail(email: String) {
        val error = ValidationUtils.validateEmail(email)
        _uiState.value = _uiState.value?.copy(emailError = error)
    }

    fun validatePassword(password: String) {
        val error = ValidationUtils.validatePassword(password)
        _uiState.value = _uiState.value?.copy(passwordError = error)
    }

    fun register(name: String, email: String, password: String) {
        val nameError = ValidationUtils.validateName(name)
        val emailError = ValidationUtils.validateEmail(email)
        val passwordError = ValidationUtils.validatePassword(password)

        if (nameError != null || emailError != null || passwordError != null) {
            _uiState.value = _uiState.value?.copy(
                nameError = nameError,
                emailError = emailError,
                passwordError = passwordError
            )
            return
        }

        _uiState.value = _uiState.value?.copy(
            isLoading = true,
            nameError = null,
            emailError = null,
            passwordError = null
        )

        viewModelScope.launch {
            when (val result = repository.register(name, email, password)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value?.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                    // FIX: Use Event to trigger dialog only once
                    _successEvent.value = Event(Unit)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value?.copy(
                        isLoading = false,
                        isSuccess = false
                    )
                    // FIX: Use Event to trigger dialog only once
                    val errorMessage = result.message ?: "An unexpected error occurred"
                    _errorEvent.value = Event(errorMessage)
                }
                is NetworkResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun clearErrors() {
        _uiState.value = _uiState.value?.copy(
            nameError = null,
            emailError = null,
            passwordError = null
        )
    }

    fun resetAllStates() {
        _uiState.value = RegisterUIState()
    }
}