package com.example.yourroom.network

import com.example.yourroom.model.AuthRequest
import com.example.yourroom.model.AuthResponse
import com.example.yourroom.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface YourRoomApi {

    @POST("/api/users/register")
    suspend fun register(@Body user: User): Response<User>

    @POST("/api/users/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    data class FirebaseTokenResponse(val token: String)

    @GET("/api/auth/firebase-token")
    suspend fun getFirebaseToken(
        @Header("Authorization") bearer: String
    ): FirebaseTokenResponse

}
