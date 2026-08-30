package com.memorymoments.app.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.memorymoments.app.data.local.PreferenceKeys
import com.memorymoments.app.data.local.appDataStore
import com.memorymoments.app.model.Memory
import com.memorymoments.app.utils.ImageStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MemoryRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore = appContext.appDataStore
    private val imageStorage = ImageStorage(appContext)
    private val gson = Gson()
    private val listType = object : TypeToken<List<Memory>>() {}.type
    private val mutex = Mutex()

    val memories: Flow<List<Memory>> = dataStore.data.map { prefs ->
        decode(prefs[PreferenceKeys.MEMORIES])
    }

    suspend fun add(memory: Memory): Result<Unit> = mutex.withLock {
        val current = memories.first()
        if (current.any { it.id == memory.id }) {
            return Result.failure(IllegalStateException("duplicate"))
        }
        persist(current + memory)
        Result.success(Unit)
    }

    suspend fun update(memory: Memory): Result<Unit> = mutex.withLock {
        val current = memories.first()
        if (current.none { it.id == memory.id }) {
            return Result.failure(IllegalStateException("missing"))
        }
        persist(current.map { if (it.id == memory.id) memory.copy(updatedAt = System.currentTimeMillis()) else it })
        Result.success(Unit)
    }

    suspend fun saveOrUpdate(memory: Memory): Result<Unit> = mutex.withLock {
        val current = memories.first()
        val exists = current.any { it.id == memory.id }
        val updatedList = if (exists) {
            current.map { if (it.id == memory.id) memory.copy(updatedAt = System.currentTimeMillis()) else it }
        } else {
            current + memory
        }
        persist(updatedList)
        Result.success(Unit)
    }

    suspend fun delete(id: String): Result<Unit> = mutex.withLock {
        val current = memories.first()
        persist(current.filterNot { it.id == id })
        // Clean up any life event references to this memory
        LifeEventRepository(appContext).removeMemoryReference(id)
        Result.success(Unit)
    }

    suspend fun removePersonReference(personId: String) = mutex.withLock {
        val current = memories.first()
        val updated = current.map { memory ->
            if (memory.personIds.contains(personId)) {
                memory.copy(personIds = memory.personIds.filterNot { it == personId })
            } else {
                memory
            }
        }
        persist(updated)
    }

    suspend fun removePlaceReference(placeId: String, placeName: String? = null) = mutex.withLock {
        val current = memories.first()
        val updated = current.map { memory ->
            var mem = memory
            if (mem.placeIds.contains(placeId)) {
                mem = mem.copy(placeIds = mem.placeIds.filterNot { it == placeId })
            }
            if (placeName != null && mem.place.equals(placeName, ignoreCase = true)) {
                mem = mem.copy(place = null)
            }
            mem
        }
        persist(updated)
    }

    suspend fun removeSongReference(songId: String) = mutex.withLock {
        val current = memories.first()
        val updated = current.map { memory ->
            if (memory.songIds.contains(songId)) {
                memory.copy(songIds = memory.songIds.filterNot { it == songId })
            } else {
                memory
            }
        }
        persist(updated)
    }

    suspend fun getById(id: String): Memory? {
        return memories.first().find { it.id == id }
    }

    suspend fun createDemoMemories(): List<Memory> {
        val photo1 = imageStorage.createDemoPlacePhoto("demo-mem-1", "Wedding Day", 0)
        val photo2 = imageStorage.createDemoPlacePhoto("demo-mem-2", "Beach Trip", 1)
        val photo3 = imageStorage.createDemoPlacePhoto("demo-mem-3", "Graduation", 2)
        val photo4 = imageStorage.createDemoPlacePhoto("demo-mem-4", "Trip to Shillong", 3)

        val demoList = listOf(
            Memory(
                id = "demo-mem-1",
                title = "Wedding Day",
                description = "Our beautiful wedding celebration with all our family and closest friends.",
                date = "1978",
                photoUris = listOf(photo1),
                createdAt = System.currentTimeMillis() - 86400000 * 4
            ),
            Memory(
                id = "demo-mem-2",
                title = "Family Beach Holiday",
                description = "Sunny summer vacation trip building sandcastles and having a picnic by the sea.",
                date = "1985",
                photoUris = listOf(photo2),
                createdAt = System.currentTimeMillis() - 86400000 * 3
            ),
            Memory(
                id = "demo-mem-3",
                title = "College Graduation",
                description = "A proud day celebrating graduation surrounded by proud parents and grandparents.",
                date = "2012",
                photoUris = listOf(photo3),
                createdAt = System.currentTimeMillis() - 86400000 * 2
            ),
            Memory(
                id = "demo-mem-4",
                title = "Our Family Trip to Shillong",
                description = "Walking through the misty pine hills and enjoying warm tea together.",
                date = "1996",
                place = "Shillong",
                state = "Meghalaya",
                region = "Northeast India",
                heritageCategory = "Journeys",
                photoUris = listOf(photo4),
                createdAt = System.currentTimeMillis() - 86400000 * 1
            )
        )

        mutex.withLock {
            val current = memories.first()
            val missing = demoList.filter { d -> current.none { it.id == d.id } }
            if (missing.isNotEmpty()) {
                persist(current + missing)
            }
        }
        return memories.first()
    }

    private suspend fun persist(list: List<Memory>) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.MEMORIES] = gson.toJson(list)
        }
    }

    private fun decode(json: String?): List<Memory> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            gson.fromJson<List<Memory>>(json, listType).orEmpty()
        }.getOrDefault(emptyList())
    }
}
