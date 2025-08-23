package com.example.yourroom.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// ---------------------------------------------------------------------
// CLIENTE RETROFIT PRINCIPAL
// ---------------------------------------------------------------------

/**
 * Objeto singleton que configura Retrofit para comunicarse con el backend.
 *
 * Responsabilidades:
 * - Define la `BASE_URL` (host + puerto) donde corre el backend.
 * - Configura un cliente OkHttp con logging de peticiones/respuestas.
 * - Expone la implementación de [YourRoomApi] lista para usar.
 *
 * Notas:
 * - `http://10.0.2.2:8080` → dirección especial para acceder al `localhost`
 *   del PC desde el emulador de Android.
 * - Si pruebas en un dispositivo físico, debes cambiarlo por la IP local de tu PC
 *   (ej: `http://192.168.1.34:8080`).
 */
object RetrofitClient {

    /** Dirección base de la API (host + puerto). */
    private const val BASE_URL = "http://10.0.2.2:8080" // localhost para emulador Android

    /** Interceptor de logging: muestra en Logcat el detalle de las peticiones/respuestas. */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /** Cliente HTTP configurado con logging. */
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    /**
     * Implementación de la API de YourRoom creada automáticamente por Retrofit.
     * Se instancia de forma lazy (única instancia compartida).
     */
    val api: YourRoomApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // 👈 aquí Retrofit concatena esta base con los endpoints de YourRoomApi
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create()) // usa Gson para convertir JSON
            .build()
            .create(YourRoomApi::class.java)
    }
}
