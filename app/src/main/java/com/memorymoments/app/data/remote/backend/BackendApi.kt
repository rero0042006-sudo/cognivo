package com.memorymoments.app.data.remote.backend

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Data Transfer Objects for FastAPI / Neon PostgreSQL backend communication.
 */
data class PatientUpsertRequest(
    val id: String,
    @SerializedName("full_name") val fullName: String,
    val age: Int? = null,
    @SerializedName("date_of_birth") val dateOfBirth: String? = null,
    val gender: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @SerializedName("is_completed") val isCompleted: Boolean = true
)

data class ActivityLogRequest(
    val id: String,
    @SerializedName("patient_id") val patientId: String,
    @SerializedName("activity_name") val activityName: String,
    val score: Double,
    val category: String = "Memory"
)

data class BackendGeneralResponse(
    val status: String,
    val message: String? = null
)

data class NextGameRecommendationResponse(
    @SerializedName("patient_id") val patientId: String,
    @SerializedName("next_game") val nextGame: String,
    @SerializedName("predicted_class_code") val predictedClassCode: Int,
    @SerializedName("sessions_completed") val sessionsCompleted: Int,
    @SerializedName("last_game") val lastGame: String
)

interface BackendApi {
    @POST("api/patients")
    suspend fun upsertPatient(@Body request: PatientUpsertRequest): Response<BackendGeneralResponse>

    @POST("api/activities")
    suspend fun logActivity(@Body request: ActivityLogRequest): Response<BackendGeneralResponse>

    @GET("api/patients/{patient_id}/next-game")
    suspend fun getNextGameRecommendation(
        @Path("patient_id") patientId: String
    ): Response<NextGameRecommendationResponse>
}
