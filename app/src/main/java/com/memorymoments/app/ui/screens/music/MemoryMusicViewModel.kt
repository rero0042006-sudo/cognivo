package com.memorymoments.app.ui.screens.music

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.memorymoments.app.audio.AudioPlaybackManager
import com.memorymoments.app.model.Memory
import com.memorymoments.app.model.Song
import com.memorymoments.app.repository.MemoryRepository
import com.memorymoments.app.repository.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MemoryMusicState(
    val songs: List<Song> = emptyList(),
    val currentIndex: Int = 0,
    val currentSong: Song? = null,
    val associatedMemory: Memory? = null,
    val isLoading: Boolean = true
)

class MemoryMusicViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepo = SongRepository(application)
    private val memoryRepo = MemoryRepository(application)
    val audioPlayer = AudioPlaybackManager(application)

    private val _state = MutableStateFlow(MemoryMusicState())
    val state: StateFlow<MemoryMusicState> = _state.asStateFlow()

    val isPlaying: StateFlow<Boolean> = audioPlayer.isPlaying
    val currentPositionMs: StateFlow<Int> = audioPlayer.currentPositionMs
    val durationMs: StateFlow<Int> = audioPlayer.durationMs

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val songList = songRepo.songs.first().ifEmpty {
                songRepo.createDemoSongs()
            }
            val memories = memoryRepo.memories.first()

            val initialSong = songList.firstOrNull()
            val initialMemory = memories.find { it.id == initialSong?.memoryId }

            _state.update {
                it.copy(
                    songs = songList,
                    currentIndex = 0,
                    currentSong = initialSong,
                    associatedMemory = initialMemory,
                    isLoading = false
                )
            }
        }
    }

    fun togglePlay() {
        val currentSong = _state.value.currentSong ?: return
        if (audioPlayer.currentPath.value == currentSong.localAudioUri) {
            audioPlayer.togglePlayPause()
        } else {
            audioPlayer.play(currentSong.localAudioUri)
        }
    }

    fun seekTo(positionMs: Int) {
        audioPlayer.seekTo(positionMs)
    }

    fun nextSong() {
        val songs = _state.value.songs
        if (songs.isEmpty()) return

        val nextIndex = (_state.value.currentIndex + 1) % songs.size
        selectSongAtIndex(nextIndex)
    }

    fun previousSong() {
        val songs = _state.value.songs
        if (songs.isEmpty()) return

        val prevIndex = if (_state.value.currentIndex - 1 < 0) songs.size - 1 else _state.value.currentIndex - 1
        selectSongAtIndex(prevIndex)
    }

    private fun selectSongAtIndex(index: Int) {
        viewModelScope.launch {
            val songs = _state.value.songs
            val targetSong = songs.getOrNull(index) ?: return@launch
            val memories = memoryRepo.memories.first()
            val associatedMemory = memories.find { it.id == targetSong.memoryId }

            audioPlayer.stop()
            _state.update {
                it.copy(
                    currentIndex = index,
                    currentSong = targetSong,
                    associatedMemory = associatedMemory
                )
            }
            audioPlayer.play(targetSong.localAudioUri)
        }
    }

    fun stopAudio() {
        audioPlayer.stop()
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}
