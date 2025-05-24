package com.application.storyapp.model

data class LoginResponse(
    val error: Boolean,
    val message: String,
    val loginResult: LoginResult?
)