package com.memorymoments.app.ui.screens.places

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.memorymoments.app.model.Place
import com.memorymoments.app.repository.PlaceRepository
import com.memorymoments.app.utils.ImageStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class PlaceFormState(
    val id: String,
    val isEditing: Boolean,
    val name: String = "",
    val location: String = "",
    val description: String = "",
    val datePeriod: String = "",
    val photoUris: List<String> = emptyList(),
    val nameError: String? = null,
    val photoError: String? = null,
    val isSaving: Boolean = false,
    val isLoadingPhoto: Boolean = false,
    val saveSucceeded: Boolean = false,
    val shouldClose: Boolean = false,
    val placeMissing: Boolean = false
) {
    val isValid: Boolean
        get() = name.trim().isNotEmpty() && photoUris.isNotEmpty()
}

class PlaceFormViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val existingId: String? = savedStateHandle.get<String>(ID_KEY)
    private val repository = PlaceRepository(application)
    private val imageStorage = ImageStorage(application)

    private val _state = MutableStateFlow(
        PlaceFormState(
            id = existingId ?: UUID.randomUUID().toString(),
            isEditing = existingId != null
        )
    )
    val state: StateFlow<PlaceFormState> = _state.asStateFlow()

    init {
        if (existingId != null) {
            viewModelScope.launch { loadExisting(existingId) }
        }
    }

    fun onNameChange(value: String) {
        _state.update { it.copy(name = value, nameError = null) }
    }

    fun onLocationChange(value: String) {
        _state.update { it.copy(location = value) }
    }

    fun onDescriptionChange(value: String) {
        _state.update { it.copy(description = value) }
    }

    fun onDatePeriodChange(value: String) {
        _state.update { it.copy(datePeriod = value) }
    }

    fun onPhotoPicked(uri: Uri) {
        val placeId = _state.value.id
        viewModelScope.launch {
            _state.update { it.copy(isLoadingPhoto = true, photoError = null) }
            val result = withContext(Dispatchers.IO) {
                imageStorage.copyPlacePhoto(uri, placeId, 0)
            }
            _state.update { current ->
                result.fold(
                    onSuccess = { path ->
                        current.copy(photoUris = listOf(path), isLoadingPhoto = false, photoError = null)
                    },
                    onFailure = {
                        current.copy(
                            isLoadingPhoto = false,
                            photoError = "Couldn't save that photo. Please try another image."
                        )
                    }
                )
            }
        }
    }

    fun save() {
        val current = _state.value
        val nameError = if (current.name.trim().isEmpty()) "Please enter a place name." else null

        if (nameError != null) {
            _state.update {
                it.copy(
                    nameError = nameError
                )
            }
            return
        }

        val place = Place(
            id = current.id,
            name = current.name.trim(),
            location = current.location.trim().ifEmpty { null },
            description = current.description.trim().ifEmpty { null },
            datePeriod = current.datePeriod.trim().ifEmpty { null },
            photoUris = current.photoUris
        )

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val result = if (current.isEditing) repository.update(place) else repository.add(place)
            _state.update {
                it.copy(
                    isSaving = false,
                    saveSucceeded = result.isSuccess,
                    shouldClose = result.isSuccess
                )
            }
        }
    }

    fun deletePlace() {
        val id = _state.value.id
        viewModelScope.launch {
            repository.delete(id)
            _state.update { it.copy(shouldClose = true, isSaving = false) }
        }
    }

    private suspend fun loadExisting(id: String) {
        val place = repository.places.first().find { it.id == id }
        if (place == null) {
            _state.update { it.copy(placeMissing = true) }
            return
        }
        _state.update {
            it.copy(
                id = place.id,
                isEditing = true,
                name = place.name,
                location = place.location.orEmpty(),
                description = place.description.orEmpty(),
                datePeriod = place.datePeriod.orEmpty(),
                photoUris = place.photoUris
            )
        }
    }

    companion object {
        const val ID_KEY = "id"
    }
}
