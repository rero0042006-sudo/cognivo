package com.memorymoments.app.ui.screens.memories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.memorymoments.app.model.Memory
import com.memorymoments.app.model.Person
import com.memorymoments.app.model.Place
import com.memorymoments.app.model.Song
import com.memorymoments.app.repository.MemoryRepository
import com.memorymoments.app.repository.PersonRepository
import com.memorymoments.app.repository.PlaceRepository
import com.memorymoments.app.repository.SongRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MemoryDisplayItem(
    val memory: Memory,
    val peopleNames: List<String> = emptyList(),
    val placeName: String? = null,
    val songTitle: String? = null
)

class MemoriesViewModel(application: Application) : AndroidViewModel(application) {
    private val memoryRepo = MemoryRepository(application)
    private val personRepo = PersonRepository(application)
    private val placeRepo = PlaceRepository(application)
    private val songRepo = SongRepository(application)

    val memoryItems: StateFlow<List<MemoryDisplayItem>> = combine(
        memoryRepo.memories,
        personRepo.people,
        placeRepo.places,
        songRepo.songs
    ) { memories, people, places, songs ->
        memories.map { mem ->
            val pNames = mem.personIds.mapNotNull { pid -> people.find { it.id == pid }?.name }
            val plName = mem.place ?: mem.placeIds.firstOrNull()?.let { pid -> places.find { it.id == pid }?.name }
            val sTitle = mem.songIds.firstOrNull()?.let { sid -> songs.find { it.id == sid }?.title }

            MemoryDisplayItem(
                memory = mem,
                peopleNames = pNames,
                placeName = plName,
                songTitle = sTitle
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun deleteMemory(id: String) {
        viewModelScope.launch {
            memoryRepo.delete(id)
        }
    }

    fun loadDemoMemories() {
        viewModelScope.launch {
            memoryRepo.createDemoMemories()
        }
    }
}
