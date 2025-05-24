package com.application.storyapp.network

import android.content.Context
import com.application.storyapp.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient {

    companion object {
        private const val BASE_URL = "https://story-api.dicoding.dev/v1/"

        @Volatile
        private var INSTANCE: ApiService? = null
        private var userPreferences: UserPreferences? = null

        fun initialize(context: Context) {
            userPreferences = UserPreferences.getInstance(context)
        }

        fun getApiService(): ApiService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildApiService().also { INSTANCE = it }
            }
        }

        private fun buildApiService(): ApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val authInterceptor = AuthInterceptor()

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            return retrofit.create(ApiService::class.java)
        }

        private class AuthInterceptor : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val originalRequest = chain.request()

                // Skip adding token for login and register endpoints
                val url = originalRequest.url.toString()
                if (url.contains("/login") || url.contains("/register")) {
                    return chain.proceed(originalRequest)
                }

                // Get token from DataStore
                val token = userPreferences?.let { prefs ->
                    runBlocking {
                        try {
                            prefs.getAuthToken().first()
                        } catch (e: Exception) {
                            null
                        }
                    }
                }

                return if (!token.isNullOrEmpty()) {
                    val newRequest = originalRequest.newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                    chain.proceed(newRequest)
                } else {
                    chain.proceed(originalRequest)
                }
            }
        }
    }
}