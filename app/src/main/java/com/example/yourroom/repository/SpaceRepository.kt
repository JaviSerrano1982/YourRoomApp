package com.example.yourroom.repository

import com.example.yourroom.model.SpaceBasicsRequest
import com.example.yourroom.model.SpaceDetailsRequest
import com.example.yourroom.model.SpaceResponse
import com.example.yourroom.network.SpaceApiService
import javax.inject.Inject


class SpaceRepository @Inject constructor(
    private val api: SpaceApiService
) {
    suspend fun createDraft(): SpaceResponse = api.createDraft()
    suspend fun createSpace(body: SpaceBasicsRequest): SpaceResponse = api.createSpace(body)
    suspend fun updateBasics(id: Long, body: SpaceBasicsRequest): SpaceResponse = api.updateBasics(id, body)
    suspend fun updateDetails(id: Long, body: SpaceDetailsRequest): SpaceResponse = api.updateDetails(id, body)
    suspend fun getOne(id: Long): SpaceResponse = api.getOne(id)
    suspend fun getMine(): List<SpaceResponse> = api.getMine()
    suspend fun deleteSpace(id: Long): retrofit2.Response<Unit> {
        return api.deleteSpace(id)
    }
}