package com.example.yourroom.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "favorites_prefs")

class FavoriteSpacesStore(private val context: Context) {

    private companion object {
        val KEY_FAVORITE_IDS = stringSetPreferencesKey("favorite_space_ids")
    }

    val favoriteIds: Flow<Set<Long>> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_FAVORITE_IDS]
                ?.mapNotNull { it.toLongOrNull() }
                ?.toSet()
                ?: emptySet()
        }

    suspend fun toggle(spaceId: Long) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_FAVORITE_IDS]?.toMutableSet() ?: mutableSetOf()
            val idStr = spaceId.toString()

            if (current.contains(idStr)) current.remove(idStr) else current.add(idStr)

            prefs[KEY_FAVORITE_IDS] = current
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_FAVORITE_IDS)
        }
    }
}