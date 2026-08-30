package com.memorymoments.app

import com.memorymoments.app.model.AiConfidence
import com.memorymoments.app.model.AlertSeverity
import com.memorymoments.app.model.CaregiverAlert
import com.memorymoments.app.model.CaregiverChatMessage
import com.memorymoments.app.model.CognitiveDomainJourney
import com.memorymoments.app.model.CognitiveTrendDirection
import com.memorymoments.app.model.PatientReminder
import com.memorymoments.app.model.ReminderStatus
import com.memorymoments.app.model.ReminderType
import com.memorymoments.app.model.UserRole
import com.memorymoments.app.repository.CaregiverRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CaregiverModuleTest {

    @Test
    fun testCaregiverCodeGeneration() {
        val code = CaregiverRepository.generateCaregiverCode()
        assertNotNull(code)
        assertTrue("Caregiver code must match CG-XXXXXX format: $code", code.matches(Regex("^CG-\\d{6}$")))
    }

    @Test
    fun testPatientReminderStatusTransition() {
        val reminder = PatientReminder(
            id = "rem_1",
            title = "Morning Blood Pressure Pill",
            type = ReminderType.MEDICATION,
            scheduledTime = "8:00 AM",
            status = ReminderStatus.PENDING
        )

        assertEquals(ReminderStatus.PENDING, reminder.status)

        val completedReminder = reminder.copy(
            status = ReminderStatus.COMPLETED,
            completedAt = System.currentTimeMillis()
        )

        assertEquals(ReminderStatus.COMPLETED, completedReminder.status)
        assertNotNull(completedReminder.completedAt)
    }

    @Test
    fun testChatMessageCreationAndRoles() {
        val caregiverMsg = CaregiverChatMessage(
            id = "msg_cg",
            senderRole = UserRole.CAREGIVER,
            text = "Good morning Eleanor! How are you feeling today?",
            timestamp = System.currentTimeMillis()
        )

        val patientMsg = CaregiverChatMessage(
            id = "msg_pt",
            senderRole = UserRole.PATIENT,
            text = "I'm feeling good, just finished my morning walk.",
            timestamp = System.currentTimeMillis() + 1000
        )

        assertEquals(UserRole.CAREGIVER, caregiverMsg.senderRole)
        assertEquals(UserRole.PATIENT, patientMsg.senderRole)
        assertTrue(patientMsg.timestamp >= caregiverMsg.timestamp)
    }

    @Test
    fun testCognitiveJourneyBaselineConfidence() {
        val journey = CognitiveDomainJourney(
            domain = "Family Recognition",
            trend = CognitiveTrendDirection.IMPROVING,
            baselineComparison = "12% above 30-day baseline",
            explanation = "Recognized close family portraits within 3 seconds.",
            confidence = AiConfidence.HIGH
        )

        assertEquals(CognitiveTrendDirection.IMPROVING, journey.trend)
        assertEquals(AiConfidence.HIGH, journey.confidence)
        assertTrue(journey.baselineComparison.contains("baseline"))
    }

    @Test
    fun testCaregiverAlertSeverityAndExplainability() {
        val alert = CaregiverAlert(
            id = "alert_1",
            title = "Routine Engagement Pattern",
            description = "Engagement consistently peak at 11:00 AM.",
            severity = AlertSeverity.INFO,
            reason = "Comparing reaction times over 14 daily windows.",
            confidence = AiConfidence.HIGH
        )

        assertEquals(AlertSeverity.INFO, alert.severity)
        assertTrue(alert.reason.isNotBlank())
    }
}
