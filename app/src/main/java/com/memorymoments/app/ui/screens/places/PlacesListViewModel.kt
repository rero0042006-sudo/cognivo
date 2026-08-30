package com.memorymoments.app.ui.screens.places

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.memorymoments.app.model.Place
import com.memorymoments.app.repository.PlaceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlacesListViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PlaceRepository(application)

    val places: StateFlow<List<Place>> = repository.places.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun loadDemoPlaces() {
        viewModelScope.launch {
            val demos = repository.createDemoPlaces()
            demos.forEach { repository.add(it) }
        }
    }
}
