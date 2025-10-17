package com.kabukabu.driver.core.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.kabukabu.driver.features.profile.data.ProfileData
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class UserPreferences(private val context: Context) {

    private val dataStore = context.dataStore

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val ACTIVE_ORDER_ID_KEY = stringPreferencesKey("active_order_id")
        private val ONBOARDING_STAGE = intPreferencesKey("onboarding_stage")
        private val USER_DETAILS = stringPreferencesKey("user_details")
        private val FULL_NAME = stringPreferencesKey("full_name")
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

    val fullName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[FULL_NAME]
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

//    val userDetails: Flow<ProfileData?> = context.dataStore.data.map { preferences ->
//        preferences[USER_DETAILS]?.let { json ->
//            val moshi = Moshi.Builder().build()
//            val adapter = moshi.adapter(ProfileData::class.java)
//            adapter.fromJson(json)
//        }
//    }

    /**
     * Save auth token
     */
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun saveFullName(fullName: String) {
        context.dataStore.edit { preferences ->
            preferences[FULL_NAME] = fullName
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


    suspend fun saveUserDetails(userDetails: ProfileData?) {
        val json = Gson().toJson(userDetails)
        context.dataStore.edit { preferences ->
            preferences[USER_DETAILS] = json
        }
    }


    suspend fun getUserDetails(): ProfileData? {
        val json = context.dataStore.data
            .map { preferences -> preferences[USER_DETAILS] }
            .firstOrNull()

        return json?.let {
            Gson().fromJson(it, ProfileData::class.java)
        }
    }

    val userDetails: Flow<ProfileData?> = context.dataStore.data.map { preferences ->
        preferences[USER_DETAILS]?.let { json ->
            Gson().fromJson(json, ProfileData::class.java)
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