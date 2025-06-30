package com.application.storyapp.data.network

import com.application.storyapp.data.data_store.UserPreferences
import com.application.storyapp.data.request.LoginRequest
import com.application.storyapp.data.request.RegisterRequest
import com.application.storyapp.data.response.ErrorResponse
import com.application.storyapp.data.response.LoginResponse
import com.application.storyapp.data.response.RegisterResponse
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AuthRepository private constructor(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences
) {

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): NetworkResult<RegisterResponse> {
        return try {
            val request = RegisterRequest(name, email, password)
            val response = apiService.register(request)
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

    suspend fun login(
        email: String,
        password: String
    ): NetworkResult<LoginResponse> {
        return try {
            val request = LoginRequest(email, password)
            val response = apiService.login(request)
            if (response.error) {
                NetworkResult.Error(response.message)
            } else {
                response.loginResult?.let { loginResult ->
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

    private fun <T> handleHttpException(e: HttpException): NetworkResult<T> {
        val errorMessage = try {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            errorResponse.message ?: "HTTP ${e.code()} error occurred"
        } catch (parseException: Exception) {
            "HTTP ${e.code()} error occurred"
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
        private var INSTANCE: AuthRepository? = null

        fun getInstance(
            apiService: ApiService,
            userPreferences: UserPreferences
        ): AuthRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthRepository(apiService, userPreferences).also {
                    INSTANCE = it
                }
            }
        }
    }
}
