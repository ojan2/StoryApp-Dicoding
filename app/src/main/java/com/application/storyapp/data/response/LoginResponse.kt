package com.application.storyapp.data.response

import com.application.storyapp.utils.LoginResult

data class LoginResponse(
    val error: Boolean,
    val message: String,
    val loginResult: LoginResult?
)