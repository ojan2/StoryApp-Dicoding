package com.application.storyapp.data.response

import com.application.storyapp.model.Story

data class FileUploadResponse(
    val error: Boolean,
    val message: String,
    val listStory: List<Story>? = null
)