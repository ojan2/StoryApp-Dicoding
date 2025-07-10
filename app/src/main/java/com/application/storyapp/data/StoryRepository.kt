package com.application.storyapp.data


import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.application.storyapp.data.data_store.UserPreferences
import com.application.storyapp.data.response.ErrorResponse
import com.application.storyapp.data.response.FileUploadResponse
import com.application.storyapp.data.response.GetAllStoriesResponse
import com.application.storyapp.data.network.ApiService
import com.application.storyapp.data.network.NetworkResult
import com.application.storyapp.data.paging.StoryPagingSource
import com.application.storyapp.model.Story
import com.application.storyapp.utils.Constants
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
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

    fun getStoriesPagingData(): Flow<PagingData<Story>> {
        return Pager(
            config = PagingConfig(
                pageSize = Constants.PAGE_SIZE,
                enablePlaceholders = false,
                prefetchDistance = Constants.PAGE_SIZE / 2
            ),
            pagingSourceFactory = {
                StoryPagingSource(this)
            }
        ).flow
    }

    suspend fun getStories(page: Int, size: Int, location: Int = 0): NetworkResult<GetAllStoriesResponse> {
        val token = userPreferences.getAuthToken().firstOrNull() ?:
        return NetworkResult.Error("Authentication token not found. Please login again.")

        if (token.isEmpty()) {
            return NetworkResult.Error("Authentication token not found. Please login again.")
        }

        return try {
            val response = apiService.getStories("Bearer $token", page, size, location)
            if (response.error) NetworkResult.Error(response.message)
            else NetworkResult.Success(response)
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private fun <T> handleException(e: Exception): NetworkResult<T> {
        val actualException = (e as? RuntimeException)?.cause as? IOException ?: e

        return when (actualException) {
            is HttpException -> handleHttpException(actualException)
            is IOException -> handleNetworkException(actualException)
            else -> NetworkResult.Error(actualException.message ?: "An unexpected error occurred")
        }
    }

    private fun <T> handleHttpException(e: HttpException): NetworkResult<T> {
        val errorMessage = try {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            errorResponse.message ?: "HTTP ${e.code()} error occurred."
        } catch (_: Exception) {
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