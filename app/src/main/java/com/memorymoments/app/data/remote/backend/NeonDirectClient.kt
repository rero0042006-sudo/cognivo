package com.memorymoments.app.data.remote.backend

import android.util.Log
import com.google.gson.annotations.SerializedName
import com.memorymoments.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class NeonSqlQueryRequest(
    @SerializedName("query") val query: String
)

data class NeonSqlQueryResponse(
    @SerializedName("rowCount") val rowCount: Int? = null,
    @SerializedName("command") val command: String? = null
)

interface NeonSqlApi {
    @POST("sql")
    suspend fun executeSql(
        @Header("Neon-Connection-String") connectionString: String,
        @Body request: NeonSqlQueryRequest
    ): Response<NeonSqlQueryResponse>
}

/**
 * Direct HTTPS Client for Neon Serverless PostgreSQL Database.
 *
 * Allows the Android mobile app to write directly to Cloud PostgreSQL over HTTPS,
 * ensuring immediate database reflection regardless of whether the local FastAPI server
 * is running or on the same network.
 */
object NeonDirectClient {
    private const val TAG = "NeonDirectClient"

    private val connectionString: String
        get() = BuildConfig.NEON_DATABASE_URL.trim()

    private val isConfigured: Boolean
        get() = connectionString.isNotBlank() && connectionString.startsWith("postgres")

    private val host: String
        get() {
            return try {
                val afterAt = connectionString.substringAfter("@")
                val hostPort = afterAt.substringBefore("/")
                hostPort.substringBefore(":")
            } catch (e: Exception) {
                "ep-green-sea-axwjg4hg-pooler.c-4.us-east-2.aws.neon.tech"
            }
        }

