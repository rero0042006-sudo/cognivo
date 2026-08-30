package com.memorymoments.app.utils

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object SoundManager {
    private var toneGen: ToneGenerator? = null

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (_: Exception) {
            toneGen = null
        }
    }

    fun playCorrect(enabled: Boolean = true) {
        if (!enabled || toneGen == null) return
        CoroutineScope(Dispatchers.Default).launch {
            try {
                toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                delay(90)
                toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP2, 140)
            } catch (_: Exception) {}
        }
    }

    fun playIncorrect(enabled: Boolean = true) {
        if (!enabled || toneGen == null) return
        CoroutineScope(Dispatchers.Default).launch {
            try {
                toneGen?.startTone(ToneGenerator.TONE_PROP_NACK, 120)
            } catch (_: Exception) {}
        }
    }

    fun playGameStart(enabled: Boolean = true) {
        if (!enabled || toneGen == null) return
        CoroutineScope(Dispatchers.Default).launch {
            try {
                toneGen?.startTone(ToneGenerator.TONE_PROP_PROMPT, 100)
            } catch (_: Exception) {}
        }
    }

    fun playResults(enabled: Boolean = true) {
        if (!enabled || toneGen == null) return
        CoroutineScope(Dispatchers.Default).launch {
            try {
                toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                delay(100)
                toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP2, 120)
                delay(120)
                toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 200)
            } catch (_: Exception) {}
        }
    }
}
