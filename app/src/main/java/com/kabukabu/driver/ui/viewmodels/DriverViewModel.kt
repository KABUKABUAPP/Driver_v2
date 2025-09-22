package com.kabukabu.driver.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.data.local.UserPreferences
import com.kabukabu.driver.data.model.ActiveTrip
import com.kabukabu.driver.data.model.OnlineStatusRequest
import com.kabukabu.driver.data.model.ProfileResponse
import com.kabukabu.driver.data.model.UserProfile
import com.kabukabu.driver.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import android.util.Log

class DriverViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    private val _isOnline = MutableStateFlow(false)
    val isOnline = _isOnline.asStateFlow()

    private val _activeTrip = MutableStateFlow<ActiveTrip?>(null)
    val activeTrip = _activeTrip.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    init {
        // Fetch early so UI like the drawer can consume cached state immediately
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            val token = userPreferences.authToken.firstOrNull()
            if (token.isNullOrBlank()) {
                Log.e("DriverViewModel", "Cannot fetch profile, token is missing.")
                return@launch
            }
            try {
                Log.d("DriverViewModel", "Fetching user profile with token: $token")
                val profileResponse = ApiClient.authService.getProfile("Bearer $token")
                if (profileResponse.isSuccessful) {
                    val rawResponse = profileResponse.body()?.string()
                    Log.d("DriverViewModel", "User Profile Response: $rawResponse")

                    // Now, parse the response and update the online status
                    if (rawResponse != null) {
                        val moshi = ApiClient.moshi
                        val adapter = moshi.adapter(ProfileResponse::class.java)
                        val parsedResponse = adapter.fromJson(rawResponse)
                        val user = parsedResponse?.data?.user
                        val onlineStatus = user?.onlineStatus
                        _isOnline.value = onlineStatus == "online"
                        Log.d("DriverViewModel", "Driver online status updated to: ${isOnline.value}")

                        // Update active trip
                        _activeTrip.value = parsedResponse?.data?.activeTrip
                        Log.d("DriverViewModel", "Active trip updated: ${_activeTrip.value}")

                        // Update user profile
                        _userProfile.value = user
                        Log.d("DriverViewModel", "User profile updated: ${_userProfile.value}")
                    }
                } else {
                    Log.e("DriverViewModel", "Failed to fetch profile: ${profileResponse.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("DriverViewModel", "Exception fetching profile: ${e.message}", e)
            }
        }
    }

    fun updateOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch {
            val token = userPreferences.authToken.firstOrNull()
            val userId = userPreferences.userId.firstOrNull()

            if (token.isNullOrBlank() || userId.isNullOrBlank()) {
                Log.e("DriverViewModel", "Cannot update status, token or userId is missing.")
                return@launch
            }

            val status = if (isOnline) "online" else "offline"
            try {
                val response = ApiClient.rideService.updateOnlineStatus(
                    token = "Bearer $token",
                    userId = userId,
                    request = OnlineStatusRequest(onlineStatus = status)
                )

                if (response.status == "success") {
                    _isOnline.value = isOnline
                    Log.d("DriverViewModel", "Successfully updated online status to $status")
                } else {
                    Log.e("DriverViewModel", "Failed to update online status: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("DriverViewModel", "Error updating online status: ${e.message}", e)
            }
        }
    }
} 