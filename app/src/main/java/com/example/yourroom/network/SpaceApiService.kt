package com.example.yourroom.network

import com.example.yourroom.model.SpaceBasicsRequest
import com.example.yourroom.model.SpaceDetailsRequest
import com.example.yourroom.model.SpaceResponse
import retrofit2.http.*


interface SpaceApiService {


    // Crea un espacio con datos de Básicos
    @POST("api/spaces/basics")
    suspend fun createSpace(@Body body: SpaceBasicsRequest): SpaceResponse


    // Actualiza Básicos
    @PUT("api/spaces/{id}/basics")
    suspend fun updateBasics(
        @Path("id") id: Long,
        @Body body: SpaceBasicsRequest
    ): SpaceResponse


    // Actualiza Detalles
    @PUT("api/spaces/{id}/details")
    suspend fun updateDetails(
        @Path("id") id: Long,
        @Body body: SpaceDetailsRequest
    ): SpaceResponse


    // Obtiene un espacio (del owner autenticado)
    @GET("api/spaces/{id}")
    suspend fun getOne(@Path("id") id: Long): SpaceResponse


    // Lista mis espacios
    @GET("api/spaces/me")
    suspend fun getMine(): List<SpaceResponse>

    @DELETE("api/spaces/{id}")
    suspend fun deleteSpace(@Path("id") id: Long): retrofit2.Response<Unit>

}