package com.memorymoments.app.model

enum class DistractorStyle(
    val displayName: String,
    val caption: String,
    val apiDifficulty: String
) {
    EASY(
        displayName = "Easy",
        caption = "Different-looking people",
        apiDifficulty = "easy"
    ),
    NORMAL(
        displayName = "Normal",
        caption = "More similar-looking people",
        apiDifficulty = "medium"
    ),
    CHALLENGE(
        displayName = "Challenge",
        caption = "Harder to distinguish",
        apiDifficulty = "hard"
    )
}
