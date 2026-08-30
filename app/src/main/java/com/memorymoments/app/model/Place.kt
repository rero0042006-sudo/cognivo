package com.memorymoments.app.model

/**
 * Local domain model for meaningful places (Childhood Home, School, Workplace, Hometown, Landmark, etc.)
 */
data class Place(
    val id: String,
    val name: String,
    val description: String? = null,
    val location: String? = null,
    val state: String? = null,
    val region: String? = null,
    val landmarkType: String? = null,
    val datePeriod: String? = null,
    val photoUris: List<String> = emptyList(),
    val memoryId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val displayPhotoUri: String?
        get() = photoUris.firstOrNull()
}

