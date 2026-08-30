package com.memorymoments.app.game.music

import com.memorymoments.app.model.DistractorStyle
import com.memorymoments.app.model.Song
import java.io.File
import java.util.UUID

class MusicGameEngine(
    private val songs: List<Song>,
    private val difficulty: DistractorStyle = DistractorStyle.NORMAL
) {
    fun isReady(): Boolean {
        val usable = songs.filter { song ->
            val file = File(song.localAudioUri)
            file.exists() && file.length() > 0
        }
        return usable.size >= 3
    }

    fun generateQuestions(
        roundCount: Int = 10,
        prioritizedSongIds: Set<String> = emptySet()
    ): List<MusicQuestion> {
        val usableSongs = songs.filter { song ->
            val file = File(song.localAudioUri)
            file.exists() && file.length() > 0
        }

        if (usableSongs.size < 3) return emptyList()

        val totalOptions = when (difficulty) {
            DistractorStyle.EASY -> 3
            DistractorStyle.NORMAL,
            DistractorStyle.CHALLENGE -> 4
        }.coerceAtMost(usableSongs.size)

        val questions = mutableListOf<MusicQuestion>()
        var lastTargetId: String? = null

        // Split into memory-associated songs and remaining pool
        val prioritizedPool = usableSongs.filter { prioritizedSongIds.contains(it.id) }.shuffled().toMutableList()
        val generalPool = usableSongs.shuffled().toMutableList()

        for (i in 0 until roundCount) {
            val target = if (prioritizedPool.isNotEmpty()) {
                val candidate = if (prioritizedPool.size > 1 && prioritizedPool[0].id == lastTargetId) {
                    prioritizedPool.removeAt(1)
                } else {
                    prioritizedPool.removeAt(0)
                }
                candidate
            } else {
                val candidates = if (usableSongs.size > 1 && lastTargetId != null) {
                    usableSongs.filter { it.id != lastTargetId }
                } else {
                    usableSongs
                }
                candidates.random()
            }
            lastTargetId = target.id

            val distractors = selectDistractors(target, usableSongs, totalOptions - 1)

            val correctOption = MusicOption(
                id = target.id,
                title = target.title,
                artist = target.artist,
                isCorrect = true
            )

            val distractorOptions = distractors.map { song ->
                MusicOption(
                    id = song.id,
                    title = song.title,
                    artist = song.artist,
                    isCorrect = false
                )
            }

            val allOptions = (listOf(correctOption) + distractorOptions).shuffled()

            questions.add(
                MusicQuestion(
                    id = UUID.randomUUID().toString(),
                    targetSong = target,
                    options = allOptions,
                    promptText = "WHAT SONG IS THIS?"
                )
            )
        }

        return questions
    }

    private fun selectDistractors(target: Song, allSongs: List<Song>, count: Int): List<Song> {
        val pool = allSongs.filter { it.id != target.id }

        if (difficulty == DistractorStyle.CHALLENGE) {
            // Hard mode: Prioritize songs by same artist or similar name
            val sameArtist = pool.filter { !it.artist.isNullOrBlank() && it.artist.equals(target.artist, ignoreCase = true) }
            val otherSongs = pool.filter { it !in sameArtist }.shuffled()

            val selected = mutableListOf<Song>()
            selected.addAll(sameArtist.shuffled().take(count))

            if (selected.size < count) {
                val needed = count - selected.size
                selected.addAll(otherSongs.take(needed))
            }
            return selected.take(count)
        }

        return pool.shuffled().take(count)
    }
}
