package com.example.yourroom.model

// ---------------------------------------------------------------------
// MODELO DE USUARIO (DTO PARA REGISTRO/AUTENTICACIÓN)
// ---------------------------------------------------------------------

/**
 * Representa a un usuario en la app para operaciones de
 * registro, login y persistencia básica.
 *
 * Notas:
 * - Se usa como DTO (Data Transfer Object) para intercambiar datos con la API.
 * - No contiene lógica de negocio; solo estructura de datos.
 *
 * Campos:
 * @param id Identificador único del usuario (nullable porque el backend lo asigna).
 * @param name Nombre del usuario.
 * @param email Correo electrónico del usuario.
 * @param password Contraseña en texto plano al crear/enviar al backend (el backend debe encriptarla).
 * @param role Rol del usuario dentro de la app (por defecto "USUARIO").
 */
data class User(
    val id: Long? = null,
    val name: String,
    val email: String,
    val password: String,
    val role: String = "USUARIO"
)
