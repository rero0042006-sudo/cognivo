package com.memorymoments.app.game

import com.memorymoments.app.model.GameMode
import com.memorymoments.app.utils.Constants

data class GameConfig(
    val mode: GameMode = GameMode.EASY,
    val roundCount: Int = Constants.DEFAULT_ROUNDS,
    val isDemo: Boolean = false
)
