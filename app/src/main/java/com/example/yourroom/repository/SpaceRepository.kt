package com.example.yourroom.repository

import com.example.yourroom.model.SpaceBasicsRequest
import com.example.yourroom.model.SpaceDetailsRequest
import com.example.yourroom.model.SpaceResponse
import com.example.yourroom.network.SpaceApiService
import javax.inject.Inject

// ------------------------------
// REPOSITORY: SpaceRepository
// ------------------------------

/**
 * Repositorio encargado de gestionar la lógica de acceso a datos
 * para las salas (espacios), utilizando la API de Retrofit.
 *
 * Inyecta automáticamente la dependencia de SpaceApiService.
 */
class SpaceRepository @Inject constructor(
    private val api: SpaceApiService
) {

    /**
     * Crea un borrador de sala vacío.
     *
     * @return Sala en estado borrador.
     */
    suspend fun createDraft(): SpaceResponse =
        api.createDraft()

    /**
     * Crea una sala con los datos básicos.
     *
     * @param body Objeto con la información básica.
     * @return     Sala creada.
     */
    suspend fun createSpace(body: SpaceBasicsRequest): SpaceResponse =
        api.createSpace(body)

    /**
     * Actualiza los datos básicos de una sala.
     *
     * @param id   Id de la sala.
     * @param body Datos básicos actualizados.
     * @return     Sala actualizada.
     */
    suspend fun updateBasics(id: Long, body: SpaceBasicsRequest): SpaceResponse =
        api.updateBasics(id, body)

    /**
     * Actualiza los detalles de una sala.
     *
     * @param id   Id de la sala.
     * @param body Datos de detalle actualizados.
     * @return     Sala actualizada.
     */
    suspend fun updateDetails(id: Long, body: SpaceDetailsRequest): SpaceResponse =
        api.updateDetails(id, body)

    /**
     * Obtiene una sala concreta por su id.
     *
     * @param id Id de la sala.
     * @return   Objeto SpaceResponse con los datos de la sala.
     */
    suspend fun getOne(id: Long): SpaceResponse =
        api.getOne(id)

    /**
     * Obtiene la lista de salas propias del usuario autenticado.
     *
     * @return Lista de salas.
     */
    suspend fun getMine(): List<SpaceResponse> =
        api.getMine()

    /**
     * Elimina una sala por su id.
     *
     * @param id Id de la sala.
     * @return   Respuesta HTTP vacía (Unit).
     */
    suspend fun deleteSpace(id: Long): retrofit2.Response<Unit> =
        api.deleteSpace(id)
}
