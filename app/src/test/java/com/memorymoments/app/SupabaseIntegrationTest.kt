package com.memorymoments.app

import com.google.gson.GsonBuilder
import com.memorymoments.app.data.remote.firebase.FirebaseAuthResponse
import com.memorymoments.app.data.remote.firebase.FirestoreBuilders
import com.memorymoments.app.data.remote.firebase.FirestoreDocument
import com.memorymoments.app.repository.AuthRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseIntegrationTest {

    private val gson = GsonBuilder().disableHtmlEscaping().create()

    @Test
    fun testFirebaseEmailConversion() {
        val email = "eleanor.vance@gmail.com"
        val phone = "9876543210"

        assertEquals("eleanor.vance@gmail.com", AuthRepository.toFirebaseEmail(email))
        assertEquals("patient_9876543210@cogniva.app", AuthRepository.toFirebaseEmail(phone))
    }

    @Test
    fun testFirebaseAuthResponseSerialization() {
        val json = """
            {
                "localId": "firebase-uid-1234",
                "email": "test@cogniva.app",
                "idToken": "jwt-token-abcd",
                "refreshToken": "refresh-token-1234",
                "expiresIn": "3600"
            }
        """.trimIndent()

        val response = gson.fromJson(json, FirebaseAuthResponse::class.java)
        assertEquals("firebase-uid-1234", response.localId)
        assertEquals("test@cogniva.app", response.email)
        assertEquals("jwt-token-abcd", response.idToken)
    }

    @Test
    fun testFirestoreDocumentBuildersAndAccessors() {
        val fields = mapOf(
            "fullName" to FirestoreBuilders.stringVal("Eleanor Vance"),
            "age" to FirestoreBuilders.intVal(74),
            "score" to FirestoreBuilders.doubleVal(0.95),
            "isCompleted" to FirestoreBuilders.boolVal(true),
            "conditions" to FirestoreBuilders.stringListVal(listOf("Alzheimer's", "Hypertension"))
        )

        val doc = FirestoreDocument(
            name = "projects/cogniva-772ce/databases/(default)/documents/users/user123",
            fields = fields
        )

        assertEquals("Eleanor Vance", doc.getString("fullName"))
        assertEquals(74, doc.getInt("age"))
        assertEquals(0.95, doc.getDouble("score") ?: 0.0, 0.001)
        assertEquals(true, doc.getBoolean("isCompleted"))
        assertEquals(listOf("Alzheimer's", "Hypertension"), doc.getStringList("conditions"))

        val json = gson.toJson(doc)
        assertTrue(json.contains("Eleanor Vance"))
        assertTrue(json.contains("74"))
    }
}
