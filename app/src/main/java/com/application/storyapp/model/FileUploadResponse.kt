package com.application.storyapp.model

data class FileUploadResponse(
    val error: Boolean,
    val message: String,
    val listStory: List<Story>? = null
)