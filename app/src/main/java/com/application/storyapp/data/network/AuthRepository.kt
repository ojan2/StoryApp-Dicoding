package com.application.storyapp.data.network

import android.content.Context
import com.application.storyapp.data.data_store.UserPreferences
import com.application.storyapp.data.request.LoginRequest
import com.application.storyapp.data.request.RegisterRequest
import com.application.storyapp.data.response.ErrorResponse
import com.application.storyapp.data.response.LoginResponse
import com.application.storyapp.data.response.RegisterResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okio.IOException
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AuthRepository private constructor(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences
) {

    // Register function
    suspend fun register(name: String, email: String, password: String): NetworkResult<RegisterResponse> {
        return try {
            val request = RegisterRequest(name, email, password)
            val response = apiService.register(request)
            if (response.error) {
                NetworkResult.Error(response.message ?: "Registration failed")
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

    // Login function
    suspend fun login(email: String, password: String): NetworkResult<LoginResponse> {
        return try {
            val request = LoginRequest(email,password)
            val response = apiService.login(request)
            if (response.error) {
                NetworkResult.Error(response.message ?: "Login failed")
            } else {
                response.loginResult?.let { loginResult ->
                    // Save token to DataStore
                    userPreferences.saveAuthToken(
                        token = loginResult.token,
                        userId = loginResult.userId,
                        userName = loginResult.name
                    )
                }
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

    // Logout function
    suspend fun logout() {
        userPreferences.clearAuthData()
    }

    // Get user data
    fun getAuthToken(): Flow<String?> = userPreferences.getAuthToken()
    fun getUserId(): Flow<String?> = userPreferences.getUserId()
    fun getUserName(): Flow<String?> = userPreferences.getUserName()
    fun isLoggedIn(): Flow<Boolean> = userPreferences.isLoggedIn()

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
            422 -> "Invalid data provided."
            500 -> "Server error. Please try again later."
            502, 503 -> "Service temporarily unavailable."
            else -> "HTTP Error $code"
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AuthRepository? = null

        // Updated method to support both old and new usage patterns
        fun getInstance(context: Context, apiService: ApiService): AuthRepository {
            return INSTANCE ?: synchronized(this) {
                val userPreferences = UserPreferences.getInstance(context)
                INSTANCE ?: AuthRepository(apiService, userPreferences).also { INSTANCE = it }
            }
        }

        // New method for Injection pattern
        fun getInstance(
            apiService: ApiService,
            userPreferences: UserPreferences
        ): AuthRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthRepository(apiService, userPreferences).also { INSTANCE = it }
            }
        }
    }
}