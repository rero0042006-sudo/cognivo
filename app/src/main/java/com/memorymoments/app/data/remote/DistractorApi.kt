package com.memorymoments.app.data.remote

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Streaming

interface DistractorApi {
    @Streaming
    @Headers("Accept: image/jpeg")
    @POST("/")
    suspend fun generateDistractor(
        @Body request: DistractorRequest
    ): ResponseBody
}
