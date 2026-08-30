package com.memorymoments.app.model

data class AppStats(
    val level: Int = 1,
    val stars: Int = 0,
    val bestCombo: Int = 0,
    val xp: Int = 0
) {
    val levelLabel: String
        get() = level.toString().padStart(2, '0')
}
