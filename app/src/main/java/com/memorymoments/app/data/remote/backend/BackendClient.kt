package com.memorymoments.app.data.remote.backend

import com.memorymoments.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

import android.os.Build

/**
 * Singleton Retrofit Client for Cogniva FastAPI / Neon PostgreSQL Backend.
 */
object BackendClient {

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    private fun resolveBaseUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        val isEmulator = Build.FINGERPRINT.startsWith("generic") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.HARDWARE.contains("goldfish") ||
                Build.HARDWARE.contains("ranchu")

        val target = if (!isEmulator && trimmed.contains("10.0.2.2")) {
            "http://127.0.0.1:8000/"
        } else {
            trimmed
        }
        return if (target.endsWith("/")) target else "$target/"
    }

    val api: BackendApi by lazy {
        val baseUrl = resolveBaseUrl(BuildConfig.BACKEND_API_URL)
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BackendApi::class.java)
    }
}
