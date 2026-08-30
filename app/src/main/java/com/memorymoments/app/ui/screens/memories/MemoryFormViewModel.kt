package com.memorymoments.app.ui.screens.memories

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.memorymoments.app.model.Memory
import com.memorymoments.app.model.Person
import com.memorymoments.app.model.Place
import com.memorymoments.app.model.Song
import com.memorymoments.app.navigation.Routes
import com.memorymoments.app.repository.MemoryRepository
import com.memorymoments.app.repository.PersonRepository
import com.memorymoments.app.repository.PlaceRepository
import com.memorymoments.app.repository.SongRepository
import com.memorymoments.app.utils.ImageStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class MemoryFormState(
    val id: String = UUID.randomUUID().toString(),
    val isEditMode: Boolean = false,
    val title: String = "",
    val description: String = "",
    val photoUri: String? = null,
    val selectedPersonIds: Set<String> = emptySet(),
    val selectedPlaceId: String? = null,
    val selectedSongId: String? = null,
    val dateOrYear: String = "",
    val heritageCategory: String? = null,
    val region: String = "",
    val state: String = "",
    val availablePeople: List<Person> = emptyList(),
    val availablePlaces: List<Place> = emptyList(),
    val availableSongs: List<Song> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val shouldClose: Boolean = false,
    val memoryMissing: Boolean = false
)

class MemoryFormViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val memoryRepo = MemoryRepository(application)
    private val personRepo = PersonRepository(application)
    private val placeRepo = PlaceRepository(application)
    private val songRepo = SongRepository(application)
    private val imageStorage = ImageStorage(application)

    private val memoryId: String? = savedStateHandle[Routes.MEMBER_ID_ARG]

    private val _state = MutableStateFlow(MemoryFormState())
    val state: StateFlow<MemoryFormState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val people = personRepo.people.first()
            val places = placeRepo.places.first()
            val songs = songRepo.songs.first()

            if (memoryId != null) {
                val existing = memoryRepo.getById(memoryId)
                if (existing != null) {
                    _state.update {
                        it.copy(
                            id = existing.id,
                            isEditMode = true,
                            title = existing.title,
                            description = existing.description.orEmpty(),
                            photoUri = existing.photoUris.firstOrNull(),
                            selectedPersonIds = existing.personIds.toSet(),
                            selectedPlaceId = existing.placeIds.firstOrNull() ?: places.find { p -> p.name.equals(existing.place, ignoreCase = true) }?.id,
                            selectedSongId = existing.songIds.firstOrNull(),
                            dateOrYear = existing.date.orEmpty(),
                            heritageCategory = existing.heritageCategory,
                            region = existing.region.orEmpty(),
                            state = existing.state.orEmpty(),
                            availablePeople = people,
                            availablePlaces = places,
                            availableSongs = songs
                        )
                    }
                } else {
                    _state.update { it.copy(memoryMissing = true) }
                }
            } else {
                _state.update {
                    it.copy(
                        availablePeople = people,
                        availablePlaces = places,
                        availableSongs = songs
                    )
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _state.update { it.copy(title = newTitle, errorMessage = null) }
    }

    fun onDescriptionChange(newDesc: String) {
        _state.update { it.copy(description = newDesc) }
    }

    fun onDateOrYearChange(newDate: String) {
        _state.update { it.copy(dateOrYear = newDate) }
    }

    fun onHeritageCategoryChange(category: String?) {
        _state.update { it.copy(heritageCategory = if (it.heritageCategory == category) null else category) }
    }

    fun togglePersonSelection(personId: String) {
        _state.update { current ->
            val currentSelected = current.selectedPersonIds
            val updated = if (currentSelected.contains(personId)) {
                currentSelected - personId
            } else {
                currentSelected + personId
            }
            current.copy(selectedPersonIds = updated)
        }
    }

    fun onPlaceSelected(placeId: String?) {
        _state.update { it.copy(selectedPlaceId = if (it.selectedPlaceId == placeId) null else placeId) }
    }

    fun onSongSelected(songId: String?) {
        _state.update { it.copy(selectedSongId = if (it.selectedSongId == songId) null else songId) }
    }

    fun onPhotoPicked(uri: Uri) {
        viewModelScope.launch {
            val localPath = withContext(Dispatchers.IO) {
                imageStorage.copyMemoryPhoto(uri, _state.value.id).getOrNull()
            }
            if (localPath != null) {
                _state.update { it.copy(photoUri = localPath, errorMessage = null) }
            }
        }
    }

    fun save() {
        val current = _state.value
        val titleTrimmed = current.title.trim()
        if (titleTrimmed.isBlank()) {
            _state.update { it.copy(errorMessage = "Please enter a title for this memory") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            val selectedPlace = current.availablePlaces.find { it.id == current.selectedPlaceId }

            val memory = Memory(
                id = current.id,
                title = titleTrimmed,
                description = current.description.trim().ifBlank { null },
                date = current.dateOrYear.trim().ifBlank { null },
                personIds = current.selectedPersonIds.toList(),
                placeIds = listOfNotNull(current.selectedPlaceId),
                songIds = listOfNotNull(current.selectedSongId),
                photoUris = listOfNotNull(current.photoUri),
                heritageCategory = current.heritageCategory,
                region = current.region.ifBlank { selectedPlace?.region },
                state = current.state.ifBlank { selectedPlace?.state },
                place = selectedPlace?.name,
                updatedAt = System.currentTimeMillis()
            )

            val result = memoryRepo.saveOrUpdate(memory)
            if (result.isSuccess) {
                _state.update { it.copy(isSaving = false, shouldClose = true) }
            } else {
                _state.update { it.copy(isSaving = false, errorMessage = "Could not save memory. Please try again.") }
            }
        }
    }

    fun delete() {
        val id = _state.value.id
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            memoryRepo.delete(id)
            _state.update { it.copy(isSaving = false, shouldClose = true) }
        }
    }
}
