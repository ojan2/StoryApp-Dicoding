package com.application.storyapp.utils

sealed class HomeUIState {
    object Loading : HomeUIState()
    object Success : HomeUIState()
    data class Error(val message: String?) : HomeUIState()
}