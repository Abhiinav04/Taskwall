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
        
        // New features preferences
        val FONT_FAMILY = stringPreferencesKey("font_family")
        val THEME_COLOR = longPreferencesKey("theme_color")
        val PARTICLE_EFFECTS_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("particle_effects_enabled")
        val ACTIVE_POMODORO_TASK_ID = longPreferencesKey("active_pomodoro_task_id")
        val POMODORO_END_TIME = longPreferencesKey("pomodoro_end_time")
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
    
    val fontFamily: Flow<String?> = dataStore.data.map { it[FONT_FAMILY] }
    val themeColor: Flow<Long?> = dataStore.data.map { it[THEME_COLOR] }
    val particleEffectsEnabled: Flow<Boolean> = dataStore.data.map { it[PARTICLE_EFFECTS_ENABLED] ?: false }
    val activePomodoroTaskId: Flow<Long?> = dataStore.data.map { it[ACTIVE_POMODORO_TASK_ID] }
    val pomodoroEndTime: Flow<Long?> = dataStore.data.map { it[POMODORO_END_TIME] }

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

    suspend fun setFontFamily(font: String?) {
        dataStore.edit { if (font == null) it.remove(FONT_FAMILY) else it[FONT_FAMILY] = font }
    }
    suspend fun setThemeColor(color: Long?) {
        dataStore.edit { if (color == null) it.remove(THEME_COLOR) else it[THEME_COLOR] = color }
    }
    suspend fun setParticleEffectsEnabled(enabled: Boolean) {
        dataStore.edit { it[PARTICLE_EFFECTS_ENABLED] = enabled }
    }
    suspend fun setPomodoroState(taskId: Long?, endTime: Long?) {
        dataStore.edit { 
            if (taskId == null) it.remove(ACTIVE_POMODORO_TASK_ID) else it[ACTIVE_POMODORO_TASK_ID] = taskId
            if (endTime == null) it.remove(POMODORO_END_TIME) else it[POMODORO_END_TIME] = endTime
        }
    }
}
