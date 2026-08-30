package com.memorymoments.app.data.remote.firebase

import com.google.gson.annotations.SerializedName

// --- Firebase Auth DTOs ---

data class FirebaseAuthSignUpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("returnSecureToken") val returnSecureToken: Boolean = true
)

data class FirebaseAuthSignInRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("returnSecureToken") val returnSecureToken: Boolean = true
)

data class FirebaseAuthResponse(
    @SerializedName("localId") val localId: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("idToken") val idToken: String? = null,
    @SerializedName("refreshToken") val refreshToken: String? = null,
    @SerializedName("expiresIn") val expiresIn: String? = null
)

// --- Cloud Firestore DTOs ---

data class FirestoreValue(
    @SerializedName("stringValue") val stringValue: String? = null,
    @SerializedName("integerValue") val integerValue: Long? = null,
    @SerializedName("doubleValue") val doubleValue: Double? = null,
    @SerializedName("booleanValue") val booleanValue: Boolean? = null,
    @SerializedName("timestampValue") val timestampValue: String? = null,
    @SerializedName("mapValue") val mapValue: FirestoreMapValue? = null,
    @SerializedName("arrayValue") val arrayValue: FirestoreArrayValue? = null,
    @SerializedName("nullValue") val nullValue: String? = null
)

data class FirestoreMapValue(
    @SerializedName("fields") val fields: Map<String, FirestoreValue> = emptyMap()
)

data class FirestoreArrayValue(
    @SerializedName("values") val values: List<FirestoreValue> = emptyList()
)

data class FirestoreDocument(
    @SerializedName("name") val name: String? = null,
    @SerializedName("fields") val fields: Map<String, FirestoreValue> = emptyMap(),
    @SerializedName("createTime") val createTime: String? = null,
    @SerializedName("updateTime") val updateTime: String? = null
) {
    fun getString(field: String): String? = fields[field]?.stringValue
    fun getInt(field: String): Int? = fields[field]?.integerValue?.toInt()
    fun getDouble(field: String): Double? = fields[field]?.doubleValue ?: fields[field]?.integerValue?.toDouble()
    fun getBoolean(field: String): Boolean? = fields[field]?.booleanValue
    fun getStringList(field: String): List<String> = fields[field]?.arrayValue?.values?.mapNotNull { it.stringValue } ?: emptyList()
}

data class FirestoreQueryResponse(
    @SerializedName("documents") val documents: List<FirestoreDocument>? = null
)

object FirestoreBuilders {
    fun stringVal(value: String?): FirestoreValue =
        if (value != null) FirestoreValue(stringValue = value) else FirestoreValue(nullValue = "NULL_VALUE")

    fun intVal(value: Int?): FirestoreValue =
        if (value != null) FirestoreValue(integerValue = value.toLong()) else FirestoreValue(nullValue = "NULL_VALUE")

    fun doubleVal(value: Double?): FirestoreValue =
        if (value != null) FirestoreValue(doubleValue = value) else FirestoreValue(nullValue = "NULL_VALUE")

    fun boolVal(value: Boolean?): FirestoreValue =
        if (value != null) FirestoreValue(booleanValue = value) else FirestoreValue(nullValue = "NULL_VALUE")

    fun timestampVal(value: String?): FirestoreValue =
        if (value != null) FirestoreValue(timestampValue = value) else FirestoreValue(nullValue = "NULL_VALUE")

    fun stringListVal(values: List<String>): FirestoreValue =
        FirestoreValue(arrayValue = FirestoreArrayValue(values.map { FirestoreValue(stringValue = it) }))
}
