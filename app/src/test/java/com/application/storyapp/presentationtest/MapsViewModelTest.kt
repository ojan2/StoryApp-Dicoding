package com.application.storyapp.presentationtest
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.application.storyapp.MainCoroutineRule
import com.application.storyapp.data.StoryRepository
import com.application.storyapp.data.network.NetworkResult
import com.application.storyapp.data.response.GetAllStoriesResponse
import com.application.storyapp.getOrAwaitValue
import com.application.storyapp.model.Story
import com.application.storyapp.presentation.home.HomeViewModel
import com.application.storyapp.presentation.maps.MapsViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class MapsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()


    @Mock
    private lateinit var storyRepository: StoryRepository

    private lateinit var viewModel: MapsViewModel

    @Before
    fun setup() {
        viewModel = MapsViewModel(storyRepository)
    }


    @Test
    fun `loadStoriesWithLocation should update LiveData with stories`() = runTest {
        val dummyStories = listOf(
            Story("1", "Name", "Desc", "photoUrl", "2024-07-05", 1.0f, 2.0f),
            Story("2", "Name", "Desc", "photoUrl", "2024-07-05", 1.0f, 2.0f),

        )
        val dummyResponse = GetAllStoriesResponse(
            error = false,
            message = "Success",
            listStory = dummyStories
        )

        whenever(storyRepository.getStories(1, 100, 1))
            .thenReturn(NetworkResult.Success(dummyResponse))

        viewModel.loadStoriesWithLocation()
        advanceUntilIdle()
        val result = viewModel.storiesWithLocation.getOrAwaitValue()
        assertEquals(dummyStories, result)
    }

@Test
    fun `loadStoriesWithLocation error should update errorMessage and set empty stories`() = runTest {
        val errorMsg = "Something went wrong"
        val response = NetworkResult.Error<GetAllStoriesResponse>(message = errorMsg)

        Mockito.`when`(storyRepository.getStories(page = 1, size = 100, location = 1))
            .thenReturn(response)

        viewModel.loadStoriesWithLocation()
        advanceUntilIdle()

        val result = viewModel.storiesWithLocation.getOrAwaitValue()
        assertTrue(result.isEmpty())
        assertEquals(errorMsg, viewModel.errorMessage.getOrAwaitValue())
        assertFalse(viewModel.isLoading.getOrAwaitValue())
    }

    @Test
    fun `getLatLngBounds should return null when no stories available`() {
        val result = viewModel.getLatLngBounds()
        assertNull(result)
    }

    @Test
    fun `onMapReady should update isMapReady to true`() {
        viewModel.onMapReady()
        assertTrue(viewModel.isMapReady.getOrAwaitValue())
    }
    @Test
    fun `when Network Error should return error message`() = runTest {
        java.net.UnknownHostException()
        val expectedMessage = "No internet connection. Please check your network."

        whenever(storyRepository.getStories(1, 100, 1))
            .thenReturn(NetworkResult.Error(expectedMessage))

        viewModel.loadStoriesWithLocation()
        advanceUntilIdle()

        val result = viewModel.storiesWithLocation.getOrAwaitValue()
        val error = viewModel.errorMessage.getOrAwaitValue()
        val loading = viewModel.isLoading.getOrAwaitValue()

        assertTrue(result.isEmpty())
        assertEquals(expectedMessage, error)
        assertFalse(loading)
    }

}
