package com.memorymoments.app.ui.screens.distractor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.memorymoments.app.model.DistractorStyle
import com.memorymoments.app.navigation.Routes
import com.memorymoments.app.repository.DistractorRepository
import com.memorymoments.app.repository.OfflineException
import com.memorymoments.app.utils.Constants
import com.memorymoments.app.utils.NetworkStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DistractorLabViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val repository = DistractorRepository(application)
    private val familyRepo = com.memorymoments.app.repository.FamilyRepository(application)
    private val visualProfileRepo = com.memorymoments.app.repository.VisualProfileRepository(application)
    private val style = runCatching {
        DistractorStyle.valueOf(
            savedStateHandle.get<String>(Routes.STYLE_ARG) ?: DistractorStyle.NORMAL.name
        )
    }.getOrDefault(DistractorStyle.NORMAL)
    private val isDemo = savedStateHandle.get<Boolean>(Routes.DEMO_ARG) == true

    private val _state = MutableStateFlow(
        DistractorLabUiState(style = style, isDemo = isDemo)
    )
    val state: StateFlow<DistractorLabUiState> = _state.asStateFlow()

    init {
        start()
    }

    fun retry() {
        start()
    }

    fun regenerateAiDistractors() {
        viewModelScope.launch {
            if (style == DistractorStyle.CHALLENGE && !isDemo) {
                val family = familyRepo.family.first()
                for (member in family) {
                    repository.invalidateHardForMember(member.id)
                    visualProfileRepo.invalidateForMember(member.id)
                }
            } else {
                repository.clearPool(style)
            }
            startGeneration()
        }
    }

    fun useDemoDistractors() {
        viewModelScope.launch {
            _state.update {
                it.copy(phase = DistractorLabState.Generating(0, Constants.DISTRACTOR_POOL_SIZE))
            }
            val demo = repository.createDemoPool(style)
            _state.update { it.copy(phase = DistractorLabState.Success(demo)) }
        }
    }

    private fun start() {
        viewModelScope.launch {
            if (isDemo) {
                useDemoDistractors()
                return@launch
            }

            if (style == DistractorStyle.CHALLENGE) {
                val family = familyRepo.family.first()
                val cachedHard = mutableListOf<com.memorymoments.app.model.DistractorCharacter>()
                for (member in family) {
                    cachedHard.addAll(repository.cachedHardPool(member.id))
                }
                if (cachedHard.size >= family.size * Constants.HARD_DISTRACTOR_POOL_SIZE && cachedHard.isNotEmpty()) {
                    _state.update { it.copy(phase = DistractorLabState.Success(cachedHard)) }
                    return@launch
                }
            } else {
                val cached = repository.cachedPool(style, includeDemo = false)
                if (cached.size >= Constants.DISTRACTOR_MIN_POOL) {
                    _state.update { it.copy(phase = DistractorLabState.Success(cached)) }
                    return@launch
                }
            }

            if (!NetworkStatus.isOnline(getApplication())) {
                val anyCached = if (style == DistractorStyle.CHALLENGE) {
                    val family = familyRepo.family.first()
                    val hardList = mutableListOf<com.memorymoments.app.model.DistractorCharacter>()
                    for (member in family) {
                        hardList.addAll(repository.cachedHardPool(member.id))
                    }
                    if (hardList.isEmpty()) repository.cachedPool(style, includeDemo = true) else hardList
                } else {
                    repository.cachedPool(style, includeDemo = true)
                }

                if (anyCached.isNotEmpty()) {
                    _state.update { it.copy(phase = DistractorLabState.Success(anyCached)) }
                } else {
                    _state.update { it.copy(phase = DistractorLabState.Error(offline = true)) }
                }
                return@launch
            }

            startGeneration()
        }
    }

    private suspend fun startGeneration() {
        if (style == DistractorStyle.CHALLENGE) {
            val family = familyRepo.family.first()
            val totalNeeded = (family.size * Constants.HARD_DISTRACTOR_POOL_SIZE).coerceAtLeast(Constants.DISTRACTOR_POOL_SIZE)
            _state.update {
                it.copy(
                    phase = DistractorLabState.Generating(
                        completed = 0,
                        total = totalNeeded,
                        preview = null
                    )
                )
            }

            val allHard = mutableListOf<com.memorymoments.app.model.DistractorCharacter>()
            var completedCount = 0
            for (member in family) {
                val result = repository.ensureHardPool(member, visualProfileRepo) { done, _, preview ->
                    _state.update { current ->
                        current.copy(
                            phase = DistractorLabState.Generating(
                                completed = completedCount + done,
                                total = totalNeeded,
                                preview = preview
                            )
                        )
                    }
                }
                result.fold(
                    onSuccess = { characters ->
                        allHard.addAll(characters)
                        completedCount += characters.size
                    },
                    onFailure = { /* Continue with other members */ }
                )
            }

            if (allHard.isNotEmpty()) {
                _state.update { it.copy(phase = DistractorLabState.Success(allHard)) }
            } else {
                _state.update {
                    it.copy(
                        phase = DistractorLabState.Error(
                            offline = false,
                            message = "Couldn't create similar game characters right now."
                        )
                    )
                }
            }
        } else {
            val cached = repository.cachedPool(style, includeDemo = false)
            _state.update {
                it.copy(
                    phase = DistractorLabState.Generating(
                        completed = cached.size,
                        total = Constants.DISTRACTOR_POOL_SIZE,
                        preview = cached.lastOrNull()
                    )
                )
            }
            val result = repository.ensurePool(style) { completed, total, preview ->
                _state.update { current ->
                    current.copy(
                        phase = DistractorLabState.Generating(completed, total, preview)
                    )
                }
            }
            _state.update { current ->
                result.fold(
                    onSuccess = { characters ->
                        current.copy(phase = DistractorLabState.Success(characters))
                    },
                    onFailure = { error ->
                        current.copy(
                            phase = DistractorLabState.Error(
                                offline = error is OfflineException,
                                message = error.message
                            )
                        )
                    }
                )
            }
        }
    }
}
