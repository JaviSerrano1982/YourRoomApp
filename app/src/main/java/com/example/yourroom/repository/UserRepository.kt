package com.example.yourroom.repository

import com.example.yourroom.model.UserProfileDto
import com.example.yourroom.network.UserApiService
import javax.inject.Inject

// ---------------------------------------------------------------------
// REPOSITORIO DE USUARIO
// ---------------------------------------------------------------------

/**
 * Capa de acceso a datos del perfil de usuario.
 *
 * Este repositorio actúa como intermediario entre el UserProfileViewModel.
 * y la API remota [UserApiService].
 *
 */
class UserRepository @Inject constructor(
    private val api: UserApiService
) {

    /**
     * Obtiene el perfil de un usuario desde el backend.
     *
     * @param userId ID del usuario en el backend.
     * @return Objeto [UserProfileDto] con la información del perfil.
     */
    suspend fun getProfile(userId: Long): UserProfileDto {
        return api.getUserProfile(userId)
    }

    /**
     * Actualiza el perfil de un usuario en el backend.
     *
     * @param userId ID del usuario en el backend.
     * @param profile Datos actualizados del perfil.
     * @return Objeto [UserProfileDto] con el perfil tal como quedó guardado en el servidor.
     */
    suspend fun updateProfile(userId: Long, profile: UserProfileDto): UserProfileDto {
        return api.updateUserProfile(userId, profile)
    }
}
