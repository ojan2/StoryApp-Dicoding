package com.application.storyapp.data.network

import com.application.storyapp.data.request.LoginRequest
import com.application.storyapp.data.request.RegisterRequest
import com.application.storyapp.data.response.FileUploadResponse
import com.application.storyapp.data.response.GetAllStoriesResponse
import com.application.storyapp.data.response.LoginResponse
import com.application.storyapp.data.response.RegisterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @POST("register")
    suspend fun register(
        @Body body: RegisterRequest
    ): RegisterResponse

    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @Multipart
    @POST("stories")
    suspend fun uploadStory(
        @Header("Authorization") token: String,
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part,
        @Part("lat") lat: RequestBody? = null,
        @Part("lon") lon: RequestBody? = null
    ): FileUploadResponse

    @GET("stories")
    suspend fun getStories(
        @Header("Authorization") token: String,
    ): GetAllStoriesResponse
}