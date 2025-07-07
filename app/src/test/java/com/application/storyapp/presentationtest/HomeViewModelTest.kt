@file:OptIn(ExperimentalCoroutinesApi::class)

package com.application.storyapp.presentationtest

import androidx.paging.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.application.storyapp.data.StoryRepository
import com.application.storyapp.model.Story
import com.application.storyapp.presentation.home.HomeViewModel
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
import org.junit.Before
import org.junit.Test

class HomeViewModelTest {

    private lateinit var repository: StoryRepository
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test fun `when loadStories success - data is not null, size is correct, first item is correct`() = runTest {
        val dummyStories = listOf(
            Story("1", "Story 1", "desc 1", "url1", "2021-10-10", -6.1809716f, 106.82445f),
            Story("2", "Story 2", "desc 2", "url2", "2021-10-11", -6.1809716f, 106.82445f),
            Story("3", "Story 3", "desc 3", "url3", "2021-10-12", -6.1809716f, 106.82445f)
        )

        val pagingData = PagingData.from(dummyStories)
        coEvery { repository.getStoriesPagingData() } returns flowOf(pagingData)

        viewModel = HomeViewModel(repository)

        val differ = AsyncPagingDataDiffer(
            diffCallback = DiffCallback,
            updateCallback = NoopListCallback(),
            mainDispatcher = testDispatcher,
            workerDispatcher = testDispatcher
        )

        val job = launch {
            viewModel.stories.collectLatest {
                differ.submitData(it)
            }
        }

        advanceUntilIdle()

        // Then
        val snapshot = differ.snapshot()
        Assert.assertNotNull(snapshot)
        Assert.assertEquals(3, snapshot.size)
        Assert.assertEquals("1", snapshot[0]?.id)
        job.cancel()
    }

    @Test
    fun `when loadStories returns empty - size is 0`() = runTest {
        val pagingData = PagingData.from(emptyList<Story>())
        coEvery { repository.getStoriesPagingData() } returns flowOf(pagingData)


        viewModel = HomeViewModel(repository)

        val differ = AsyncPagingDataDiffer(
            diffCallback = DiffCallback,
            updateCallback = NoopListCallback(),
            mainDispatcher = testDispatcher,
            workerDispatcher = testDispatcher
        )

        val job = launch {
            viewModel.stories.collectLatest {
                differ.submitData(it)
            }
        }

        advanceUntilIdle()

        val snapshot = differ.snapshot()
        Assert.assertTrue(snapshot.isEmpty())
        job.cancel()
    }


    object DiffCallback : DiffUtil.ItemCallback<Story>() {
        override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
            return oldItem == newItem
        }
    }

    class NoopListCallback : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }
}
