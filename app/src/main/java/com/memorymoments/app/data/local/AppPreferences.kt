package com.memorymoments.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.memorymoments.app.utils.Constants

val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.DATASTORE_NAME
)

object PreferenceKeys {
    val LEVEL = intPreferencesKey("level")
    val STARS = intPreferencesKey("stars")
    val BEST_COMBO = intPreferencesKey("best_combo")
    val XP = intPreferencesKey("xp")
    val FAMILY_MEMBERS = stringPreferencesKey("family_members")
    val DISTRACTORS = stringPreferencesKey("distractors")
    val DISTRACTOR_STYLE = stringPreferencesKey("distractor_style")

    // Phase 7 Accessibility & Settings
    val TEXT_SIZE = stringPreferencesKey("text_size")
    val REDUCE_ANIMATIONS = booleanPreferencesKey("reduce_animations")
    val SOUND_EFFECTS = booleanPreferencesKey("sound_effects")
    val HAPTICS = booleanPreferencesKey("haptics")
    val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")

    // Caregiver Gameplay Statistics
    val GAMES_PLAYED = intPreferencesKey("games_played")
    val BEST_SCORE = intPreferencesKey("best_score")
    val BEST_STREAK = intPreferencesKey("best_streak")
    val TOTAL_CORRECT = intPreferencesKey("total_correct")
    val TOTAL_QUESTIONS = intPreferencesKey("total_questions")

    // Hard mode visual profile cache
    val VISUAL_PROFILES = stringPreferencesKey("visual_profiles")

    // Phase 8 Local Data Foundation
    val PEOPLE = stringPreferencesKey("people")
    val PLACES = stringPreferencesKey("places")
    val SONGS = stringPreferencesKey("songs")
    val MEMORIES = stringPreferencesKey("memories")
    val LIFE_EVENTS = stringPreferencesKey("life_events")

    // Phase 8 UI Presentation Mode
    val UI_MODE = stringPreferencesKey("ui_mode")

    // Phase 11 Voice & Speech Settings
    val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
    val TTS_SLOW_RATE = booleanPreferencesKey("tts_slow_rate")

    // Phase 13 Daily Companion Settings & State
    val DAILY_COMPANION_ENABLED = booleanPreferencesKey("daily_companion_enabled")
    val DAILY_MUSIC_ENABLED = booleanPreferencesKey("daily_music_enabled")
    val DAILY_MEMORIES_ENABLED = booleanPreferencesKey("daily_memories_enabled")
    val DAILY_GAMES_ENABLED = booleanPreferencesKey("daily_games_enabled")
    val DAILY_TALK_ENABLED = booleanPreferencesKey("daily_talk_enabled")
    val DAILY_DATE = stringPreferencesKey("daily_date")
    val DAILY_SONG_ID = stringPreferencesKey("daily_song_id")
    val DAILY_MEMORY_ID = stringPreferencesKey("daily_memory_id")
    val DAILY_GAME_TYPE = stringPreferencesKey("daily_game_type")
    val DAILY_COMPLETED_ACTIVITIES = stringSetPreferencesKey("daily_completed_activities")
    val RECENT_DAILY_MEMORY_IDS = stringSetPreferencesKey("recent_daily_memory_ids")
    val RECENT_DAILY_SONG_IDS = stringSetPreferencesKey("recent_daily_song_ids")

    // Northeast Heritage Settings
    val SHOW_HERITAGE_CONTENT = booleanPreferencesKey("show_heritage_content")

    // Authentication & User Accounts
    val USER_ACCOUNTS = stringPreferencesKey("user_accounts")
    val CURRENT_USER_ID = stringPreferencesKey("current_user_id")
    val CURRENT_USER_ROLE = stringPreferencesKey("current_user_role")

    // Caregiver Module Features
    val CAREGIVER_CODE = stringPreferencesKey("caregiver_code")
    val LINKED_PATIENT_INFO = stringPreferencesKey("linked_patient_info")
    val PATIENT_REMINDERS = stringPreferencesKey("patient_reminders")
    val CAREGIVER_CHAT_MESSAGES = stringPreferencesKey("caregiver_chat_messages")
    val CAREGIVER_NOTES = stringPreferencesKey("caregiver_notes")
    val CAREGIVER_ALERTS = stringPreferencesKey("caregiver_alerts")

    // Firebase Session Tokens
    val FIREBASE_ID_TOKEN = stringPreferencesKey("firebase_id_token")
    val FIREBASE_USER_ID = stringPreferencesKey("firebase_user_id")
    val FIREBASE_REFRESH_TOKEN = stringPreferencesKey("firebase_refresh_token")
}

