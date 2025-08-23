package com.example.yourroom.network

import com.example.yourroom.model.AuthRequest
import com.example.yourroom.model.AuthResponse
import com.example.yourroom.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

// ---------------------------------------------------------------------
// API PRINCIPAL DE YOURROOM (AUTENTICACIÓN Y REGISTRO)
// ---------------------------------------------------------------------

/**
 * Interfaz Retrofit que define las llamadas HTTP a la API de YourRoom
 * relacionadas con el registro, login y obtención del token de Firebase.
 *
 * Notas:
 * - Retrofit implementa automáticamente esta interfaz en tiempo de ejecución.
 * - Todas las funciones son `suspend` para trabajar con corrutinas.
 */
interface YourRoomApi {

    /**
     * Registra un nuevo usuario en el backend.
     *
     * Endpoint: POST /api/users/register
     * @param user objeto [User] con nombre, email, password y rol.
     * @return Respuesta HTTP con el usuario creado (o error).
     */
    @POST("/api/users/register")
    suspend fun register(@Body user: User): Response<User>

    /**
     * Inicia sesión con email y contraseña.
     *
     * Endpoint: POST /api/users/login
     * @param request objeto [AuthRequest] con email y password.
     * @return Respuesta HTTP con [AuthResponse] (incluye JWT y userId).
     */
    @POST("/api/users/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    /**
     * Respuesta al solicitar un token personalizado de Firebase
     * para iniciar sesión en Firebase Auth.
     *
     * El backend valida el JWT y devuelve un Custom Token.
     */
    data class FirebaseTokenResponse(val token: String)

    /**
     * Solicita un Firebase Custom Token al backend.
     *
     * Endpoint: GET /api/auth/firebase-token
     * @param bearer token JWT en el header Authorization (ej: "Bearer eyJ...").
     * @return [FirebaseTokenResponse] con el token de Firebase.
     */
    @GET("/api/auth/firebase-token")
    suspend fun getFirebaseToken(
        @Header("Authorization") bearer: String
    ): FirebaseTokenResponse
}
