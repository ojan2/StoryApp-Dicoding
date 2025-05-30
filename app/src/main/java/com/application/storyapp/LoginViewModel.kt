package com.application.storyapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.storyapp.model.LoginResult
import com.application.storyapp.model.LoginUIState
import com.application.storyapp.network.AuthRepository
import com.application.storyapp.network.NetworkResult
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(LoginUIState())
    val uiState: LiveData<LoginUIState> = _uiState

    // Event-based LiveData for dialogs and navigation
    private val _errorEvent = MutableLiveData<Event<String>>()
    val errorEvent: LiveData<Event<String>> = _errorEvent

    private val _successEvent = MutableLiveData<Event<LoginResult>>()
    val successEvent: LiveData<Event<LoginResult>> = _successEvent

    private val _navigateToMainEvent = MutableLiveData<Event<Unit>>()
    val navigateToMainEvent: LiveData<Event<Unit>> = _navigateToMainEvent

    fun validateEmail(email: String) {
        val error = ValidationUtils.validateEmail(email)
        _uiState.value = _uiState.value?.copy(emailError = error)
    }

    fun validatePassword(password: String) {
        val error = ValidationUtils.validatePassword(password)
        _uiState.value = _uiState.value?.copy(passwordError = error)
    }

    fun login(email: String, password: String) {
        val emailError = ValidationUtils.validateEmail(email)
        val passwordError = ValidationUtils.validatePassword(password)

        if (emailError != null || passwordError != null) {
            _uiState.value = _uiState.value?.copy(
                emailError = emailError,
                passwordError = passwordError
            )
            return
        }

        _uiState.value = _uiState.value?.copy(
            isLoading = true,
            emailError = null,
            passwordError = null
        )

        viewModelScope.launch {
            when (val result = repository.login(email, password)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value?.copy(
                        isLoading = false,
                        isSuccess = true
                    )

                    result.data?.loginResult?.let { loginResult ->
                        _successEvent.value = Event(
                            LoginResult(
                                userId = loginResult.userId,
                                name = loginResult.name,
                                token = loginResult.token
                            )
                        )
                        // Navigate to main after successful login
                        _navigateToMainEvent.value = Event(Unit)
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value?.copy(
                        isLoading = false,
                        isSuccess = false
                    )
                    val errorMessage = result.message ?: "Login failed. Please try again."
                    _errorEvent.value = Event(errorMessage)
                }
                is NetworkResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    // Clear error states
    fun clearErrors() {
        _uiState.value = _uiState.value?.copy(
            emailError = null,
            passwordError = null
        )
    }

    // Reset UI state
    fun resetState() {
        _uiState.value = LoginUIState()
    }
}