package com.memorymoments.app.data.remote.firebase

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FirebaseAuthService {

    @POST("accounts:signUp")
    suspend fun signUp(
        @Query("key") apiKey: String,
        @Body request: FirebaseAuthSignUpRequest
    ): Response<FirebaseAuthResponse>

    @POST("accounts:signInWithPassword")
    suspend fun signInWithPassword(
        @Query("key") apiKey: String,
        @Body request: FirebaseAuthSignInRequest
    ): Response<FirebaseAuthResponse>
}

interface FirestoreService {

    @GET("{documentPath}")
    suspend fun getDocument(
        @Header("Authorization") authorization: String? = null,
        @Path(value = "documentPath", encoded = true) documentPath: String,
        @Query("key") apiKey: String
    ): Response<FirestoreDocument>

    @PATCH("{documentPath}")
    suspend fun setDocument(
        @Header("Authorization") authorization: String? = null,
        @Path(value = "documentPath", encoded = true) documentPath: String,
        @Query("key") apiKey: String,
        @Body document: FirestoreDocument
    ): Response<FirestoreDocument>

    @POST("{collectionPath}")
    suspend fun createDocument(
        @Header("Authorization") authorization: String? = null,
        @Path(value = "collectionPath", encoded = true) collectionPath: String,
        @Query("key") apiKey: String,
        @Query("documentId") documentId: String? = null,
        @Body document: FirestoreDocument
    ): Response<FirestoreDocument>

    @DELETE("{documentPath}")
    suspend fun deleteDocument(
        @Header("Authorization") authorization: String? = null,
        @Path(value = "documentPath", encoded = true) documentPath: String,
        @Query("key") apiKey: String
    ): Response<Unit>

    @GET("{collectionPath}")
    suspend fun listDocuments(
        @Header("Authorization") authorization: String? = null,
        @Path(value = "collectionPath", encoded = true) collectionPath: String,
        @Query("key") apiKey: String
    ): Response<FirestoreQueryResponse>
}
