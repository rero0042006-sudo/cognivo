package com.memorymoments.app.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.memorymoments.app.audio.AudioPlaybackManager
import com.memorymoments.app.model.AppStats
import com.memorymoments.app.model.Song
import com.memorymoments.app.repository.AppStateRepository
import com.memorymoments.app.repository.DailyCompanionData
import com.memorymoments.app.repository.DailyCompanionRepository
import com.memorymoments.app.navigation.Routes
import com.memorymoments.app.repository.AuthRepository
import com.memorymoments.app.repository.RecommendationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecommendationUiState(
    val isLoading: Boolean = false,
    val nextGameDomain: String? = null,
    val recommendedGameTitle: String? = null,
    val domainTag: String? = null,
    val recommendedRoute: String? = null,
    val error: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val appStateRepo = AppStateRepository(application)
    val dailyCompanionRepo = DailyCompanionRepository(application)
    val audioPlaybackManager = AudioPlaybackManager(application)
    private val caregiverRepo = com.memorymoments.app.repository.CaregiverRepository(application)
    private val recommendationRepo = RecommendationRepository(application)
    private val authRepo = AuthRepository(application)

    private val _recommendationState = MutableStateFlow(RecommendationUiState(isLoading = true))
    val recommendationState: StateFlow<RecommendationUiState> = _recommendationState.asStateFlow()

    private var activePatientId: String? = null

    val stats: StateFlow<AppStats> = appStateRepo.stats.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppStats()
    )

    val dailyCompanion: StateFlow<DailyCompanionData> = dailyCompanionRepo.dailyCompanionData.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DailyCompanionData()
    )

    val isPlayingSong: StateFlow<Boolean> = audioPlaybackManager.isPlaying

    init {
        viewModelScope.launch {
            dailyCompanionRepo.checkAndRefreshDailyState()
        }

        // Account-switching protection: Monitor active user ID. Clear & fetch fresh prediction when account changes.
        viewModelScope.launch {
            authRepo.currentUserId.collectLatest { userId ->
                if (userId != activePatientId) {
                    activePatientId = userId
                    _recommendationState.value = RecommendationUiState(isLoading = true)
                    fetchRecommendation()
                }
            }
        }
    }

    fun fetchRecommendation() {
        viewModelScope.launch {
            _recommendationState.value = _recommendationState.value.copy(isLoading = true, error = null)
            val result = recommendationRepo.getNextGameRecommendation(activePatientId)
            result.onSuccess { res ->
                val (gameTitle, domainTag, route) = mapDomainToGame(res.nextGame)
                _recommendationState.value = RecommendationUiState(
                    isLoading = false,
                    nextGameDomain = res.nextGame,
                    recommendedGameTitle = gameTitle,
                    domainTag = domainTag,
                    recommendedRoute = route,
                    error = null
                )
            }.onFailure { err ->
                _recommendationState.value = RecommendationUiState(
                    isLoading = false,
                    nextGameDomain = null,
                    recommendedGameTitle = null,
                    domainTag = null,
                    recommendedRoute = null,
                    error = "Recommendation unavailable right now."
                )
            }
        }
    }

    private fun mapDomainToGame(domain: String): Triple<String, String, String> {
        return when (domain.lowercase()) {
            "memory" -> Triple("Who's Who?", "Memory Focus", Routes.GAME)
            "attention" -> Triple("Distractor Lab", "Attention Focus", Routes.DISTRACTOR_LAB)
            "recognition" -> Triple("Where Was It?", "Places Recall", Routes.PLACES_GAME)
            "routine" -> Triple("Life Timeline", "Routine Memory", Routes.TIMELINE)
            "pattern" -> Triple("Name That Tune", "Music Pattern", Routes.MUSIC_GAME)
            else -> Triple("Who's Who?", "Memory Focus", Routes.GAME)
        }
    }

    fun togglePlaySong(song: Song?) {
        if (song == null) return
        if (audioPlaybackManager.isPlaying.value) {
            audioPlaybackManager.stop()
        } else {
            audioPlaybackManager.play(song.localAudioUri)
            viewModelScope.launch {
                dailyCompanionRepo.markActivityCompleted("music")
                caregiverRepo.recordActivityCompletion("music")
                fetchRecommendation()
            }
        }
    }

    fun stopAudio() {
        audioPlaybackManager.stop()
    }

    fun nextSong() {
        viewModelScope.launch {
            audioPlaybackManager.stop()
            dailyCompanionRepo.nextDailySong()
        }
    }

    fun nextMemory() {
        viewModelScope.launch {
            dailyCompanionRepo.nextDailyMemory()
        }
    }

    fun markActivityComplete(type: String) {
        viewModelScope.launch {
            dailyCompanionRepo.markActivityCompleted(type)
            caregiverRepo.recordActivityCompletion(type)
            fetchRecommendation()
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlaybackManager.release()
    }
}
