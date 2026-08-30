package com.memorymoments.app.navigation

object Routes {
    const val LOGIN = "login"
    const val SIGN_UP = "sign-up"
    const val PATIENT_ONBOARDING = "patient-onboarding"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val CAREGIVER = "caregiver"
    const val CAREGIVER_CHAT = "caregiver-chat"
    const val FAMILY = "family"
    const val ADD_FAMILY = "add-family-member"
    const val EDIT_FAMILY = "edit-family-member/{id}"
    const val PLACES = "places"
    const val ADD_PLACE = "add-place"
    const val EDIT_PLACE = "edit-place/{id}"
    const val MEMORIES = "memories"
    const val ADD_MEMORY = "add-memory"
    const val EDIT_MEMORY = "edit-memory/{id}"
    const val MEMORY_TALK = "memory-talk"
    const val TIMELINE = "timeline"
    const val ADD_LIFE_EVENT = "add-life-event"
    const val EDIT_LIFE_EVENT = "edit-life-event/{id}"
    const val MUSIC = "music"
    const val ADD_SONG = "add-song"
    const val EDIT_SONG = "edit-song/{id}"
    const val MEMORY_MUSIC = "memory-music"
    const val GAME_SELECTION = "game-selection"
    const val GAME_SETUP = "game-setup"
    const val GAME = "game"
    const val PLACES_GAME = "places-game"
    const val MUSIC_GAME = "music-game"
    const val RESULTS = "results"
    const val SETTINGS = "settings"
    const val DISTRACTOR_LAB = "distractor-lab"

    const val DEMO_ARG = "demo"
    const val STYLE_ARG = "style"
    const val MEMBER_ID_ARG = "id"
    const val PREVIEW_ARG = "preview"

    const val STARS_ARG = "stars"
    const val XP_ARG = "xp"
    const val COMBO_ARG = "combo"
    const val TOTAL_ARG = "total"
    const val CORRECT_ARG = "correct"

    const val HOME_ROUTE = "$HOME?$PREVIEW_ARG={$PREVIEW_ARG}"
    const val GAME_SETUP_ROUTE = "$GAME_SETUP?$DEMO_ARG={$DEMO_ARG}"
    const val DISTRACTOR_LAB_ROUTE =
        "$DISTRACTOR_LAB?$STYLE_ARG={$STYLE_ARG}&$DEMO_ARG={$DEMO_ARG}"
    const val GAME_ROUTE =
        "$GAME?$STYLE_ARG={$STYLE_ARG}&$DEMO_ARG={$DEMO_ARG}"
    const val PLACES_GAME_ROUTE =
        "$PLACES_GAME?$STYLE_ARG={$STYLE_ARG}&$DEMO_ARG={$DEMO_ARG}"
    const val MUSIC_GAME_ROUTE =
        "$MUSIC_GAME?$STYLE_ARG={$STYLE_ARG}&$DEMO_ARG={$DEMO_ARG}"
    const val MEMORY_TALK_ROUTE =
        "$MEMORY_TALK?$MEMBER_ID_ARG={$MEMBER_ID_ARG}"
    const val RESULTS_ROUTE =
        "$RESULTS?$STARS_ARG={$STARS_ARG}&$XP_ARG={$XP_ARG}&$COMBO_ARG={$COMBO_ARG}&$TOTAL_ARG={$TOTAL_ARG}&$CORRECT_ARG={$CORRECT_ARG}"

    fun home(preview: Boolean = false): String =
        if (preview) "$HOME?$PREVIEW_ARG=true" else HOME

    fun gameSetup(demo: Boolean = false): String = "$GAME_SETUP?$DEMO_ARG=$demo"

    fun editFamily(id: String): String = "edit-family-member/$id"

    fun editPlace(id: String): String = "edit-place/$id"

    fun editSong(id: String): String = "edit-song/$id"

    fun editMemory(id: String): String = "edit-memory/$id"

    fun editLifeEvent(id: String): String = "edit-life-event/$id"

    fun memoryTalk(id: String? = null): String =
        if (id != null) "$MEMORY_TALK?$MEMBER_ID_ARG=$id" else MEMORY_TALK

    fun distractorLab(style: String, demo: Boolean): String =
        "$DISTRACTOR_LAB?$STYLE_ARG=$style&$DEMO_ARG=$demo"

    fun game(style: String, demo: Boolean): String =
        "$GAME?$STYLE_ARG=$style&$DEMO_ARG=$demo"

    fun placesGame(style: String = "NORMAL", demo: Boolean = false): String =
        "$PLACES_GAME?$STYLE_ARG=$style&$DEMO_ARG=$demo"

    fun musicGame(style: String = "NORMAL", demo: Boolean = false): String =
        "$MUSIC_GAME?$STYLE_ARG=$style&$DEMO_ARG=$demo"

    fun results(
        stars: Int,
        xp: Int,
        combo: Int,
        total: Int,
        correct: Int
    ): String =
        "$RESULTS?$STARS_ARG=$stars&$XP_ARG=$xp&$COMBO_ARG=$combo&$TOTAL_ARG=$total&$CORRECT_ARG=$correct"
}
