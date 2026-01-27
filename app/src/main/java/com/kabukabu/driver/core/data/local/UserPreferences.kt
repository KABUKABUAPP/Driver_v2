package com.kabukabu.driver.core.data.local

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.kabukabu.driver.features.profile.data.ProfileData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences private constructor(private val context: Context) {

    companion object {

        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

        // Preference keys
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val ACTIVE_ORDER_ID_KEY = stringPreferencesKey("active_order_id")
        private val ONBOARDING_STAGE = intPreferencesKey("onboarding_stage")
        private val USER_DETAILS = stringPreferencesKey("user_details")
        private val FULL_NAME = stringPreferencesKey("full_name")
        // Chat head overlay toggle
        private val CHAT_HEAD_ENABLED = booleanPreferencesKey("chat_head_enabled")
        // Overlay permission requested flag (when we direct the user to settings)
        private val OVERLAY_PERMISSION_REQUESTED = booleanPreferencesKey("overlay_permission_requested")
        // Boot start opt-in
        private val BOOT_START_ENABLED = booleanPreferencesKey("boot_start_enabled")
        // OneSignal Player ID
        private val ONESIGNAL_PLAYER_ID = stringPreferencesKey("onesignal_player_id")
        // Active Support ID for notification handling
        private val ACTIVE_SUPPORT_ID = stringPreferencesKey("active_support_id")

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: UserPreferences? = null

        fun getInstance(context: Context): UserPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }


    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }

    val fullName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[FULL_NAME]
    }

    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    val activeOrderId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACTIVE_ORDER_ID_KEY]
    }

    val onboardingStep: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_STAGE]
    }

    val userDetails: Flow<ProfileData?> = context.dataStore.data.map { preferences ->
        preferences[USER_DETAILS]?.let { json ->
            Gson().fromJson(json, ProfileData::class.java)
        }
    }

    // Flow for chat head overlay enabled flag
    val chatHeadEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CHAT_HEAD_ENABLED] ?: true
    }

    // New flow to track if we directed the user to overlay settings
    val overlayPermissionRequested: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[OVERLAY_PERMISSION_REQUESTED] ?: false
    }

    // Boot-start opt-in flow
    val bootStartEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[BOOT_START_ENABLED] ?: false
    }

    // OneSignal Player ID flow
    val oneSignalPlayerId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ONESIGNAL_PLAYER_ID]
    }

    // Active Support ID flow
    val activeSupportId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACTIVE_SUPPORT_ID]
    }


    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            Log.d("UserPref", "succ saved token, ${token.take(10)}")
        }
    }

    suspend fun saveFullName(fullName: String) {
        context.dataStore.edit { preferences ->
            preferences[FULL_NAME] = fullName
        }
    }

    suspend fun saveUserEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_EMAIL_KEY] = email
        }
    }

    suspend fun saveUserId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = id
        }
    }

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

    suspend fun saveUserDetails(userDetails: ProfileData?) {
        val json = Gson().toJson(userDetails)
        context.dataStore.edit { preferences ->
            preferences[USER_DETAILS] = json
        }
    }

    suspend fun clearUserDetails() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_DETAILS)
            preferences.remove(USER_ID_KEY)
        }
    }

    // Save chat head enabled flag
    suspend fun saveChatHeadEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CHAT_HEAD_ENABLED] = enabled
        }
    }

    // Save overlay permission requested flag
    suspend fun saveOverlayPermissionRequested(requested: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[OVERLAY_PERMISSION_REQUESTED] = requested
        }
    }

    // Save boot start enabled flag
    suspend fun saveBootStartEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BOOT_START_ENABLED] = enabled
        }
    }

    // Save OneSignal Player ID
    suspend fun saveOneSignalPlayerId(playerId: String) {
        context.dataStore.edit { preferences ->
            preferences[ONESIGNAL_PLAYER_ID] = playerId
        }
    }

    // Update active support ID
    suspend fun updateActiveSupportId(supportId: String?) {
        context.dataStore.edit { preferences ->
            if (supportId != null) {
                preferences[ACTIVE_SUPPORT_ID] = supportId
            } else {
                preferences.remove(ACTIVE_SUPPORT_ID)
            }
        }
    }

    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
