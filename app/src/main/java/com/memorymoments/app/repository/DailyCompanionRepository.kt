package com.memorymoments.app.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.memorymoments.app.data.local.PreferenceKeys
import com.memorymoments.app.data.local.appDataStore
import com.memorymoments.app.model.Memory
import com.memorymoments.app.model.Song
import com.memorymoments.app.navigation.Routes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DailyGameType(val title: String, val description: String, val icon: String) {
    WHOS_WHO("Who's Who?", "Recognize familiar faces of your family.", "🎮"),
    WHERE_WAS_IT("Where Was It?", "Recall special hometowns and travel spots.", "📍"),
    NAME_THAT_TUNE("Name That Tune", "Listen and recognize favorite melodies.", "🎵"),
    MEMORY_TALK("Memory Talk", "Look at a photo and chat about old times.", "🎤")
}

data class DailyGameOption(
    val type: DailyGameType,
    val title: String,
    val description: String,
    val route: String
)

data class DailyCompanionData(
    val isEnabled: Boolean = true,
    val isMusicEnabled: Boolean = true,
    val isMemoriesEnabled: Boolean = true,
    val isGamesEnabled: Boolean = true,
    val isTalkEnabled: Boolean = true,
    val greeting: String = "GOOD MORNING ❤️",
    val todaySong: Song? = null,
    val todayMemory: Memory? = null,
    val todayGame: DailyGameOption? = null,
    val completedActivities: Set<String> = emptySet(),
    val hasAnyContent: Boolean = true
)

class DailyCompanionRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore = appContext.appDataStore
    private val personRepo = PersonRepository(appContext)
    private val placeRepo = PlaceRepository(appContext)
    private val songRepo = SongRepository(appContext)
    private val memoryRepo = MemoryRepository(appContext)
    private val mutex = Mutex()

    val isCompanionEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.DAILY_COMPANION_ENABLED] ?: true
    }

    val isMusicEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.DAILY_MUSIC_ENABLED] ?: true
    }

    val isMemoriesEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.DAILY_MEMORIES_ENABLED] ?: true
    }

    val isGamesEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.DAILY_GAMES_ENABLED] ?: true
    }

    val isTalkEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.DAILY_TALK_ENABLED] ?: true
    }

    suspend fun setCompanionEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.DAILY_COMPANION_ENABLED] = enabled }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.DAILY_MUSIC_ENABLED] = enabled }
    }

    suspend fun setMemoriesEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.DAILY_MEMORIES_ENABLED] = enabled }
    }

    suspend fun setGamesEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.DAILY_GAMES_ENABLED] = enabled }
    }

    suspend fun setTalkEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.DAILY_TALK_ENABLED] = enabled }
    }

    val dailyCompanionData: Flow<DailyCompanionData> = combine(
        dataStore.data,
        songRepo.songs,
        memoryRepo.memories,
        personRepo.people,
        placeRepo.places
    ) { prefs, songs, memories, people, places ->
        val isEnabled = prefs[PreferenceKeys.DAILY_COMPANION_ENABLED] ?: true
        val musicOn = prefs[PreferenceKeys.DAILY_MUSIC_ENABLED] ?: true
        val memoriesOn = prefs[PreferenceKeys.DAILY_MEMORIES_ENABLED] ?: true
        val gamesOn = prefs[PreferenceKeys.DAILY_GAMES_ENABLED] ?: true
        val talkOn = prefs[PreferenceKeys.DAILY_TALK_ENABLED] ?: true

        val todayDate = getTodayDateString()
        val savedDate = prefs[PreferenceKeys.DAILY_DATE]
        val completed = if (savedDate == todayDate) {
            prefs[PreferenceKeys.DAILY_COMPLETED_ACTIVITIES] ?: emptySet()
        } else {
            emptySet()
        }

        val savedSongId = prefs[PreferenceKeys.DAILY_SONG_ID]
        val savedMemoryId = prefs[PreferenceKeys.DAILY_MEMORY_ID]
        val savedGameTypeStr = prefs[PreferenceKeys.DAILY_GAME_TYPE]

        val todaySong = if (musicOn && songs.isNotEmpty()) {
            songs.find { it.id == savedSongId } ?: songs.first()
        } else null

        val todayMemory = if (memoriesOn && memories.isNotEmpty()) {
            memories.find { it.id == savedMemoryId } ?: memories.first()
        } else null

        val availableGameTypes = mutableListOf<DailyGameType>()
        if (gamesOn && people.size >= 1) availableGameTypes.add(DailyGameType.WHOS_WHO)
        if (gamesOn && places.size >= 1) availableGameTypes.add(DailyGameType.WHERE_WAS_IT)
        if (gamesOn && songs.size >= 1) availableGameTypes.add(DailyGameType.NAME_THAT_TUNE)
        if (talkOn && memories.isNotEmpty()) availableGameTypes.add(DailyGameType.MEMORY_TALK)

        val selectedGameType = if (availableGameTypes.isNotEmpty()) {
            val fromSaved = savedGameTypeStr?.let { str ->
                runCatching { DailyGameType.valueOf(str) }.getOrNull()
            }
            if (fromSaved != null && availableGameTypes.contains(fromSaved)) {
                fromSaved
            } else {
                availableGameTypes.first()
            }
        } else null

        val todayGameOption = selectedGameType?.let { type ->
            val route = when (type) {
                DailyGameType.WHOS_WHO -> Routes.game(style = "NORMAL", demo = false)
                DailyGameType.WHERE_WAS_IT -> Routes.placesGame(style = "NORMAL", demo = false)
                DailyGameType.NAME_THAT_TUNE -> Routes.musicGame(style = "NORMAL", demo = false)
                DailyGameType.MEMORY_TALK -> Routes.memoryTalk(todayMemory?.id)
            }
            DailyGameOption(type, type.title, type.description, route)
        }

        val hasAnyContent = songs.isNotEmpty() || memories.isNotEmpty() || people.isNotEmpty() || places.isNotEmpty()

        DailyCompanionData(
            isEnabled = isEnabled,
            isMusicEnabled = musicOn,
            isMemoriesEnabled = memoriesOn,
            isGamesEnabled = gamesOn,
            isTalkEnabled = talkOn,
            greeting = getGreetingForCurrentTime(),
            todaySong = todaySong,
            todayMemory = todayMemory,
            todayGame = todayGameOption,
            completedActivities = completed,
            hasAnyContent = hasAnyContent
        )
    }

    suspend fun checkAndRefreshDailyState() = mutex.withLock {
        val todayDate = getTodayDateString()
        val prefs = dataStore.data.first()
        val savedDate = prefs[PreferenceKeys.DAILY_DATE]

        val songs = songRepo.songs.first()
        val memories = memoryRepo.memories.first()
        val people = personRepo.people.first()
        val places = placeRepo.places.first()
        val gamesOn = prefs[PreferenceKeys.DAILY_GAMES_ENABLED] ?: true
        val talkOn = prefs[PreferenceKeys.DAILY_TALK_ENABLED] ?: true

        if (savedDate != todayDate) {
            // New day rollover
            val recentMemories = prefs[PreferenceKeys.RECENT_DAILY_MEMORY_IDS] ?: emptySet()
            val recentSongs = prefs[PreferenceKeys.RECENT_DAILY_SONG_IDS] ?: emptySet()

            // Pick non-repeating memory if possible
            val nextMem = if (memories.isNotEmpty()) {
                val candidates = memories.filterNot { recentMemories.contains(it.id) }
                (candidates.ifEmpty { memories }).first()
            } else null

            // Pick non-repeating song if possible
            val nextSong = if (songs.isNotEmpty()) {
                val candidates = songs.filterNot { recentSongs.contains(it.id) }
                (candidates.ifEmpty { songs }).first()
            } else null

            // Pick next game type deterministically based on day of year
            val availableGameTypes = mutableListOf<DailyGameType>()
            if (gamesOn && people.size >= 1) availableGameTypes.add(DailyGameType.WHOS_WHO)
            if (gamesOn && places.size >= 1) availableGameTypes.add(DailyGameType.WHERE_WAS_IT)
            if (gamesOn && songs.size >= 1) availableGameTypes.add(DailyGameType.NAME_THAT_TUNE)
            if (talkOn && memories.isNotEmpty()) availableGameTypes.add(DailyGameType.MEMORY_TALK)

            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val groqRepo = GroqPersonalizationRepository(appContext)
            val difficulty = prefs[PreferenceKeys.DISTRACTOR_STYLE] ?: "EASY"
            val groqRecommendedName = if (availableGameTypes.size > 1) {
                groqRepo.recommendNextActivity(
                    availableActivities = availableGameTypes.map { it.name },
                    candidateMemories = memories.map { it.title },
                    difficulty = difficulty
                )
            } else null

            val nextGame = if (groqRecommendedName != null) {
                availableGameTypes.find { it.name == groqRecommendedName }
                    ?: availableGameTypes[dayOfYear % availableGameTypes.size]
            } else if (availableGameTypes.isNotEmpty()) {
                availableGameTypes[dayOfYear % availableGameTypes.size]
            } else null

            val updatedRecentMemories = if (nextMem != null) {
                if (recentMemories.size >= 7) setOf(nextMem.id) else recentMemories + nextMem.id
            } else recentMemories

            val updatedRecentSongs = if (nextSong != null) {
                if (recentSongs.size >= 7) setOf(nextSong.id) else recentSongs + nextSong.id
            } else recentSongs

            dataStore.edit { p ->
                p[PreferenceKeys.DAILY_DATE] = todayDate
                p[PreferenceKeys.DAILY_COMPLETED_ACTIVITIES] = emptySet()
                if (nextMem != null) p[PreferenceKeys.DAILY_MEMORY_ID] = nextMem.id
                if (nextSong != null) p[PreferenceKeys.DAILY_SONG_ID] = nextSong.id
                if (nextGame != null) p[PreferenceKeys.DAILY_GAME_TYPE] = nextGame.name
                p[PreferenceKeys.RECENT_DAILY_MEMORY_IDS] = updatedRecentMemories
                p[PreferenceKeys.RECENT_DAILY_SONG_IDS] = updatedRecentSongs
            }
        }
    }

    suspend fun nextDailyMemory() = mutex.withLock {
        val memories = memoryRepo.memories.first()
        if (memories.isEmpty()) return@withLock
        val prefs = dataStore.data.first()
        val currentId = prefs[PreferenceKeys.DAILY_MEMORY_ID]
        val currentIndex = memories.indexOfFirst { it.id == currentId }
        val nextIndex = if (currentIndex >= 0) (currentIndex + 1) % memories.size else 0
        val next = memories[nextIndex]

        dataStore.edit {
            it[PreferenceKeys.DAILY_MEMORY_ID] = next.id
        }
    }

    suspend fun nextDailySong() = mutex.withLock {
        val songs = songRepo.songs.first()
        if (songs.isEmpty()) return@withLock
        val prefs = dataStore.data.first()
        val currentId = prefs[PreferenceKeys.DAILY_SONG_ID]
        val currentIndex = songs.indexOfFirst { it.id == currentId }
        val nextIndex = if (currentIndex >= 0) (currentIndex + 1) % songs.size else 0
        val next = songs[nextIndex]

        dataStore.edit {
            it[PreferenceKeys.DAILY_SONG_ID] = next.id
        }
    }

    suspend fun markActivityCompleted(activityKey: String) = mutex.withLock {
        dataStore.edit { prefs ->
            val current = prefs[PreferenceKeys.DAILY_COMPLETED_ACTIVITIES] ?: emptySet()
            prefs[PreferenceKeys.DAILY_COMPLETED_ACTIVITIES] = current + activityKey
        }
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun getGreetingForCurrentTime(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "GOOD MORNING ❤️"
            in 12..16 -> "GOOD AFTERNOON ❤️"
            else -> "GOOD EVENING ❤️"
        }
    }
}
