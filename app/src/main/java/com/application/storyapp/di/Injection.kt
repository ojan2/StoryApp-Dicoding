package com.application.storyapp.di

import android.content.Context
import com.application.storyapp.data.StoryRepository
import com.application.storyapp.data.data_store.UserPreferences
import com.application.storyapp.data.network.ApiClient
import com.application.storyapp.data.AuthRepository

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