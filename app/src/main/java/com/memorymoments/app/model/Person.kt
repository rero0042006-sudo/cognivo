package com.memorymoments.app.model

/**
 * Local domain model for a recognized person in Memory Moments.
 * Unifies family members and future people recognition.
 */
data class Person(
    val id: String,
    val name: String,
    val relationship: String,
    val photoUri: String? = null,
    val voiceUri: String? = null,
    val notes: String? = null,
    val nickname: String = "",
    val memoryContext: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val displayPhotoUri: String?
        get() = photoUri

    fun toFamilyMember(): FamilyMember {
        return FamilyMember(
            id = id,
            name = name,
            relationship = relationship,
            nickname = nickname,
            memoryContext = memoryContext,
            originalPhotoUri = photoUri
        )
    }

    companion object {
        fun fromFamilyMember(member: FamilyMember): Person {
            return Person(
                id = member.id,
                name = member.name,
                relationship = member.relationship,
                photoUri = member.originalPhotoUri,
                nickname = member.nickname,
                memoryContext = member.memoryContext
            )
        }
    }
}
