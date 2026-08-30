package com.memorymoments.app.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.memorymoments.app.data.local.PreferenceKeys
import com.memorymoments.app.data.local.appDataStore
import com.memorymoments.app.model.LifeEvent
import com.memorymoments.app.utils.ImageStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LifeEventRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore = appContext.appDataStore
    private val imageStorage = ImageStorage(appContext)
    private val personRepo = PersonRepository(appContext)
    private val placeRepo = PlaceRepository(appContext)
    private val songRepo = SongRepository(appContext)
    private val memoryRepo = MemoryRepository(appContext)
    private val gson = Gson()
    private val listType = object : TypeToken<List<LifeEvent>>() {}.type
    private val mutex = Mutex()

    val lifeEvents: Flow<List<LifeEvent>> = dataStore.data.map { prefs ->
        decode(prefs[PreferenceKeys.LIFE_EVENTS])
    }

    suspend fun add(event: LifeEvent): Result<Unit> = mutex.withLock {
        val current = lifeEvents.first()
        if (current.any { it.id == event.id }) {
            return Result.failure(IllegalStateException("duplicate"))
        }
        persist(current + event)
        Result.success(Unit)
    }

    suspend fun update(event: LifeEvent): Result<Unit> = mutex.withLock {
        val current = lifeEvents.first()
        if (current.none { it.id == event.id }) {
            return Result.failure(IllegalStateException("missing"))
        }
        persist(current.map { if (it.id == event.id) event.copy(updatedAt = System.currentTimeMillis()) else it })
        Result.success(Unit)
    }

    suspend fun delete(id: String): Result<Unit> = mutex.withLock {
        val current = lifeEvents.first()
        persist(current.filterNot { it.id == id })
        Result.success(Unit)
    }

    suspend fun removePersonReference(personId: String) = mutex.withLock {
        val current = lifeEvents.first()
        val updated = current.map { event ->
            if (event.personIds.contains(personId)) {
                event.copy(personIds = event.personIds.filterNot { it == personId })
            } else {
                event
            }
        }
        persist(updated)
    }

    suspend fun removePlaceReference(placeId: String) = mutex.withLock {
        val current = lifeEvents.first()
        val updated = current.map { event ->
            if (event.placeId == placeId) {
                event.copy(placeId = null)
            } else {
                event
            }
        }
        persist(updated)
    }

    suspend fun removeSongReference(songId: String) = mutex.withLock {
        val current = lifeEvents.first()
        val updated = current.map { event ->
            if (event.songId == songId) {
                event.copy(songId = null)
            } else {
                event
            }
        }
        persist(updated)
    }

    suspend fun removeMemoryReference(memoryId: String) = mutex.withLock {
        val current = lifeEvents.first()
        val updated = current.map { event ->
            if (event.memoryId == memoryId) {
                event.copy(memoryId = null)
            } else {
                event
            }
        }
        persist(updated)
    }

    suspend fun getById(id: String): LifeEvent? {
        return lifeEvents.first().find { it.id == id }
    }

    suspend fun createDemoLifeEvents(): List<LifeEvent> {
        val demoPhoto1 = imageStorage.createDemoPlacePhoto("demo-life-1", "Born in Chennai", 0)
        val demoPhoto2 = imageStorage.createDemoPlacePhoto("demo-life-2", "School Days", 1)
        val demoPhoto3 = imageStorage.createDemoPlacePhoto("demo-life-3", "Our Wedding", 2)
        val demoPhoto4 = imageStorage.createDemoPlacePhoto("demo-life-4", "Sarah Born", 3)
        val demoPhoto5 = imageStorage.createDemoPlacePhoto("demo-life-5", "New House", 4)

        val people = personRepo.people.first()
        val maryId = people.find { it.name.contains("Mary", ignoreCase = true) }?.id
        val sarahId = people.find { it.name.contains("Sarah", ignoreCase = true) }?.id

        val places = placeRepo.places.first()
        val chennaiId = places.find { it.name.contains("Chennai", ignoreCase = true) }?.id
        val bangaloreId = places.find { it.name.contains("Bangalore", ignoreCase = true) }?.id

        val songs = songRepo.songs.first()
        val weddingSongId = songs.firstOrNull()?.id

        val memories = memoryRepo.memories.first()
        val weddingMemoryId = memories.find { it.title.contains("Wedding", ignoreCase = true) }?.id

        val demoList = listOf(
            LifeEvent(
                id = "demo-event-1",
                title = "Born in Chennai",
                date = "1948",
                category = "Birth",
                description = "Born on a warm autumn afternoon in the historic district of Chennai.",
                photoUri = demoPhoto1,
                placeId = chennaiId
            ),
            LifeEvent(
                id = "demo-event-2",
                title = "High School Graduation",
                date = "1965",
                category = "School",
                description = "Completed high school with honors surrounded by schoolmates and teachers.",
                photoUri = demoPhoto2,
                placeId = chennaiId
            ),
            LifeEvent(
                id = "demo-event-3",
                title = "Wedding Day",
                date = "1978",
                category = "Marriage",
                description = "A joyous wedding ceremony celebrating life together with Mary.",
                photoUri = demoPhoto3,
                personIds = listOfNotNull(maryId),
                placeId = chennaiId,
                songId = weddingSongId,
                memoryId = weddingMemoryId
            ),
            LifeEvent(
                id = "demo-event-4",
                title = "Daughter Sarah Born",
                date = "1982",
                category = "Family",
                description = "Welcomed our beloved daughter Sarah into our family and home.",
                photoUri = demoPhoto4,
                personIds = listOfNotNull(sarahId)
            ),
            LifeEvent(
                id = "demo-event-5",
                title = "Moved to Bangalore Home",
                date = "1995",
                category = "Home",
                description = "Settled into our quiet garden house in Bangalore.",
                photoUri = demoPhoto5,
                placeId = bangaloreId
            )
        )

        mutex.withLock {
            val current = lifeEvents.first()
            val missing = demoList.filter { d -> current.none { it.id == d.id } }
            if (missing.isNotEmpty()) {
                persist(current + missing)
            }
        }
        return lifeEvents.first()
    }

    private suspend fun persist(list: List<LifeEvent>) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.LIFE_EVENTS] = gson.toJson(list)
        }
    }

    private fun decode(json: String?): List<LifeEvent> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            gson.fromJson<List<LifeEvent>>(json, listType).orEmpty()
        }.getOrDefault(emptyList())
    }

    companion object {
        fun extractYear(dateStr: String?): Int? {
            if (dateStr.isNullOrBlank()) return null
            val regex = Regex("\\b(19\\d\\d|20\\d\\d)\\b")
            val match = regex.find(dateStr)
            return match?.value?.toIntOrNull()
        }

        fun sortChronological(events: List<LifeEvent>, ascending: Boolean = true): List<LifeEvent> {
            val dated = events.filter { extractYear(it.date) != null }
            val undated = events.filter { extractYear(it.date) == null }

            val sortedDated = if (ascending) {
                dated.sortedBy { extractYear(it.date) ?: 0 }
            } else {
                dated.sortedByDescending { extractYear(it.date) ?: 0 }
            }

            return sortedDated + undated
        }
    }
}
