package com.memorymoments.app.ui.screens.timeline

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.memorymoments.app.model.LifeEvent
import com.memorymoments.app.model.Memory
import com.memorymoments.app.model.Person
import com.memorymoments.app.model.Place
import com.memorymoments.app.model.Song
import com.memorymoments.app.navigation.Routes
import com.memorymoments.app.repository.LifeEventRepository
import com.memorymoments.app.repository.MemoryRepository
import com.memorymoments.app.repository.PersonRepository
import com.memorymoments.app.repository.PlaceRepository
import com.memorymoments.app.repository.SongRepository
import com.memorymoments.app.utils.ImageStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class LifeEventFormState(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val date: String = "",
    val category: String = "Life",
    val description: String = "",
    val photoUri: String? = null,
    val selectedPersonIds: List<String> = emptyList(),
    val selectedPlaceId: String? = null,
    val selectedSongId: String? = null,
    val selectedMemoryId: String? = null,
    val availablePeople: List<Person> = emptyList(),
    val availablePlaces: List<Place> = emptyList(),
    val availableSongs: List<Song> = emptyList(),
    val availableMemories: List<Memory> = emptyList(),
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val errorMessage: String? = null
)

class LifeEventFormViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val eventRepo = LifeEventRepository(application)
    private val personRepo = PersonRepository(application)
    private val placeRepo = PlaceRepository(application)
    private val songRepo = SongRepository(application)
    private val memoryRepo = MemoryRepository(application)
    private val imageStorage = ImageStorage(application)

    private val eventId: String? = savedStateHandle[Routes.MEMBER_ID_ARG]

    private val _uiState = MutableStateFlow(LifeEventFormState())
    val uiState: StateFlow<LifeEventFormState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val people = personRepo.people.first()
            val places = placeRepo.places.first()
            val songs = songRepo.songs.first()
            val memories = memoryRepo.memories.first()

            if (eventId != null) {
                val existing = eventRepo.getById(eventId)
                if (existing != null) {
                    _uiState.update {
                        it.copy(
                            id = existing.id,
                            title = existing.title,
                            date = existing.date.orEmpty(),
                            category = existing.category ?: "Life",
                            description = existing.description.orEmpty(),
                            photoUri = existing.photoUri,
                            selectedPersonIds = existing.personIds,
                            selectedPlaceId = existing.placeId,
                            selectedSongId = existing.songId,
                            selectedMemoryId = existing.memoryId,
                            availablePeople = people,
                            availablePlaces = places,
                            availableSongs = songs,
                            availableMemories = memories,
                            isEditing = true
                        )
                    }
                    return@launch
                }
            }

            _uiState.update {
                it.copy(
                    availablePeople = people,
                    availablePlaces = places,
                    availableSongs = songs,
                    availableMemories = memories,
                    isEditing = false
                )
            }
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value, errorMessage = null) }
    fun onDateChange(value: String) = _uiState.update { it.copy(date = value) }
    fun onCategoryChange(value: String) = _uiState.update { it.copy(category = value) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }

    fun onPhotoPicked(uri: Uri) {
        viewModelScope.launch {
            val copyResult = imageStorage.copyPlacePhoto(uri, _uiState.value.id)
            copyResult.onSuccess { path ->
                _uiState.update { it.copy(photoUri = path) }
            }.onFailure {
                _uiState.update { it.copy(errorMessage = "Could not load photo.") }
            }
        }
    }

    fun removePhoto() = _uiState.update { it.copy(photoUri = null) }

    fun togglePerson(personId: String) {
        _uiState.update {
            val current = it.selectedPersonIds
            val updated = if (current.contains(personId)) current - personId else current + personId
            it.copy(selectedPersonIds = updated)
        }
    }

    fun setPlace(placeId: String?) = _uiState.update { it.copy(selectedPlaceId = placeId) }
    fun setSong(songId: String?) = _uiState.update { it.copy(selectedSongId = songId) }
    fun setMemory(memoryId: String?) = _uiState.update { it.copy(selectedMemoryId = memoryId) }

    fun requestDelete() = _uiState.update { it.copy(showDeleteConfirm = true) }
    fun cancelDelete() = _uiState.update { it.copy(showDeleteConfirm = false) }

    fun save() {
        val title = _uiState.value.title.trim()
        if (title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter an event title.") }
            return
        }

        viewModelScope.launch {
            val event = LifeEvent(
                id = _uiState.value.id,
                title = title,
                date = _uiState.value.date.trim().ifBlank { null },
                category = _uiState.value.category.trim().ifBlank { null },
                description = _uiState.value.description.trim().ifBlank { null },
                photoUri = _uiState.value.photoUri,
                personIds = _uiState.value.selectedPersonIds,
                placeId = _uiState.value.selectedPlaceId,
                songId = _uiState.value.selectedSongId,
                memoryId = _uiState.value.selectedMemoryId
            )

            val result = if (_uiState.value.isEditing) eventRepo.update(event) else eventRepo.add(event)
            result.onSuccess {
                _uiState.update { it.copy(isSaved = true) }
            }.onFailure {
                _uiState.update { it.copy(errorMessage = "Error saving event.") }
            }
        }
    }

    fun confirmDelete() {
        viewModelScope.launch {
            _uiState.update { it.copy(showDeleteConfirm = false) }
            eventRepo.delete(_uiState.value.id)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
