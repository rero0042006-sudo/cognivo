import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        localFile.inputStream().use { load(it) }
    }
    val envFile = rootProject.file(".env")
    if (envFile.exists()) {
        envFile.inputStream().use { load(it) }
    }
}
val cloudflareWorkerUrl = localProperties.getProperty(
    "cloudflare.worker.url",
    "https://memory-moments-ai.rero0042006.workers.dev/"
).let { url ->
    if (url.endsWith("/")) url else "$url/"
}
val groqApiKey = localProperties.getProperty("groq.api.key", "")

val firebaseApiKey = localProperties.getProperty("VITE_FIREBASE_API_KEY")
    ?: localProperties.getProperty("firebase.api.key")
    ?: System.getenv("VITE_FIREBASE_API_KEY")
    ?: ""
val firebaseAuthDomain = localProperties.getProperty("VITE_FIREBASE_AUTH_DOMAIN")
    ?: localProperties.getProperty("firebase.auth.domain")
    ?: System.getenv("VITE_FIREBASE_AUTH_DOMAIN")
    ?: ""
val firebaseProjectId = localProperties.getProperty("VITE_FIREBASE_PROJECT_ID")
    ?: localProperties.getProperty("firebase.project.id")
    ?: System.getenv("VITE_FIREBASE_PROJECT_ID")
    ?: ""
val firebaseStorageBucket = localProperties.getProperty("VITE_FIREBASE_STORAGE_BUCKET")
    ?: localProperties.getProperty("firebase.storage.bucket")
    ?: System.getenv("VITE_FIREBASE_STORAGE_BUCKET")
    ?: ""
val firebaseMessagingSenderId = localProperties.getProperty("VITE_FIREBASE_MESSAGING_SENDER_ID")
    ?: localProperties.getProperty("firebase.messaging.sender.id")
    ?: System.getenv("VITE_FIREBASE_MESSAGING_SENDER_ID")
    ?: ""
val firebaseAppId = localProperties.getProperty("VITE_FIREBASE_APP_ID")
    ?: localProperties.getProperty("firebase.app.id")
    ?: System.getenv("VITE_FIREBASE_APP_ID")
    ?: ""

val backendApiUrl = localProperties.getProperty("backend.api.url")
    ?: localProperties.getProperty("BACKEND_API_URL")
    ?: System.getenv("BACKEND_API_URL")
    ?: "http://10.0.2.2:8000/"

val databaseUrl = localProperties.getProperty("DATABASE_URL")
    ?: localProperties.getProperty("database.url")
    ?: System.getenv("DATABASE_URL")
    ?: "postgresql://neondb_owner:npg_NbEjgay1k2Ls@ep-green-sea-axwjg4hg-pooler.c-4.us-east-2.aws.neon.tech/neondb?sslmode=require"

android {
    namespace = "com.memorymoments.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.memorymoments.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "CLOUDFLARE_WORKER_URL", "\"$cloudflareWorkerUrl\"")
        buildConfigField("String", "GROQ_API_KEY", "\"$groqApiKey\"")
        buildConfigField("String", "FIREBASE_API_KEY", "\"$firebaseApiKey\"")
        buildConfigField("String", "FIREBASE_AUTH_DOMAIN", "\"$firebaseAuthDomain\"")
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"$firebaseProjectId\"")
        buildConfigField("String", "FIREBASE_STORAGE_BUCKET", "\"$firebaseStorageBucket\"")
        buildConfigField("String", "FIREBASE_MESSAGING_SENDER_ID", "\"$firebaseMessagingSenderId\"")
        buildConfigField("String", "FIREBASE_APP_ID", "\"$firebaseAppId\"")
        buildConfigField("String", "BACKEND_API_URL", "\"$backendApiUrl\"")
        buildConfigField("String", "NEON_DATABASE_URL", "\"$databaseUrl\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.05.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")
    implementation("androidx.navigation:navigation-compose:2.9.0")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
