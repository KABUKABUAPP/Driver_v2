package com.kabukabu.driver.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.data.model.SetOnlineStatusRequest
import com.kabukabu.driver.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

enum class OnlineStatus {
    ONLINE, OFFLINE, UNKNOWN
}

class DriverViewModel : ViewModel() {

    private val _onlineStatus = MutableStateFlow(OnlineStatus.UNKNOWN)
    val onlineStatus = _onlineStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun setOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val statusString = if (isOnline) "online" else "offline"
                val response = ApiClient.apiService.setOnlineStatus(
                    SetOnlineStatusRequest(status = statusString)
                )

                if (response.status == "success" && response.data != null) {
                    _onlineStatus.value = if (response.data.onlineStatus == "online") {
                        OnlineStatus.ONLINE
                    } else {
                        OnlineStatus.OFFLINE
                    }
                } else {
                    _error.value = response.message
                }
            } catch (e: Exception) {
                Log.e("DriverViewModel", "Failed to set online status", e)
                _error.value = e.message ?: "An unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
} 