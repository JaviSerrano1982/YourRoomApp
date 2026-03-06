package com.example.yourroom.network

import com.example.yourroom.model.SpaceResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FavoriteApiService {

    @POST("api/favourites/{spaceId}")
    suspend fun addFavourite(
        @Path("spaceId") spaceId: Long
    ): Response<Unit>

    @DELETE("api/favourites/{spaceId}")
    suspend fun removeFavourite(
        @Path("spaceId") spaceId: Long
    ): Response<Unit>

    @GET("api/favourites/ids")
    suspend fun getFavouriteIds(): List<Long>

    @GET("api/favourites")
    suspend fun getFavourites(): List<SpaceResponse>
}
