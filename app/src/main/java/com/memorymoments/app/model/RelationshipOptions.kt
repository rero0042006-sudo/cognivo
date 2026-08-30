package com.memorymoments.app.model

object RelationshipOptions {
    const val OTHER = "Other"

    val all: List<String> = listOf(
        "Mother",
        "Father",
        "Daughter",
        "Son",
        "Grandmother",
        "Grandfather",
        "Granddaughter",
        "Grandson",
        "Sister",
        "Brother",
        "Wife",
        "Husband",
        "Partner",
        "Friend",
        OTHER
    )

    fun isPreset(value: String): Boolean = all.any { it.equals(value, ignoreCase = true) }

    fun menuSelection(stored: String): String {
        if (stored.isBlank()) return ""
        return all.firstOrNull { it.equals(stored, ignoreCase = true) } ?: OTHER
    }
}
