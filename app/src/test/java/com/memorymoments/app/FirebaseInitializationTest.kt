package com.memorymoments.app

import com.memorymoments.app.data.remote.firebase.FirebaseClient
import com.memorymoments.app.data.remote.firebase.FirebaseConfig
import org.junit.Assert.assertNotNull
import org.junit.Test

class FirebaseInitializationTest {

    @Test
    fun testFirebaseClientInitialization() {
        // Verify Firebase configuration can be read
        assertNotNull(FirebaseConfig.authBaseUrl)
        assertNotNull(FirebaseConfig.firestoreBaseUrl)

        // Verify Firebase services can be instantiated via singleton client
        val authService = FirebaseClient.authService
        assertNotNull(authService)

        val firestoreService = FirebaseClient.firestoreService
        assertNotNull(firestoreService)
    }
}
