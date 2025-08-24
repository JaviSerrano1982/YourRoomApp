package com.example.yourroom.model

// ---------------------------------------------------------------------
// DATA TRANSFER OBJECT (DTO) PARA EL PERFIL DE USUARIO
// ---------------------------------------------------------------------

/**
 * Representa el perfil de usuario tal como se recibe/envía
 * en la comunicación con el backend.
 *
 * Es un DTO (Data Transfer Object):
 * - Solo contiene datos, sin lógica de negocio.
 * - Se usa para transportar información entre la app y la API REST.
 **/
data class UserProfileDto(
    val firstName: String? = null,
    val lastName: String? = null,
    val location: String? = null,
    val gender: String? = null,
    val birthDate: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val photoUrl: String? = null
)

