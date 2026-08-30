package com.memorymoments.app.data.remote.firebase

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Centralized Firebase Client Singleton
 *
 * Provides thread-safe, reusable client instances for Firebase Authentication
 * and Cloud Firestore REST APIs.
 */
object FirebaseClient {

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    val authService: FirebaseAuthService by lazy {
        val baseUrl = if (FirebaseConfig.authBaseUrl.endsWith("/")) {
            FirebaseConfig.authBaseUrl
        } else {
            "${FirebaseConfig.authBaseUrl}/"
        }
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FirebaseAuthService::class.java)
    }

    val firestoreService: FirestoreService by lazy {
        val baseUrl = if (FirebaseConfig.firestoreBaseUrl.endsWith("/")) {
            FirebaseConfig.firestoreBaseUrl
        } else {
            "${FirebaseConfig.firestoreBaseUrl}/"
        }
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FirestoreService::class.java)
    }
}
