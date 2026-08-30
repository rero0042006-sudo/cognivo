package com.memorymoments.app.repository

import android.content.Context
import android.util.Log
import com.memorymoments.app.data.local.PreferenceKeys
import com.memorymoments.app.data.local.appDataStore
import com.memorymoments.app.data.remote.backend.BackendClient
import com.memorymoments.app.data.remote.backend.NextGameRecommendationResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Repository responsible for fetching Model 1 next-game recommendations
 * from the FastAPI backend (which queries Neon DB and executes XGBoost inference).
 */
class RecommendationRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore = appContext.appDataStore

    companion object {
        private const val TAG = "RecommendationRepo"
    }

    suspend fun getNextGameRecommendation(overridePatientId: String? = null): Result<NextGameRecommendationResponse> {
        val patientId = if (!overridePatientId.isNullOrBlank()) {
            overridePatientId
        } else {
            dataStore.data.map { prefs ->
                val fb = prefs[PreferenceKeys.FIREBASE_USER_ID]
                val curr = prefs[PreferenceKeys.CURRENT_USER_ID]
                if (!fb.isNullOrBlank()) fb else curr
            }.first()
        }

        if (patientId.isNullOrBlank() || patientId == "guest") {
            Log.w(TAG, "No authenticated patient ID found for recommendation request.")
            return Result.failure(IllegalStateException("No authenticated patient session"))
        }

        return try {
            Log.i(TAG, "Requesting Model 1 recommendation for patient ID: $patientId")
            val response = BackendClient.api.getNextGameRecommendation(patientId)
            if (response.isSuccessful && response.body() != null) {
                val recommendation = response.body()!!
                Log.i(TAG, "Successfully received Model 1 recommendation: ${recommendation.nextGame} for patient: $patientId")
                Result.success(recommendation)
            } else {
                val errorMsg = "HTTP ${response.code()}: ${response.errorBody()?.string() ?: "Unknown error"}"
                Log.e(TAG, "Failed to fetch recommendation: $errorMsg")
                Result.failure(Exception("Recommendation request failed ($errorMsg)"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network or server exception during recommendation fetch: ${e.message}", e)
            Result.failure(e)
        }
    }
}
