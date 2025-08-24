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
    val firstName: String = "",
    val lastName: String = "",
    val location: String = "",
    val gender: String = "",
    val birthDate: String = "",
    val phone: String = "",
    val email: String = "",
    val photoUrl: String = ""
)