    private val api: NeonSqlApi by lazy {
        val baseUrl = "https://$host/"
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NeonSqlApi::class.java)
    }

    private fun escapeSql(value: String?): String {
        if (value == null) return "NULL"
        val escaped = value.replace("'", "''")
        return "'$escaped'"
    }

    suspend fun upsertPatient(
        id: String,
        fullName: String,
        age: Int? = null,
        dob: String? = null,
        gender: String? = null,
        email: String? = null,
        phone: String? = null,
        isCompleted: Boolean = true
    ): Boolean {
        if (!isConfigured) {
            Log.w(TAG, "Neon Database URL not configured in BuildConfig.")
            return false
        }
        return try {
            val ageVal = age?.toString() ?: "NULL"
            val isCompVal = if (isCompleted) "TRUE" else "FALSE"
            val sql = """
                INSERT INTO patients (id, full_name, age, date_of_birth, gender, email, phone, is_completed, updated_at)
                VALUES (${escapeSql(id)}, ${escapeSql(fullName)}, $ageVal, ${escapeSql(dob)}, ${escapeSql(gender)}, ${escapeSql(email)}, ${escapeSql(phone)}, $isCompVal, CURRENT_TIMESTAMP)
                ON CONFLICT (id) DO UPDATE SET
                    full_name = EXCLUDED.full_name,
                    age = COALESCE(EXCLUDED.age, patients.age),
                    date_of_birth = COALESCE(EXCLUDED.date_of_birth, patients.date_of_birth),
                    gender = COALESCE(EXCLUDED.gender, patients.gender),
                    email = COALESCE(EXCLUDED.email, patients.email),
                    phone = COALESCE(EXCLUDED.phone, patients.phone),
                    is_completed = EXCLUDED.is_completed,
                    updated_at = CURRENT_TIMESTAMP;
            """.trimIndent()

            Log.i(TAG, "Executing Neon SQL Upsert for patient ID: $id ($fullName)")
            val response = api.executeSql(connectionString, NeonSqlQueryRequest(sql))
            if (response.isSuccessful) {
                Log.i(TAG, "Direct Neon SQL Upsert SUCCEEDED for patient ID: $id")
                true
            } else {
                Log.e(TAG, "Direct Neon SQL Upsert FAILED: Code ${response.code()} ${response.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception executing Direct Neon SQL Upsert: ${e.message}", e)
            false
        }
    }

    suspend fun logActivity(
        id: String,
        patientId: String,
        activityName: String,
        score: Double,
        category: String = "Memory"
    ): Boolean {
        if (!isConfigured) return false
        return try {
            val sql = """
                INSERT INTO activities (id, patient_id, activity_name, score, category, completed_at)
                VALUES (${escapeSql(id)}, ${escapeSql(patientId)}, ${escapeSql(activityName)}, $score, ${escapeSql(category)}, CURRENT_TIMESTAMP)
                ON CONFLICT (id) DO NOTHING;
            """.trimIndent()

            val response = api.executeSql(connectionString, NeonSqlQueryRequest(sql))
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Exception logging activity to Neon: ${e.message}", e)
            false
        }
    }

    suspend fun upsertCaregiver(
        id: String,
        fullName: String,
        email: String? = null,
        phone: String? = null,
        patientRelationship: String? = null,
        linkedPatientId: String? = null,
        isCompleted: Boolean = true
    ): Boolean {
        if (!isConfigured) return false
        return try {
            val isCompVal = if (isCompleted) "TRUE" else "FALSE"
            val sql = """
                INSERT INTO caregivers (id, full_name, email, phone, patient_relationship, linked_patient_id, is_completed, updated_at)
                VALUES (${escapeSql(id)}, ${escapeSql(fullName)}, ${escapeSql(email)}, ${escapeSql(phone)}, ${escapeSql(patientRelationship)}, ${escapeSql(linkedPatientId)}, $isCompVal, CURRENT_TIMESTAMP)
                ON CONFLICT (id) DO UPDATE SET
                    full_name = EXCLUDED.full_name,
                    email = COALESCE(EXCLUDED.email, caregivers.email),
                    phone = COALESCE(EXCLUDED.phone, caregivers.phone),
                    patient_relationship = COALESCE(EXCLUDED.patient_relationship, caregivers.patient_relationship),
                    linked_patient_id = COALESCE(EXCLUDED.linked_patient_id, caregivers.linked_patient_id),
                    is_completed = EXCLUDED.is_completed,
                    updated_at = CURRENT_TIMESTAMP;
            """.trimIndent()

            Log.i(TAG, "Executing Neon SQL Upsert for caregiver ID: $id ($fullName)")
            val response = api.executeSql(connectionString, NeonSqlQueryRequest(sql))
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Exception executing Direct Neon SQL Upsert for caregiver: ${e.message}", e)
            false
        }
    }

    suspend fun upsertCondition(
        id: String,
        patientId: String,
        condition: String
    ): Boolean {
        if (!isConfigured) return false
        return try {
            val sql = """
                INSERT INTO patient_conditions (id, patient_id, condition, created_at)
                VALUES (${escapeSql(id)}, ${escapeSql(patientId)}, ${escapeSql(condition)}, CURRENT_TIMESTAMP)
                ON CONFLICT (id) DO NOTHING;
            """.trimIndent()
            val response = api.executeSql(connectionString, NeonSqlQueryRequest(sql))
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Exception inserting condition to Neon: ${e.message}", e)
            false
        }
    }

    suspend fun upsertEmergencyContact(
        id: String,
        patientId: String,
        name: String,
        relationship: String? = null,
        phone: String? = null
    ): Boolean {
        if (!isConfigured) return false
        return try {
            val sql = """
                INSERT INTO emergency_contacts (id, patient_id, name, relationship, phone, created_at)
                VALUES (${escapeSql(id)}, ${escapeSql(patientId)}, ${escapeSql(name)}, ${escapeSql(relationship)}, ${escapeSql(phone)}, CURRENT_TIMESTAMP)
                ON CONFLICT (id) DO UPDATE SET
                    name = EXCLUDED.name,
                    relationship = EXCLUDED.relationship,
                    phone = EXCLUDED.phone;
            """.trimIndent()
            val response = api.executeSql(connectionString, NeonSqlQueryRequest(sql))
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Exception inserting contact to Neon: ${e.message}", e)
            false
        }
    }

    suspend fun upsertReminder(
        id: String,
        patientId: String,
        title: String,
        date: String? = null,
        time: String? = null,
        repeatType: String = "Daily",
        completed: Boolean = false
    ): Boolean {
        if (!isConfigured) return false
        return try {
            val compVal = if (completed) "TRUE" else "FALSE"
            val compAt = if (completed) "CURRENT_TIMESTAMP" else "NULL"
            val sql = """
                INSERT INTO reminders (id, patient_id, title, reminder_date, reminder_time, repeat_type, completed, completed_at)
                VALUES (${escapeSql(id)}, ${escapeSql(patientId)}, ${escapeSql(title)}, ${escapeSql(date)}, ${escapeSql(time)}, ${escapeSql(repeatType)}, $compVal, $compAt)
                ON CONFLICT (id) DO UPDATE SET
                    title = EXCLUDED.title,
                    reminder_date = EXCLUDED.reminder_date,
                    reminder_time = EXCLUDED.reminder_time,
                    repeat_type = EXCLUDED.repeat_type,
                    completed = EXCLUDED.completed,
                    completed_at = EXCLUDED.completed_at;
            """.trimIndent()
            val response = api.executeSql(connectionString, NeonSqlQueryRequest(sql))
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Exception saving reminder to Neon: ${e.message}", e)
            false
        }
    }

    suspend fun upsertAssessment(
        id: String,
        patientId: String,
        assessmentName: String,
        memoryScore: Double,
        attentionScore: Double,
        overallScore: Double
    ): Boolean {
        if (!isConfigured) return false
        return try {
            val sql = """
                INSERT INTO assessments (id, patient_id, assessment_name, memory_score, attention_score, overall_score, completed_at)
                VALUES (${escapeSql(id)}, ${escapeSql(patientId)}, ${escapeSql(assessmentName)}, $memoryScore, $attentionScore, $overallScore, CURRENT_TIMESTAMP)
                ON CONFLICT (id) DO NOTHING;
            """.trimIndent()
            val response = api.executeSql(connectionString, NeonSqlQueryRequest(sql))
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Exception saving assessment to Neon: ${e.message}", e)
            false
        }
    }
}


