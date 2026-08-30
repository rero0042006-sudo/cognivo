package com.memorymoments.app.model

/**
 * Local domain model for musical memories and songs.
 * Audio files are stored locally on device.
 * Includes optional caregiver category (Family Favorite, Childhood, Festival, Regional, Spiritual, Wedding, Other).
 */
data class Song(
    val id: String,
    val title: String,
    val artist: String? = null,
    val localAudioUri: String,
    val category: String? = null,
    val memoryId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

