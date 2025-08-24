package com.example.yourroom.di

import android.content.Context
import com.example.yourroom.datastore.UserPreferences
import com.example.yourroom.network.AuthInterceptor
import com.example.yourroom.network.UserApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

// ---------------------------------------------------------------------
// MÓDULO DE RED (HILT / DI)
// ---------------------------------------------------------------------

/**
 * Módulo de inyección de dependencias (Hilt) que configura y expone:
 * - Retrofit (cliente HTTP principal de la app).
 * - UserApiService (implementación de la interfaz de endpoints de perfil).
 * - UserPreferences (acceso a DataStore para token/userId).
 *
 * Alcance:
 * - @InstallIn(SingletonComponent::class) → instancias únicas a nivel de aplicación.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // -----------------------------------------------------------------
    // PROVEEDOR: RETROFIT (Singleton)
    // -----------------------------------------------------------------

    /**
     * Crea e inyecta una instancia única de [Retrofit], configurada con:
     * - BASE_URL del backend de YourRoom (ambiente de desarrollo).
     * - Cliente OkHttp con interceptores:
     *   • [AuthInterceptor]: añade "Authorization: Bearer <token>" a cada request.
     *   • [HttpLoggingInterceptor]: log de peticiones/respuestas (útil en debug).
     *
     * @param userPreferences acceso a DataStore para obtener el JWT.
     */
    @Provides
    @Singleton
    fun provideRetrofit(userPreferences: UserPreferences): Retrofit {
        // Interceptor de logging (nivel BODY para ver request/response completas).
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Cliente HTTP con:
        // - Interceptor de autenticación (JWT).
        // - Interceptor de logging.
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(userPreferences))
            .addInterceptor(logging)
            .build()

        // Instancia de Retrofit con conversor Gson.
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/") // ← Emulador Android → localhost del PC
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // -----------------------------------------------------------------
    // PROVEEDOR: USER API SERVICE (Singleton)
    // -----------------------------------------------------------------

    /**
     * Expone la implementación de [UserApiService] generada por Retrofit.
     * Esta interfaz define los endpoints relacionados con el perfil de usuario.
     *
     * @param retrofit instancia única creada en [provideRetrofit].
     */
    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

    // -----------------------------------------------------------------
    // PROVEEDOR: USER PREFERENCES (Singleton)
    // -----------------------------------------------------------------

    /**
     * Expone una instancia única de [UserPreferences], envoltorio de DataStore
     * para persistir datos de sesión (JWT, userId, flags de login, etc.).
     *
     * @param context contexto de aplicación inyectado por Hilt.
     */
    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }
}
