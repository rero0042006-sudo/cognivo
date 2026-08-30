package com.memorymoments.app.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.datastore.preferences.core.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.memorymoments.app.BuildConfig
import com.memorymoments.app.data.local.PreferenceKeys
import com.memorymoments.app.data.local.appDataStore
import com.memorymoments.app.data.remote.GroqChatMessage
import com.memorymoments.app.data.remote.GroqChatRequest
import com.memorymoments.app.data.remote.GroqContentPart
import com.memorymoments.app.data.remote.ImageUrlPart
import com.memorymoments.app.data.remote.NetworkModule
import com.memorymoments.app.model.AgeGroup
import com.memorymoments.app.model.ClothingStyle
import com.memorymoments.app.model.Complexion
import com.memorymoments.app.model.FamilyMember
import com.memorymoments.app.model.GeneralBuild
import com.memorymoments.app.model.GlassesOption
import com.memorymoments.app.model.HairColor
import com.memorymoments.app.model.HairStyle
import com.memorymoments.app.model.VisualProfile
import com.memorymoments.app.utils.NetworkStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Manages visual profile analysis and caching for Hard mode distractors.
 *
 * Uses Groq Vision to extract broad, non-identifying visual characteristics
 * from a family member's photo. Results are cached in DataStore so that
 * Groq is called at most once per family member photo.
 *
 * Privacy: Only broad attributes are extracted and cached.
 * No face embeddings, biometric data, or identity information is stored.
 */
class VisualProfileRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore = appContext.appDataStore
    private val api = NetworkModule.groqApi
    private val gson = Gson()
    private val mapType = object : TypeToken<Map<String, VisualProfile>>() {}.type

    /**
     * Returns a cached VisualProfile for the member, or analyzes the photo
     * with Groq Vision and caches the result. Returns null on failure.
     */
    suspend fun getOrAnalyze(member: FamilyMember): VisualProfile? {
        val cached = getCached(member.id)
        if (cached != null) return cached

        val photoPath = member.originalPhotoUri ?: return null
        return analyzeWithGroq(member.id, photoPath)
    }

    /**
     * Returns a cached profile without making any network calls.
     */
    suspend fun getCached(memberId: String): VisualProfile? {
        val profiles = loadAll()
        return profiles[memberId]
    }

    /**
     * Removes the cached visual profile for a specific member
     * (e.g., when their photo changes).
     */
    suspend fun invalidateForMember(memberId: String) {
        val profiles = loadAll().toMutableMap()
        profiles.remove(memberId)
        persist(profiles)
    }

    private suspend fun analyzeWithGroq(
        memberId: String,
        photoPath: String
    ): VisualProfile? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GROQ_API_KEY
        if (apiKey.isBlank() || !NetworkStatus.isOnline(appContext)) return@withContext null

        val base64 = encodePhotoToBase64(photoPath) ?: return@withContext null

        val systemPrompt = """You are a visual attribute analyzer for a memory game.
Analyze the person in the photo and return ONLY a JSON object with these broad visual characteristics.
Do NOT identify who the person is. Do NOT generate face embeddings or biometric data.
Only extract general visual attributes.

Return EXACTLY this JSON format with no other text:
{
  "ageGroup": "child|young_adult|adult|middle_aged|older_adult",
  "hairColor": "black|brown|blonde|red|gray|white|mixed|unknown",
  "hairStyle": "short|medium|long|curly|straight|wavy|bald|receding|unknown",
  "glasses": "true|false|unknown",
  "clothing": "casual|formal|traditional|sportswear|unknown",
  "complexion": "light|medium|deep|unknown",
  "generalBuild": "slim|average|broad|unknown"
}"""

        val userContent = listOf(
            GroqContentPart(type = "text", text = "Analyze this person's broad visual characteristics. Return only the JSON object."),
            GroqContentPart(
                type = "image_url",
                imageUrl = ImageUrlPart(url = "data:image/jpeg;base64,$base64")
            )
        )

        try {
            val request = GroqChatRequest(
                model = "meta-llama/llama-4-scout-17b-16e-instruct",
                messages = listOf(
                    GroqChatMessage(role = "system", content = systemPrompt),
                    GroqChatMessage(role = "user", content = userContent)
                ),
                temperature = 0.1,
                maxTokens = 200
            )

            val response = api.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            val text = response.choices?.firstOrNull()?.message?.content?.trim() ?: return@withContext null
            val profile = parseVisualProfile(memberId, text)
            if (profile != null) {
                val profiles = loadAll().toMutableMap()
                profiles[memberId] = profile
                persist(profiles)
            }
            profile
        } catch (_: Exception) {
            null
        }
    }

    private fun encodePhotoToBase64(photoPath: String): String? {
        return try {
            val file = File(photoPath)
            if (!file.exists() || file.length() == 0L) return null

            // Decode and re-encode at reduced quality to keep payload reasonable
            val options = BitmapFactory.Options().apply {
                inSampleSize = 2 // Half resolution
            }
            val bitmap = BitmapFactory.decodeFile(photoPath, options) ?: return null
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, outputStream)
            bitmap.recycle()
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseVisualProfile(memberId: String, rawText: String): VisualProfile? {
        return try {
            // Extract JSON from response (handle markdown code blocks)
            val jsonStr = rawText
                .replace("```json", "")
                .replace("```", "")
                .trim()
                .let { text ->
                    val start = text.indexOf('{')
                    val end = text.lastIndexOf('}')
                    if (start >= 0 && end > start) text.substring(start, end + 1) else return null
                }

            val map = gson.fromJson<Map<String, String>>(
                jsonStr,
                object : TypeToken<Map<String, String>>() {}.type
            ) ?: return null

            VisualProfile(
                familyMemberId = memberId,
                ageGroup = AgeGroup.fromString(map["ageGroup"]),
                hairColor = HairColor.fromString(map["hairColor"]),
                hairStyle = HairStyle.fromString(map["hairStyle"]),
                glasses = GlassesOption.fromString(map["glasses"]),
                clothing = ClothingStyle.fromString(map["clothing"]),
                complexion = Complexion.fromString(map["complexion"]),
                generalBuild = GeneralBuild.fromString(map["generalBuild"]),
                createdAt = System.currentTimeMillis()
            )
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun loadAll(): Map<String, VisualProfile> {
        val json = dataStore.data.first()[PreferenceKeys.VISUAL_PROFILES]
        if (json.isNullOrBlank()) return emptyMap()
        return runCatching {
            gson.fromJson<Map<String, VisualProfile>>(json, mapType) ?: emptyMap()
        }.getOrDefault(emptyMap())
    }

    private suspend fun persist(profiles: Map<String, VisualProfile>) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.VISUAL_PROFILES] = gson.toJson(profiles)
        }
    }
}
