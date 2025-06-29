package com.application.storyapp.utils

// UI State data class
data class AddStoryUIState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val hasImage: Boolean = false,
    val descriptionError: String? = null,
    val imageError: String? = null
)