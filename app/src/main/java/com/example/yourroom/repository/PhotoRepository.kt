package com.example.yourroom.repository

import com.example.yourroom.model.PhotoRequest
import com.example.yourroom.model.PhotoResponse
import com.example.yourroom.network.PhotoApiService
import javax.inject.Inject
import retrofit2.HttpException

// ------------------------------
// REPOSITORY: PhotoRepository
// ------------------------------

/**
 * Repositorio encargado de gestionar la lógica de acceso a datos
 * para las fotos de una sala, utilizando la API de Retrofit.
 *
 * Inyecta automáticamente la dependencia de PhotoApiService.
 */
class PhotoRepository @Inject constructor(
    private val api: PhotoApiService
) {

    /**
     * Añade una nueva foto a una sala.
     *
     * @param spaceId Id de la sala.
     * @param req     Datos de la foto (url y si es principal).
     * @return        La foto creada con toda la información (PhotoResponse).
     */
    suspend fun add(spaceId: Long, req: PhotoRequest): PhotoResponse =
        api.addPhoto(spaceId, req)

    /**
     * Lista todas las fotos de una sala.
     *
     * @param spaceId Id de la sala.
     * @return        Lista de fotos.
     */
    suspend fun list(spaceId: Long): List<PhotoResponse> =
        api.listPhotos(spaceId)

    /**
     * Elimina todas las fotos de una sala.
     *
     * @param spaceId Id de la sala.
     */
    suspend fun deleteAll(spaceId: Long) {
        val res = api.deleteAllPhotos(spaceId)
        if (!res.isSuccessful) throw HttpException(res)
    }

    /**
     * Elimina una foto concreta por su id.
     *
     * @param photoId Id de la foto a eliminar.
     */
    suspend fun deleteOne(photoId: Long) {
        val res = api.deletePhoto(photoId)
        if (!res.isSuccessful) throw HttpException(res)
    }
}
