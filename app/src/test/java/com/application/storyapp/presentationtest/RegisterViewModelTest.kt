package com.application.storyapp.presentationtest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.application.storyapp.utils.MainCoroutineRule
import com.application.storyapp.data.AuthRepository
import com.application.storyapp.data.network.NetworkResult
import com.application.storyapp.data.response.RegisterResponse
import com.application.storyapp.utils.getOrAwaitValue
import com.application.storyapp.presentation.register.RegisterViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class RegisterViewModelTest {

    @get:Rule
    val instantTaskExecutor = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var repository: AuthRepository

    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setup() {
        viewModel = RegisterViewModel(repository)
    }

    @Test
    fun `when register success should emit success event`() = runTest {
        val name = "Fauzan"
        val email = "fauzan@gmail.com"
        val password = "password123"

        val response = RegisterResponse(
            error = false,
            message = "User created"
        )

        Mockito.`when`(repository.register(name, email, password))
            .thenReturn(NetworkResult.Success(response))

        viewModel.register(name, email, password)
        advanceUntilIdle()

        val state = viewModel.uiState.getOrAwaitValue()
        assertFalse(state.isLoading)
        assertTrue(state.isSuccess)

        val success = viewModel.successEvent.getOrAwaitValue().getContentIfNotHandled()
        assertNotNull(success)
    }

    @Test
    fun `when register failed should emit error event`() = runTest {
        val name = "Fauzan"
        val email = "fauzan@example.com"
        val password = "password123"
        val errorMsg = "Email already taken"

        Mockito.`when`(repository.register(name, email, password))
            .thenReturn(NetworkResult.Error(errorMsg))

        viewModel.register(name, email, password)
        advanceUntilIdle()

        val state = viewModel.uiState.getOrAwaitValue()
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)

        val error = viewModel.errorEvent.getOrAwaitValue().getContentIfNotHandled()
        assertEquals(errorMsg, error)
    }

    @Test
    fun `when name is invalid should return validation error`() {
        val invalidName = "f"
        val email = "fauzan@gmail.com"
        val password = "password123"

        viewModel.register(invalidName, email, password)

        val state = viewModel.uiState.getOrAwaitValue()
        assertEquals("Name must be at least 2 characters", state.nameError)
        assertNull(viewModel.successEvent.value)
    }

    @Test
    fun `when email is invalid should return validation error`() {
        val name = "Fauzan"
        val invalidEmail = "fauzan"
        val password = "password123"

        viewModel.register(name, invalidEmail, password)

        val state = viewModel.uiState.getOrAwaitValue()
        assertEquals("Enter a valid email address", state.emailError)
        assertNull(viewModel.successEvent.value)
    }

    @Test
    fun `when password is empty should return validation error`() {
        val name = ""
        val email = "fauzan@example.com"
        val invalidPassword = ""

        viewModel.register(name, email, invalidPassword)

        val state = viewModel.uiState.getOrAwaitValue()
        assertEquals("Password is required", state.passwordError)
        assertNull(viewModel.successEvent.value)
    }

    @Test
    fun `when all input invalid should return all validation error`() {
        val name = ""
        val email = "wrongemail"
        val password = "123"

        viewModel.register(name, email, password)

        val state = viewModel.uiState.getOrAwaitValue()
        assertEquals("Name is required", state.nameError)
        assertEquals("Enter a valid email address", state.emailError)
        assertEquals("Password must be at least 8 characters", state.passwordError)
        assertNull(viewModel.successEvent.value)
    }
    @Test
    fun `when register fail should return error and message not null`() = runTest {
        val name = "Fauzan"
        val email = "fauzan@gmail.com"
        val password = "password123"
        val errorMessage = "Email is already taken"

        Mockito.`when`(repository.register(name, email, password))
            .thenReturn(NetworkResult.Error(errorMessage))

        viewModel.register(name, email, password)
        advanceUntilIdle()

        val state = viewModel.uiState.getOrAwaitValue()
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)

        val error = viewModel.errorEvent.getOrAwaitValue().getContentIfNotHandled()
        assertNotNull("Error message should not be null", error)
        assertEquals(errorMessage, error)
    }
}
