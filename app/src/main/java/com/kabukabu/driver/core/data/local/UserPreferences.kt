package com.kabukabu.driver.core.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kabukabu.driver.features.profile.data.ProfileData
import com.squareup.moshi.Moshi
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
        private val ACTIVE_ORDER_ID_KEY = stringPreferencesKey("active_order_id")
        private val ONBOARDING_STAGE = intPreferencesKey("onboarding_stage")
        private val USER_DETAILS = stringPreferencesKey("user_details")
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
     * Get the active order ID flow
     */
    val activeOrderId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACTIVE_ORDER_ID_KEY]
    }
    /**
     * Get the user's onboarding stage (range is 1-4)
     */
    val onboardingStep: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_STAGE]
    }

    val userDetails: Flow<ProfileData?> = context.dataStore.data.map { preferences ->
        preferences[USER_DETAILS]?.let { json ->
            val moshi = Moshi.Builder().build()
            val adapter = moshi.adapter(ProfileData::class.java)
            adapter.fromJson(json)
        }
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
     * Save active order ID
     */
    suspend fun saveActiveOrderId(orderId: String) {
        context.dataStore.edit { preferences ->
            preferences[ACTIVE_ORDER_ID_KEY] = orderId
        }
    }

    suspend fun saveOnboardingStep(step: Int) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_STAGE] = step
        }
    }


    suspend fun saveUserDetails(profileResponse: ProfileData?) {
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(ProfileData::class.java)
        val json = adapter.toJson(profileResponse)

        context.dataStore.edit { preferences ->
            preferences[USER_DETAILS] = json
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