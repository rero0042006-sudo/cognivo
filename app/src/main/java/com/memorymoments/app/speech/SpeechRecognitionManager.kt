package com.memorymoments.app.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed interface SpeechState {
    object Idle : SpeechState
    object Listening : SpeechState
    object Processing : SpeechState
    data class Result(val text: String) : SpeechState
    data class Error(val message: String) : SpeechState
}

class SpeechRecognitionManager(context: Context) {
    private val appContext = context.applicationContext
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _state = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val state: StateFlow<SpeechState> = _state.asStateFlow()

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(appContext)
    }

    fun startListening() {
        mainHandler.post {
            try {
                destroyRecognizer()

                if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
                    _state.value = SpeechState.Error("Speech recognition is not available on this device.")
                    return@post
                }

                val recognizer = SpeechRecognizer.createSpeechRecognizer(appContext)
                speechRecognizer = recognizer

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }

                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _state.value = SpeechState.Listening
                    }

                    override fun onBeginningOfSpeech() {
                        _state.value = SpeechState.Listening
                    }

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _state.value = SpeechState.Processing
                    }

                    override fun onError(error: Int) {
                        val errorMsg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "I couldn't hear that clearly."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected."
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording issue."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                            else -> "Could not hear speech."
                        }
                        _state.value = SpeechState.Error(errorMsg)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim().orEmpty()
                        if (text.isNotBlank()) {
                            _state.value = SpeechState.Result(text)
                        } else {
                            _state.value = SpeechState.Error("I couldn't hear that.")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim()
                        if (!text.isNullOrBlank()) {
                            _state.value = SpeechState.Result(text)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                recognizer.startListening(intent)
                _state.value = SpeechState.Listening
            } catch (e: Exception) {
                _state.value = SpeechState.Error("Could not start speech recognition.")
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                _state.value = SpeechState.Processing
            } catch (_: Exception) {
                _state.value = SpeechState.Idle
            }
        }
    }

    fun reset() {
        mainHandler.post {
            destroyRecognizer()
            _state.value = SpeechState.Idle
        }
    }

    fun destroy() {
        mainHandler.post {
            destroyRecognizer()
            _state.value = SpeechState.Idle
        }
    }

    private fun destroyRecognizer() {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
    }
}
