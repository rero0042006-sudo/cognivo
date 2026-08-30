package com.memorymoments.app.ui.screens.memorytalk

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.memorymoments.app.model.Memory
import com.memorymoments.app.navigation.Routes
import com.memorymoments.app.repository.ConversationMessage
import com.memorymoments.app.repository.GameSettingsRepository
import com.memorymoments.app.repository.MemoryRepository
import com.memorymoments.app.repository.MemoryTalkRepository
import com.memorymoments.app.speech.SpeechRecognitionManager
import com.memorymoments.app.speech.SpeechState
import com.memorymoments.app.speech.TextToSpeechManager
import com.memorymoments.app.utils.NetworkStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MemoryTalkUiState(
    val memories: List<Memory> = emptyList(),
    val currentMemory: Memory? = null,
    val currentIndex: Int = 0,
    val speechState: SpeechState = SpeechState.Idle,
    val conversation: List<ConversationMessage> = emptyList(),
    val isGeneratingResponse: Boolean = false,
    val currentAiResponse: String? = null,
    val turnCount: Int = 0,
    val isSessionLimitReached: Boolean = false,
    val isOffline: Boolean = false,
    val isTtsSpeaking: Boolean = false,
    val ttsEnabled: Boolean = true,
    val ttsSlowRate: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = true
)

class MemoryTalkViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val memoryRepo = MemoryRepository(application)
    private val talkRepo = MemoryTalkRepository(application)
    private val settingsRepo = GameSettingsRepository(application)
    val speechManager = SpeechRecognitionManager(application)
    val ttsManager = TextToSpeechManager(application)

    private val targetMemoryId: String? = savedStateHandle[Routes.MEMBER_ID_ARG]

    private val _uiState = MutableStateFlow(MemoryTalkUiState())
    val uiState: StateFlow<MemoryTalkUiState> = _uiState.asStateFlow()

    init {
        observeSpeechState()
        observeTts()
        loadData()
    }

    private fun observeSpeechState() {
        viewModelScope.launch {
            speechManager.state.collect { speechState ->
                _uiState.update { it.copy(speechState = speechState) }
                if (speechState is SpeechState.Result) {
                    onUserSpeechRecognized(speechState.text)
                }
            }
        }
    }

    private fun observeTts() {
        viewModelScope.launch {
            ttsManager.isSpeaking.collect { isSpeaking ->
                _uiState.update { it.copy(isTtsSpeaking = isSpeaking) }
            }
        }
        viewModelScope.launch {
            settingsRepo.ttsEnabled.collect { enabled ->
                _uiState.update { it.copy(ttsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepo.ttsSlowRate.collect { slowRate ->
                _uiState.update { it.copy(ttsSlowRate = slowRate) }
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val list = memoryRepo.memories.first().ifEmpty {
                memoryRepo.createDemoMemories()
            }

            val isOnline = NetworkStatus.isOnline(getApplication())
            val initialIndex = if (targetMemoryId != null) {
                list.indexOfFirst { it.id == targetMemoryId }.coerceAtLeast(0)
            } else {
                0
            }
            val initialMemory = list.getOrNull(initialIndex)

            _uiState.update {
                it.copy(
                    memories = list,
                    currentIndex = initialIndex,
                    currentMemory = initialMemory,
                    conversation = emptyList(),
                    currentAiResponse = null,
                    turnCount = 0,
                    isSessionLimitReached = false,
                    isOffline = !isOnline,
                    isLoading = false
                )
            }
        }
    }

    fun startListening() {
        if (_uiState.value.isSessionLimitReached) return
        ttsManager.stop()
        speechManager.startListening()
    }

    fun stopListening() {
        speechManager.stopListening()
    }

    fun cancelListening() {
        speechManager.reset()
    }

    fun onUserSpeechRecognized(userText: String) {
        val memory = _uiState.value.currentMemory ?: return
        val currentTurns = _uiState.value.turnCount + 1

        val userMessage = ConversationMessage(speaker = "user", text = userText)
        val updatedHistory = _uiState.value.conversation + userMessage

        _uiState.update {
            it.copy(
                conversation = updatedHistory,
                isGeneratingResponse = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val response = talkRepo.generateReminiscenceResponse(
                memory = memory,
                userTranscript = userText,
                history = updatedHistory
            )

            val isLimit = currentTurns >= 5

            if (response != null) {
                val aiMessage = ConversationMessage(speaker = "companion", text = response)
                _uiState.update {
                    it.copy(
                        conversation = updatedHistory + aiMessage,
                        currentAiResponse = response,
                        turnCount = currentTurns,
                        isSessionLimitReached = isLimit,
                        isGeneratingResponse = false
                    )
                }

                if (_uiState.value.ttsEnabled) {
                    ttsManager.speak(response, _uiState.value.ttsSlowRate)
                }
            } else {
                // Gentle offline/fallback response
                val fallback = if (!NetworkStatus.isOnline(getApplication())) {
                    "You're offline right now, but it's lovely to look back on this memory."
                } else {
                    "That sounds like a very special memory to look back on."
                }
                val aiMessage = ConversationMessage(speaker = "companion", text = fallback)
                _uiState.update {
                    it.copy(
                        conversation = updatedHistory + aiMessage,
                        currentAiResponse = fallback,
                        turnCount = currentTurns,
                        isSessionLimitReached = isLimit,
                        isGeneratingResponse = false
                    )
                }
            }
        }
    }

    fun onDontRemember() {
        ttsManager.stop()
        speechManager.reset()
        val reassurance = "That is completely okay! We can just enjoy looking at this lovely photo together."
        val userMsg = ConversationMessage(speaker = "user", text = "I don't remember.")
        val aiMsg = ConversationMessage(speaker = "companion", text = reassurance)
        _uiState.update {
            it.copy(
                conversation = it.conversation + userMsg + aiMsg,
                currentAiResponse = reassurance,
                isGeneratingResponse = false
            )
        }
        if (_uiState.value.ttsEnabled) {
            ttsManager.speak(reassurance, _uiState.value.ttsSlowRate)
        }
    }

    fun speakCurrentResponse() {
        val response = _uiState.value.currentAiResponse ?: return
        ttsManager.speak(response, _uiState.value.ttsSlowRate)
    }


    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun nextMemory() {
        ttsManager.stop()
        speechManager.reset()
        val list = _uiState.value.memories
        if (list.isEmpty()) return

        val nextIndex = (_uiState.value.currentIndex + 1) % list.size
        val nextMemory = list.getOrNull(nextIndex)

        _uiState.update {
            it.copy(
                currentIndex = nextIndex,
                currentMemory = nextMemory,
                conversation = emptyList(),
                currentAiResponse = null,
                turnCount = 0,
                isSessionLimitReached = false,
                errorMessage = null
            )
        }
    }

    fun resetConversation() {
        ttsManager.stop()
        speechManager.reset()
        _uiState.update {
            it.copy(
                conversation = emptyList(),
                currentAiResponse = null,
                turnCount = 0,
                isSessionLimitReached = false,
                errorMessage = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
        ttsManager.shutdown()
    }
}
