package com.memorymoments.app.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.memorymoments.app.data.local.PreferenceKeys
import com.memorymoments.app.data.local.appDataStore
import com.memorymoments.app.model.CaregiverStats
import com.memorymoments.app.model.DistractorStyle
import com.memorymoments.app.model.TextSizeOption
import com.memorymoments.app.model.UiMode
import com.memorymoments.app.utils.ImageStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameSettingsRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore = appContext.appDataStore
    private val imageStorage = ImageStorage(appContext)

    val distractorStyle: Flow<DistractorStyle> = dataStore.data.map { prefs ->
        runCatching {
            DistractorStyle.valueOf(prefs[PreferenceKeys.DISTRACTOR_STYLE] ?: DistractorStyle.NORMAL.name)
        }.getOrDefault(DistractorStyle.NORMAL)
    }

    val textSize: Flow<TextSizeOption> = dataStore.data.map { prefs ->
        runCatching {
            TextSizeOption.valueOf(prefs[PreferenceKeys.TEXT_SIZE] ?: TextSizeOption.DEFAULT.name)
        }.getOrDefault(TextSizeOption.DEFAULT)
    }

    val reduceAnimations: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.REDUCE_ANIMATIONS] ?: false
    }

    val soundEffects: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.SOUND_EFFECTS] ?: true
    }

    val haptics: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.HAPTICS] ?: true
    }

    val hasSeenOnboarding: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.HAS_SEEN_ONBOARDING] ?: false
    }

    val ttsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.TTS_ENABLED] ?: true
    }

    val ttsSlowRate: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.TTS_SLOW_RATE] ?: false
    }

    val caregiverStats: Flow<CaregiverStats> = dataStore.data.map { prefs ->
        CaregiverStats(
            gamesPlayed = prefs[PreferenceKeys.GAMES_PLAYED] ?: 0,
            bestScore = prefs[PreferenceKeys.BEST_SCORE] ?: 0,
            bestStreak = prefs[PreferenceKeys.BEST_STREAK] ?: 0,
            totalCorrect = prefs[PreferenceKeys.TOTAL_CORRECT] ?: 0,
            totalQuestions = prefs[PreferenceKeys.TOTAL_QUESTIONS] ?: 0
        )
    }

    val uiMode: Flow<UiMode> = dataStore.data.map { prefs ->
        runCatching {
            UiMode.valueOf(
                prefs[PreferenceKeys.UI_MODE] ?: UiMode.DEFAULT.name
            )
        }.getOrDefault(UiMode.DEFAULT)
    }

    suspend fun setUiMode(mode: UiMode) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.UI_MODE] = mode.name
        }
    }

    suspend fun setDistractorStyle(style: DistractorStyle) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.DISTRACTOR_STYLE] = style.name
        }
    }

    suspend fun setTextSize(textSize: TextSizeOption) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.TEXT_SIZE] = textSize.name
        }
    }

    suspend fun setReduceAnimations(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.REDUCE_ANIMATIONS] = enabled
        }
    }

    suspend fun setSoundEffects(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.SOUND_EFFECTS] = enabled
        }
    }

    suspend fun setHaptics(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.HAPTICS] = enabled
        }
    }

    suspend fun setHasSeenOnboarding(hasSeen: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.HAS_SEEN_ONBOARDING] = hasSeen
        }
    }

    suspend fun setTtsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.TTS_ENABLED] = enabled
        }
    }

    suspend fun setTtsSlowRate(slowRate: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.TTS_SLOW_RATE] = slowRate
        }
    }

    suspend fun recordGameCompletion(correct: Int, total: Int, bestComboInGame: Int) {
        dataStore.edit { prefs ->
            val games = (prefs[PreferenceKeys.GAMES_PLAYED] ?: 0) + 1
            val currentBestScore = prefs[PreferenceKeys.BEST_SCORE] ?: 0
            val newBestScore = maxOf(currentBestScore, correct)
            val currentBestStreak = prefs[PreferenceKeys.BEST_STREAK] ?: 0
            val newBestStreak = maxOf(currentBestStreak, bestComboInGame)
            val totalCorrect = (prefs[PreferenceKeys.TOTAL_CORRECT] ?: 0) + correct
            val totalQuestions = (prefs[PreferenceKeys.TOTAL_QUESTIONS] ?: 0) + total

            prefs[PreferenceKeys.GAMES_PLAYED] = games
            prefs[PreferenceKeys.BEST_SCORE] = newBestScore
            prefs[PreferenceKeys.BEST_STREAK] = newBestStreak
            prefs[PreferenceKeys.TOTAL_CORRECT] = totalCorrect
            prefs[PreferenceKeys.TOTAL_QUESTIONS] = totalQuestions
        }
    }

    val showHeritageContent: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.SHOW_HERITAGE_CONTENT] ?: true
    }

    suspend fun setShowHeritageContent(show: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.SHOW_HERITAGE_CONTENT] = show
        }
    }

    val appLanguage: Flow<String> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.APP_LANGUAGE] ?: "en"
    }

    suspend fun setAppLanguage(languageCode: String) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.APP_LANGUAGE] = languageCode
        }
    }

    suspend fun resetAllData() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
        imageStorage.clearAllDistractors()
    }
}

