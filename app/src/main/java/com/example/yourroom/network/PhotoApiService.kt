package com.example.yourroom.network

import com.example.yourroom.model.PhotoRequest
import com.example.yourroom.model.PhotoResponse
import retrofit2.http.*

interface PhotoApiService {
    @POST("api/spaces/{spaceId}/photos")
    suspend fun addPhoto(
        @Path("spaceId") spaceId: Long,
        @Body body: PhotoRequest): PhotoResponse

    @GET("api/spaces/{spaceId}/photos")
    suspend fun listPhotos(@Path("spaceId") spaceId: Long): List<PhotoResponse>

    @DELETE("api/spaces/{spaceId}/photos")
    suspend fun deleteAllPhotos(@Path("spaceId") spaceId: Long)

    @DELETE("api/spaces/photos/{photoId}")
    suspend fun deletePhoto(@Path("photoId") photoId: Long)
}
