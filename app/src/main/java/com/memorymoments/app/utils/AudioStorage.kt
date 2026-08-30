package com.memorymoments.app.utils

import android.content.Context
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

class AudioStorage(context: Context) {
    private val appContext = context.applicationContext
    private val songsDir: File = File(appContext.filesDir, "songs").apply { mkdirs() }

    fun copyAudioUri(sourceUri: Uri, songId: String): String {
        val extension = runCatching {
            val mime = appContext.contentResolver.getType(sourceUri)
            when (mime) {
                "audio/mpeg", "audio/mp3" -> "mp3"
                "audio/wav", "audio/x-wav" -> "wav"
                "audio/ogg", "audio/opus" -> "ogg"
                "audio/aac", "audio/mp4", "audio/m4a" -> "m4a"
                else -> "mp3"
            }
        }.getOrDefault("mp3")

        val destination = File(songsDir, "$songId.$extension")
        appContext.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destination).use { output ->
                input.copyTo(output)
            }
        } ?: error("Unable to open audio URI: $sourceUri")

        return destination.absolutePath
    }

    fun deleteForSong(songId: String) {
        val files = songsDir.listFiles { _, name -> name.startsWith(songId) }
        files?.forEach { runCatching { it.delete() } }
    }

    fun fileExists(path: String): Boolean {
        val file = File(path)
        return file.exists() && file.length() > 0
    }

    /**
     * Generates a gentle 16-bit 44.1kHz mono WAV musical chime for demo testing without external MP3 files.
     */
    fun createDemoSongAudio(songId: String, songIndex: Int): String {
        val destination = File(songsDir, "$songId.wav")
        val sampleRate = 44100
        val durationSeconds = 12 // 12 seconds clip for 10-second game rounds

        // Frequencies for chords:
        val chords = when (songIndex % 3) {
            0 -> listOf(261.63, 329.63, 392.00, 523.25) // C Major (C4, E4, G4, C5)
            1 -> listOf(392.00, 493.88, 587.33, 783.99) // G Major (G4, B4, D5, G5)
            else -> listOf(349.23, 440.00, 523.25, 698.46) // F Major (F4, A4, C5, F5)
        }

        val totalSamples = sampleRate * durationSeconds
        val pcmData = ByteArray(totalSamples * 2)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val chordIndex = ((t / 3.0).toInt()) % chords.size
            val baseFreq = chords[chordIndex]

            // Soft harmonics with envelope decay
            val noteTime = t % 1.5
            val envelope = (1.0 - (noteTime / 1.5)).coerceIn(0.0, 1.0)
            val sampleVal = (sin(2.0 * Math.PI * baseFreq * t) * 0.6 +
                    sin(2.0 * Math.PI * (baseFreq * 2) * t) * 0.3 +
                    sin(2.0 * Math.PI * (baseFreq * 1.5) * t) * 0.1) * envelope * 0.45

            val shortVal = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            val byteIndex = i * 2
            pcmData[byteIndex] = (shortVal.toInt() and 0xFF).toByte()
            pcmData[byteIndex + 1] = ((shortVal.toInt() shr 8) and 0xFF).toByte()
        }

        val wavHeader = createWavHeader(pcmData.size, sampleRate, 1, 16)
        FileOutputStream(destination).use { out ->
            out.write(wavHeader)
            out.write(pcmData)
        }

        return destination.absolutePath
    }

    private fun createWavHeader(pcmDataLength: Int, sampleRate: Int, channels: Int, bitsPerSample: Int): ByteArray {
        val totalDataLen = pcmDataLength + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8

        val header = ByteArray(44)
        val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

        buffer.put("RIFF".toByteArray())
        buffer.putInt(totalDataLen)
        buffer.put("WAVE".toByteArray())
        buffer.put("fmt ".toByteArray())
        buffer.putInt(16) // Subchunk1Size for PCM
        buffer.putShort(1.toShort()) // AudioFormat 1 = PCM
        buffer.putShort(channels.toShort())
        buffer.putInt(sampleRate)
        buffer.putInt(byteRate)
        buffer.putShort(blockAlign.toShort())
        buffer.putShort(bitsPerSample.toShort())
        buffer.put("data".toByteArray())
        buffer.putInt(pcmDataLength)

        return header
    }
}
