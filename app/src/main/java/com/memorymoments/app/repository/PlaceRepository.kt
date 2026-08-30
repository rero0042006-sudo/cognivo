package com.memorymoments.app.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.memorymoments.app.data.local.PreferenceKeys
import com.memorymoments.app.data.local.appDataStore
import com.memorymoments.app.model.Place
import com.memorymoments.app.utils.ImageStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PlaceRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore = appContext.appDataStore
    private val imageStorage = ImageStorage(appContext)
    private val gson = Gson()
    private val listType = object : TypeToken<List<Place>>() {}.type
    private val mutex = Mutex()

    val places: Flow<List<Place>> = dataStore.data.map { prefs ->
        decode(prefs[PreferenceKeys.PLACES])
    }

    suspend fun add(place: Place): Result<Unit> = mutex.withLock {
        val current = places.first()
        if (current.any { it.id == place.id }) {
            return Result.failure(IllegalStateException("duplicate"))
        }
        persist(current + place)
        Result.success(Unit)
    }

    suspend fun update(place: Place): Result<Unit> = mutex.withLock {
        val current = places.first()
        if (current.none { it.id == place.id }) {
            return Result.failure(IllegalStateException("missing"))
        }
        persist(current.map { if (it.id == place.id) place.copy(updatedAt = System.currentTimeMillis()) else it })
        Result.success(Unit)
    }

    suspend fun delete(id: String): Result<Unit> = mutex.withLock {
        val current = places.first()
        val targetPlace = current.find { it.id == id }
        persist(current.filterNot { it.id == id })
        imageStorage.deleteForPlace(id)
        MemoryRepository(appContext).removePlaceReference(id, targetPlace?.name)
        LifeEventRepository(appContext).removePlaceReference(id)
        Result.success(Unit)
    }

    suspend fun getById(id: String): Place? {
        return places.first().find { it.id == id }
    }

    suspend fun createDemoPlaces(): List<Place> {
        val demoPlaces = listOf(
            Place(
                id = "demo-place-1",
                name = "Childhood Home",
                location = "Chennai",
                description = "The house on Greenways Road where John grew up.",
                datePeriod = "1950s",
                photoUris = listOf(imageStorage.createDemoPlacePhoto("demo-place-1", "Chennai Home", 0))
            ),
            Place(
                id = "demo-place-2",
                name = "St. Mary's School",
                location = "Chennai",
                description = "High school attended in the 1960s.",
                datePeriod = "1960s",
                photoUris = listOf(imageStorage.createDemoPlacePhoto("demo-place-2", "School", 1))
            ),
            Place(
                id = "demo-place-3",
                name = "Marina Beach",
                location = "Chennai",
                description = "Favorite Sunday evening family walking spot.",
                datePeriod = "1970s",
                photoUris = listOf(imageStorage.createDemoPlacePhoto("demo-place-3", "Marina Beach", 2))
            ),
            Place(
                id = "demo-place-4",
                name = "Botanical Gardens",
                location = "Ooty",
                description = "Honeymoon and annual summer vacation destination.",
                datePeriod = "1975",
                photoUris = listOf(imageStorage.createDemoPlacePhoto("demo-place-4", "Ooty Garden", 3))
            ),
            Place(
                id = "demo-place-5",
                name = "Railway Central Station",
                location = "Bangalore",
                description = "Historic station from weekend work trips.",
                datePeriod = "1980s",
                photoUris = listOf(imageStorage.createDemoPlacePhoto("demo-place-5", "Bangalore", 4))
            )
        )
        return demoPlaces
    }

    private suspend fun persist(list: List<Place>) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.PLACES] = gson.toJson(list)
        }
    }

    private fun decode(json: String?): List<Place> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            gson.fromJson<List<Place>>(json, listType).orEmpty()
        }.getOrDefault(emptyList())
    }
}
