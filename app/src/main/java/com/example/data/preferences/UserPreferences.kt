package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "math_master_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val KEY_ACTIVE_USER_ID = stringPreferencesKey("active_user_id")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "SYSTEM", "LIGHT", "DARK"
        private val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        private val KEY_HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        private val KEY_MAXSUS_UNLOCKED = booleanPreferencesKey("maxsus_unlocked")
    }

    val activeUserId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACTIVE_USER_ID]
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE] ?: "SYSTEM"
    }

    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SOUND_ENABLED] ?: true
    }

    val hapticsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HAPTICS_ENABLED] ?: true
    }

    val maxsusUnlocked: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_MAXSUS_UNLOCKED] ?: false
    }

    suspend fun setActiveUserId(userId: String?) {
        context.dataStore.edit { prefs ->
            if (userId != null) {
                prefs[KEY_ACTIVE_USER_ID] = userId
            } else {
                prefs.remove(KEY_ACTIVE_USER_ID)
                prefs.remove(KEY_MAXSUS_UNLOCKED)
            }
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SOUND_ENABLED] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun setMaxsusUnlocked(unlocked: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MAXSUS_UNLOCKED] = unlocked
        }
    }
}
