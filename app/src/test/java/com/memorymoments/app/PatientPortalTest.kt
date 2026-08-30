package com.memorymoments.app

import com.memorymoments.app.model.CognitiveAbilityProgress
import com.memorymoments.app.model.PatientActivityRecord
import com.memorymoments.app.model.PatientAssessmentRecord
import com.memorymoments.app.model.PatientProfile
import com.memorymoments.app.model.PatientReminder
import com.memorymoments.app.model.ReminderStatus
import com.memorymoments.app.model.ReminderType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PatientPortalTest {

    @Test
    fun testPatientProfileWithDiagnosedConditionsAndEmergencyContact() {
        val profile = PatientProfile(
            fullName = "Eleanor Vance",
            age = "74",
            dateOfBirth = "1952-04-15",
            gender = "Female",
            contactInfo = "eleanor.vance@gmail.com",
            diagnosedConditions = listOf("Alzheimer's Disease", "Mild Cognitive Impairment"),
            emergencyContactName = "Sarah Vance",
            emergencyContactRelationship = "Daughter",
            emergencyContactPhone = "555-019283",
            isCompleted = true
        )

        assertEquals("Eleanor Vance", profile.fullName)
        assertEquals("74", profile.age)
        assertEquals("1952-04-15", profile.dateOfBirth)
        assertEquals(2, profile.diagnosedConditions.size)
        assertTrue(profile.diagnosedConditions.contains("Alzheimer's Disease"))
        assertTrue(profile.diagnosedConditions.contains("Mild Cognitive Impairment"))
        assertEquals("Sarah Vance", profile.emergencyContactName)
        assertEquals("Daughter", profile.emergencyContactRelationship)
        assertEquals("555-019283", profile.emergencyContactPhone)
    }

    @Test
    fun testPatientReminderRepeatAndMarkCompleted() {
        val reminder = PatientReminder(
            id = "rem_med_1",
            title = "Morning Blood Pressure Pill",
            type = ReminderType.MEDICATION,
            scheduledTime = "8:00 AM",
            date = "Today",
            repeatOption = "Daily",
            status = ReminderStatus.PENDING
        )

        assertEquals("Today", reminder.date)
        assertEquals("Daily", reminder.repeatOption)
        assertEquals(ReminderStatus.PENDING, reminder.status)

        val completed = reminder.copy(
            status = ReminderStatus.COMPLETED,
            completedAt = System.currentTimeMillis()
        )

        assertEquals(ReminderStatus.COMPLETED, completed.status)
        assertNotNull(completed.completedAt)
    }

    @Test
    fun testCognitiveAbilityProgressImprovementCalculation() {
        val memoryProgress = CognitiveAbilityProgress(
            domain = "Memory",
            currentPercent = 72,
            improvementPercent = 8,
            baselinePercent = 64,
            statusLabel = "+8% improvement"
        )

        val attentionProgress = CognitiveAbilityProgress(
            domain = "Attention",
            currentPercent = 68,
            improvementPercent = 5,
            baselinePercent = 63,
            statusLabel = "+5% improvement"
        )

        assertEquals(72, memoryProgress.currentPercent)
        assertEquals(8, memoryProgress.improvementPercent)
        assertEquals("+8% improvement", memoryProgress.statusLabel)

        assertEquals(68, attentionProgress.currentPercent)
        assertEquals(5, attentionProgress.improvementPercent)
        assertEquals("+5% improvement", attentionProgress.statusLabel)
    }

    @Test
    fun testActivityAndAssessmentRecords() {
        val activity = PatientActivityRecord(
            id = "act_1",
            title = "Who's Who Family Match",
            dateCompleted = "Today, 11:30 AM",
            score = "10 / 10 Correct",
            category = "Memory"
        )

        val assessment = PatientAssessmentRecord(
            id = "ass_1",
            title = "Cognitive Survey",
            dateCompleted = "Aug 28, 2026",
            resultSummary = "Strong recognition",
            scorePercent = 84
        )

        assertEquals("Who's Who Family Match", activity.title)
        assertEquals("10 / 10 Correct", activity.score)
        assertEquals(84, assessment.scorePercent)
    }
}
