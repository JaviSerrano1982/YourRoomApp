package com.example.yourroom.network

import com.example.yourroom.datastore.UserPreferences
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

// ---------------------------------------------------------------------
// INTERCEPTOR DE AUTENTICACIÓN (OKHTTP)
// ---------------------------------------------------------------------

/**
 * Interceptor que añade automáticamente el header `Authorization` con
 * el token JWT a todas las peticiones HTTP salientes.
 *
 * Flujo:
 * - Obtiene el token guardado en [UserPreferences].
 * - Si existe, lo añade al header: `Authorization: Bearer <token>`.
 * - Si no existe, la petición continúa sin header de autenticación.
 *
 * Uso:
 * - Se inyecta en el cliente OkHttp usado por Retrofit.
 * - Evita tener que añadir el token manualmente en cada request.
 */
class AuthInterceptor(
    private val userPreferences: UserPreferences
) : Interceptor {

    /**
     * Intercepta cada request saliente y añade el token JWT si está disponible.
     *
     * @param chain Cadena de interceptores de OkHttp.
     * @return Respuesta HTTP tras ejecutar la request modificada.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        // Recuperamos el token desde DataStore (suspend → se fuerza con runBlocking aquí)
        val token = runBlocking { userPreferences.getAuthToken() }


        // Construimos la request con el header Authorization si procede
        val requestBuilder = chain.request().newBuilder()
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        // Continuamos con la request (ya modificada o no)
        return chain.proceed(requestBuilder.build())
    }
}
