package com.memorymoments.app.model

/**
 * Local domain model connecting people, places, songs, photos, and voice notes into a rich reminiscence memory.
 * Includes optional cultural and regional heritage fields for personalized reminiscence.
 */
data class Memory(
    val id: String,
    val title: String,
    val description: String? = null,
    val date: String? = null,
    val personIds: List<String> = emptyList(),
    val placeIds: List<String> = emptyList(),
    val songIds: List<String> = emptyList(),
    val photoUris: List<String> = emptyList(),
    val voiceUris: List<String> = emptyList(),
    val heritageCategory: String? = null,
    val region: String? = null,
    val state: String? = null,
    val language: String? = null,
    val place: String? = null,
    val era: String? = null,
    val familyContext: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

