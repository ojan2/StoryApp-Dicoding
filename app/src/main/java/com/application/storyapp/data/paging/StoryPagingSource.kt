package com.application.storyapp.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.application.storyapp.data.StoryRepository
import com.application.storyapp.data.network.NetworkResult
import com.application.storyapp.model.Story
import com.application.storyapp.utils.Constants
import kotlinx.coroutines.delay

class StoryPagingSource(
    private val storyRepository: StoryRepository
) : PagingSource<Int, Story>() {

    override fun getRefreshKey(state: PagingState<Int, Story>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Story> {
        return try {
            val page = params.key ?: Constants.INITIAL_PAGE_INDEX

            delay(1000)

            when (val result = storyRepository.getStories(page, Constants.PAGE_SIZE)) {
                is NetworkResult.Success -> {
                    val stories = result.data?.listStory.orEmpty()
                    LoadResult.Page(
                        data = stories,
                        prevKey = if (page == Constants.INITIAL_PAGE_INDEX) null else page - 1,
                        nextKey = if (stories.isEmpty()) null else page + 1
                    )
                }
                is NetworkResult.Error -> {
                    LoadResult.Error(Exception(result.message))
                }
                else -> {
                    LoadResult.Error(Exception("Unknown error occurred"))
                }
            }
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

}