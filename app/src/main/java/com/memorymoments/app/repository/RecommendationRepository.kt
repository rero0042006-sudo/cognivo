package com.memorymoments.app.repository

import android.content.Context
import android.util.Log
import com.memorymoments.app.data.local.PreferenceKeys
import com.memorymoments.app.data.local.appDataStore
import com.memorymoments.app.data.remote.backend.BackendClient
import com.memorymoments.app.data.remote.backend.NextGameRecommendationResponse
import com.memorymoments.app.navigation.Routes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * UI State for Model 1 Next-Game Recommendation.
 */
data class RecommendationUiState(
    val isLoading: Boolean = false,
    val recommendedDomain: String? = null,
    val recommendedGameTitle: String? = null,
    val recommendedRoute: String? = null,
    val sessionsCompleted: Int = 0,
    val lastGame: String? = null,
    val error: String? = null
)

/**
 * Domain-to-Game Display and Route Mapper.
 * Maps Model 1 predicted domains (memory, attention, recognition, routine, pattern)
 * to existing Cogniva games and navigation routes.
 */
object DomainGameMapper {
    fun getGameTitle(domain: String?): String {
        return when (domain?.lowercase()) {
            "memory" -> "Who's Who? (Face & Name Recall)"
            "recognition" -> "Who's Who? (Family Recognition)"
            "attention" -> "Where Was It? (Places Recall)"
            "routine" -> "Where Was It? (Daily Landmarks)"
            "pattern" -> "Name That Tune (Melody Recall)"
            else -> "Who's Who? (Face Recognition)"
        }
    }

    fun getGameRoute(domain: String?): String {
        return when (domain?.lowercase()) {
            "memory", "recognition" -> Routes.gameSetup(demo = false)
            "attention", "routine" -> Routes.placesGame(style = "NORMAL", demo = false)
            "pattern" -> Routes.musicGame(style = "NORMAL", demo = false)
            else -> Routes.gameSetup(demo = false)
        }
    }

    fun getDomainDisplayName(domain: String?): String {
        return when (domain?.lowercase()) {
            "memory" -> "Memory"
            "recognition" -> "Recognition"
            "attention" -> "Attention"
            "routine" -> "Routine"
            "pattern" -> "Pattern"
            else -> "Memory"
        }
    }
}

/**
 * Repository for Model 1 Next-Game Recommendations.
 * Connects directly to the existing FastAPI ML backend endpoint:
 * GET /api/patients/{patient_id}/next-game
 */
class RecommendationRepository(private val context: Context) {
    private val dataStore = context.appDataStore
    private val TAG = "RecommendationRepo"

    val currentPatientId: Flow<String?> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.FIREBASE_USER_ID] ?: prefs[PreferenceKeys.CURRENT_USER_ID]
    }

    suspend fun fetchNextGameRecommendation(patientId: String?): Result<NextGameRecommendationResponse> {
        val targetId = patientId ?: currentPatientId.first()
        if (targetId.isNullOrBlank() || targetId == "guest") {
            Log.w(TAG, "No valid authenticated patient ID available for recommendation.")
            return Result.failure(IllegalStateException("No authenticated patient ID"))
        }

        return try {
            Log.i(TAG, "Requesting Model 1 recommendation for patient: $targetId")
            val response = BackendClient.api.getNextGameRecommendation(targetId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.i(TAG, "Model 1 returned next_game: '${body.nextGame}' for patient: $targetId")
                Result.success(body)
            } else {
                val err = "Backend returned code ${response.code()}: ${response.errorBody()?.string()}"
                Log.e(TAG, err)
                Result.failure(RuntimeException(err))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception contacting Model 1 recommendation backend: ${e.message}", e)
            Result.failure(e)
        }
    }
}
