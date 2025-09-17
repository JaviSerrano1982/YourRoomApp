package com.example.yourroom.repository

import com.example.yourroom.model.PhotoRequest
import com.example.yourroom.model.PhotoResponse
import com.example.yourroom.network.PhotoApiService
import javax.inject.Inject

class PhotoRepository @Inject constructor(
    private val api: PhotoApiService
) {
    suspend fun add(spaceId: Long, req: PhotoRequest): PhotoResponse = api.addPhoto(spaceId, req)
    suspend fun list(spaceId: Long): List<PhotoResponse> = api.listPhotos(spaceId)
    suspend fun deleteAll(spaceId: Long) = api.deleteAllPhotos(spaceId)
    suspend fun deleteOne(photoId: Long) = api.deletePhoto(photoId)
}
