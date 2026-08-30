package com.memorymoments.app.model

enum class UserRole {
    PATIENT,
    CAREGIVER
}

data class PatientProfile(
    val fullName: String = "",
    val age: String = "",
    val dateOfBirth: String = "",
    val gender: String = "Female",
    val contactInfo: String = "",
    val diagnosedConditions: List<String> = emptyList(),
    val emergencyContactName: String = "",
    val emergencyContactRelationship: String = "",
    val emergencyContactPhone: String = "",
    val language: String = "English",
    val emergencyContact: String = "",
    val caregiverName: String = "",
    val healthContext: String = "",
    val isCompleted: Boolean = false
)

data class CaregiverProfile(
    val fullName: String = "",
    val patientRelationship: String = "",
    val patientNameOrCode: String = "",
    val isCompleted: Boolean = false
)

data class UserAccount(
    val id: String,
    val identifier: String,
    val passwordHash: String,
    val role: UserRole,
    val createdAt: Long = System.currentTimeMillis(),
    val patientProfile: PatientProfile? = null,
    val caregiverProfile: CaregiverProfile? = null
)
