package com.application.storyapp

import android.content.Context
import com.application.storyapp.network.ApiClient
import com.application.storyapp.network.AuthRepository

object Injection {

    fun provideAuthRepository(context: Context): AuthRepository {
        val apiService = ApiClient.getApiService()
        val userPreferences = UserPreferences.getInstance(context)
        return AuthRepository.getInstance(apiService, userPreferences)
    }

    fun provideStoryRepository(context: Context): StoryRepository {
        val apiService = ApiClient.getApiService()
        val userPreferences = UserPreferences.getInstance(context)
        return StoryRepository.getInstance(apiService, userPreferences)
    }
}