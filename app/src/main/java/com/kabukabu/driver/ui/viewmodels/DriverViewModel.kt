package com.kabukabu.driver.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.data.model.OnlineStatusRequest
import com.kabukabu.driver.data.remote.ApiClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DriverViewModel : ViewModel() {

    private val userPreferences = KabukabuDriverApp.getInstance().userPreferences

    fun updateOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch {
            val token = userPreferences.authToken.first()
            val userId = userPreferences.userId.first()

            if (token.isNullOrBlank() || userId.isNullOrBlank()) {
                Log.e("DriverViewModel", "Auth token or User ID is missing.")
                // Handle error state, e.g., show a message to the user
                return@launch
            }

            try {
                val status = if (isOnline) "online" else "offline"
                val response = ApiClient.rideService.updateOnlineStatus(
                    token = "Bearer $token",
                    userId = userId,
                    request = OnlineStatusRequest(onlineStatus = status)
                )

                if (response.status == "success") {
                    Log.d("DriverViewModel", "Successfully updated status to $status")
                    // Optionally, update a StateFlow here to notify the UI of success
                } else {
                    Log.e("DriverViewModel", "Failed to update status: ${response.message}")
                    // Handle error state
                }
            } catch (e: Exception) {
                Log.e("DriverViewModel", "Exception when updating status: ${e.message}", e)
                // Handle error state
            }
        }
    }
} 