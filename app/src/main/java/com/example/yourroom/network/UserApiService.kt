package com.example.yourroom.network



import com.example.yourroom.model.UserProfileDto
import retrofit2.http.*

interface UserApiService {

    @GET("api/profile/{id}")
    suspend fun getUserProfile(@Path("id") userId: Long): UserProfileDto

    @POST("api/profile/{id}")
    suspend fun updateUserProfile(
        @Path("id") userId: Long,
        @Body profile: UserProfileDto
    ): UserProfileDto


}
