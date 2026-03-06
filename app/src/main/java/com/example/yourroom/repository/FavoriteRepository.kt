package com.example.yourroom.repository

import com.example.yourroom.model.SpaceResponse
import com.example.yourroom.network.FavoriteApiService
import javax.inject.Inject

class FavoriteRepository @Inject constructor(
    private val api: FavoriteApiService
) {

    suspend fun getFavouriteIds(): List<Long> {
        return api.getFavouriteIds()
    }

    suspend fun getFavourites(): List<SpaceResponse> {
        return api.getFavourites()
    }

    suspend fun addFavourite(spaceId: Long) {
        val response = api.addFavourite(spaceId)
        if (!response.isSuccessful) {
            throw Exception("Error al añadir favorito: ${response.code()}")
        }
    }

    suspend fun removeFavourite(spaceId: Long) {
        val response = api.removeFavourite(spaceId)
        if (!response.isSuccessful) {
            throw Exception("Error al eliminar favorito: ${response.code()}")
        }
    }
}