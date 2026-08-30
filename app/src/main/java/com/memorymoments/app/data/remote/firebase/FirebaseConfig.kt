package com.memorymoments.app.data.remote.firebase

import com.memorymoments.app.BuildConfig

/**
 * Firebase Configuration Provider
 *
 * Reads Firebase Web/Android configuration settings from environment variables
 * injected via Gradle BuildConfig.
 */
object FirebaseConfig {
    val apiKey: String get() = BuildConfig.FIREBASE_API_KEY.trim()
    val authDomain: String get() = BuildConfig.FIREBASE_AUTH_DOMAIN.trim()
    val projectId: String get() = BuildConfig.FIREBASE_PROJECT_ID.trim()
    val storageBucket: String get() = BuildConfig.FIREBASE_STORAGE_BUCKET.trim()
    val messagingSenderId: String get() = BuildConfig.FIREBASE_MESSAGING_SENDER_ID.trim()
    val appId: String get() = BuildConfig.FIREBASE_APP_ID.trim()

    val isConfigured: Boolean
        get() = projectId.isNotBlank() && apiKey.isNotBlank() &&
                !projectId.contains("your-project-id") && !apiKey.contains("your-firebase")

    val authBaseUrl: String
        get() = "https://identitytoolkit.googleapis.com/v1/"

    val firestoreBaseUrl: String
        get() = if (projectId.isNotBlank()) {
            "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/"
        } else {
            "https://firestore.googleapis.com/v1/"
        }
}
