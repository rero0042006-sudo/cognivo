package com.memorymoments.app.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.memorymoments.app.data.local.PreferenceKeys
import com.memorymoments.app.data.local.appDataStore
import com.memorymoments.app.data.remote.DistractorRequest
import com.memorymoments.app.data.remote.NetworkModule
import com.memorymoments.app.data.remote.VisualAttributes
import com.memorymoments.app.model.DistractorCharacter
import com.memorymoments.app.model.DistractorStyle
import com.memorymoments.app.model.FamilyMember
import com.memorymoments.app.model.VisualProfile
import com.memorymoments.app.utils.Constants
import com.memorymoments.app.utils.ImageStorage
import com.memorymoments.app.utils.NetworkStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.util.UUID

class OfflineException : IOException("offline")

class DistractorUnavailableException(message: String) : IOException(message)

class DistractorRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore = appContext.appDataStore
    private val imageStorage = ImageStorage(appContext)
    private val api = NetworkModule.distractorApi
    private val gson = Gson()
    private val listType = object : TypeToken<List<DistractorCharacter>>() {}.type

    /** Mutex to prevent duplicate simultaneous generation requests. */
    private val generationMutex = Mutex()

    // ── Easy / Medium Pool (unchanged behavior) ──────────────────────

    suspend fun cachedPool(style: DistractorStyle, includeDemo: Boolean = true): List<DistractorCharacter> {
        return loadAll()
            .filter {
                it.difficulty == style &&
                    it.sourceFamilyMemberId == null &&
                    File(it.imageUri).exists() &&
                    (includeDemo || it.source == DistractorCharacter.Source.CLOUDFLARE)
            }
            .sortedBy { it.generatedAt }
    }

    suspend fun clearPool(style: DistractorStyle) {
        val all = loadAll()
        val toDelete = all.filter { it.difficulty == style }
        toDelete.forEach { imageStorage.deleteDistractor(it.imageUri) }
        persist(all.filterNot { it.difficulty == style })
    }

    suspend fun unusedFromPool(
        style: DistractorStyle,
        usedIds: Set<String>,
        count: Int
    ): List<DistractorCharacter> {
        return cachedPool(style, includeDemo = true)
            .filterNot { it.id in usedIds }
            .shuffled()
            .take(count)
    }

    suspend fun ensurePool(
        style: DistractorStyle,
        onProgress: (completed: Int, total: Int, preview: DistractorCharacter?) -> Unit = { _, _, _ -> }
    ): Result<List<DistractorCharacter>> = generationMutex.withLock {
        withContext(Dispatchers.IO) {
            val existing = cachedPool(style, includeDemo = false)
            if (existing.size >= Constants.DISTRACTOR_MIN_POOL) {
                onProgress(existing.size, existing.size, existing.lastOrNull())
                return@withContext Result.success(existing)
            }
            generatePool(style, existing, onProgress)
        }
    }

    suspend fun generatePool(
        style: DistractorStyle,
        existing: List<DistractorCharacter> = emptyList(),
        onProgress: (completed: Int, total: Int, preview: DistractorCharacter?) -> Unit = { _, _, _ -> }
    ): Result<List<DistractorCharacter>> = withContext(Dispatchers.IO) {
        if (!NetworkStatus.isOnline(appContext)) {
            return@withContext if (existing.isNotEmpty()) {
                Result.success(existing)
            } else {
                Result.failure(OfflineException())
            }
        }
        val needed = Constants.DISTRACTOR_POOL_SIZE - existing.size
        val generated = existing.toMutableList()
        onProgress(generated.size, Constants.DISTRACTOR_POOL_SIZE, generated.lastOrNull())
        var lastError: Exception? = null
        repeat(needed.coerceAtLeast(0)) {
            val result = generateDistractor(style)
            result.fold(
                onSuccess = { character ->
                    generated += character
                    onProgress(generated.size, Constants.DISTRACTOR_POOL_SIZE, character)
                },
                onFailure = { error ->
                    lastError = error as? Exception ?: Exception(error)
                }
            )
            if (lastError is OfflineException) {
                return@withContext if (generated.isNotEmpty()) {
                    Result.success(generated)
                } else {
                    Result.failure(lastError)
                }
            }
        }
        if (generated.isNotEmpty()) {
            Result.success(generated)
        } else {
            Result.failure(
                lastError ?: DistractorUnavailableException("Couldn't create game characters right now.")
            )
        }
    }

    suspend fun generateDistractor(
        difficulty: DistractorStyle
    ): Result<DistractorCharacter> = withContext(Dispatchers.IO) {
        if (!NetworkStatus.isOnline(appContext)) {
            return@withContext Result.failure(OfflineException())
        }
        try {
            val body = withTimeout(Constants.GENERATION_TIMEOUT_MS) {
                api.generateDistractor(
                    DistractorRequest(difficulty = difficulty.apiDifficulty)
                )
            }
            saveGenerated(difficulty, body, sourceFamilyMemberId = null)
        } catch (_: TimeoutCancellationException) {
            Result.failure(DistractorUnavailableException("Creating game characters took too long."))
        } catch (error: HttpException) {
            Result.failure(DistractorUnavailableException(parseHttpError(error)))
        } catch (_: OfflineException) {
            Result.failure(OfflineException())
        } catch (error: IOException) {
            Result.failure(
                if (error is OfflineException) error
                else DistractorUnavailableException("Couldn't create game characters right now.")
            )
        }
    }

    // ── Hard Mode (per-family-member similar distractors) ────────────

    /**
     * Returns cached Hard distractors for a specific family member.
     * Only returns characters whose image files still exist on disk.
     */
    suspend fun cachedHardPool(familyMemberId: String): List<DistractorCharacter> {
        return loadAll()
            .filter {
                it.difficulty == DistractorStyle.CHALLENGE &&
                    it.sourceFamilyMemberId == familyMemberId &&
                    File(it.imageUri).exists()
            }
            .sortedBy { it.generatedAt }
    }

    /**
     * Ensures a sufficient pool of Hard distractors for a specific family member.
     * Uses cached visual profile and cached images when available.
     * Generates only what is missing.
     */
    suspend fun ensureHardPool(
        member: FamilyMember,
        visualProfileRepo: VisualProfileRepository,
        targetSize: Int = Constants.HARD_DISTRACTOR_POOL_SIZE,
        onProgress: (completed: Int, total: Int, preview: DistractorCharacter?) -> Unit = { _, _, _ -> }
    ): Result<List<DistractorCharacter>> = generationMutex.withLock {
        withContext(Dispatchers.IO) {
            val existing = cachedHardPool(member.id)
            if (existing.size >= targetSize) {
                onProgress(existing.size, targetSize, existing.lastOrNull())
                return@withContext Result.success(existing.take(targetSize))
            }

            // Get or analyze visual profile (cached or via Groq Vision)
            val profile = visualProfileRepo.getOrAnalyze(member)

            if (profile == null) {
                // Groq analysis failed — fall back to generic distractor generation
                return@withContext generateHardFallback(existing, targetSize, onProgress)
            }

            // Generate only the missing distractors using the visual profile
            val needed = targetSize - existing.size
            val generated = existing.toMutableList()
            onProgress(generated.size, targetSize, generated.lastOrNull())
            var lastError: Exception? = null

            repeat(needed) {
                val result = generateHardDistractor(member.id, profile)
                result.fold(
                    onSuccess = { character ->
                        generated += character
                        onProgress(generated.size, targetSize, character)
                    },
                    onFailure = { error ->
                        lastError = error as? Exception ?: Exception(error)
                    }
                )
                if (lastError is OfflineException) {
                    return@withContext if (generated.isNotEmpty()) {
                        Result.success(generated)
                    } else {
                        Result.failure(lastError)
                    }
                }
            }

            if (generated.isNotEmpty()) {
                Result.success(generated)
            } else {
                Result.failure(
                    lastError ?: DistractorUnavailableException("Couldn't create similar game characters.")
                )
            }
        }
    }

    /**
     * Generates a single Hard distractor using visual attributes via Cloudflare.
     * The family photo is NOT sent to Cloudflare — only validated attributes.
     */
    private suspend fun generateHardDistractor(
        familyMemberId: String,
        profile: VisualProfile
    ): Result<DistractorCharacter> = withContext(Dispatchers.IO) {
        if (!NetworkStatus.isOnline(appContext)) {
            return@withContext Result.failure(OfflineException())
        }

        val attrs = profile.toAttributesMap()
        val visualAttributes = VisualAttributes(
            ageGroup = attrs["ageGroup"] ?: "unknown",
            hairColor = attrs["hairColor"] ?: "unknown",
            hairStyle = attrs["hairStyle"] ?: "unknown",
            glasses = attrs["glasses"] ?: "unknown",
            clothing = attrs["clothing"] ?: "unknown",
            complexion = attrs["complexion"] ?: "unknown",
            generalBuild = attrs["generalBuild"] ?: "unknown"
        )

        try {
            val body = withTimeout(Constants.GENERATION_TIMEOUT_MS) {
                api.generateDistractor(
                    DistractorRequest(
                        difficulty = DistractorStyle.CHALLENGE.apiDifficulty,
                        visualAttributes = visualAttributes
                    )
                )
            }
            saveGenerated(DistractorStyle.CHALLENGE, body, sourceFamilyMemberId = familyMemberId)
        } catch (_: TimeoutCancellationException) {
            Result.failure(DistractorUnavailableException("Creating similar characters took too long."))
        } catch (error: HttpException) {
            Result.failure(DistractorUnavailableException(parseHttpError(error)))
        } catch (_: OfflineException) {
            Result.failure(OfflineException())
        } catch (error: IOException) {
            Result.failure(
                if (error is OfflineException) error
                else DistractorUnavailableException("Couldn't create similar game characters right now.")
            )
        }
    }

    /**
     * Fallback for Hard mode when Groq Vision fails:
     * generate generic distractors using the standard Cloudflare pipeline.
     */
    private suspend fun generateHardFallback(
        existing: List<DistractorCharacter>,
        targetSize: Int,
        onProgress: (completed: Int, total: Int, preview: DistractorCharacter?) -> Unit
    ): Result<List<DistractorCharacter>> {
        val needed = targetSize - existing.size
        val generated = existing.toMutableList()
        onProgress(generated.size, targetSize, generated.lastOrNull())
        var lastError: Exception? = null

        repeat(needed.coerceAtLeast(0)) {
            val result = generateDistractor(DistractorStyle.CHALLENGE)
            result.fold(
                onSuccess = { character ->
                    generated += character
                    onProgress(generated.size, targetSize, character)
                },
                onFailure = { error ->
                    lastError = error as? Exception ?: Exception(error)
                }
            )
        }

        return if (generated.isNotEmpty()) {
            Result.success(generated)
        } else {
            Result.failure(
                lastError ?: DistractorUnavailableException("Couldn't create game characters.")
            )
        }
    }

    /**
     * Invalidates all Hard distractors for a specific family member.
     * Called when the caregiver replaces their photo.
     */
    suspend fun invalidateHardForMember(memberId: String) {
        val all = loadAll()
        val toDelete = all.filter {
            it.difficulty == DistractorStyle.CHALLENGE &&
                it.sourceFamilyMemberId == memberId
        }
        toDelete.forEach { imageStorage.deleteDistractor(it.imageUri) }
        persist(all.filterNot {
            it.difficulty == DistractorStyle.CHALLENGE &&
                it.sourceFamilyMemberId == memberId
        })
    }

    // ── Demo Pool ────────────────────────────────────────────────────

    suspend fun createDemoPool(style: DistractorStyle): List<DistractorCharacter> {
        return withContext(Dispatchers.IO) {
            val demo = (0 until Constants.DISTRACTOR_POOL_SIZE).map { index ->
                val id = "demo-${style.name.lowercase()}-$index"
                val path = imageStorage.createDemoDistractor(id, index)
                DistractorCharacter(
                    id = id,
                    imageUri = path,
                    difficulty = style,
                    generatedAt = System.currentTimeMillis(),
                    source = DistractorCharacter.Source.DEMO
                )
            }
            persist(merge(loadAll().filterNot { it.difficulty == style }, demo))
            demo
        }
    }

    // ── Internal Helpers ─────────────────────────────────────────────

    private suspend fun saveGenerated(
        style: DistractorStyle,
        body: ResponseBody,
        sourceFamilyMemberId: String? = null
    ): Result<DistractorCharacter> {
        return body.use { response ->
            val mimeType = response.contentType()?.toString().orEmpty()
            if (mimeType.isNotBlank() && !mimeType.startsWith("image/")) {
                return@use Result.failure(
                    DistractorUnavailableException("Couldn't create game characters right now.")
                )
            }
            val bytes = response.bytes()
            if (bytes.isEmpty()) {
                return@use Result.failure(
                    DistractorUnavailableException("Couldn't create game characters right now.")
                )
            }
            val id = UUID.randomUUID().toString()
            val path = imageStorage.saveDistractor(
                id = id,
                bytes = bytes,
                mimeType = mimeType.ifBlank { "image/jpeg" }
            )
            val character = DistractorCharacter(
                id = id,
                imageUri = path,
                difficulty = style,
                generatedAt = System.currentTimeMillis(),
                source = DistractorCharacter.Source.CLOUDFLARE,
                sourceFamilyMemberId = sourceFamilyMemberId
            )
            var currentAll = merge(loadAll(), listOf(character))
            if (sourceFamilyMemberId != null) {
                val memberPool = currentAll.filter { it.sourceFamilyMemberId == sourceFamilyMemberId }.sortedBy { it.generatedAt }
                if (memberPool.size > Constants.HARD_DISTRACTOR_POOL_SIZE) {
                    val excessCount = memberPool.size - Constants.HARD_DISTRACTOR_POOL_SIZE
                    val toEvict = memberPool.take(excessCount)
                    toEvict.forEach { imageStorage.deleteDistractor(it.imageUri) }
                    val evictIds = toEvict.map { it.id }.toSet()
                    currentAll = currentAll.filterNot { it.id in evictIds }
                }
            }
            persist(currentAll)
            Result.success(character)
        }
    }

    private suspend fun loadAll(): List<DistractorCharacter> {
        val json = dataStore.data.first()[PreferenceKeys.DISTRACTORS]
        if (json.isNullOrBlank()) return emptyList()
        val normalized = json.replace("\"GEMINI\"", "\"CLOUDFLARE\"")
        return runCatching {
            gson.fromJson<List<DistractorCharacter>>(normalized, listType).orEmpty()
        }.getOrDefault(emptyList())
    }

    private suspend fun persist(characters: List<DistractorCharacter>) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.DISTRACTORS] = gson.toJson(characters)
        }
    }

    private fun merge(
        current: List<DistractorCharacter>,
        incoming: List<DistractorCharacter>
    ): List<DistractorCharacter> {
        val byId = current.associateBy { it.id }.toMutableMap()
        incoming.forEach { byId[it.id] = it }
        return byId.values.toList()
    }

    private fun parseHttpError(error: HttpException): String {
        return when (error.code()) {
            429 -> "The character service is busy. Please try again in a moment."
            else -> "Couldn't create game characters right now."
        }
    }
}
