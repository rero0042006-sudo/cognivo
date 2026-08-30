package com.memorymoments.app.model

data class Question(
    val targetMember: FamilyMember,
    val text: String,
    val options: List<AnswerOption>,
    val personalizedText: String? = null,
    val encouragement: String? = null
) {
    val displayText: String
        get() = personalizedText?.takeIf { it.isNotBlank() } ?: text
}
