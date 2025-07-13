package com.example.yourroom.datastore


import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore("user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val LOGGED_IN = booleanPreferencesKey("logged_in")
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
}