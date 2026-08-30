package com.memorymoments.app.repository

import android.content.Context
import com.memorymoments.app.BuildConfig
import com.memorymoments.app.data.remote.GroqChatMessage
import com.memorymoments.app.data.remote.GroqChatRequest
import com.memorymoments.app.data.remote.NetworkModule
import com.memorymoments.app.model.FamilyMember
import com.memorymoments.app.model.GameMode
import com.memorymoments.app.utils.NetworkStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GroqPersonalizationRepository(context: Context) {
    private val appContext = context.applicationContext
    private val api = NetworkModule.groqApi

    suspend fun generatePersonalizedQuestion(
        member: FamilyMember,
        mode: GameMode = GameMode.EASY
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GROQ_API_KEY
        if (apiKey.isBlank() || !NetworkStatus.isOnline(appContext)) {
            return@withContext null
        }

        val memoryContext = member.memoryContext.trim()
        val hasContext = memoryContext.isNotBlank()

        val systemPrompt = "You are a gentle cognitive game assistant for seniors. " +
            "Given a family member's details, generate a short, simple question (under 8 words, ALL CAPS) asking the player to identify them. " +
            (if (hasContext) "Incorporate the memory context if helpful. " else "") +
            "Example: WHO VISITS SUNDAYS WITH FLOWERS? or WHO IS YOUR DAUGHTER? " +
            "Output ONLY the question text without quotes or explanations."

        val userPrompt = buildString {
            append("Name: ${member.name.trim()}, ")
            append("Relationship: ${member.relationship.trim()}")
            if (hasContext) {
                append(", MemoryContext: $memoryContext")
            }
        }

        try {
            val request = GroqChatRequest(
                messages = listOf(
                    GroqChatMessage(role = "system", content = systemPrompt),
                    GroqChatMessage(role = "user", content = userPrompt)
                ),
                temperature = 0.3,
                maxTokens = 350
            )

            val response = api.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            val text = response.choices?.firstOrNull()?.message?.content?.trim().orEmpty()
            cleanQuestion(text)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun generateEncouragement(
        member: FamilyMember
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GROQ_API_KEY
        if (apiKey.isBlank() || !NetworkStatus.isOnline(appContext)) {
            return@withContext null
        }

        val systemPrompt = "Generate a short, warm encouragement (under 6 words, all caps) for a senior player who correctly recognized their family member. " +
            "Example: YOU FOUND SARAH, WELL DONE! Output ONLY the encouragement text."

        val userPrompt = "Name: ${member.name.trim()}, Relationship: ${member.relationship.trim()}"

        try {
            val request = GroqChatRequest(
                messages = listOf(
                    GroqChatMessage(role = "system", content = systemPrompt),
                    GroqChatMessage(role = "user", content = userPrompt)
                ),
                temperature = 0.5,
                maxTokens = 250
            )

            val response = api.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            val text = response.choices?.firstOrNull()?.message?.content?.trim().orEmpty()
            cleanEncouragement(text)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun recommendNextActivity(
        availableActivities: List<String>,
        candidateMemories: List<String>,
        difficulty: String
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GROQ_API_KEY
        if (apiKey.isBlank() || !NetworkStatus.isOnline(appContext) || availableActivities.isEmpty()) {
            return@withContext null
        }

        val systemPrompt = "You are an intelligent activity selector for a dementia reminiscence cognitive app. " +
            "Select exactly ONE activity identifier from the provided list: ${availableActivities.joinToString(", ")}. " +
            "Output ONLY the activity identifier string without quotes, explanations or extra text."

        val userPrompt = buildString {
            append("Activities: ${availableActivities.joinToString(", ")}. ")
            if (candidateMemories.isNotEmpty()) {
                append("Memories: ${candidateMemories.take(5).joinToString("; ")}. ")
            }
            append("Difficulty: $difficulty.")
        }

        try {
            val request = GroqChatRequest(
                messages = listOf(
                    GroqChatMessage(role = "system", content = systemPrompt),
                    GroqChatMessage(role = "user", content = userPrompt)
                ),
                temperature = 0.2,
                maxTokens = 50
            )

            val response = api.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            val text = response.choices?.firstOrNull()?.message?.content?.trim().orEmpty().uppercase()
            availableActivities.find { text.contains(it, ignoreCase = true) }
        } catch (_: Exception) {
            null
        }
    }

    private fun cleanQuestion(raw: String): String? {
        if (raw.isBlank()) return null
        val cleaned = raw
            .lines()
            .lastOrNull { it.isNotBlank() }
            ?.trim()
            ?.removeSurrounding("\"")
            ?.removeSurrounding("'")
            ?.removePrefix("Question:")
            ?.removePrefix("QUESTION:")
            ?.trim()
            ?.uppercase()
            .orEmpty()

        return if (cleaned.length in 5..60 && cleaned.contains("WHO", ignoreCase = true)) {
            if (cleaned.endsWith("?")) cleaned else "$cleaned?"
        } else {
            null
        }
    }

    private fun cleanEncouragement(raw: String): String? {
        if (raw.isBlank()) return null
        val cleaned = raw
            .lines()
            .lastOrNull { it.isNotBlank() }
            ?.trim()
            ?.removeSurrounding("\"")
            ?.removeSurrounding("'")
            ?.uppercase()
            .orEmpty()

        return if (cleaned.length in 4..50) cleaned else null
    }
}
