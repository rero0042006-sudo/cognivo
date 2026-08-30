package com.memorymoments.app.model

data class DistractorCharacter(
    val id: String,
    val imageUri: String,
    val difficulty: DistractorStyle,
    val generatedAt: Long,
    val source: Source = Source.CLOUDFLARE,
    val sourceFamilyMemberId: String? = null
) {
    enum class Source { CLOUDFLARE, DEMO }
}
