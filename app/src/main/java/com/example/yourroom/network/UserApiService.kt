package com.example.yourroom.network

import com.example.yourroom.model.UserProfileDto
import retrofit2.http.*

// ---------------------------------------------------------------------
// API DE PERFIL DE USUARIO
// ---------------------------------------------------------------------

/**
 * Interfaz Retrofit para gestionar el perfil de usuario.
 *
 * Define los endpoints relacionados con la carga y actualización
 * de la información de perfil en el backend.
 *
 * Notas:
 * - Todas las funciones son `suspend` → se usan dentro de corrutinas.
 * - Retrofit genera automáticamente la implementación en tiempo de ejecución.
 */
interface UserApiService {

    /**
     * Obtiene el perfil de un usuario desde el backend.
     *
     * Endpoint: GET /api/profile/{id}
     * @param userId ID del usuario en el backend.
     * @return [UserProfileDto] con los datos del perfil.
     */
    @GET("api/profile/{id}")
    suspend fun getUserProfile(@Path("id") userId: Long): UserProfileDto

    /**
     * Actualiza el perfil de un usuario en el backend.
     *
     * Endpoint: POST /api/profile/{id}
     * @param userId ID del usuario en el backend.
     * @param profile Datos actualizados del perfil.
     * @return [UserProfileDto] con el perfil tal como quedó guardado en el servidor.
     */
    @POST("api/profile/{id}")
    suspend fun updateUserProfile(
        @Path("id") userId: Long,
        @Body profile: UserProfileDto
    ): UserProfileDto
}
