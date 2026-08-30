package com.memorymoments.app.ui.screens.musicgame

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.memorymoments.app.audio.AudioPlaybackManager
import com.memorymoments.app.game.music.MusicGameEngine
import com.memorymoments.app.game.music.MusicQuestion
import com.memorymoments.app.model.DistractorStyle
import com.memorymoments.app.navigation.Routes
import com.memorymoments.app.repository.AppStateRepository
import com.memorymoments.app.repository.GameSettingsRepository
import com.memorymoments.app.repository.SongRepository
import com.memorymoments.app.ui.screens.game.GameSessionResult
import com.memorymoments.app.utils.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface MusicGameState {
    object Loading : MusicGameState
    object NotEnoughMusic : MusicGameState
    data class Playing(
        val roundNumber: Int,
        val totalRounds: Int,
        val question: MusicQuestion,
        val selectedOptionId: String? = null,
        val isCorrect: Boolean? = null,
        val stars: Int,
        val xp: Int,
        val combo: Int,
        val bestCombo: Int
    ) : MusicGameState
    data class Completed(val result: GameSessionResult) : MusicGameState
}

class MusicGameViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val songRepo = SongRepository(application)
    private val appStateRepo = AppStateRepository(application)
    private val settingsRepo = GameSettingsRepository(application)
    val audioPlayer = AudioPlaybackManager(application)

    private val isDemo: Boolean = savedStateHandle.get<Boolean>(Routes.DEMO_ARG) == true
    private val style: DistractorStyle = runCatching {
        DistractorStyle.valueOf(
            savedStateHandle.get<String>(Routes.STYLE_ARG) ?: DistractorStyle.NORMAL.name
        )
    }.getOrDefault(DistractorStyle.NORMAL)

    private val _state = MutableStateFlow<MusicGameState>(MusicGameState.Loading)
    val state: StateFlow<MusicGameState> = _state.asStateFlow()

    val isAudioPlaying: StateFlow<Boolean> = audioPlayer.isPlaying

    private var questions: List<MusicQuestion> = emptyList()
    private var currentRoundIndex = 0
    private var starsEarned = 0
    private var xpEarned = 0
    private var currentCombo = 0
    private var bestComboInSession = 0
    private var correctAnswersCount = 0
    private var hadIncorrectAttemptInRound = false

    init {
        startSession()
    }

    fun playCurrentClip() {
        val playing = _state.value as? MusicGameState.Playing ?: return
        val targetSong = playing.question.targetSong
        audioPlayer.play(targetSong.localAudioUri, startMs = 0, clipDurationMs = 10_000)
    }

    fun toggleAudio() {
        if (audioPlayer.isPlaying.value) {
            audioPlayer.pause()
        } else {
            playCurrentClip()
        }
    }

    fun onOptionSelected(optionId: String) {
        val currentPlaying = _state.value as? MusicGameState.Playing ?: return
        if (currentPlaying.selectedOptionId != null) return

        val option = currentPlaying.question.options.firstOrNull { it.id == optionId } ?: return
        val isCorrect = option.isCorrect

        if (isCorrect) {
            if (!hadIncorrectAttemptInRound) {
                starsEarned += 1
                xpEarned += Constants.XP_PER_CORRECT
                currentCombo += 1
                correctAnswersCount += 1
                if (currentCombo > bestComboInSession) {
                    bestComboInSession = currentCombo
                }
            }
            _state.value = currentPlaying.copy(
                selectedOptionId = optionId,
                isCorrect = true,
                stars = starsEarned,
                xp = xpEarned,
                combo = currentCombo,
                bestCombo = bestComboInSession
            )

            viewModelScope.launch {
                delay(1500)
                advanceNextRound()
            }
        } else {
            hadIncorrectAttemptInRound = true
            currentCombo = 0
            _state.value = currentPlaying.copy(
                selectedOptionId = optionId,
                isCorrect = false,
                combo = 0
            )

            viewModelScope.launch {
                delay(2000)
                advanceNextRound()
            }
        }
    }

    fun advanceNextRound() {
        audioPlayer.stop()
        currentRoundIndex++
        hadIncorrectAttemptInRound = false

        if (currentRoundIndex < questions.size) {
            val nextQuestion = questions[currentRoundIndex]
            _state.value = MusicGameState.Playing(
                roundNumber = currentRoundIndex + 1,
                totalRounds = questions.size,
                question = nextQuestion,
                selectedOptionId = null,
                isCorrect = null,
                stars = starsEarned,
                xp = xpEarned,
                combo = currentCombo,
                bestCombo = bestComboInSession
            )
            // Automatically play the clip for the new round
            audioPlayer.play(nextQuestion.targetSong.localAudioUri, startMs = 0, clipDurationMs = 10_000)
        } else {
            finishSession()
        }
    }

    private fun finishSession() {
        audioPlayer.stop()
        viewModelScope.launch {
            if (!isDemo && questions.isNotEmpty()) {
                val prevStats = appStateRepo.stats.first()
                val newTotalXp = prevStats.xp + xpEarned
                val newStars = prevStats.stars + starsEarned
                val newBestCombo = maxOf(prevStats.bestCombo, bestComboInSession)
                val newLevel = 1 + (newTotalXp / 100)

                appStateRepo.updateStats(
                    prevStats.copy(
                        xp = newTotalXp,
                        stars = newStars,
                        bestCombo = newBestCombo,
                        level = newLevel
                    )
                )

                settingsRepo.recordGameCompletion(
                    correct = correctAnswersCount,
                    total = questions.size,
                    bestComboInGame = bestComboInSession
                )
            }

            val sessionResult = GameSessionResult(
                starsEarned = starsEarned,
                xpEarned = xpEarned,
                bestCombo = bestComboInSession,
                totalRounds = questions.size,
                correctAnswers = correctAnswersCount
            )
            _state.value = MusicGameState.Completed(sessionResult)
        }
    }

    private fun startSession() {
        viewModelScope.launch {
            _state.value = MusicGameState.Loading

            val songs = if (isDemo) {
                songRepo.createDemoSongs()
            } else {
                val list = songRepo.songs.first()
                if (list.size < 3) songRepo.createDemoSongs() else list
            }

            val engine = MusicGameEngine(songs, difficulty = style)
            if (!engine.isReady()) {
                _state.value = MusicGameState.NotEnoughMusic
                return@launch
            }

            val memories = com.memorymoments.app.repository.MemoryRepository(getApplication()).memories.first()
            val memorySongIds = memories.flatMap { it.songIds }.toSet()

            questions = engine.generateQuestions(
                roundCount = Constants.DEFAULT_ROUNDS,
                prioritizedSongIds = memorySongIds
            )
            currentRoundIndex = 0
            starsEarned = 0
            xpEarned = 0
            currentCombo = 0
            bestComboInSession = 0
            correctAnswersCount = 0
            hadIncorrectAttemptInRound = false

            if (questions.isNotEmpty()) {
                val firstQuestion = questions[0]
                _state.value = MusicGameState.Playing(
                    roundNumber = 1,
                    totalRounds = questions.size,
                    question = firstQuestion,
                    stars = 0,
                    xp = 0,
                    combo = 0,
                    bestCombo = 0
                )
                // Start playing clip
                audioPlayer.play(firstQuestion.targetSong.localAudioUri, startMs = 0, clipDurationMs = 10_000)
            } else {
                _state.value = MusicGameState.NotEnoughMusic
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}
