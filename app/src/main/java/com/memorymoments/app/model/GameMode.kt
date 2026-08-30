package com.memorymoments.app.model

enum class GameMode(
    val displayName: String,
    val memberRange: String,
    val questionTypes: String
) {
    EASY(
        displayName = "Easy",
        memberRange = "3 family members",
        questionTypes = "Name questions"
    ),
    NORMAL(
        displayName = "Normal",
        memberRange = "4–5 family members",
        questionTypes = "Name + relationship"
    ),
    CHALLENGE(
        displayName = "Challenge",
        memberRange = "5–6 family members",
        questionTypes = "Name + relationship + memory"
    )
}
