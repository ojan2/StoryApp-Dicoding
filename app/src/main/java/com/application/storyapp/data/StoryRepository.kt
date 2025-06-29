package com.application.storyapp.data

import android.content.Context
import com.application.storyapp.data.data_store.UserPreferences
import com.application.storyapp.data.response.ErrorResponse
import com.application.storyapp.data.response.FileUploadResponse
import com.application.storyapp.data.response.GetAllStoriesResponse
import com.application.storyapp.data.network.ApiService
import com.application.storyapp.data.network.NetworkResult
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.io.File

class StoryRepository private constructor(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences
) {

    // Upload story with image
    suspend fun uploadStory(
        imageFile: File,
        description: String,
        lat: Float? = null,
        lon: Float? = null
    ): NetworkResult<FileUploadResponse> {
        return try {
            // Get token from preferences
            val token = userPreferences.getAuthToken().first()
            if (token.isNullOrEmpty()) {
                return NetworkResult.Error("Authentication token not found. Please login again.")
            }

            // Prepare image file
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val imageMultipart = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImageFile
            )

            // Prepare description
            val descriptionRequestBody = description.toRequestBody("text/plain".toMediaType())

            // Prepare optional location data
            val latRequestBody = lat?.toString()?.toRequestBody("text/plain".toMediaType())
            val lonRequestBody = lon?.toString()?.toRequestBody("text/plain".toMediaType())

            // Make API call - single call with optional parameters
            val response = apiService.uploadStory(
                token = "Bearer $token",
                description = descriptionRequestBody,
                photo = imageMultipart,
                lat = latRequestBody,     // Will be null if lat is null
                lon = lonRequestBody      // Will be null if lon is null
            )

            if (response.error) {
                NetworkResult.Error(response.message ?: "Upload failed")
            } else {
                NetworkResult.Success(response)
            }

        } catch (e: HttpException) {
            handleHttpException(e)
        } catch (e: IOException) {
            handleNetworkException(e)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "An unexpected error occurred")
        }
    }
    suspend fun getAllStories(): NetworkResult<GetAllStoriesResponse> {
        val token = getToken() ?: return NetworkResult.Error("Authentication token not found. Please login again.")
        return try {
            val response = apiService.getStories("Bearer $token")
            if (response.error) NetworkResult.Error(response.message)
            else NetworkResult.Success(response)
        } catch (e: Exception) {
            handleException(e)
        }
    }
    private suspend fun getToken(): String? {
        return userPreferences.getAuthToken().first()
    }
    private fun <T> handleException(e: Exception): NetworkResult<T> {
        return when (e) {
            is HttpException -> handleHttpException(e)
            is IOException -> handleNetworkException(e)
            else -> NetworkResult.Error(e.message ?: "An unexpected error occurred")
        }
    }

    // Error handling helpers
    private fun <T> handleHttpException(e: HttpException): NetworkResult<T> {
        val errorMessage = try {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            errorResponse.message ?: getHttpErrorMessage(e.code())
        } catch (parseException: Exception) {
            getHttpErrorMessage(e.code())
        }
        return NetworkResult.Error(errorMessage)
    }

    private fun <T> handleNetworkException(e: IOException): NetworkResult<T> {
        val errorMessage = when (e) {
            is UnknownHostException -> "No internet connection. Please check your network."
            is SocketTimeoutException -> "Connection timeout. Please try again."
            else -> "Network error. Please check your connection and try again."
        }
        return NetworkResult.Error(errorMessage)
    }

    private fun getHttpErrorMessage(code: Int): String {
        return when (code) {
            400 -> "Bad request. Please check your input."
            401 -> "Unauthorized. Please login again."
            403 -> "Access forbidden."
            404 -> "Service not found."
            408 -> "Request timeout. Please try again."
            413 -> "File too large. Maximum size is 1MB."
            422 -> "Invalid data provided."
            500 -> "Server error. Please try again later."
            502, 503 -> "Service temporarily unavailable."
            else -> "HTTP Error $code"
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: StoryRepository? = null

        // Updated method to support both old and new usage patterns
        fun getInstance(context: Context, apiService: ApiService): StoryRepository {
            return INSTANCE ?: synchronized(this) {
                val userPreferences = UserPreferences.getInstance(context)
                INSTANCE ?: StoryRepository(apiService, userPreferences).also { INSTANCE = it }
            }
        }

        // New method for Injection pattern
        fun getInstance(
            apiService: ApiService,
            userPreferences: UserPreferences
        ): StoryRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StoryRepository(apiService, userPreferences).also { INSTANCE = it }
            }
        }
    }
}