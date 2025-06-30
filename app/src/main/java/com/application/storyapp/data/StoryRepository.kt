package com.application.storyapp.data

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

    suspend fun uploadStory(
        imageFile: File,
        description: String,
        lat: Float? = null,
        lon: Float? = null
    ): NetworkResult<FileUploadResponse> {
        return try {
            val token = userPreferences.getAuthToken().first()
            if (token.isNullOrEmpty()) {
                return NetworkResult.Error("Authentication token not found. Please login again.")
            }

            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val imageMultipart = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImageFile
            )

            val descriptionRequestBody = description.toRequestBody("text/plain".toMediaType())
            val latRequestBody = lat?.toString()?.toRequestBody("text/plain".toMediaType())
            val lonRequestBody = lon?.toString()?.toRequestBody("text/plain".toMediaType())

            val response = apiService.uploadStory(
                token = "Bearer $token",
                description = descriptionRequestBody,
                photo = imageMultipart,
                lat = latRequestBody,
                lon = lonRequestBody
            )

            if (response.error) {
                NetworkResult.Error(response.message)
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

    private fun <T> handleHttpException(e: HttpException): NetworkResult<T> {
        val errorMessage = try {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            errorResponse.message ?: "HTTP ${e.code()} error occurred."
        } catch (parseException: Exception) {
            "HTTP ${e.code()} error occurred."
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

    companion object {
        @Volatile
        private var INSTANCE: StoryRepository? = null
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
