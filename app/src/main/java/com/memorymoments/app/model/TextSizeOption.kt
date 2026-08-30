package com.memorymoments.app.model

enum class TextSizeOption(
    val displayName: String,
    val scaleFactor: Float
) {
    SMALL("Small", 0.9f),
    NORMAL("Normal", 1.0f),
    LARGE("Large", 1.15f),
    EXTRA_LARGE("Extra Large", 1.3f);

    companion object {
        val DEFAULT = LARGE
    }
}

data class CaregiverStats(
    val gamesPlayed: Int = 0,
    val bestScore: Int = 0,
    val bestStreak: Int = 0,
    val totalCorrect: Int = 0,
    val totalQuestions: Int = 0
) {
    val accuracyPercentage: Int
        get() = if (totalQuestions <= 0) 0 else ((totalCorrect.toFloat() / totalQuestions) * 100).toInt()
}
