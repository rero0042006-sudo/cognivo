package com.memorymoments.app.ui.screens.music

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.memorymoments.app.audio.AudioPlaybackManager
import com.memorymoments.app.model.Song
import com.memorymoments.app.repository.SongRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicListViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepo = SongRepository(application)
    val audioPlayer = AudioPlaybackManager(application)

    val songs: StateFlow<List<Song>> = songRepo.songs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val isPlaying: StateFlow<Boolean> = audioPlayer.isPlaying
    val currentPath: StateFlow<String?> = audioPlayer.currentPath
    val currentPositionMs: StateFlow<Int> = audioPlayer.currentPositionMs
    val durationMs: StateFlow<Int> = audioPlayer.durationMs

    fun togglePlaySong(song: Song) {
        if (currentPath.value == song.localAudioUri && isPlaying.value) {
            audioPlayer.pause()
        } else if (currentPath.value == song.localAudioUri) {
            audioPlayer.resume()
        } else {
            audioPlayer.play(song.localAudioUri)
        }
    }

    fun seekTo(positionMs: Int) {
        audioPlayer.seekTo(positionMs)
    }

    fun stopAudio() {
        audioPlayer.stop()
    }

    fun loadDemoSongs() {
        viewModelScope.launch {
            songRepo.createDemoSongs()
        }
    }

    fun deleteSong(id: String) {
        viewModelScope.launch {
            if (currentPath.value != null) {
                audioPlayer.stop()
            }
            songRepo.delete(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}
