package com.example.yourroom.model

// ---------------------------------------------------------------------
// DTO DE RESPUESTA DE LOGIN (RESPONSE)
// ---------------------------------------------------------------------

/**
 * Representa la respuesta del backend tras un login correcto.
 *
 * Campos:
 * @param token JWT emitido por el backend. Se usa en el header "Authorization: Bearer <token>"
 *              para autenticar todas las peticiones posteriores.
 * @param userId Identificador del usuario en el backend (se persiste en DataStore).
 *
 * Uso:
 * - Retrofit parsea automáticamente el JSON a esta data class.
 * - Se guarda el token/userId y se encadena el flujo de Firebase (custom token).
 */
data class AuthResponse(
    val token: String,
    val userId: Long
)
