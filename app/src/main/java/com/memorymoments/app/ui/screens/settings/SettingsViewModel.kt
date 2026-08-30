package com.memorymoments.app.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.memorymoments.app.model.CaregiverStats
import com.memorymoments.app.model.DistractorStyle
import com.memorymoments.app.model.TextSizeOption
import com.memorymoments.app.model.UiMode
import com.memorymoments.app.repository.AppStateRepository
import com.memorymoments.app.repository.DailyCompanionRepository
import com.memorymoments.app.repository.DistractorRepository
import com.memorymoments.app.repository.FamilyRepository
import com.memorymoments.app.repository.GameSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val showResetConfirm: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepo = GameSettingsRepository(application)
    private val familyRepo = FamilyRepository(application)
    private val distractorRepo = DistractorRepository(application)
    private val appStateRepo = AppStateRepository(application)
    private val dailyRepo = DailyCompanionRepository(application)

    val distractorStyle: StateFlow<DistractorStyle> = settingsRepo.distractorStyle.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DistractorStyle.NORMAL
    )

    val textSize: StateFlow<TextSizeOption> = settingsRepo.textSize.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TextSizeOption.DEFAULT
    )

    val reduceAnimations: StateFlow<Boolean> = settingsRepo.reduceAnimations.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    val soundEffects: StateFlow<Boolean> = settingsRepo.soundEffects.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true
    )

    val haptics: StateFlow<Boolean> = settingsRepo.haptics.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true
    )

    val ttsEnabled: StateFlow<Boolean> = settingsRepo.ttsEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true
    )

    val ttsSlowRate: StateFlow<Boolean> = settingsRepo.ttsSlowRate.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    val caregiverStats: StateFlow<CaregiverStats> = settingsRepo.caregiverStats.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CaregiverStats()
    )

    val uiMode: StateFlow<UiMode> = settingsRepo.uiMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiMode.DEFAULT
    )

    // Phase 13 Daily Companion Settings
    val dailyCompanionEnabled: StateFlow<Boolean> = dailyRepo.isCompanionEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true
    )

    val dailyMusicEnabled: StateFlow<Boolean> = dailyRepo.isMusicEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true
    )

    val dailyMemoriesEnabled: StateFlow<Boolean> = dailyRepo.isMemoriesEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true
    )

    val dailyGamesEnabled: StateFlow<Boolean> = dailyRepo.isGamesEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true
    )

    val dailyTalkEnabled: StateFlow<Boolean> = dailyRepo.isTalkEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true
    )

    val showHeritageContent: StateFlow<Boolean> = settingsRepo.showHeritageContent.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true
    )

    fun setShowHeritageContent(show: Boolean) {
        viewModelScope.launch {
            settingsRepo.setShowHeritageContent(show)
        }
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()


    fun setUiMode(mode: UiMode) {
        viewModelScope.launch { settingsRepo.setUiMode(mode) }
    }

    fun setDistractorStyle(style: DistractorStyle) {
        viewModelScope.launch { settingsRepo.setDistractorStyle(style) }
    }

    fun setTextSize(size: TextSizeOption) {
        viewModelScope.launch { settingsRepo.setTextSize(size) }
    }

    fun setReduceAnimations(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setReduceAnimations(enabled) }
    }

    fun setSoundEffects(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setSoundEffects(enabled) }
    }

    fun setHaptics(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setHaptics(enabled) }
    }

    fun setTtsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setTtsEnabled(enabled) }
    }

    fun setTtsSlowRate(slowRate: Boolean) {
        viewModelScope.launch { settingsRepo.setTtsSlowRate(slowRate) }
    }

    fun setDailyCompanionEnabled(enabled: Boolean) {
        viewModelScope.launch { dailyRepo.setCompanionEnabled(enabled) }
    }

    fun setDailyMusicEnabled(enabled: Boolean) {
        viewModelScope.launch { dailyRepo.setMusicEnabled(enabled) }
    }

    fun setDailyMemoriesEnabled(enabled: Boolean) {
        viewModelScope.launch { dailyRepo.setMemoriesEnabled(enabled) }
    }

    fun setDailyGamesEnabled(enabled: Boolean) {
        viewModelScope.launch { dailyRepo.setGamesEnabled(enabled) }
    }

    fun setDailyTalkEnabled(enabled: Boolean) {
        viewModelScope.launch { dailyRepo.setTalkEnabled(enabled) }
    }

    fun requestReset() {
        _uiState.value = _uiState.value.copy(showResetConfirm = true)
    }

    fun cancelReset() {
        _uiState.value = _uiState.value.copy(showResetConfirm = false)
    }

    fun confirmReset() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showResetConfirm = false)
            settingsRepo.resetAllData()
            distractorRepo.clearPool(DistractorStyle.EASY)
            distractorRepo.clearPool(DistractorStyle.NORMAL)
            distractorRepo.clearPool(DistractorStyle.CHALLENGE)
        }
    }
}
