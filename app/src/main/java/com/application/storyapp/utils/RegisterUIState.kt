package com.application.storyapp.utils

data class RegisterUIState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null
)
