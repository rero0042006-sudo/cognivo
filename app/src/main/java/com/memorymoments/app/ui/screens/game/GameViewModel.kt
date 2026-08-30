package com.memorymoments.app.ui.screens.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.memorymoments.app.game.GameConfig
import com.memorymoments.app.game.GameEngine
import com.memorymoments.app.model.DistractorCharacter
import com.memorymoments.app.model.DistractorStyle
import com.memorymoments.app.model.FamilyMember
import com.memorymoments.app.model.Question
import com.memorymoments.app.navigation.Routes
import com.memorymoments.app.repository.AppStateRepository
import com.memorymoments.app.repository.DistractorRepository
import com.memorymoments.app.repository.FamilyRepository
import com.memorymoments.app.repository.GroqPersonalizationRepository
import com.memorymoments.app.repository.VisualProfileRepository
import com.memorymoments.app.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val familyRepo = FamilyRepository(application)
    private val distractorRepo = DistractorRepository(application)
    private val appStateRepo = AppStateRepository(application)
    private val groqRepo = GroqPersonalizationRepository(application)

    private val isDemo = savedStateHandle.get<Boolean>(Routes.DEMO_ARG) == true
    private val style = runCatching {
        DistractorStyle.valueOf(
            savedStateHandle.get<String>(Routes.STYLE_ARG) ?: DistractorStyle.NORMAL.name
        )
    }.getOrDefault(DistractorStyle.NORMAL)

    private val _state = MutableStateFlow<GameState>(GameState.Loading)
    val state: StateFlow<GameState> = _state.asStateFlow()

    private var questions: List<Question> = emptyList()
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

    fun retry() {
        startSession()
    }

    fun onOptionSelected(optionId: String) {
        val currentState = _state.value as? GameState.Playing ?: return
        if (currentState.isAdvancing || currentState.isCorrect == true) return

        val option = currentState.question.options.firstOrNull { it.id == optionId } ?: return

        if (option.isCorrect) {
            if (!hadIncorrectAttemptInRound) {
                starsEarned += 1
                xpEarned += Constants.XP_PER_CORRECT
                currentCombo += 1
                if (currentCombo > bestComboInSession) {
                    bestComboInSession = currentCombo
                }
                correctAnswersCount += 1
            }

            _state.update {
                currentState.copy(
                    selectedOptionId = optionId,
                    isCorrect = true,
                    stars = starsEarned,
                    xp = xpEarned,
                    combo = currentCombo,
                    bestCombo = bestComboInSession,
                    isAdvancing = true
                )
            }

            // Automatically advance after positive reinforcement delay
            viewModelScope.launch {
                delay(1200)
                advanceNextRound()
            }
        } else {
            hadIncorrectAttemptInRound = true
            currentCombo = 0

            _state.update {
                currentState.copy(
                    selectedOptionId = optionId,
                    isCorrect = false,
                    combo = 0
                )
            }
        }
    }

    fun advanceNextRound() {
        if (currentRoundIndex + 1 < questions.size) {
            currentRoundIndex += 1
            hadIncorrectAttemptInRound = false
            val nextQuestion = questions[currentRoundIndex]
            _state.value = GameState.Playing(
                roundNumber = currentRoundIndex + 1,
                totalRounds = questions.size,
                question = nextQuestion,
                stars = starsEarned,
                xp = xpEarned,
                combo = currentCombo,
                bestCombo = bestComboInSession
            )
        } else {
            finishGame()
        }
    }

    private fun finishGame() {
        viewModelScope.launch {
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

            val sessionResult = GameSessionResult(
                starsEarned = starsEarned,
                xpEarned = xpEarned,
                bestCombo = bestComboInSession,
                totalRounds = questions.size,
                correctAnswers = correctAnswersCount
            )
            _state.value = GameState.Completed(sessionResult)
        }
    }

    private fun startSession() {
        viewModelScope.launch {
            _state.value = GameState.Loading

            val familyMembers = if (isDemo) {
                loadDemoFamily()
            } else {
                familyRepo.family.first()
            }

            if (familyMembers.size < Constants.MIN_FAMILY_FOR_GAME) {
                _state.value = GameState.NotEnoughFamily
                return@launch
            }

            // Load per-member AI distractors for family members
            val distractors = if (!isDemo) {
                loadHardDistractors(familyMembers)
            } else {
                val pool = distractorRepo.cachedPool(style, includeDemo = true)
                if (pool.isEmpty()) distractorRepo.createDemoPool(style) else pool
            }

            val engine = GameEngine(
                family = familyMembers,
                distractors = distractors,
                config = GameConfig(
                    roundCount = Constants.DEFAULT_ROUNDS,
                    isDemo = isDemo
                ),
                style = style
            )

            // Trigger non-blocking background enrichment of the 15-image distractor pool for family members
            if (!isDemo) {
                viewModelScope.launch(Dispatchers.IO) {
                    val visualProfileRepo = VisualProfileRepository(getApplication())
                    for (member in familyMembers) {
                        distractorRepo.ensureHardPool(member, visualProfileRepo, targetSize = Constants.HARD_DISTRACTOR_POOL_SIZE)
                    }
                }
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
                _state.value = GameState.Playing(
                    roundNumber = 1,
                    totalRounds = questions.size,
                    question = questions[0],
                    stars = 0,
                    xp = 0,
                    combo = 0,
                    bestCombo = 0
                )

                // Asynchronously enrich questions with Groq AI personalization in the background
                enrichQuestionsWithGroq(familyMembers)
            } else {
                _state.value = GameState.NotEnoughFamily
            }
        }
    }

    /**
     * Collect per-member cached AI distractors for family members.
     * Falls back to generic pool if per-member cache is empty.
     */
    private suspend fun loadHardDistractors(
        familyMembers: List<FamilyMember>
    ): List<DistractorCharacter> {
        val allHard = mutableListOf<DistractorCharacter>()
        for (member in familyMembers) {
            val memberDistractors = distractorRepo.cachedHardPool(member.id)
            allHard.addAll(memberDistractors)
        }
        if (allHard.isNotEmpty()) return allHard

        // Fall back to generic pool
        val generic = distractorRepo.cachedPool(style, includeDemo = true)
        if (generic.isNotEmpty()) return generic

        return distractorRepo.createDemoPool(style)
    }

    private fun enrichQuestionsWithGroq(familyMembers: List<FamilyMember>) {
        viewModelScope.launch {
            val updatedList = questions.toMutableList()
            for (i in updatedList.indices) {
                val q = updatedList[i]
                val personalized = groqRepo.generatePersonalizedQuestion(q.targetMember)
                val encouragement = groqRepo.generateEncouragement(q.targetMember)

                if (personalized != null || encouragement != null) {
                    val enriched = q.copy(
                        personalizedText = personalized,
                        encouragement = encouragement
                    )
                    updatedList[i] = enriched

                    // If currently on this round, update state smoothly
                    if (currentRoundIndex == i) {
                        _state.update { current ->
                            if (current is GameState.Playing && current.roundNumber == i + 1) {
                                current.copy(question = enriched)
                            } else {
                                current
                            }
                        }
                    }
                }
            }
            questions = updatedList
        }
    }

    private suspend fun loadDemoFamily(): List<FamilyMember> {
        val existing = familyRepo.family.first()
        if (existing.size >= Constants.MIN_FAMILY_FOR_GAME) return existing

        val demoDistractors = distractorRepo.createDemoPool(DistractorStyle.EASY)
        return listOf(
            FamilyMember(
                id = "demo-1",
                name = "Sarah",
                relationship = "Daughter",
                memoryContext = "Visits on Sundays with flowers",
                originalPhotoUri = demoDistractors.getOrNull(0)?.imageUri
            ),
            FamilyMember(
                id = "demo-2",
                name = "Michael",
                relationship = "Son",
                memoryContext = "Plays guitar and loves old jazz",
                originalPhotoUri = demoDistractors.getOrNull(1)?.imageUri
            ),
            FamilyMember(
                id = "demo-3",
                name = "Emma",
                relationship = "Granddaughter",
                memoryContext = "Bakes cookies and brings her dog",
                originalPhotoUri = demoDistractors.getOrNull(2)?.imageUri
            )
        )
    }
}
