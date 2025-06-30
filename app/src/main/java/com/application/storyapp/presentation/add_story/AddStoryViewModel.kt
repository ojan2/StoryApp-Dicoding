package com.application.storyapp.presentation.add_story

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.storyapp.utils.Event
import com.application.storyapp.data.StoryRepository
import com.application.storyapp.utils.AddStoryUIState
import com.application.storyapp.data.network.NetworkResult
import kotlinx.coroutines.launch
import java.io.File


class AddStoryViewModel(
    private val repository: StoryRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(AddStoryUIState())
    val uiState: LiveData<AddStoryUIState> = _uiState

    private val _errorEvent = MutableLiveData<Event<String>>()
    val errorEvent: LiveData<Event<String>> = _errorEvent

    private val _successEvent = MutableLiveData<Event<String>>()
    val successEvent: LiveData<Event<String>> = _successEvent

    private val _navigateBackEvent = MutableLiveData<Event<Unit>>()
    val navigateBackEvent: LiveData<Event<Unit>> = _navigateBackEvent

    private var currentImageFile: File? = null

    fun setImageFile(file: File) {
        currentImageFile = file
        _uiState.value = _uiState.value?.copy(
            hasImage = true,
            imageError = null
        )
    }

    fun validateDescription(description: String) {
        val error = if (description.isBlank()) {
            "Description cannot be empty"
        } else null

        _uiState.value = _uiState.value?.copy(descriptionError = error)
    }

    fun uploadStory(description: String, lat: Float? = null, lon: Float? = null) {
        // Validate inputs
        val descriptionError = if (description.isBlank()) "Description cannot be empty" else null
        val imageError = if (currentImageFile == null) "Please select an image" else null

        if (descriptionError != null || imageError != null) {
            _uiState.value = _uiState.value?.copy(
                descriptionError = descriptionError,
                imageError = imageError
            )
            return
        }

        _uiState.value = _uiState.value?.copy(
            isLoading = true,
            descriptionError = null,
            imageError = null
        )

        viewModelScope.launch {
            currentImageFile?.let { file ->
                when (val result = repository.uploadStory(file, description, lat, lon)) {
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value?.copy(
                            isLoading = false,
                            isSuccess = true
                        )

                        val message = result.data?.message ?: "Story uploaded successfully!"
                        _successEvent.value = Event(message)

                        // Navigate back after successful upload
                        _navigateBackEvent.value = Event(Unit)
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value?.copy(
                            isLoading = false,
                            isSuccess = false
                        )
                        val errorMessage = result.message ?: "Upload failed. Please try again."
                        _errorEvent.value = Event(errorMessage)
                    }
                    is NetworkResult.Loading -> {
                    }
                }
            }
        }
    }
}

