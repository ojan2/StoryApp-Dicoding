package com.application.storyapp.presentation.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.storyapp.utils.Event
import com.application.storyapp.utils.ValidationUtils
import com.application.storyapp.utils.RegisterUIState
import com.application.storyapp.data.AuthRepository
import com.application.storyapp.data.network.NetworkResult
import kotlinx.coroutines.launch


class RegisterViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(RegisterUIState())
    val uiState: LiveData<RegisterUIState> = _uiState

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
                    _successEvent.value = Event(Unit)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value?.copy(
                        isLoading = false,
                        isSuccess = false
                    )

                    val errorMessage = result.message ?: "An unexpected error occurred"
                    _errorEvent.value = Event(errorMessage)
                }
                is NetworkResult.Loading -> {
                }
            }
        }
    }
}