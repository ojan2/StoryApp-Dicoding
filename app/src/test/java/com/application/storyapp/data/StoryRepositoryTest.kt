package com.application.storyapp.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.application.storyapp.utils.MainCoroutineRule
import com.application.storyapp.data.network.ApiService
import com.application.storyapp.data.network.NetworkResult
import com.application.storyapp.data.response.FileUploadResponse
import com.application.storyapp.data.response.GetAllStoriesResponse
import com.application.storyapp.data.data_store.UserPreferences
import com.application.storyapp.model.Story
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import org.mockito.Mockito.isNull
import java.net.SocketTimeoutException

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class StoryRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var apiService: ApiService

    @Mock
    private lateinit var userPreferences: UserPreferences

    private lateinit var storyRepository: StoryRepository
    private lateinit var testFile: File

    @Before
    fun setUp() {
        val field = StoryRepository::class.java.getDeclaredField("INSTANCE")
        field.isAccessible = true
        field.set(null, null)

        storyRepository = StoryRepository.getInstance(apiService, userPreferences)
        testFile = File.createTempFile("test", ".jpg")
        testFile.writeText("test image content")
    }


    @Test
    fun `uploadStory success returns NetworkResult Success`() = runTest {
        val token = "fake_token"
        val description = "Test Description"
        val response = FileUploadResponse(error = false, message = "Story created successfully")

        whenever(userPreferences.getAuthToken()).thenReturn(flowOf(token))

        whenever(
            apiService.uploadStory(
                token = eq("Bearer $token"),
                description = any(),
                photo = any(),
                lat = isNull(),
                lon = isNull()
            )
        ).thenReturn(response)

        val result = storyRepository.uploadStory(testFile, description)

        assertTrue(result is NetworkResult.Success)
        assertEquals("Story created successfully", (result as NetworkResult.Success).data?.message)
    }

    @Test
    fun `uploadStory without token returns Error`() = runTest {
        whenever(userPreferences.getAuthToken()).thenReturn(flowOf(""))
        val result = storyRepository.uploadStory(testFile, "desc")
        assertTrue(result is NetworkResult.Error)
        assertEquals("Authentication token not found. Please login again.", (result as NetworkResult.Error).message)
    }

    @Test
    fun `uploadStory with null token returns Error`() = runTest {
        whenever(userPreferences.getAuthToken()).thenReturn(flowOf(null))
        val result = storyRepository.uploadStory(testFile, "desc")
        assertTrue(result is NetworkResult.Error)
        assertEquals("Authentication token not found. Please login again.", (result as NetworkResult.Error).message)
    }

    @Test
    fun `uploadStory throws RuntimeException returns Network error`() = runTest {
        val token = "dummy_token"
        whenever(userPreferences.getAuthToken()).thenReturn(flowOf(token))

        whenever(
            apiService.uploadStory(
                token = eq("Bearer $token"),
                description = any(),
                photo = any(),
                lat = isNull(),
                lon = isNull()
            )
        ).thenThrow(RuntimeException("Network down"))

        val result = storyRepository.uploadStory(testFile, "desc")

        assertTrue(result is NetworkResult.Error)
        assertEquals("Network down", (result as NetworkResult.Error).message)
    }

    @Test
    fun `getStories success returns NetworkResult Success`() = runTest {
        val token = "dummy_token"
        val stories = listOf(
            Story("1", "uhuy", "Desc1", "url", "2025-01-01"),
            Story("2", "ojan", "Desc2", "url", "2025-01-02")
        )
        val response = GetAllStoriesResponse(false, "success", stories)

        whenever(userPreferences.getAuthToken()).thenReturn(flowOf(token))
        whenever(apiService.getStories(eq("Bearer $token"), any(), any(), any()))
            .thenReturn(response)

        val result = storyRepository.getStories(1, 5)
        assertTrue(result is NetworkResult.Success)
        assertEquals(2, (result as NetworkResult.Success).data?.listStory?.size)
        assertEquals("success", result.data?.message)
    }

    @Test
    fun `getStories returns error when token is null`() = runTest {
        whenever(userPreferences.getAuthToken()).thenReturn(flowOf(null))
        val result = storyRepository.getStories(1, 5)
        assertTrue(result is NetworkResult.Error)
        assertEquals("Authentication token not found. Please login again.", (result as NetworkResult.Error).message)
    }

    @Test
    fun `getStories returns error when token is empty`() = runTest {
        whenever(userPreferences.getAuthToken()).thenReturn(flowOf(""))
        val result = storyRepository.getStories(1, 5)
        assertTrue(result is NetworkResult.Error)
        assertEquals("Authentication token not found. Please login again.", (result as NetworkResult.Error).message)
    }

    @Test
    fun `getStories throws HttpException returns parsed error message`() = runTest {
        val token = "dummy_token"
        val errorMessage = "HTTP 400 error occurred."
        val errorJson = """{"error":true,"message":"$errorMessage"}"""
        val errorBody = ResponseBody.create("application/json".toMediaType(), errorJson)
        val httpException = HttpException(Response.error<GetAllStoriesResponse>(400, errorBody))

        whenever(userPreferences.getAuthToken()).thenReturn(flowOf(token))
        whenever(apiService.getStories(eq("Bearer $token"), any(), any(), any()))
            .thenThrow(httpException)

        val result = storyRepository.getStories(1, 5)
        assertTrue(result is NetworkResult.Error)
        assertEquals(errorMessage, (result as NetworkResult.Error).message)
    }



    @Test
    fun `getStories throws RuntimeException returns fallback error message`() = runTest {
        val token = "dummy_token"
        whenever(userPreferences.getAuthToken()).thenReturn(flowOf(token))

        whenever(apiService.getStories(eq("Bearer $token"), any(), any(), any()))
            .thenThrow(RuntimeException("Generic network error"))

        val result = storyRepository.getStories(1, 5)

        assertTrue(result is NetworkResult.Error)
        assertEquals("Generic network error", (result as NetworkResult.Error).message)
    }
    @Test
    fun `getStories throws SocketTimeoutException returns timeout error`() = runTest {
        val token = "dummy_token"
        whenever(userPreferences.getAuthToken()).thenReturn(flowOf(token))

        val timeoutException = RuntimeException(SocketTimeoutException("timeout"))
        whenever(apiService.getStories(eq("Bearer $token"), any(), any(), any()))
            .thenThrow(timeoutException)

        val result = storyRepository.getStories(1, 5)

        assertTrue(result is NetworkResult.Error)
        assertEquals("Connection timeout. Please try again.", (result as NetworkResult.Error).message)
    }
}