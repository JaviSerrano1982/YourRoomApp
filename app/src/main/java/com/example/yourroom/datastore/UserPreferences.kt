package com.example.yourroom.datastore


import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_prefs")

class UserPreferences(private val context: Context) {
    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[LOGGED_IN] ?: false }
    val userIdFlow: Flow<Long> = context.dataStore.data
        .map { it[USER_ID_KEY] ?: 0L }



    companion object {
        val LOGGED_IN = booleanPreferencesKey("logged_in")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val USER_ID_KEY = longPreferencesKey("user_id")
    }

    suspend fun isUserLoggedIn(): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[LOGGED_IN] ?: false
    }

    suspend fun setUserLoggedIn(loggedIn: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[LOGGED_IN] = loggedIn
        }
    }
    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[AUTH_TOKEN] = token
        }
    }

    suspend fun getAuthToken(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[AUTH_TOKEN]
    }
    suspend fun saveUserId(userId: Long) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }


}