package com.memorymoments.app.repository

import android.content.Context
import com.memorymoments.app.BuildConfig
import com.memorymoments.app.data.remote.GroqChatMessage
import com.memorymoments.app.data.remote.GroqChatRequest
import com.memorymoments.app.data.remote.NetworkModule
import com.memorymoments.app.model.Memory
import com.memorymoments.app.utils.NetworkStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

data class ConversationMessage(
    val speaker: String, // "user" or "companion"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class MemoryTalkRepository(context: Context) {
    private val appContext = context.applicationContext
    private val api = NetworkModule.groqApi
    private val personRepo = PersonRepository(appContext)
    private val placeRepo = PlaceRepository(appContext)

    suspend fun generateReminiscenceResponse(
        memory: Memory,
        userTranscript: String,
        history: List<ConversationMessage> = emptyList()
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GROQ_API_KEY
        if (apiKey.isBlank() || !NetworkStatus.isOnline(appContext)) {
            return@withContext null
        }

        val allPeople = personRepo.people.first()
        val people = memory.personIds.mapNotNull { id -> allPeople.find { it.id == id }?.name }.joinToString(", ")

        val allPlaces = placeRepo.places.first()
        val places = memory.placeIds.mapNotNull { id -> allPlaces.find { it.id == id }?.name }.joinToString(", ")

        val systemPrompt = """
            You are a gentle, warm reminiscence companion for an older adult.
            Your job is to encourage warm, joyful conversation about a personal memory.
            Never diagnose or assess cognitive ability.
            Never score memory or judge accuracy.
            Never tell the person they are wrong or correct their memories.
            Never pressure the user to remember details.
            Respond warmly, kindly, and naturally in plain language.
            Keep the response to 1-2 short, simple sentences.
            Ask at most ONE gentle follow-up question.
            If the user says they don't remember, reassure them warmly that it is completely okay.
            Do not invent facts about their life.
        """.trimIndent()

        val contextInfo = buildString {
            append("Memory Title: ${memory.title}\n")
            if (!memory.date.isNullOrBlank()) append("Date/Year: ${memory.date}\n")
            if (places.isNotBlank()) append("Place: $places\n")
            if (people.isNotBlank()) append("People: $people\n")
            if (!memory.description.isNullOrBlank()) append("Details: ${memory.description}\n")
        }

        val messages = mutableListOf<GroqChatMessage>()
        messages.add(GroqChatMessage(role = "system", content = systemPrompt))

        // History
        val historyContext = history.takeLast(6).map { msg ->
            GroqChatMessage(
                role = if (msg.speaker == "user") "user" else "assistant",
                content = msg.text
            )
        }
        messages.addAll(historyContext)

        // Current turn
        val currentPrompt = "Context:\n$contextInfo\nUser said: \"$userTranscript\""
        messages.add(GroqChatMessage(role = "user", content = currentPrompt))

        try {
            val request = GroqChatRequest(
                messages = messages,
                temperature = 0.6,
                maxTokens = 150
            )

            val response = api.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            val text = response.choices?.firstOrNull()?.message?.content?.trim().orEmpty()
            cleanResponse(text)
        } catch (_: Exception) {
            null
        }
    }

    private fun cleanResponse(raw: String): String? {
        if (raw.isBlank()) return null
        val cleaned = raw
            .trim()
            .removeSurrounding("\"")
            .removeSurrounding("'")
            .replace(Regex("^(Companion|Assistant|AI):\\s*", RegexOption.IGNORE_CASE), "")
            .trim()

        return if (cleaned.isNotBlank()) cleaned else null
    }
}
