package com.memorymoments.app.model

enum class ReminderType(val label: String, val icon: String) {
    MEDICATION("Medication", "💊"),
    HYDRATION("Hydration", "💧"),
    ACTIVITY("Cognitive Activity", "🎮"),
    APPOINTMENT("Appointment", "📅")
}

enum class ReminderStatus {
    UPCOMING,
    PENDING,
    COMPLETED,
    MISSED
}

data class PatientReminder(
    val id: String,
    val title: String,
    val type: ReminderType,
    val scheduledTime: String,
    val date: String = "Today",
    val repeatOption: String = "Daily",
    val status: ReminderStatus = ReminderStatus.PENDING,
    val completedAt: Long? = null
)

data class PatientActivityRecord(
    val id: String,
    val title: String,
    val dateCompleted: String,
    val score: String,
    val category: String = "Memory"
)

data class PatientAssessmentRecord(
    val id: String,
    val title: String,
    val dateCompleted: String,
    val resultSummary: String,
    val scorePercent: Int
)

data class CognitiveAbilityProgress(
    val domain: String,
    val currentPercent: Int,
    val improvementPercent: Int,
    val baselinePercent: Int,
    val statusLabel: String
)

enum class AlertSeverity {
    POSITIVE,
    INFO,
    WARNING
}

enum class AiConfidence {
    HIGH,
    MODERATE,
    LOW
}

data class CaregiverAlert(
    val id: String,
    val title: String,
    val description: String,
    val severity: AlertSeverity,
    val reason: String,
    val confidence: AiConfidence,
    val timestamp: Long = System.currentTimeMillis()
)

enum class CognitiveTrendDirection {
    IMPROVING,
    STEADY,
    SLIGHT_CHANGE
}

data class CognitiveDomainJourney(
    val domain: String,
    val trend: CognitiveTrendDirection,
    val baselineComparison: String,
    val explanation: String,
    val confidence: AiConfidence
)

data class CaregiverChatMessage(
    val id: String,
    val senderRole: UserRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class CaregiverNote(
    val id: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class RoutineSlot(
    val time: String,
    val label: String,
    val engagementLevel: String,
    val isRecommendedBestTime: Boolean = false
)

data class LinkedPatientDetails(
    val name: String = "Eleanor",
    val age: String = "74",
    val caregiverCode: String = "CG-998811",
    val isLinked: Boolean = true,
    val lastActiveTime: String = "15 mins ago"
)

data class WhatChangedItem(
    val text: String,
    val type: String, // "POSITIVE", "ATTENTION", "NEUTRAL"
    val timestamp: String = "Today"
)

data class WeeklySummaryStats(
    val activitiesCompleted: Int = 18,
    val totalActivities: Int = 21,
    val remindersCompleted: Int = 32,
    val totalReminders: Int = 35,
    val engagementTrend: String = "↑ Improving",
    val memoryTrend: String = "↑ Improving",
    val attentionTrend: String = "→ Steady",
    val alertCount: Int = 2
)
