package com.memorymoments.app.ui.screens.music

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.memorymoments.app.model.Memory
import com.memorymoments.app.model.Song
import com.memorymoments.app.navigation.Routes
import com.memorymoments.app.repository.MemoryRepository
import com.memorymoments.app.repository.SongRepository
import com.memorymoments.app.utils.AudioStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class SongFormState(
    val isEditing: Boolean = false,
    val title: String = "",
    val artist: String = "",
    val localAudioUri: String? = null,
    val selectedMemoryId: String? = null,
    val titleError: String? = null,
    val audioError: String? = null,
    val isSaving: Boolean = false,
    val isLoadingAudio: Boolean = false,
    val shouldClose: Boolean = false,
    val songMissing: Boolean = false
)

class SongFormViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val songRepo = SongRepository(application)
    private val memoryRepo = MemoryRepository(application)
    private val audioStorage = AudioStorage(application)

    private val songId: String? = savedStateHandle[Routes.MEMBER_ID_ARG]

    private val _state = MutableStateFlow(SongFormState(isEditing = songId != null))
    val state: StateFlow<SongFormState> = _state.asStateFlow()

    val memories: StateFlow<List<Memory>> = memoryRepo.memories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    init {
        if (songId != null) {
            viewModelScope.launch {
                val existing = songRepo.getById(songId)
                if (existing != null) {
                    _state.update {
                        it.copy(
                            title = existing.title,
                            artist = existing.artist.orEmpty(),
                            localAudioUri = existing.localAudioUri,
                            selectedMemoryId = existing.memoryId
                        )
                    }
                } else {
                    _state.update { it.copy(songMissing = true) }
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _state.update { it.copy(title = newTitle, titleError = null) }
    }

    fun onArtistChange(newArtist: String) {
        _state.update { it.copy(artist = newArtist) }
    }

    fun onMemorySelected(memoryId: String?) {
        _state.update { it.copy(selectedMemoryId = memoryId) }
    }

    fun onAudioPicked(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingAudio = true, audioError = null) }
            runCatching {
                val targetId = songId ?: UUID.randomUUID().toString()
                val path = audioStorage.copyAudioUri(uri, targetId)
                _state.update {
                    it.copy(
                        localAudioUri = path,
                        isLoadingAudio = false,
                        audioError = null
                    )
                }
            }.onFailure { err ->
                _state.update {
                    it.copy(
                        isLoadingAudio = false,
                        audioError = "Could not load audio file. Please try another file."
                    )
                }
            }
        }
    }

    fun save() {
        val current = _state.value
        val trimmedTitle = current.title.trim()

        var hasError = false
        if (trimmedTitle.isBlank()) {
            _state.update { it.copy(titleError = "Please enter a song title") }
            hasError = true
        }

        if (current.localAudioUri.isNullOrBlank()) {
            _state.update { it.copy(audioError = "Please select an audio file from your device") }
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            val song = Song(
                id = songId ?: UUID.randomUUID().toString(),
                title = trimmedTitle,
                artist = current.artist.trim().ifBlank { null },
                localAudioUri = current.localAudioUri!!,
                memoryId = current.selectedMemoryId
            )

            val result = if (current.isEditing) songRepo.update(song) else songRepo.add(song)
            if (result.isSuccess) {
                _state.update { it.copy(isSaving = false, shouldClose = true) }
            } else {
                _state.update {
                    it.copy(
                        isSaving = false,
                        titleError = "Could not save song. Please try again."
                    )
                }
            }
        }
    }

    fun deleteSong() {
        if (songId == null) return
        viewModelScope.launch {
            songRepo.delete(songId)
            _state.update { it.copy(shouldClose = true) }
        }
    }
}
