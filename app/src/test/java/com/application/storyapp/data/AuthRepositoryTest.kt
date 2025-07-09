package com.application.storyapp.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.application.storyapp.data.data_store.UserPreferences
import com.application.storyapp.data.network.ApiService
import com.application.storyapp.data.network.NetworkResult
import com.application.storyapp.data.response.LoginResponse
import com.application.storyapp.data.response.RegisterResponse
import com.application.storyapp.utils.LoginResult
import com.application.storyapp.utils.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.net.SocketTimeoutException


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AuthRepositoryTest {

    @get:Rule
    val instanExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var apiService: ApiService

    @Mock
    private lateinit var userPreferences: UserPreferences

    private lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        val field = AuthRepository::class.java.getDeclaredField("INSTANCE")
        field.isAccessible = true
        field.set(null, null)
        authRepository = AuthRepository.getInstance(apiService, userPreferences)
    }

    @Test
    fun `register success returns NetworkResult Success`() = runTest {
        val response = RegisterResponse(error = false, message = "User registered successfully")
        whenever(apiService.register(any())).thenReturn(response)

        val result = authRepository.register("Fauzan", "fauzan@mail.com", "qwerty123")

        assertTrue(result is NetworkResult.Success)
        assertEquals("User registered successfully", (result as NetworkResult.Success).data?.message)
    }
    @Test
    fun `register with API error returns NetworkResult Error` () = runTest {
        val response = RegisterResponse(error = true, message = "Email already used")
        whenever(apiService.register(any())).thenReturn(response)

        val result = authRepository.register("Fauzan","fauzan@gmail.com","qwerty123")
        assertTrue(result is NetworkResult.Error)
        assertEquals("Email already used",(result as NetworkResult.Error).message)

    }
    @Test
    fun `register throws IOException returns network error`() = runTest {
        whenever(apiService.register(any()))
            .thenThrow(RuntimeException(SocketTimeoutException("timeout")))

        val result = authRepository.register("Fauzan", "fauzan@mail.com", "qwerty123")

        assertTrue(result is NetworkResult.Error)
        assertEquals("Connection timeout. Please try again.", (result as NetworkResult.Error).message)
    }



    @Test
    fun `login success returns NetworkResult Success and saves token`() = runTest {
        val loginResult = LoginResult(userId = "1", name = "Fauzan", token = "Azzii1m2ksa")
        val response = LoginResponse(error = false, message = "Login successful", loginResult = loginResult)

        whenever(apiService.login(any())).thenReturn(response)
        whenever(userPreferences.saveAuthToken("Azzii1m2ksa", "1", "Fauzan")).thenReturn(Unit) // suspend function mocked properly

        val result = authRepository.login("fauzan@gmail.com", "qwerty123")

        assertTrue(result is NetworkResult.Success)
        assertEquals("Login successful", (result as NetworkResult.Success).data?.message)
    }

}