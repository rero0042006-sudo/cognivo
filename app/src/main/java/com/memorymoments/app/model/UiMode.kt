package com.memorymoments.app.model

/**
 * Global presentation mode for Memory Moments:
 * - EASY: Senior-friendly simple interface (large buttons, high contrast, calm feedback) — DEFAULT
 * - GAME: Retro arcade game interface (pixel badges, arcade HUD, stars, XP bars)
 */
enum class UiMode(
    val displayName: String,
    val description: String
) {
    GAME("Game Mode", "Retro arcade game interface"),
    EASY("Easy Mode", "Simple, large, calm interface");

    companion object {
        val DEFAULT = EASY
    }
}
