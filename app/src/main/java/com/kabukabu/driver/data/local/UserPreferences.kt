package com.kabukabu.driver.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Class that handles saving and retrieving user preferences
 */
class UserPreferences(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    /**
     * Get the auth token flow
     */
    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    /**
     * Get the user email flow
     */
    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }

    /**
     * Get the user ID flow
     */
    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    /**
     * Save auth token
     */
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    /**
     * Save user email
     */
    suspend fun saveUserEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_EMAIL_KEY] = email
        }
    }

    /**
     * Save user ID
     */
    suspend fun saveUserId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = id
        }
    }

    /**
     * Clear all user data
     */
    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
} 