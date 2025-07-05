package com.application.storyapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.application.storyapp.data.StoryRepository
import com.application.storyapp.model.Story
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: StoryRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _pagingDataFlow = MutableStateFlow<PagingData<Story>>(PagingData.empty())
    val stories: Flow<PagingData<Story>> = _pagingDataFlow

    init {
        loadStories()
    }

    fun refresh() {
        loadStories()
    }

    private fun loadStories() {
        _isRefreshing.value = true
        viewModelScope.launch {
            try {
                repository.getStoriesPagingData()
                    .cachedIn(viewModelScope)
                    .collect { pagingData ->
                        _pagingDataFlow.value = pagingData
                        _isRefreshing.value = false
                    }
            } catch (e: Exception) {
                _isRefreshing.value = false

            }
        }
    }
}