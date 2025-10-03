package com.kabukabu.driver.features.home.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.core.data.local.UserPreferences
import com.kabukabu.driver.features.home.data.DeclineTripRequest
import com.kabukabu.driver.core.data.remote.ApiClient
import com.kabukabu.driver.core.data.socket.SocketService
import com.kabukabu.driver.core.utils.SoundPlayer
import com.kabukabu.driver.core.utils.TripUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import android.util.Log

class TripViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<TripUiState>(TripUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _isAccepting = MutableStateFlow(false)
    val isAccepting = _isAccepting.asStateFlow()

    private val _isDeclining = MutableStateFlow(false)
    val isDeclining = _isDeclining.asStateFlow()

    private var countdownJob: Job? = null
    private val userPreferences = UserPreferences(application)

    init {
        viewModelScope.launch {
            SocketService.tripFoundEvent.collect { tripFoundEvent ->
                _uiState.value = TripUiState.TripRequest(tripFoundEvent)
                startCountdown()
            }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel() // Cancel any existing countdown
        countdownJob = viewModelScope.launch {
            for (i in 20 downTo 0) {
                if (_uiState.value is TripUiState.TripRequest) {
                    _uiState.value = (_uiState.value as TripUiState.TripRequest).copy(remainingTime = i)
                }
                delay(1000)
            }
            // If countdown finishes, automatically decline
            if (_uiState.value is TripUiState.TripRequest) {
                declineTrip()
            }
        }
    }

    fun acceptTrip() {
        countdownJob?.cancel()
        SoundPlayer.stopTripAlert()
        viewModelScope.launch {
            _isAccepting.value = true
            try {
                val token = userPreferences.authToken.firstOrNull() ?: ""
                val userId = userPreferences.userId.firstOrNull() ?: ""
                val currentState = _uiState.value as? TripUiState.TripRequest ?: return@launch
                
                Log.d("TripViewModel", "Accepting trip with ID: ${currentState.tripDetails.eventId}")
                
                val response = ApiClient.rideService.acceptTrip(
                    bearerToken = "Bearer $token",
                    rawToken = token,
                    userId = userId,
                    orderId = currentState.tripDetails.eventId
                )
                
                Log.d("TripViewModel", "Trip accepted response: ${response.status}")
                
                if (response.status == "success") {
                    val orderId = currentState.tripDetails.eventId
                    userPreferences.saveActiveOrderId(orderId)
                    
                    Log.d("TripViewModel", "About to join trip room for orderId: $orderId")
                    try {
                        SocketService.joinTripRoom(orderId)
                        Log.d("TripViewModel", "Successfully called joinTripRoom")
                    } catch (e: Exception) {
                        Log.e("TripViewModel", "Error joining trip room: ${e.message}", e)
                    }
                    
                    _uiState.value = TripUiState.TripAccepted(response.message)
                    Log.d("TripViewModel", "Trip accepted successfully with message: ${response.message}")
                } else {
                    _uiState.value = TripUiState.Error("Failed to accept trip: ${response.message}")
                    Log.e("TripViewModel", "Failed to accept trip with status: ${response.status}, message: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("TripViewModel", "Error accepting trip: ${e.message}", e)
                _uiState.value = TripUiState.Error("Failed to accept trip: ${e.message}")
            } finally {
                _isAccepting.value = false
            }
        }
    }

    fun declineTrip() {
        countdownJob?.cancel()
        SoundPlayer.stopTripAlert()
        viewModelScope.launch {
            _isDeclining.value = true
            val currentState = _uiState.value
            if (currentState is TripUiState.TripRequest) {
                try {
                    val rawToken = userPreferences.authToken.firstOrNull()
                    val userId = userPreferences.userId.firstOrNull()
                    if (rawToken == null || userId == null) {
                        _uiState.value = TripUiState.Error("User not authenticated")
                        return@launch
                    }

                    val response = ApiClient.rideService.declineTrip(
                        bearerToken = "Bearer $rawToken",
                        rawToken = rawToken,
                        userId = userId,
                        orderId = currentState.tripDetails.eventId,
                        request = DeclineTripRequest(reasonForCancel = "Not going")
                    )

                    if (response.status == "success") {
                        userPreferences.saveActiveOrderId("") // Clear active order
                        _uiState.value = TripUiState.NoTrip
                    } else {
                        _uiState.value = TripUiState.Error("Failed to decline trip: ${response.message}")
                    }
                } catch (e: Exception) {
                    _uiState.value = TripUiState.Error("Failed to decline trip: ${e.message}")
                }
            }
            _isDeclining.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        SoundPlayer.stopTripAlert()
    }
} 