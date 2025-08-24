package com.example.yourroom.model

// ---------------------------------------------------------------------
// DTO DE PETICIÓN DE LOGIN (REQUEST)
// ---------------------------------------------------------------------

/**
 * Representa el cuerpo (JSON) que se envía al backend
 * cuando el usuario intenta iniciar sesión.
 *
 * Campos:
 * @param email Correo electrónico del usuario.
 * @param password Contraseña del usuario.
 *
 * Uso:
 * - Se pasa como @Body en la llamada Retrofit a /api/users/login.
 */
data class AuthRequest(
    val email: String,
    val password: String
)
