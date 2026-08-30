package com.memorymoments.app.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.memorymoments.app.data.local.PreferenceKeys
import com.memorymoments.app.data.local.appDataStore
import com.memorymoments.app.model.AppStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppStateRepository(context: Context) {
    private val dataStore = context.applicationContext.appDataStore

    val stats: Flow<AppStats> = dataStore.data.map { prefs ->
        AppStats(
            level = prefs[PreferenceKeys.LEVEL] ?: 1,
            stars = prefs[PreferenceKeys.STARS] ?: 0,
            bestCombo = prefs[PreferenceKeys.BEST_COMBO] ?: 0,
            xp = prefs[PreferenceKeys.XP] ?: 0
        )
    }

    suspend fun updateStats(stats: AppStats) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.LEVEL] = stats.level
            prefs[PreferenceKeys.STARS] = stats.stars
            prefs[PreferenceKeys.BEST_COMBO] = stats.bestCombo
            prefs[PreferenceKeys.XP] = stats.xp
        }
    }
}
