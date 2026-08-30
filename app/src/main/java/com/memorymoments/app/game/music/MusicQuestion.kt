package com.memorymoments.app.game.music

import com.memorymoments.app.model.Song

data class MusicOption(
    val id: String,
    val title: String,
    val artist: String?,
    val isCorrect: Boolean
)

data class MusicQuestion(
    val id: String,
    val targetSong: Song,
    val options: List<MusicOption>,
    val promptText: String = "WHAT SONG IS THIS?"
)
