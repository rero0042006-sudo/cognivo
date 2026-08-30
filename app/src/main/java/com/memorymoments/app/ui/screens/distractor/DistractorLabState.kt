package com.memorymoments.app.ui.screens.distractor

import com.memorymoments.app.model.DistractorCharacter
import com.memorymoments.app.model.DistractorStyle

sealed interface DistractorLabState {
    data object Idle : DistractorLabState
    data class Generating(
        val completed: Int,
        val total: Int,
        val preview: DistractorCharacter? = null
    ) : DistractorLabState
    data class Success(
        val characters: List<DistractorCharacter>
    ) : DistractorLabState
    data class Error(
        val offline: Boolean,
        val message: String? = null
    ) : DistractorLabState
}

data class DistractorLabUiState(
    val style: DistractorStyle = DistractorStyle.NORMAL,
    val isDemo: Boolean = false,
    val phase: DistractorLabState = DistractorLabState.Idle
)
