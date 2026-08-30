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
import com.memorymoments.app.repository.DomainGameMapper
import com.memorymoments.app.repository.RecommendationRepository
import com.memorymoments.app.repository.RecommendationUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val appStateRepo = AppStateRepository(application)
    val dailyCompanionRepo = DailyCompanionRepository(application)
    val audioPlaybackManager = AudioPlaybackManager(application)
    private val caregiverRepo = com.memorymoments.app.repository.CaregiverRepository(application)
    private val recommendationRepo = RecommendationRepository(application)

    private val _recommendationState = MutableStateFlow(RecommendationUiState(isLoading = true))
    val recommendationState: StateFlow<RecommendationUiState> = _recommendationState.asStateFlow()

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
        viewModelScope.launch {
            recommendationRepo.currentPatientId.collectLatest { patientId ->
                if (!patientId.isNullOrBlank() && patientId != "guest") {
                    loadRecommendation(patientId)
                } else {
                    _recommendationState.value = RecommendationUiState(
                        isLoading = false,
                        recommendedDomain = "memory",
                        recommendedGameTitle = DomainGameMapper.getGameTitle("memory"),
                        recommendedRoute = DomainGameMapper.getGameRoute("memory"),
                        error = null
                    )
                }
            }
        }
    }

    fun refreshRecommendation() {
        viewModelScope.launch {
            val patientId = recommendationRepo.currentPatientId.first()
            loadRecommendation(patientId)
        }
    }

    private suspend fun loadRecommendation(patientId: String?) {
        _recommendationState.value = _recommendationState.value.copy(isLoading = true, error = null)
        val result = recommendationRepo.fetchNextGameRecommendation(patientId)
        result.onSuccess { response ->
            val domain = response.nextGame
            _recommendationState.value = RecommendationUiState(
                isLoading = false,
                recommendedDomain = domain,
                recommendedGameTitle = DomainGameMapper.getGameTitle(domain),
                recommendedRoute = DomainGameMapper.getGameRoute(domain),
                sessionsCompleted = response.sessionsCompleted,
                lastGame = response.lastGame,
                error = null
            )
        }.onFailure { err ->
            _recommendationState.value = RecommendationUiState(
                isLoading = false,
                recommendedDomain = "memory",
                recommendedGameTitle = DomainGameMapper.getGameTitle("memory"),
                recommendedRoute = DomainGameMapper.getGameRoute("memory"),
                error = err.message ?: "Recommendation unavailable"
            )
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
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlaybackManager.release()
    }
}
