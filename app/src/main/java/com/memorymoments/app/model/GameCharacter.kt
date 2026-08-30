package com.memorymoments.app.model

sealed interface GameCharacter {
    val id: String
    val imageUri: String

    data class RealFamilyMember(
        val familyMember: FamilyMember
    ) : GameCharacter {
        override val id: String get() = familyMember.id
        override val imageUri: String get() = familyMember.originalPhotoUri.orEmpty()
    }

    data class AiDistractor(
        val distractor: DistractorCharacter
    ) : GameCharacter {
        override val id: String get() = distractor.id
        override val imageUri: String get() = distractor.imageUri
    }
}
