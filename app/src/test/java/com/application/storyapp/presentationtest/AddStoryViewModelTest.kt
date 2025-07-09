package com.application.storyapp.presentationtest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.application.storyapp.utils.MainCoroutineRule
import com.application.storyapp.data.StoryRepository
import com.application.storyapp.data.network.NetworkResult
import com.application.storyapp.data.response.FileUploadResponse
import com.application.storyapp.utils.getOrAwaitValue
import com.application.storyapp.presentation.add_story.AddStoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class AddStoryViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineScopeRule = MainCoroutineRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var repository: StoryRepository

    private lateinit var viewModel: AddStoryViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AddStoryViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when uploadStory success should trigger success event`() = runTest {
        val file = File("dummy.jpg")
        val description = "This is a test story"
        val message = "Upload success"

        Mockito.`when`(
            repository.uploadStory(file, description, null, null)
        ).thenReturn(NetworkResult.Success(FileUploadResponse(false, message)))

        viewModel.setImageFile(file)
        viewModel.uploadStory(description)

        advanceUntilIdle()

        val state = viewModel.uiState.getOrAwaitValue()
        val successMessage = viewModel.successEvent.getOrAwaitValue().getContentIfNotHandled()

        Assert.assertTrue(state.isSuccess)
        Assert.assertEquals(message, successMessage)
    }

    @Test
    fun `when uploadStory returns error should trigger error event`() = runTest {
        val file = File("dummy.jpg")
        val description = "This is a test"
        val errorMsg = "Upload failed"

        Mockito.`when`(
            repository.uploadStory(file, description, null, null)
        ).thenReturn(NetworkResult.Error(errorMsg))

        viewModel.setImageFile(file)
        viewModel.uploadStory(description)

        advanceUntilIdle()

        val state = viewModel.uiState.getOrAwaitValue()
        val errorMessage = viewModel.errorEvent.getOrAwaitValue().getContentIfNotHandled()

        Assert.assertFalse(state.isSuccess)
        Assert.assertEquals(errorMsg, errorMessage)
    }

    @Test
    fun `when description is empty should show validation error`() {
        val file = File("dummy.jpg")
        viewModel.setImageFile(file)

        viewModel.uploadStory("")

        val state = viewModel.uiState.getOrAwaitValue()
        Assert.assertEquals("Description cannot be empty", state.descriptionError)
    }

    @Test
    fun `when image file is not set should show image error`() {
        viewModel.uploadStory("Valid description")

        val state = viewModel.uiState.getOrAwaitValue()
        Assert.assertEquals("Please select an image", state.imageError)
    }
}
