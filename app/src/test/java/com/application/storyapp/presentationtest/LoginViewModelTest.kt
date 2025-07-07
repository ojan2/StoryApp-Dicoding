package com.application.storyapp.presentationtest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.application.storyapp.MainCoroutineRule
import com.application.storyapp.data.network.AuthRepository
import com.application.storyapp.data.network.NetworkResult
import com.application.storyapp.data.response.LoginResponse
import com.application.storyapp.getOrAwaitValue
import com.application.storyapp.presentation.login.LoginViewModel
import com.application.storyapp.utils.LoginResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class LoginViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var repository: AuthRepository

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        viewModel = LoginViewModel(repository)
    }

    @Test
    fun `when login success should emit success event and navigate`() = runTest {
        val email = "test@example.com"
        val password = "validPass123"
        val loginResult = LoginResult("123", "Test User", "token_abc")

        Mockito.`when`(repository.login(email, password)).thenReturn(
            NetworkResult.Success(
                LoginResponse(
                    error = false,
                    message = "Login berhasil",
                    loginResult = loginResult
                )
            )
        )

        viewModel.login(email, password)
        advanceUntilIdle()

        val state = viewModel.uiState.getOrAwaitValue()
        println("State after success: $state") // debug

        assertFalse(state.isLoading)
        assertTrue(state.isSuccess) // pastikan ini benar di ViewModel

        val success = viewModel.successEvent.getOrAwaitValue().getContentIfNotHandled()
        assertNotNull(success)
        assertEquals(loginResult.token, success?.token)

        val navigate = viewModel.navigateToMainEvent.getOrAwaitValue().getContentIfNotHandled()
        assertNotNull(navigate)
    }


    @Test
    fun `when login error should emit error event`() = runTest {
        val email = "test@example.com"
        val password = "validPass123"
        val errorMsg = "Invalid credentials"

        Mockito.`when`(repository.login(email, password)).thenReturn(
            NetworkResult.Error(errorMsg)
        )

        viewModel.login(email, password)
        advanceUntilIdle()
        val state = viewModel.uiState.getOrAwaitValue()
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)

        val error = viewModel.errorEvent.getOrAwaitValue().getContentIfNotHandled()
        assertEquals(errorMsg, error)
    }

    @Test
    fun `when email is invalid should return validation error`() {
        val invalidEmail = "invalid-email"
        val password = "password123"

        viewModel.login(invalidEmail, password)

        val state = viewModel.uiState.getOrAwaitValue()
        assertEquals("Enter a valid email address", state.emailError)
        assertNull(viewModel.successEvent.value)
    }

    @Test
    fun `when password is empty should return validation error`() {
        val email = "test@example.com"
        val invalidPassword = ""

        viewModel.login(email, invalidPassword)

        val state = viewModel.uiState.getOrAwaitValue()
        assertEquals("Password is required", state.passwordError)
        assertNull(viewModel.successEvent.value)
    }

    @Test
    fun `when password is less than 8 characters should return validation error`() {
        val email = "test@example.com"
        val shortPassword = "123"

        viewModel.login(email, shortPassword)

        val state = viewModel.uiState.getOrAwaitValue()
        assertEquals("Password must be at least 8 characters", state.passwordError)
        assertNull(viewModel.successEvent.value)
    }

    @Test
    fun `when login error without message should show fallback error`() = runTest {
        val email = "test@example.com"
        val password = "password123"

        Mockito.`when`(repository.login(email, password)).thenReturn(
            NetworkResult.Error(null)
        )

        viewModel.login(email, password)
        advanceUntilIdle()

        val error = viewModel.errorEvent.getOrAwaitValue().getContentIfNotHandled()
        assertEquals("Login failed. Please try again.", error)
    }

    @Test
    fun `when login called should set loading true then false`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val loginResult = LoginResult("1", "Test", "token_abc")

        Mockito.`when`(repository.login(email, password)).thenReturn(
            NetworkResult.Success(
                LoginResponse(false, "Login berhasil", loginResult)
            )
        )

        viewModel.login(email, password)

        val loadingState = viewModel.uiState.getOrAwaitValue()
        assertTrue(loadingState.isLoading)

        advanceUntilIdle()


        val finalState = viewModel.uiState.getOrAwaitValue()
        assertFalse(finalState.isLoading)
    }

}
