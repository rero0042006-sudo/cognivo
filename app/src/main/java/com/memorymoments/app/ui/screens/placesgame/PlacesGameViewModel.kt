package com.memorymoments.app.ui.screens.placesgame

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.memorymoments.app.game.places.PlacesGameEngine
import com.memorymoments.app.game.places.PlacesQuestion
import com.memorymoments.app.model.DistractorStyle
import com.memorymoments.app.model.Place
import com.memorymoments.app.navigation.Routes
import com.memorymoments.app.repository.AppStateRepository
import com.memorymoments.app.repository.GameSettingsRepository
import com.memorymoments.app.repository.PlaceRepository
import com.memorymoments.app.ui.screens.game.GameSessionResult
import com.memorymoments.app.utils.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface PlacesGameState {
    object Loading : PlacesGameState
    object NotEnoughPlaces : PlacesGameState
    data class Playing(
        val roundNumber: Int,
        val totalRounds: Int,
        val question: PlacesQuestion,
        val selectedOptionId: String? = null,
        val isCorrect: Boolean? = null,
        val stars: Int,
        val xp: Int,
        val combo: Int,
        val bestCombo: Int
    ) : PlacesGameState
    data class Completed(val result: GameSessionResult) : PlacesGameState
}

class PlacesGameViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val placeRepo = PlaceRepository(application)
    private val appStateRepo = AppStateRepository(application)
    private val settingsRepo = GameSettingsRepository(application)

    private val isDemo: Boolean = savedStateHandle.get<Boolean>(Routes.DEMO_ARG) == true
    private val style: DistractorStyle = runCatching {
        DistractorStyle.valueOf(
            savedStateHandle.get<String>(Routes.STYLE_ARG) ?: DistractorStyle.NORMAL.name
        )
    }.getOrDefault(DistractorStyle.NORMAL)

    private val _state = MutableStateFlow<PlacesGameState>(PlacesGameState.Loading)
    val state: StateFlow<PlacesGameState> = _state.asStateFlow()

    private var questions: List<PlacesQuestion> = emptyList()
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

    fun onOptionSelected(optionId: String) {
        val currentPlaying = _state.value as? PlacesGameState.Playing ?: return
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
                delay(1200)
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
                delay(1800)
                advanceNextRound()
            }
        }
    }

    fun advanceNextRound() {
        currentRoundIndex++
        hadIncorrectAttemptInRound = false

        if (currentRoundIndex < questions.size) {
            _state.value = PlacesGameState.Playing(
                roundNumber = currentRoundIndex + 1,
                totalRounds = questions.size,
                question = questions[currentRoundIndex],
                selectedOptionId = null,
                isCorrect = null,
                stars = starsEarned,
                xp = xpEarned,
                combo = currentCombo,
                bestCombo = bestComboInSession
            )
        } else {
            finishSession()
        }
    }

    private fun finishSession() {
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
            _state.value = PlacesGameState.Completed(sessionResult)
        }
    }

    private fun startSession() {
        viewModelScope.launch {
            _state.value = PlacesGameState.Loading

            val memoryRepo = com.memorymoments.app.repository.MemoryRepository(getApplication())
            val memories = memoryRepo.memories.first()

            val basePlaces = if (isDemo) {
                placeRepo.createDemoPlaces()
            } else {
                val list = placeRepo.places.first()
                if (list.size < 3) placeRepo.createDemoPlaces() else list
            }

            // Enrich places with photos and locations from memories if available
            val enrichedPlaces = basePlaces.toMutableList()
            memories.forEach { mem ->
                val placeName = mem.place ?: mem.placeIds.firstOrNull()?.let { pid -> basePlaces.find { it.id == pid }?.name }
                val photoUri = mem.photoUris.firstOrNull()
                if (!placeName.isNullOrBlank() && !photoUri.isNullOrBlank()) {
                    val matchingPlaceIndex = enrichedPlaces.indexOfFirst { it.name.equals(placeName, ignoreCase = true) || it.id in mem.placeIds }
                    if (matchingPlaceIndex >= 0) {
                        val existing = enrichedPlaces[matchingPlaceIndex]
                        if (existing.photoUris.isEmpty()) {
                            enrichedPlaces[matchingPlaceIndex] = existing.copy(photoUris = listOf(photoUri))
                        }
                    } else {
                        enrichedPlaces.add(
                            Place(
                                id = "mem-place-${mem.id}",
                                name = placeName,
                                location = placeName,
                                state = mem.state,
                                region = mem.region,
                                photoUris = listOf(photoUri),
                                memoryId = mem.id
                            )
                        )
                    }
                }
            }

            val engine = PlacesGameEngine(enrichedPlaces, difficulty = style)
            if (!engine.isReady()) {
                _state.value = PlacesGameState.NotEnoughPlaces
                return@launch
            }

            questions = engine.generateQuestions(Constants.DEFAULT_ROUNDS)
            currentRoundIndex = 0
            starsEarned = 0
            xpEarned = 0
            currentCombo = 0
            bestComboInSession = 0
            correctAnswersCount = 0
            hadIncorrectAttemptInRound = false

            if (questions.isNotEmpty()) {
                _state.value = PlacesGameState.Playing(
                    roundNumber = 1,
                    totalRounds = questions.size,
                    question = questions[0],
                    stars = 0,
                    xp = 0,
                    combo = 0,
                    bestCombo = 0
                )
            } else {
                _state.value = PlacesGameState.NotEnoughPlaces
            }
        }
    }
}
