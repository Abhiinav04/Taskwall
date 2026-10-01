package com.abhinav.taskwall.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppPreferences(private val context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val CURRENT_QUOTE_ID = longPreferencesKey("current_quote_id")
        val LAST_MIDNIGHT_ROLLOVER = longPreferencesKey("last_midnight_rollover")
        val LOCKED_BACKGROUND_PATH = stringPreferencesKey("locked_background_path")
        val IS_24_HOUR = androidx.datastore.preferences.core.booleanPreferencesKey("is_24_hour")
        val SHOW_SECONDS = androidx.datastore.preferences.core.booleanPreferencesKey("show_seconds")
    }

    val currentQuoteId: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[CURRENT_QUOTE_ID]
    }

    val lastMidnightRollover: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[LAST_MIDNIGHT_ROLLOVER]
    }

    val lockedBackgroundPath: Flow<String?> = dataStore.data.map { preferences ->
        preferences[LOCKED_BACKGROUND_PATH]
    }

    val is24Hour: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_24_HOUR] ?: false
    }

    val showSeconds: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_SECONDS] ?: false
    }

    suspend fun setCurrentQuoteId(id: Long) {
        dataStore.edit { preferences ->
            preferences[CURRENT_QUOTE_ID] = id
        }
    }

    suspend fun setLastMidnightRollover(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_MIDNIGHT_ROLLOVER] = timestamp
        }
    }

    suspend fun setLockedBackgroundPath(path: String?) {
        dataStore.edit { preferences ->
            if (path == null) {
                preferences.remove(LOCKED_BACKGROUND_PATH)
            } else {
                preferences[LOCKED_BACKGROUND_PATH] = path
            }
        }
    }
    
    suspend fun set24Hour(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_24_HOUR] = enabled
        }
    }

    suspend fun setShowSeconds(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_SECONDS] = enabled
        }
    }
}
