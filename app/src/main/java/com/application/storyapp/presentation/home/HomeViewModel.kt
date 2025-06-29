package com.application.storyapp.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.storyapp.data.StoryRepository
import com.application.storyapp.utils.HomeUIState
import com.application.storyapp.model.Story
import com.application.storyapp.data.network.NetworkResult
import kotlinx.coroutines.launch

class HomeViewModel(
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<HomeUIState>()
    val uiState: LiveData<HomeUIState> = _uiState

    private val _stories = MutableLiveData<List<Story>>()
    val stories: LiveData<List<Story>> = _stories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadStories()
    }

    fun loadStories() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = storyRepository.getAllStories()) {
                is NetworkResult.Success -> {
                    val response = result.data
                    _stories.value = response?.listStory ?: emptyList()
                    _uiState.value = HomeUIState.Success
                }

                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _uiState.value = HomeUIState.Error(result.message)
                }

                is NetworkResult.Loading -> {
                    _uiState.value = HomeUIState.Loading
                }
            }

            _isLoading.value = false
        }
    }
}
