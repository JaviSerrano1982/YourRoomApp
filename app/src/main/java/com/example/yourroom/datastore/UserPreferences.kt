package com.example.yourroom.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// ---------------------------------------------------------------------
// DATASTORE: USER PREFERENCES
// ---------------------------------------------------------------------

/**
 * Clase que encapsula el acceso a DataStore para gestionar las
 * preferencias del usuario relacionadas con la sesión:
 * - Estado de login (true/false).
 * - Token JWT.
 * - ID del usuario.
 *
 * Ventajas:
 * - DataStore es asíncrono, seguro para hilos y reemplazo moderno de SharedPreferences.
 * - Exponer datos como Flow permite a la UI observar cambios en tiempo real.
 */
private val Context.dataStore by preferencesDataStore("user_prefs")

class UserPreferences(private val context: Context) {

    // -----------------------------------------------------------------
    // FLOWS OBSERVABLES
    // -----------------------------------------------------------------

    /** Flow que emite true/false según el estado de login guardado. */
    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[LOGGED_IN] ?: false }

    /** Flow que emite el userId persistido (0L si no existe). */
    val userIdFlow: Flow<Long> = context.dataStore.data
        .map { it[USER_ID_KEY] ?: 0L }

    // -----------------------------------------------------------------
    // CLAVES DE PREFERENCIAS (COMPANION OBJECT)
    // -----------------------------------------------------------------

    companion object {
        /** Clave para persistir estado de login. */
        val LOGGED_IN = booleanPreferencesKey("logged_in")

        /** Clave para persistir el JWT. */
        val AUTH_TOKEN = stringPreferencesKey("auth_token")

        /** Clave para persistir el ID de usuario. */
        val USER_ID_KEY = longPreferencesKey("user_id")
    }

    // -----------------------------------------------------------------
    // MÉTODOS DE ACCESO (SUSPEND)
    // -----------------------------------------------------------------

    /**
     * Devuelve el estado de login actual.
     * @return true si hay sesión iniciada, false si no.
     */
    suspend fun isUserLoggedIn(): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[LOGGED_IN] ?: false
    }

    /**
     * Actualiza el estado de login en DataStore.
     * @param loggedIn true para iniciar sesión, false para cerrar.
     */
    suspend fun setUserLoggedIn(loggedIn: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[LOGGED_IN] = loggedIn
        }
    }

    /**
     * Borra toda la sesión guardada (estado, token, userId).
     */
    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    /**
     * Guarda el JWT del usuario.
     * @param token el JWT recibido del backend.
     */
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[AUTH_TOKEN] = token
        }
    }

    /**
     * Recupera el JWT guardado, o null si no existe.
     */
    suspend fun getAuthToken(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[AUTH_TOKEN]
    }

    /**
     * Guarda el identificador de usuario.
     * @param userId ID proporcionado por el backend.
     */
    suspend fun saveUserId(userId: Long) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }
}
