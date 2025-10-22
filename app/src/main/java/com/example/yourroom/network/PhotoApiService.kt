package com.example.yourroom.network

import com.example.yourroom.model.PhotoRequest
import com.example.yourroom.model.PhotoResponse
import retrofit2.http.*
import retrofit2.Response


// ------------------------------
// API SERVICE: PhotoApiService
// ------------------------------

/**
 * Interfaz Retrofit para gestionar las fotos de una sala.
 *
 * Contiene endpoints para:
 * - Añadir una foto.
 * - Listar fotos.
 * - Eliminar todas las fotos de una sala.
 * - Eliminar una foto específica.
 */
interface PhotoApiService {

    /**
     * Añade una nueva foto a una sala concreta.
     *
     * @param spaceId Id de la sala a la que se añadirá la foto.
     * @param body    Objeto con la URL y si es principal.
     * @return        La foto creada con su información completa.
     */
    @POST("api/spaces/{spaceId}/photos")
    suspend fun addPhoto(
        @Path("spaceId") spaceId: Long,
        @Body body: PhotoRequest
    ): PhotoResponse

    /**
     * Obtiene la lista de fotos asociadas a una sala.
     *
     * @param spaceId Id de la sala.
     * @return        Lista de fotos (PhotoResponse).
     */
    @GET("api/spaces/{spaceId}/photos")
    suspend fun listPhotos(@Path("spaceId") spaceId: Long): List<PhotoResponse>

    /**
     * Elimina todas las fotos asociadas a una sala.
     *
     * @param spaceId Id de la sala.
     */
    @DELETE("api/spaces/{spaceId}/photos")
    suspend fun deleteAllPhotos(@Path("spaceId") spaceId: Long): Response<Unit>

    /**
     * Elimina una foto concreta por su id.
     *
     * @param photoId Id de la foto a eliminar.
     */
    @DELETE("api/spaces/photos/{photoId}")
    suspend fun deletePhoto(@Path("photoId") photoId: Long): Response<Unit>
}
