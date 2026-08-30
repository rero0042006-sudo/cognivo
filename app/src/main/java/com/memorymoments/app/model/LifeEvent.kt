package com.memorymoments.app.model

/**
 * Local domain model for significant life events (Birth, School, Marriage, Career, Children, Travel, Home, Achievement, etc.)
 */
data class LifeEvent(
    val id: String,
    val title: String,
    val date: String? = null,
    val category: String? = null,
    val description: String? = null,
    val photoUri: String? = null,
    val personIds: List<String> = emptyList(),
    val placeId: String? = null,
    val songId: String? = null,
    val memoryId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
