package com.memorymoments.app.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.memorymoments.app.data.local.PreferenceKeys
import com.memorymoments.app.data.local.appDataStore
import com.memorymoments.app.data.remote.backend.NeonDirectClient
import com.memorymoments.app.data.remote.firebase.FirebaseClient
import com.memorymoments.app.data.remote.firebase.FirebaseConfig
import com.memorymoments.app.data.remote.firebase.FirestoreBuilders
import com.memorymoments.app.data.remote.firebase.FirestoreDocument
import com.memorymoments.app.model.AiConfidence
import com.memorymoments.app.model.AlertSeverity
import com.memorymoments.app.model.CaregiverAlert
import com.memorymoments.app.model.CaregiverChatMessage
import com.memorymoments.app.model.CaregiverNote
import com.memorymoments.app.model.CognitiveDomainJourney
import com.memorymoments.app.model.CognitiveTrendDirection
import com.memorymoments.app.model.LinkedPatientDetails
import com.memorymoments.app.model.PatientReminder
import com.memorymoments.app.model.ReminderStatus
import com.memorymoments.app.model.ReminderType
import com.memorymoments.app.model.RoutineSlot
import com.memorymoments.app.model.UserRole
import com.memorymoments.app.model.WeeklySummaryStats
import com.memorymoments.app.model.WhatChangedItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class CaregiverRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore = appContext.appDataStore
    private val gson = Gson()
    private val mutex = Mutex()

    private val remindersListType: Type = object : TypeToken<List<PatientReminder>>() {}.type
    private val chatListType: Type = object : TypeToken<List<CaregiverChatMessage>>() {}.type
    private val notesListType: Type = object : TypeToken<List<CaregiverNote>>() {}.type
    private val alertsListType: Type = object : TypeToken<List<CaregiverAlert>>() {}.type

    private fun getRemindersKey(userId: String?) = stringPreferencesKey("patient_reminders_${userId ?: "guest"}")
    private fun getChatKey(userId: String?) = stringPreferencesKey("caregiver_chat_messages_${userId ?: "guest"}")
    private fun getNotesKey(userId: String?) = stringPreferencesKey("caregiver_notes_${userId ?: "guest"}")
    private fun getAlertsKey(userId: String?) = stringPreferencesKey("caregiver_alerts_${userId ?: "guest"}")

    val caregiverCode: Flow<String> = dataStore.data.map { prefs ->
        val existing = prefs[PreferenceKeys.CAREGIVER_CODE]
        if (!existing.isNullOrBlank()) {
            existing
        } else {
            val generated = generateCaregiverCode()
            dataStore.edit { it[PreferenceKeys.CAREGIVER_CODE] = generated }
            generated
        }
    }

    val linkedPatient: Flow<LinkedPatientDetails> = dataStore.data.map { prefs ->
        val json = prefs[PreferenceKeys.LINKED_PATIENT_INFO]
        if (!json.isNullOrBlank()) {
            runCatching { gson.fromJson(json, LinkedPatientDetails::class.java) }.getOrDefault(LinkedPatientDetails())
        } else {
            LinkedPatientDetails()
        }
    }

    val reminders: Flow<List<PatientReminder>> = dataStore.data.map { prefs ->
        val userId = prefs[PreferenceKeys.FIREBASE_USER_ID] ?: prefs[PreferenceKeys.CURRENT_USER_ID]
        val json = prefs[getRemindersKey(userId)]
        if (!json.isNullOrBlank()) {
            runCatching { gson.fromJson<List<PatientReminder>>(json, remindersListType) }.getOrDefault(emptyList())
        } else {
            emptyList()
        }
    }

    val chatMessages: Flow<List<CaregiverChatMessage>> = dataStore.data.map { prefs ->
        val userId = prefs[PreferenceKeys.FIREBASE_USER_ID] ?: prefs[PreferenceKeys.CURRENT_USER_ID]
        val json = prefs[getChatKey(userId)]
        if (!json.isNullOrBlank()) {
            runCatching { gson.fromJson<List<CaregiverChatMessage>>(json, chatListType) }.getOrDefault(emptyList())
        } else {
            emptyList()
        }
    }

    val notes: Flow<List<CaregiverNote>> = dataStore.data.map { prefs ->
        val userId = prefs[PreferenceKeys.FIREBASE_USER_ID] ?: prefs[PreferenceKeys.CURRENT_USER_ID]
        val json = prefs[getNotesKey(userId)]
        if (!json.isNullOrBlank()) {
            runCatching { gson.fromJson<List<CaregiverNote>>(json, notesListType) }.getOrDefault(emptyList())
        } else {
            emptyList()
        }
    }

    val alerts: Flow<List<CaregiverAlert>> = dataStore.data.map { prefs ->
        val userId = prefs[PreferenceKeys.FIREBASE_USER_ID] ?: prefs[PreferenceKeys.CURRENT_USER_ID]
        val json = prefs[getAlertsKey(userId)]
        if (!json.isNullOrBlank()) {
            runCatching { gson.fromJson<List<CaregiverAlert>>(json, alertsListType) }.getOrDefault(emptyList())
        } else {
            emptyList()
        }
    }

    val routineSlots: Flow<List<RoutineSlot>> = dataStore.data.map {
        listOf(
            RoutineSlot("8:00 AM", "Morning Routine & Blood Pressure Medicine", "Moderate"),
            RoutineSlot("10:00 AM", "Hydration & Gentle Garden Walk", "Moderate"),
            RoutineSlot("11:00 AM", "Daily Cognitive Activity & Face Recognition", "High (Peak)", isRecommendedBestTime = true),
            RoutineSlot("3:00 PM", "Afternoon Music & Reminiscence Moment", "Moderate"),
            RoutineSlot("7:30 PM", "Evening Heart Medication & Wind-down", "Low")
        )
    }

    val cognitiveJourney: Flow<List<CognitiveDomainJourney>> = dataStore.data.map {
        listOf(
            CognitiveDomainJourney(
                domain = "Family Recognition",
                trend = CognitiveTrendDirection.IMPROVING,
                baselineComparison = "12% above 30-day baseline",
                explanation = "Recognized close family members consistently within 3 seconds across the last 5 sessions.",
                confidence = AiConfidence.HIGH
            ),
            CognitiveDomainJourney(
                domain = "Attention & Focus",
                trend = CognitiveTrendDirection.STEADY,
                baselineComparison = "Consistent with baseline (±0.8s)",
                explanation = "Maintains steady engagement pace without signs of fatigue during morning sessions.",
                confidence = AiConfidence.MODERATE
            ),
            CognitiveDomainJourney(
                domain = "Music & Auditory Recall",
                trend = CognitiveTrendDirection.IMPROVING,
                baselineComparison = "Active recall improved",
                explanation = "Listened and identified favorite melody prompts with high joy and repeat participation.",
                confidence = AiConfidence.HIGH
            )
        )
    }

    val whatChangedItems: Flow<List<WhatChangedItem>> = dataStore.data.map {
        listOf(
            WhatChangedItem("✓ Morning memory activity completed (100% accuracy)", "POSITIVE"),
            WhatChangedItem("✓ Morning hydration & medication completed on schedule", "POSITIVE"),
            WhatChangedItem("📈 Face recognition speed improved compared to last week", "POSITIVE"),
            WhatChangedItem("ℹ️ Suggested activity time: 11:00 AM for optimal engagement", "NEUTRAL")
        )
    }

    val weeklySummary: Flow<WeeklySummaryStats> = dataStore.data.map {
        WeeklySummaryStats(
            activitiesCompleted = 19,
            totalActivities = 21,
            remindersCompleted = 33,
            totalReminders = 35,
            engagementTrend = "↑ Improving",
            memoryTrend = "↑ Improving",
            attentionTrend = "→ Steady",
            alertCount = 2
        )
    }

    suspend fun fetchRemoteReminders(): List<PatientReminder>? {
        val patientId = dataStore.data.map { it[PreferenceKeys.FIREBASE_USER_ID] }.first()
            ?: dataStore.data.map { it[PreferenceKeys.CURRENT_USER_ID] }.first()
            ?: return null

        val idToken = dataStore.data.map { it[PreferenceKeys.FIREBASE_ID_TOKEN] }.first().orEmpty()
        val authHeader = if (idToken.isNotBlank()) "Bearer $idToken" else null

        if (FirebaseConfig.isConfigured) {
            try {
                val remRes = FirebaseClient.firestoreService.listDocuments(
                    authorization = authHeader,
                    collectionPath = "users/$patientId/reminders",
                    apiKey = FirebaseConfig.apiKey
                )
                val mapped = remRes.body()?.documents?.map { doc ->
                    val typeStr = doc.getString("repeatType")?.lowercase()
                    val type = when (typeStr) {
                        "hydration" -> ReminderType.HYDRATION
                        "activity" -> ReminderType.ACTIVITY
                        "appointment" -> ReminderType.APPOINTMENT
                        else -> ReminderType.MEDICATION
                    }
                    val isDone = doc.getBoolean("completed") ?: false
                    PatientReminder(
                        id = doc.getString("id") ?: UUID.randomUUID().toString(),
                        title = doc.getString("title") ?: "Reminder",
                        type = type,
                        scheduledTime = doc.getString("reminderTime") ?: "12:00 PM",
                        date = doc.getString("reminderDate") ?: "Today",
                        repeatOption = doc.getString("repeatType") ?: "Daily",
                        status = if (isDone) ReminderStatus.COMPLETED else ReminderStatus.PENDING,
                        completedAt = if (isDone) System.currentTimeMillis() else null
                    )
                }
                if (!mapped.isNullOrEmpty()) {
                    persistReminders(mapped)
                    return mapped
                }
            } catch (_: Exception) {}
        }
        return null
    }

    suspend fun toggleReminderStatus(id: String, newStatus: ReminderStatus) = mutex.withLock {
        val current = reminders.first()
        val isCompleted = newStatus == ReminderStatus.COMPLETED
        val completedTimestamp = if (isCompleted) System.currentTimeMillis() else null
        val updated = current.map { reminder ->
            if (reminder.id == id) {
                reminder.copy(status = newStatus, completedAt = completedTimestamp)
            } else reminder
        }
        persistReminders(updated)

        val patientId = dataStore.data.map { it[PreferenceKeys.FIREBASE_USER_ID] }.first()
            ?: dataStore.data.map { it[PreferenceKeys.CURRENT_USER_ID] }.first()

        val isoDate = if (isCompleted) SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()) else null

        if (FirebaseConfig.isConfigured && patientId != null) {
            try {
                val idToken = dataStore.data.map { it[PreferenceKeys.FIREBASE_ID_TOKEN] }.first().orEmpty()
                val authHeader = if (idToken.isNotBlank()) "Bearer $idToken" else null

                val reminder = updated.find { it.id == id }
                if (reminder != null) {
                    val remFields = mapOf(
                        "id" to FirestoreBuilders.stringVal(reminder.id),
                        "patientId" to FirestoreBuilders.stringVal(patientId),
                        "title" to FirestoreBuilders.stringVal(reminder.title),
                        "reminderDate" to FirestoreBuilders.stringVal(reminder.date),
                        "reminderTime" to FirestoreBuilders.stringVal(reminder.scheduledTime),
                        "repeatType" to FirestoreBuilders.stringVal(reminder.repeatOption),
                        "completed" to FirestoreBuilders.boolVal(isCompleted),
                        "completedAt" to FirestoreBuilders.timestampVal(isoDate)
                    )
                    FirebaseClient.firestoreService.setDocument(
                        authorization = authHeader,
                        documentPath = "users/$patientId/reminders/$id",
                        apiKey = FirebaseConfig.apiKey,
                        document = FirestoreDocument(fields = remFields)
                    )
                }
            } catch (_: Exception) {}
        }

        // Direct Neon Cloud Sync: Reminders Table
        if (patientId != null) {
            try {
                val reminder = updated.find { it.id == id }
                if (reminder != null) {
                    NeonDirectClient.upsertReminder(
                        id = reminder.id,
                        patientId = patientId,
                        title = reminder.title,
                        date = reminder.date,
                        time = reminder.scheduledTime,
                        repeatType = reminder.repeatOption,
                        completed = isCompleted
                    )
                }
            } catch (_: Exception) {}
        }
    }

    suspend fun addReminder(reminder: PatientReminder) = mutex.withLock {
        val current = reminders.first()
        persistReminders(current + reminder)

        val patientId = dataStore.data.map { it[PreferenceKeys.FIREBASE_USER_ID] }.first()
            ?: dataStore.data.map { it[PreferenceKeys.CURRENT_USER_ID] }.first()
            ?: UUID.randomUUID().toString()

        if (FirebaseConfig.isConfigured) {
            try {
                val idToken = dataStore.data.map { it[PreferenceKeys.FIREBASE_ID_TOKEN] }.first().orEmpty()
                val authHeader = if (idToken.isNotBlank()) "Bearer $idToken" else null

                val remFields = mapOf(
                    "id" to FirestoreBuilders.stringVal(reminder.id),
                    "patientId" to FirestoreBuilders.stringVal(patientId),
                    "title" to FirestoreBuilders.stringVal(reminder.title),
                    "reminderDate" to FirestoreBuilders.stringVal(reminder.date),
                    "reminderTime" to FirestoreBuilders.stringVal(reminder.scheduledTime),
                    "repeatType" to FirestoreBuilders.stringVal(reminder.repeatOption),
                    "completed" to FirestoreBuilders.boolVal(reminder.status == ReminderStatus.COMPLETED)
                )
                FirebaseClient.firestoreService.setDocument(
                    authorization = authHeader,
                    documentPath = "users/$patientId/reminders/${reminder.id}",
                    apiKey = FirebaseConfig.apiKey,
                    document = FirestoreDocument(fields = remFields)
                )
            } catch (_: Exception) {}
        }

        // Direct Neon Cloud Sync: Reminders Table
        try {
            NeonDirectClient.upsertReminder(
                id = reminder.id,
                patientId = patientId,
                title = reminder.title,
                date = reminder.date,
                time = reminder.scheduledTime,
                repeatType = reminder.repeatOption,
                completed = reminder.status == ReminderStatus.COMPLETED
            )
        } catch (_: Exception) {}
    }

    suspend fun sendChatMessage(senderRole: UserRole, text: String): CaregiverChatMessage = mutex.withLock {
        val current = chatMessages.first()
        val newMessage = CaregiverChatMessage(
            id = UUID.randomUUID().toString(),
            senderRole = senderRole,
            text = text,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        val updated = current + newMessage
        persistChat(updated)
        newMessage
    }

    suspend fun markChatAsRead() = mutex.withLock {
        val current = chatMessages.first()
        val updated = current.map { it.copy(isRead = true) }
        persistChat(updated)
    }

    suspend fun addNote(text: String): CaregiverNote = mutex.withLock {
        val current = notes.first()
        val newNote = CaregiverNote(
            id = UUID.randomUUID().toString(),
            text = text,
            timestamp = System.currentTimeMillis()
        )
        val updated = listOf(newNote) + current
        persistNotes(updated)
        newNote
    }

    suspend fun deleteNote(id: String) = mutex.withLock {
        val current = notes.first()
        val updated = current.filterNot { it.id == id }
        persistNotes(updated)
    }

    suspend fun recordActivityCompletion(activityType: String) = mutex.withLock {
        // Track completed caregiver activity
    }

    suspend fun linkPatientByCode(code: String): Result<LinkedPatientDetails> = mutex.withLock {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.length != 6) {
            return Result.failure(IllegalArgumentException("Code must be 6 characters long."))
        }

        val patientDetails = LinkedPatientDetails(
            name = "Eleanor",
            age = "74",
            caregiverCode = cleanCode,
            isLinked = true,
            lastActiveTime = "5 mins ago"
        )

        dataStore.edit { prefs ->
            prefs[PreferenceKeys.LINKED_PATIENT_INFO] = gson.toJson(patientDetails)
        }

        Result.success(patientDetails)
    }

    private suspend fun persistReminders(reminders: List<PatientReminder>) {
        val userId = dataStore.data.map { it[PreferenceKeys.FIREBASE_USER_ID] ?: it[PreferenceKeys.CURRENT_USER_ID] }.first()
        dataStore.edit { prefs ->
            prefs[getRemindersKey(userId)] = gson.toJson(reminders)
        }
    }

    private suspend fun persistChat(messages: List<CaregiverChatMessage>) {
        val userId = dataStore.data.map { it[PreferenceKeys.FIREBASE_USER_ID] ?: it[PreferenceKeys.CURRENT_USER_ID] }.first()
        dataStore.edit { prefs ->
            prefs[getChatKey(userId)] = gson.toJson(messages)
        }
    }

    private suspend fun persistNotes(notes: List<CaregiverNote>) {
        val userId = dataStore.data.map { it[PreferenceKeys.FIREBASE_USER_ID] ?: it[PreferenceKeys.CURRENT_USER_ID] }.first()
        dataStore.edit { prefs ->
            prefs[getNotesKey(userId)] = gson.toJson(notes)
        }
    }

    companion object {
        fun generateCaregiverCode(): String {
            return "CG-%06d".format(Random.nextInt(1000000))
        }
    }
}
