package com.memorymoments.app.ui.screens.gamesetup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.memorymoments.app.model.DistractorStyle
import com.memorymoments.app.model.GameMode
import com.memorymoments.app.repository.GameSettingsRepository
import com.memorymoments.app.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameSetupViewModel(application: Application) : AndroidViewModel(application) {
    private val settings = GameSettingsRepository(application)

    val distractorStyle: StateFlow<DistractorStyle> = settings.distractorStyle.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DistractorStyle.NORMAL
    )

    private val _mode = MutableStateFlow(GameMode.EASY)
    val mode: StateFlow<GameMode> = _mode.asStateFlow()

    private val _rounds = MutableStateFlow(Constants.DEFAULT_ROUNDS)
    val rounds: StateFlow<Int> = _rounds.asStateFlow()

    fun setMode(mode: GameMode) {
        _mode.value = mode
    }

    fun setRounds(rounds: Int) {
        _rounds.value = rounds
    }

    fun setDistractorStyle(style: DistractorStyle) {
        viewModelScope.launch { settings.setDistractorStyle(style) }
    }
}
