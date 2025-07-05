package com.application.storyapp.presentation.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.storyapp.data.StoryRepository
import com.application.storyapp.data.network.NetworkResult
import com.application.storyapp.model.Story
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.launch

class MapsViewModel(
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val _storiesWithLocation = MutableLiveData<List<Story>>()
    val storiesWithLocation: LiveData<List<Story>> get() = _storiesWithLocation

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _isMapReady = MutableLiveData<Boolean>()
    val isMapReady: LiveData<Boolean> get() = _isMapReady

    fun loadStoriesWithLocation() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = storyRepository.getStories(
                    page = 1,
                    size = 100,
                    location = 1
                )

                when (result) {
                    is NetworkResult.Success -> {
                        val stories = result.data?.listStory?.filter { story ->
                            story.lat != null && story.lon != null
                        } ?: emptyList()
                        _storiesWithLocation.value = stories
                        _isLoading.value = false
                    }

                    is NetworkResult.Error -> {
                        _errorMessage.value = result.message ?: "Something went wrong"
                        _storiesWithLocation.value = emptyList()
                        _isLoading.value = false
                    }

                    is NetworkResult.Loading -> {
                        _isLoading.value = true
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load stories: ${e.message}"
                _storiesWithLocation.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    fun onMapReady() {
        _isMapReady.value = true
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun getLatLngBounds(): LatLngBounds? {
        val stories = _storiesWithLocation.value
        return if (stories != null && stories.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.Builder()
            stories.forEach { story ->
                story.lat?.let { lat ->
                    story.lon?.let { lon ->
                        boundsBuilder.include(LatLng(lat.toDouble(), lon.toDouble()))
                    }
                }
            }
            boundsBuilder.build()
        } else {
            null
        }
    }

}