package com.memorymoments.app.ui.screens.game

import com.memorymoments.app.model.Question

data class GameSessionResult(
    val starsEarned: Int = 0,
    val xpEarned: Int = 0,
    val bestCombo: Int = 0,
    val totalRounds: Int = 0,
    val correctAnswers: Int = 0
)

sealed interface GameState {
    data object Loading : GameState
    data object NotEnoughFamily : GameState

    data class Playing(
        val roundNumber: Int,
        val totalRounds: Int,
        val question: Question,
        val selectedOptionId: String? = null,
        val isCorrect: Boolean? = null,
        val stars: Int = 0,
        val xp: Int = 0,
        val combo: Int = 0,
        val bestCombo: Int = 0,
        val isAdvancing: Boolean = false
    ) : GameState

    data class Completed(
        val result: GameSessionResult
    ) : GameState
}
