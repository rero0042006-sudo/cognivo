package com.memorymoments.app.ui.screens.timeline

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.memorymoments.app.audio.AudioPlaybackManager
import com.memorymoments.app.model.LifeEvent
import com.memorymoments.app.model.Memory
import com.memorymoments.app.model.Person
import com.memorymoments.app.model.Place
import com.memorymoments.app.model.Song
import com.memorymoments.app.repository.LifeEventRepository
import com.memorymoments.app.repository.MemoryRepository
import com.memorymoments.app.repository.PersonRepository
import com.memorymoments.app.repository.PlaceRepository
import com.memorymoments.app.repository.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LifeEventItemUi(
    val event: LifeEvent,
    val placeName: String? = null,
    val personNames: List<String> = emptyList(),
    val songTitle: String? = null,
    val songAudioUri: String? = null,
    val memoryTitle: String? = null,
    val yearDisplay: String? = null,
    val isUndated: Boolean = false
)

data class LifeTimelineUiState(
    val items: List<LifeEventItemUi> = emptyList(),
    val ascending: Boolean = true,
    val isPlayingSongId: String? = null,
    val isLoading: Boolean = true
)

private data class CombinedData(
    val events: List<LifeEvent>,
    val people: List<Person>,
    val places: List<Place>,
    val songs: List<Song>,
    val memories: List<Memory>
)

class LifeTimelineViewModel(application: Application) : AndroidViewModel(application) {
    private val eventRepo = LifeEventRepository(application)
    private val personRepo = PersonRepository(application)
    private val placeRepo = PlaceRepository(application)
    private val songRepo = SongRepository(application)
    private val memoryRepo = MemoryRepository(application)
    val audioPlaybackManager = AudioPlaybackManager(application)

    private val _ascending = MutableStateFlow(true)
    private val _uiState = MutableStateFlow(LifeTimelineUiState())
    val uiState: StateFlow<LifeTimelineUiState> = _uiState.asStateFlow()

    init {
        observeTimeline()
    }

    private fun observeTimeline() {
        viewModelScope.launch {
            val entitiesFlow = combine(
                eventRepo.lifeEvents,
                personRepo.people,
                placeRepo.places,
                songRepo.songs,
                memoryRepo.memories
            ) { events, people, places, songs, memories ->
                CombinedData(events, people, places, songs, memories)
            }

            combine(entitiesFlow, _ascending) { data, ascending ->
                val sorted = LifeEventRepository.sortChronological(data.events, ascending)
                val mapped = sorted.map { event ->
                    val year = LifeEventRepository.extractYear(event.date)
                    val pNames = event.personIds.mapNotNull { pid -> data.people.find { it.id == pid }?.name }
                    val plName = data.places.find { it.id == event.placeId }?.name
                    val song = data.songs.find { it.id == event.songId }
                    val mem = data.memories.find { it.id == event.memoryId }

                    LifeEventItemUi(
                        event = event,
                        placeName = plName,
                        personNames = pNames,
                        songTitle = song?.title,
                        songAudioUri = song?.localAudioUri,
                        memoryTitle = mem?.title,
                        yearDisplay = if (year != null) "$year" else event.date,
                        isUndated = year == null && event.date.isNullOrBlank()
                    )
                }
                Pair(mapped, ascending)
            }.collect { (mappedItems, ascending) ->
                _uiState.update {
                    it.copy(
                        items = mappedItems,
                        ascending = ascending,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun toggleSortOrder() {
        _ascending.update { !it }
    }

    fun loadDemoEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            eventRepo.createDemoLifeEvents()
        }
    }

    fun togglePlaySong(songId: String?, audioUri: String?) {
        if (songId == null || audioUri == null) return
        if (_uiState.value.isPlayingSongId == songId) {
            audioPlaybackManager.stop()
            _uiState.update { it.copy(isPlayingSongId = null) }
        } else {
            audioPlaybackManager.play(audioUri)
            _uiState.update { it.copy(isPlayingSongId = songId) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlaybackManager.release()
    }
}
