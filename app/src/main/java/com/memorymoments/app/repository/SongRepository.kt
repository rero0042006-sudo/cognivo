package com.memorymoments.app.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.memorymoments.app.data.local.PreferenceKeys
import com.memorymoments.app.data.local.appDataStore
import com.memorymoments.app.model.Song
import com.memorymoments.app.utils.AudioStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SongRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore = appContext.appDataStore
    private val audioStorage = AudioStorage(appContext)
    private val gson = Gson()
    private val listType = object : TypeToken<List<Song>>() {}.type
    private val mutex = Mutex()

    val songs: Flow<List<Song>> = dataStore.data.map { prefs ->
        decode(prefs[PreferenceKeys.SONGS])
    }

    suspend fun add(song: Song): Result<Unit> = mutex.withLock {
        val current = songs.first()
        if (current.any { it.id == song.id }) {
            return Result.failure(IllegalStateException("duplicate"))
        }
        persist(current + song)
        Result.success(Unit)
    }

    suspend fun update(song: Song): Result<Unit> = mutex.withLock {
        val current = songs.first()
        if (current.none { it.id == song.id }) {
            return Result.failure(IllegalStateException("missing"))
        }
        persist(current.map { if (it.id == song.id) song else it })
        Result.success(Unit)
    }

    suspend fun delete(id: String): Result<Unit> = mutex.withLock {
        audioStorage.deleteForSong(id)
        val current = songs.first()
        persist(current.filterNot { it.id == id })
        MemoryRepository(appContext).removeSongReference(id)
        LifeEventRepository(appContext).removeSongReference(id)
        Result.success(Unit)
    }

    suspend fun getById(id: String): Song? {
        return songs.first().find { it.id == id }
    }

    suspend fun createDemoSongs(): List<Song> {
        val demoDefinitions = listOf(
            Triple("demo-song-1", "Here Comes the Sun", "The Beatles"),
            Triple("demo-song-2", "What a Wonderful World", "Louis Armstrong"),
            Triple("demo-song-3", "Stand by Me", "Ben E. King"),
            Triple("demo-song-4", "Hey Jude", "The Beatles"),
            Triple("demo-song-5", "Fly Me to the Moon", "Frank Sinatra")
        )

        val demoSongs = demoDefinitions.mapIndexed { index, (id, title, artist) ->
            val audioPath = audioStorage.createDemoSongAudio(id, index)
            Song(
                id = id,
                title = title,
                artist = artist,
                localAudioUri = audioPath,
                createdAt = System.currentTimeMillis() - (index * 60_000)
            )
        }

        mutex.withLock {
            val current = songs.first()
            val missing = demoSongs.filter { d -> current.none { it.id == d.id } }
            if (missing.isNotEmpty()) {
                persist(current + missing)
            }
        }
        return songs.first()
    }

    private suspend fun persist(list: List<Song>) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.SONGS] = gson.toJson(list)
        }
    }

    private fun decode(json: String?): List<Song> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            gson.fromJson<List<Song>>(json, listType).orEmpty()
        }.getOrDefault(emptyList())
    }
}
