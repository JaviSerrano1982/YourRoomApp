package com.example.yourroom.network

import com.example.yourroom.model.SpaceBasicsRequest
import com.example.yourroom.model.SpaceDetailsRequest
import com.example.yourroom.model.SpaceResponse
import retrofit2.http.*

// ------------------------------
// API SERVICE: SpaceApiService
// ------------------------------

/**
 * Interfaz Retrofit para gestionar las salas (espacios).
 *
 * Incluye operaciones CRUD principales:
 * - Crear borradores y espacios.
 * - Actualizar información básica y de detalles.
 * - Consultar salas propias o una sala concreta.
 * - Eliminar una sala.
 */
interface SpaceApiService {

    /**
     * Crea un nuevo espacio con los datos básicos.
     *
     * @param body Objeto con información básica de la sala.
     * @return     La sala creada.
     */
    @POST("api/spaces/basics")
    suspend fun createSpace(@Body body: SpaceBasicsRequest): SpaceResponse

    /**
     * Actualiza los datos básicos de una sala existente.
     *
     * @param id   Id de la sala a actualizar.
     * @param body Objeto con los datos básicos.
     * @return     La sala actualizada.
     */
    @PUT("api/spaces/{id}/basics")
    suspend fun updateBasics(
        @Path("id") id: Long,
        @Body body: SpaceBasicsRequest
    ): SpaceResponse

    /**
     * Actualiza los detalles de una sala existente.
     *
     * @param id   Id de la sala a actualizar.
     * @param body Objeto con los datos de detalle.
     * @return     La sala actualizada.
     */
    @PUT("api/spaces/{id}/details")
    suspend fun updateDetails(
        @Path("id") id: Long,
        @Body body: SpaceDetailsRequest
    ): SpaceResponse

    /**
     * Obtiene una sala concreta por su id.
     * El backend valida que pertenezca al usuario autenticado.
     *
     * @param id Id de la sala.
     * @return   Objeto SpaceResponse con todos los datos.
     */
    @GET("api/spaces/{id}")
    suspend fun getOne(@Path("id") id: Long): SpaceResponse

    /**
     * Obtiene la lista de salas que pertenecen al usuario autenticado.
     *
     * @return Lista de salas propias.
     */
    @GET("api/spaces/me")
    suspend fun getMine(): List<SpaceResponse>

    /**
     * Elimina una sala concreta.
     *
     * @param id Id de la sala a eliminar.
     * @return   Respuesta HTTP vacía (Unit).
     */
    @DELETE("api/spaces/{id}")
    suspend fun deleteSpace(@Path("id") id: Long): retrofit2.Response<Unit>

    /**
     * Crea un borrador de sala vacío (estado inicial).
     *
     * @return La sala en estado borrador.
     */
    @POST("api/spaces/draft")
    suspend fun createDraft(): SpaceResponse

    @PATCH("api/spaces/{id}/publish")
    suspend fun publish(@Path("id") id: Long): SpaceResponse
}
