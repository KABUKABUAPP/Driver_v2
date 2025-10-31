package com.kabukabu.driver.core.data.local

import android.R.attr.tag
import android.annotation.SuppressLint
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
        }
    }

    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
