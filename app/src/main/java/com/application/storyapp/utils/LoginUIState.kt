package com.application.storyapp.utils

data class LoginUIState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null
)