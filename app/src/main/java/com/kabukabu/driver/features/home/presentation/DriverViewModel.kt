package com.kabukabu.driver.features.home.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.core.data.local.UserPreferences
import com.kabukabu.driver.features.profile.data.ActiveTrip
import com.kabukabu.driver.features.home.data.OnlineStatusRequest
import com.kabukabu.driver.features.profile.data.ProfileResponse
import com.kabukabu.driver.features.profile.data.UserProfile
import com.kabukabu.driver.core.data.remote.ApiClient
import org.json.JSONObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import android.util.Log
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.features.profile.data.ProfileData

class DriverViewModel(application: Application) : AndroidViewModel(application) {

//    private val userPreferences = UserPreferences(application)

    val userPreferences = KabukabuDriverApp.getInstance().userPreferences


    private val _isOnline = MutableStateFlow(false)
    val isOnline = _isOnline.asStateFlow()

    private val _activeTrip = MutableStateFlow<ActiveTrip?>(null)
    val activeTrip = _activeTrip.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    private val _userDetails = MutableStateFlow<ProfileData?>(null)
    val userDetails = _userProfile.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        // Fetch early so UI like the drawer can consume cached state immediately
        fetchUserProfile()

        viewModelScope.launch {
            userPreferences.userDetails.collect {
                Log.i("DataStoreDebug", "userDetails emitted: $it")
            }
        }

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
                        val userDetails = parsedResponse?.data
                        val onlineStatus = user?.onlineStatus
                        _isOnline.value = onlineStatus == "online"
                        Log.d("DriverViewModel", "Driver online status updated to: ${isOnline.value}")

                        // Update active trip
                        _activeTrip.value = parsedResponse?.data?.activeTrip
                        Log.d("DriverViewModel", "Active trip updated: ${_activeTrip.value}")

                        // Update user profile
//                        _userProfile.value = user

                        _userDetails.value = userDetails
                        userPreferences.saveUserDetails(userDetails)
                        Log.d("DriverViewModel, Datastore", "user details saved to datastore: $userDetails")

                        userPreferences.saveFullName(userDetails?.user?.fullName ?: "")
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

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success") {
                        _isOnline.value = isOnline
                        _errorMessage.value = null // Clear any previous errors
                        Log.d("DriverViewModel", "Successfully updated online status to $status")
                    } else {
                        val message = body?.message ?: "Failed to update online status"
                        _errorMessage.value = message
                        Log.e("DriverViewModel", "Failed to update online status: $message")
                    }
                } else {
                    val errorMessage = parseErrorMessage(response.errorBody()?.string())
                    _errorMessage.value = errorMessage
                    Log.e("DriverViewModel", "Failed to update online status: $errorMessage")
                }
            } catch (e: Exception) {
                // Show network/exception error in snackbar
                _errorMessage.value = e.message ?: "Network error occurred"
                Log.e("DriverViewModel", "Error updating online status: ${e.message}", e)
            }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    private fun parseErrorMessage(rawError: String?): String {
        if (rawError.isNullOrBlank()) return "Failed to update online status"
        return try {
            val json = JSONObject(rawError)
            json.optString("message", rawError)
        } catch (e: Exception) {
            Log.e("DriverViewModel", "Error parsing error message: ${e.message}")
            rawError
        }
    }
}
 