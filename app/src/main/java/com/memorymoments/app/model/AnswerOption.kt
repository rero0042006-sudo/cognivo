package com.memorymoments.app.model

data class AnswerOption(
    val id: String,
    val imageUri: String,
    val isCorrect: Boolean,
    val familyMemberId: String? = null
)
