package com.memorymoments.app.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.memorymoments.app.data.local.PreferenceKeys
import com.memorymoments.app.data.local.appDataStore
import com.memorymoments.app.data.remote.backend.BackendClient
import com.memorymoments.app.data.remote.backend.NeonDirectClient
import com.memorymoments.app.data.remote.backend.PatientUpsertRequest
import com.memorymoments.app.data.remote.firebase.FirebaseAuthSignInRequest
import com.memorymoments.app.data.remote.firebase.FirebaseAuthSignUpRequest
import com.memorymoments.app.data.remote.firebase.FirebaseClient
import com.memorymoments.app.data.remote.firebase.FirebaseConfig
import com.memorymoments.app.data.remote.firebase.FirestoreBuilders
import com.memorymoments.app.data.remote.firebase.FirestoreDocument
import com.memorymoments.app.model.CaregiverProfile
import com.memorymoments.app.model.PatientProfile
import com.memorymoments.app.model.UserAccount
import com.memorymoments.app.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.regex.Pattern

enum class IdentifierType {
    GMAIL,
    PHONE,
    INVALID
}

class AuthRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore = appContext.appDataStore
    private val gson = Gson()
    private val userAccountListType = object : TypeToken<List<UserAccount>>() {}.type
    private val mutex = Mutex()

    companion object {
        private const val TAG = "FirebaseAuth"
        private val EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", Pattern.CASE_INSENSITIVE)
        private val DOB_REGEX = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$")

        fun getIdentifierType(raw: String): IdentifierType {
            val trimmed = raw.trim()
            if (trimmed.isBlank()) return IdentifierType.INVALID
            if (trimmed.contains("@")) {
                return if (EMAIL_REGEX.matcher(trimmed).matches()) IdentifierType.GMAIL else IdentifierType.INVALID
            }
            val digits = trimmed.filter { it.isDigit() }
            return if (digits.length in 7..15) {
                IdentifierType.PHONE
            } else {
                IdentifierType.INVALID
            }
        }

        fun normalizeIdentifier(raw: String): String {
            val trimmed = raw.trim()
            return if (trimmed.contains("@")) {
                trimmed.lowercase()
            } else {
                trimmed.filter { it.isDigit() }
            }
        }

        fun toFirebaseEmail(raw: String): String {
            val normalized = normalizeIdentifier(raw)
            return if (normalized.contains("@")) {
                normalized
            } else {
                "patient_${normalized}@cogniva.app"
            }
        }

        fun hashPassword(password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }

        fun sanitizeDob(dob: String?): String? {
            if (dob.isNullOrBlank()) return null
            val trimmed = dob.trim()
            return if (DOB_REGEX.matcher(trimmed).matches()) trimmed else null
        }
    }

    val registeredAccounts: Flow<List<UserAccount>> = dataStore.data.map { prefs ->
        val json = prefs[PreferenceKeys.USER_ACCOUNTS]
        decodeAccounts(json)
    }

    val currentUserId: Flow<String?> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.CURRENT_USER_ID]
    }

    val firebaseIdToken: Flow<String?> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.FIREBASE_ID_TOKEN]
    }

    val currentUserRole: Flow<UserRole?> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.CURRENT_USER_ROLE]?.let { roleName ->
            runCatching { UserRole.valueOf(roleName) }.getOrNull()
        }
    }

    val currentUser: Flow<UserAccount?> = combine(
        registeredAccounts,
        currentUserId
    ) { accounts, userId ->
        if (userId.isNullOrBlank()) null else accounts.find { it.id == userId }
    }

    suspend fun register(
        identifier: String,
        password: String,
        role: UserRole
    ): Result<UserAccount> = mutex.withLock {
        val type = getIdentifierType(identifier)
        if (type == IdentifierType.INVALID) {
            return Result.failure(IllegalArgumentException("invalid_identifier"))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("empty_password"))
        }

        val normalized = normalizeIdentifier(identifier)
        val current = registeredAccounts.first()

        if (current.any { it.identifier == normalized }) {
            return Result.failure(IllegalStateException("account_exists"))
        }

        var assignedId = UUID.randomUUID().toString()
        var token = ""

        // 1. Firebase Auth Registration
        Log.i(TAG, "FirebaseConfig.isConfigured = ${FirebaseConfig.isConfigured}")
        Log.i(TAG, "FirebaseConfig.apiKey = '${FirebaseConfig.apiKey.take(10)}...' (length=${FirebaseConfig.apiKey.length})")
        Log.i(TAG, "FirebaseConfig.projectId = '${FirebaseConfig.projectId}'")

        if (FirebaseConfig.isConfigured) {
            try {
                val firebaseEmail = toFirebaseEmail(identifier)
                Log.i(TAG, "Attempting Firebase Auth signUp for email: $firebaseEmail")
                Log.i(TAG, "Using API Key: ${FirebaseConfig.apiKey.take(15)}...")
                val fbResponse = FirebaseClient.authService.signUp(
                    apiKey = FirebaseConfig.apiKey,
                    request = FirebaseAuthSignUpRequest(email = firebaseEmail, password = password)
                )
                Log.i(TAG, "Firebase signUp HTTP response code: ${fbResponse.code()}")
                if (fbResponse.isSuccessful && fbResponse.body() != null) {
                    val fbBody = fbResponse.body()!!
                    val fbUid = fbBody.localId
                    token = fbBody.idToken.orEmpty()
                    if (!fbUid.isNullOrBlank()) {
                        assignedId = fbUid
                        Log.i(TAG, "Firebase Auth signUp SUCCEEDED. Authenticated User UID: $assignedId")
                    }
                    dataStore.edit { prefs ->
                        if (token.isNotBlank()) prefs[PreferenceKeys.FIREBASE_ID_TOKEN] = token
                        prefs[PreferenceKeys.FIREBASE_USER_ID] = assignedId
                    }
                } else {
                    val errorBody = fbResponse.errorBody()?.string()
                    Log.e(TAG, "Firebase Auth signUp FAILED. HTTP ${fbResponse.code()}: $errorBody")
                    if (errorBody?.contains("EMAIL_EXISTS", ignoreCase = true) == true) {
                        return Result.failure(IllegalStateException("account_exists"))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during Firebase Auth signUp: ${e.message}", e)
            }
        } else {
            Log.w(TAG, "Firebase is NOT configured! BuildConfig values may be empty. Signup will only be local.")
        }

        val initialProfile = if (role == UserRole.PATIENT) {
            val displayName = if (normalized.contains("@")) {
                normalized.substringBefore("@").replace(".", " ").split(" ")
                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
            } else {
                "Patient"
            }
            PatientProfile(
                fullName = displayName,
                contactInfo = normalized,
                isCompleted = false
            )
        } else null

        val newAccount = UserAccount(
            id = assignedId,
            identifier = normalized,
            passwordHash = hashPassword(password),
            role = role,
            createdAt = System.currentTimeMillis(),
            patientProfile = initialProfile
        )

        // 2. Create initial patient document in Cloud Firestore
        if (role == UserRole.PATIENT && FirebaseConfig.isConfigured && initialProfile != null) {
            try {
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                val userFields = mapOf(
                    "id" to FirestoreBuilders.stringVal(assignedId),
                    "fullName" to FirestoreBuilders.stringVal(initialProfile.fullName),
                    "email" to FirestoreBuilders.stringVal(if (normalized.contains("@")) normalized else null),
                    "phone" to FirestoreBuilders.stringVal(if (!normalized.contains("@")) normalized else null),
                    "isCompleted" to FirestoreBuilders.boolVal(false),
                    "createdAt" to FirestoreBuilders.timestampVal(isoFormat.format(Date()))
                )
                val authHeader = if (token.isNotBlank()) "Bearer $token" else null
                val firestoreRes = FirebaseClient.firestoreService.setDocument(
                    authorization = authHeader,
                    documentPath = "users/$assignedId",
                    apiKey = FirebaseConfig.apiKey,
                    document = FirestoreDocument(fields = userFields)
                )
                if (firestoreRes.isSuccessful) {
                    Log.i(TAG, "Successfully created initial user document in Cloud Firestore for UID: $assignedId")
                } else {
                    Log.w(TAG, "Firestore create initial doc response: ${firestoreRes.code()} ${firestoreRes.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception creating initial Firestore doc: ${e.message}", e)
            }
        }

        // 3. Sync initial patient profile to Neon PostgreSQL
        if (role == UserRole.PATIENT && initialProfile != null) {
            try {
                val isEmail = normalized.contains("@")
                // Direct HTTPS Cloud Call to Neon
                NeonDirectClient.upsertPatient(
                    id = assignedId,
                    fullName = initialProfile.fullName,
                    email = if (isEmail) normalized else null,
                    phone = if (!isEmail) normalized else null,
                    isCompleted = false
                )
                // FastAPI server call
                BackendClient.api.upsertPatient(
                    PatientUpsertRequest(
                        id = assignedId,
                        fullName = initialProfile.fullName,
                        email = if (isEmail) normalized else null,
                        phone = if (!isEmail) normalized else null,
                        isCompleted = false
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception syncing initial patient to Neon PostgreSQL: ${e.message}", e)
            }
        } else if (role == UserRole.CAREGIVER) {
            try {
                val isEmail = normalized.contains("@")
                NeonDirectClient.upsertCaregiver(
                    id = assignedId,
                    fullName = if (isEmail) normalized.substringBefore("@") else "Caregiver",
                    email = if (isEmail) normalized else null,
                    phone = if (!isEmail) normalized else null,
                    isCompleted = false
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception syncing initial caregiver to Neon: ${e.message}", e)
            }
        }

        persist(current + newAccount)

        dataStore.edit { prefs ->
            prefs[PreferenceKeys.CURRENT_USER_ID] = newAccount.id
            prefs[PreferenceKeys.CURRENT_USER_ROLE] = newAccount.role.name
        }

        Result.success(newAccount)
    }

    suspend fun login(
        identifier: String,
        password: String,
        expectedRole: UserRole? = null
    ): Result<UserAccount> = mutex.withLock {
        val type = getIdentifierType(identifier)
        if (type == IdentifierType.INVALID) {
            return Result.failure(IllegalArgumentException("invalid_identifier"))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("empty_password"))
        }

        val normalized = normalizeIdentifier(identifier)
        val current = registeredAccounts.first()
        var account = current.find { it.identifier == normalized }

        // 1. Authenticate with Firebase Auth
        if (FirebaseConfig.isConfigured) {
            try {
                val firebaseEmail = toFirebaseEmail(identifier)
                Log.i(TAG, "Attempting Firebase Auth signIn for email: $firebaseEmail")
                val fbResponse = FirebaseClient.authService.signInWithPassword(
                    apiKey = FirebaseConfig.apiKey,
                    request = FirebaseAuthSignInRequest(email = firebaseEmail, password = password)
                )
                if (fbResponse.isSuccessful && fbResponse.body() != null) {
                    val fbBody = fbResponse.body()!!
                    val fbUid = fbBody.localId ?: account?.id ?: UUID.randomUUID().toString()
                    val fbToken = fbBody.idToken.orEmpty()
                    Log.i(TAG, "Firebase Auth signIn SUCCEEDED for user UID: $fbUid")

                    dataStore.edit { prefs ->
                        if (fbToken.isNotBlank()) prefs[PreferenceKeys.FIREBASE_ID_TOKEN] = fbToken
                        prefs[PreferenceKeys.FIREBASE_USER_ID] = fbUid
                    }

                    // Pull remote profile details from Cloud Firestore
                    val remoteFbProfile = fetchRemoteFirestoreProfile(fbUid, fbToken)
                    if (account == null) {
                        account = UserAccount(
                            id = fbUid,
                            identifier = normalized,
                            passwordHash = hashPassword(password),
                            role = expectedRole ?: UserRole.PATIENT,
                            patientProfile = remoteFbProfile
                        )
                        persist(current + account)
                    } else {
                        account = account.copy(
                            id = fbUid,
                            passwordHash = hashPassword(password),
                            patientProfile = remoteFbProfile ?: account.patientProfile
                        )
                        persist(current.map { if (it.identifier == normalized) account!! else it })
                    }
                } else {
                    val err = fbResponse.errorBody()?.string()
                    Log.w(TAG, "Firebase Auth signIn error: $err")
                    if (err?.contains("EMAIL_NOT_FOUND", ignoreCase = true) == true) {
                        return Result.failure(IllegalArgumentException("user_not_found"))
                    }
                    if (err?.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) == true ||
                        err?.contains("INVALID_PASSWORD", ignoreCase = true) == true) {
                        return Result.failure(IllegalArgumentException("invalid_credentials"))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during Firebase Auth signIn: ${e.message}", e)
            }
        }

        if (account == null) {
            return Result.failure(IllegalArgumentException("user_not_found"))
        }

        val expectedHash = hashPassword(password)
        if (account.passwordHash != expectedHash) {
            return Result.failure(IllegalArgumentException("invalid_credentials"))
        }

        if (expectedRole != null && account.role != expectedRole) {
            return Result.failure(IllegalArgumentException("role_mismatch"))
        }

        dataStore.edit { prefs ->
            prefs[PreferenceKeys.CURRENT_USER_ID] = account.id
            prefs[PreferenceKeys.CURRENT_USER_ROLE] = account.role.name
        }

        Result.success(account)
    }

    suspend fun updatePatientProfile(profile: PatientProfile): Result<Unit> = mutex.withLock {
        val userId = currentUserId.first() ?: return Result.failure(IllegalStateException("not_logged_in"))
        val current = registeredAccounts.first()
        val account = current.find { it.id == userId } ?: return Result.failure(IllegalStateException("user_not_found"))

        // Update local storage first
        val updated = account.copy(patientProfile = profile)
        persist(current.map { if (it.id == userId) updated else it })

        // Sync to Cloud Firestore
        if (FirebaseConfig.isConfigured) {
            try {
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                val now = isoFormat.format(Date())
                val isEmail = account.identifier.contains("@")
                val token = dataStore.data.map { it[PreferenceKeys.FIREBASE_ID_TOKEN] }.first().orEmpty()
                val authHeader = if (token.isNotBlank()) "Bearer $token" else null

                val userDocFields = mapOf(
                    "id" to FirestoreBuilders.stringVal(userId),
                    "fullName" to FirestoreBuilders.stringVal(profile.fullName.ifBlank { "Patient" }),
                    "dateOfBirth" to FirestoreBuilders.stringVal(sanitizeDob(profile.dateOfBirth)),
                    "age" to FirestoreBuilders.intVal(profile.age.toIntOrNull()),
                    "gender" to FirestoreBuilders.stringVal(profile.gender.ifBlank { null }),
                    "phone" to FirestoreBuilders.stringVal(if (!isEmail) account.identifier else profile.emergencyContactPhone.ifBlank { null }),
                    "email" to FirestoreBuilders.stringVal(if (isEmail) account.identifier else null),
                    "isCompleted" to FirestoreBuilders.boolVal(true),
                    "updatedAt" to FirestoreBuilders.timestampVal(now)
                )

                val docRes = FirebaseClient.firestoreService.setDocument(
                    authorization = authHeader,
                    documentPath = "users/$userId",
                    apiKey = FirebaseConfig.apiKey,
                    document = FirestoreDocument(fields = userDocFields)
                )
                if (docRes.isSuccessful) {
                    Log.i(TAG, "Successfully synced patient profile to Cloud Firestore for UID: $userId")
                } else {
                    Log.e(TAG, "Firestore setDocument failed: Code ${docRes.code()} ${docRes.errorBody()?.string()}")
                }

                // Sync conditions to users/{userId}/conditions subcollection
                for (cond in profile.diagnosedConditions) {
                    val condDocId = UUID.randomUUID().toString()
                    val condFields = mapOf(
                        "id" to FirestoreBuilders.stringVal(condDocId),
                        "patientId" to FirestoreBuilders.stringVal(userId),
                        "condition" to FirestoreBuilders.stringVal(cond),
                        "createdAt" to FirestoreBuilders.timestampVal(now)
                    )
                    FirebaseClient.firestoreService.setDocument(
                        authorization = authHeader,
                        documentPath = "users/$userId/conditions/$condDocId",
                        apiKey = FirebaseConfig.apiKey,
                        document = FirestoreDocument(fields = condFields)
                    )
                }

                // Sync emergency contact to users/{userId}/emergencyContacts subcollection
                if (profile.emergencyContactName.isNotBlank() || profile.emergencyContactPhone.isNotBlank()) {
                    val contactDocId = UUID.randomUUID().toString()
                    val contactFields = mapOf(
                        "id" to FirestoreBuilders.stringVal(contactDocId),
                        "patientId" to FirestoreBuilders.stringVal(userId),
                        "name" to FirestoreBuilders.stringVal(profile.emergencyContactName.ifBlank { "Emergency Contact" }),
                        "relationship" to FirestoreBuilders.stringVal(profile.emergencyContactRelationship.ifBlank { "Family" }),
                        "phone" to FirestoreBuilders.stringVal(profile.emergencyContactPhone.ifBlank { profile.emergencyContact }),
                        "createdAt" to FirestoreBuilders.timestampVal(now)
                    )
                    FirebaseClient.firestoreService.setDocument(
                        authorization = authHeader,
                        documentPath = "users/$userId/emergencyContacts/$contactDocId",
                        apiKey = FirebaseConfig.apiKey,
                        document = FirestoreDocument(fields = contactFields)
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception syncing patient profile to Firestore: ${e.message}", e)
            }
        }

        // Sync to Neon PostgreSQL Database
        try {
            val isEmail = account.identifier.contains("@")
            // Direct Cloud HTTPS Sync
            NeonDirectClient.upsertPatient(
                id = userId,
                fullName = profile.fullName.ifBlank { "Patient" },
                age = profile.age.toIntOrNull(),
                dob = profile.dateOfBirth.ifBlank { null },
                gender = profile.gender.ifBlank { null },
                email = if (isEmail) account.identifier else null,
                phone = if (!isEmail) account.identifier else profile.emergencyContactPhone.ifBlank { null },
                isCompleted = true
            )

            // Direct Neon Sync: Patient Conditions
            for (cond in profile.diagnosedConditions) {
                val condId = UUID.randomUUID().toString()
                NeonDirectClient.upsertCondition(condId, userId, cond)
            }

            // Direct Neon Sync: Emergency Contact
            if (profile.emergencyContactName.isNotBlank() || profile.emergencyContactPhone.isNotBlank()) {
                val contactId = UUID.randomUUID().toString()
                NeonDirectClient.upsertEmergencyContact(
                    id = contactId,
                    patientId = userId,
                    name = profile.emergencyContactName.ifBlank { "Emergency Contact" },
                    relationship = profile.emergencyContactRelationship.ifBlank { "Family" },
                    phone = profile.emergencyContactPhone.ifBlank { profile.emergencyContact }
                )
            }

            // FastAPI backend sync
            BackendClient.api.upsertPatient(
                PatientUpsertRequest(
                    id = userId,
                    fullName = profile.fullName.ifBlank { "Patient" },
                    age = profile.age.toIntOrNull(),
                    dateOfBirth = profile.dateOfBirth.ifBlank { null },
                    gender = profile.gender.ifBlank { null },
                    email = if (isEmail) account.identifier else null,
                    phone = if (!isEmail) account.identifier else profile.emergencyContactPhone.ifBlank { null },
                    isCompleted = true
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception syncing patient profile to Neon PostgreSQL: ${e.message}", e)
        }

        Result.success(Unit)
    }

    suspend fun updateCaregiverProfile(profile: CaregiverProfile): Result<Unit> = mutex.withLock {
        val userId = currentUserId.first() ?: return Result.failure(IllegalStateException("not_logged_in"))
        val current = registeredAccounts.first()
        val account = current.find { it.id == userId } ?: return Result.failure(IllegalStateException("user_not_found"))

        val updated = account.copy(caregiverProfile = profile)
        persist(current.map { if (it.id == userId) updated else it })

        // Direct Cloud Sync to Neon PostgreSQL
        try {
            val isEmail = account.identifier.contains("@")
            NeonDirectClient.upsertCaregiver(
                id = userId,
                fullName = profile.fullName.ifBlank { "Caregiver" },
                email = if (isEmail) account.identifier else null,
                phone = if (!isEmail) account.identifier else null,
                patientRelationship = profile.patientRelationship.ifBlank { "Family" },
                linkedPatientId = profile.patientNameOrCode.ifBlank { null },
                isCompleted = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception syncing caregiver profile to Neon: ${e.message}", e)
        }

        Result.success(Unit)
    }

    suspend fun logout() {
        dataStore.edit { prefs ->
            prefs.remove(PreferenceKeys.CURRENT_USER_ID)
            prefs.remove(PreferenceKeys.CURRENT_USER_ROLE)
            prefs.remove(PreferenceKeys.FIREBASE_ID_TOKEN)
            prefs.remove(PreferenceKeys.FIREBASE_USER_ID)
            prefs.remove(PreferenceKeys.FIREBASE_REFRESH_TOKEN)
        }
    }

    private suspend fun fetchRemoteFirestoreProfile(userId: String, token: String? = null): PatientProfile? {
        if (!FirebaseConfig.isConfigured) return null
        return try {
            val authHeader = if (!token.isNullOrBlank()) "Bearer $token" else null
            val docRes = FirebaseClient.firestoreService.getDocument(
                authorization = authHeader,
                documentPath = "users/$userId",
                apiKey = FirebaseConfig.apiKey
            )
            val doc = docRes.body() ?: return null

            val conditionsRes = FirebaseClient.firestoreService.listDocuments(
                authorization = authHeader,
                collectionPath = "users/$userId/conditions",
                apiKey = FirebaseConfig.apiKey
            )
            val conditions = conditionsRes.body()?.documents?.mapNotNull { it.getString("condition") } ?: emptyList()

            val contactsRes = FirebaseClient.firestoreService.listDocuments(
                authorization = authHeader,
                collectionPath = "users/$userId/emergencyContacts",
                apiKey = FirebaseConfig.apiKey
            )
            val firstContact = contactsRes.body()?.documents?.firstOrNull()

            PatientProfile(
                fullName = doc.getString("fullName") ?: "",
                age = doc.getInt("age")?.toString() ?: "",
                dateOfBirth = doc.getString("dateOfBirth") ?: "",
                gender = doc.getString("gender") ?: "Female",
                contactInfo = doc.getString("email") ?: doc.getString("phone") ?: "",
                diagnosedConditions = conditions,
                emergencyContactName = firstContact?.getString("name") ?: "",
                emergencyContactRelationship = firstContact?.getString("relationship") ?: "",
                emergencyContactPhone = firstContact?.getString("phone") ?: "",
                isCompleted = doc.getBoolean("isCompleted") ?: true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching remote Firestore profile: ${e.message}", e)
            null
        }
    }

    private suspend fun persist(accounts: List<UserAccount>) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.USER_ACCOUNTS] = gson.toJson(accounts)
        }
    }

    private fun decodeAccounts(json: String?): List<UserAccount> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            gson.fromJson<List<UserAccount>>(json, userAccountListType).orEmpty()
        }.getOrDefault(emptyList())
    }
}
