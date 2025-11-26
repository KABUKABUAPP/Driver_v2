package com.kabukabu.driver.features.home.presentation.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
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
import com.kabukabu.driver.core.utils.DistanceMatrixHelper
import kotlinx.coroutines.flow.StateFlow

class TripViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<TripUiState>(TripUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _isAccepting = MutableStateFlow(false)
    val isAccepting = _isAccepting.asStateFlow()

    private val _isDeclining = MutableStateFlow(false)
    val isDeclining = _isDeclining.asStateFlow()

    private var countdownJob: Job? = null
    private val userPreferences = UserPreferences.getInstance(application.applicationContext)

    private val _distanceInfo = MutableStateFlow<DistanceInfo?>(null)
    val distanceInfo = _distanceInfo.asStateFlow()

    // Access to LocationRepository for driver location
    private val locationRepository = com.kabukabu.driver.core.data.location.LocationRepository.getInstance(application.applicationContext)
    val driverLocation: StateFlow<Location?> = locationRepository.currentLocation

    init {
        // Listen for trip found events
        viewModelScope.launch {
            SocketService.tripFoundEvent.collect { tripFoundEvent ->
                // Set trip request state

                // Automatically calculate distance and time to rider using location from repository
                val currentDriverLocation = driverLocation.value
                if (currentDriverLocation != null && tripFoundEvent.startPoint.size >= 2) {
                    Log.d("TripViewModel", "Calculating distance to rider automatically")
                    calculateTripDistance(currentDriverLocation, tripFoundEvent.startPoint)
                } else {
                    Log.w("TripViewModel", "Cannot calculate distance - driver location: $currentDriverLocation, startPoint size: ${tripFoundEvent.startPoint.size}")
                }
                _uiState.value = TripUiState.TripRequest(tripFoundEvent)

                startCountdown()
            }
        }

        // Listen for trip cancelled events
        viewModelScope.launch {
            SocketService.tripCancelledEvent.collect { tripCancelledEvent ->
                handleTripCancelled(tripCancelledEvent)
            }
        }
    }


  suspend  fun calculateTripDistance(driverLocation: Location, riderLocation: List<Double>) {
      val response = DistanceMatrixHelper.calculateDistanceAndTime(
          originLat = driverLocation.latitude,
          originLng = driverLocation.longitude,
          destLat = riderLocation[1],
          destLng = riderLocation[0]
      )

      response?.let {
          _distanceInfo.value = DistanceInfo(
              distanceText = DistanceMatrixHelper.getDistanceText(it) ?: "Unknown",
              distanceMeters = DistanceMatrixHelper.getDistanceInMeters(it) ?: 0,
              durationText = DistanceMatrixHelper.getDurationText(it) ?: "Unknown",
              durationSeconds = DistanceMatrixHelper.getDurationInSeconds(it) ?: 0
          )
      }
    }

    private fun startCountdown() {
        countdownJob?.cancel() // Cancel any existing countdown
        countdownJob = viewModelScope.launch {
            for (i in 15 downTo 0) {
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

    fun acceptTrip(driverViewModel: DriverViewModel) {
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
                    //i need to fetch profile to get active trip

                    val profileFetchedSuccessfully = driverViewModel.awaitableFetchUserProfile()

                    if (profileFetchedSuccessfully) {
                        Log.d("TripViewModel", "Profile fetched. Now joining trip room for orderId: $orderId")
                        SocketService.joinTripRoom(orderId)
                        _uiState.value = TripUiState.TripAccepted(response.message)
                    } else {
                        // Handle the case where fetching the profile failed after accepting the trip.
                        _uiState.value = TripUiState.Error("Trip accepted, but failed to refresh profile.")
                        Log.e("TripViewModel", "Trip accepted, but awaitableFetchUserProfile failed.")
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
//                        val profileFetchedSuccessfully = driverViewModel.awaitableFetchUserProfile()
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

    /**
     * Handle trip cancelled event from socket
     */
    private fun handleTripCancelled(tripCancelledEvent: com.kabukabu.driver.core.data.socket.TripCancelledEvent) {
        Log.d("TripViewModel", "Trip cancelled: Trip ID: ${tripCancelledEvent.order.id}, Status: ${tripCancelledEvent.status}")

        // Stop any ongoing countdown
        countdownJob?.cancel()
        SoundPlayer.stopTripAlert()

        // Update UI state to show cancellation
        val message = "Trip has been cancelled"

        _uiState.value = TripUiState.Error(message)

        // Clear active order ID
        viewModelScope.launch {
            userPreferences.saveActiveOrderId("")
            Log.d("TripViewModel", "Cleared active order ID for trip: ${tripCancelledEvent.order.id}")
        }
    }

    /**
     * Handle trip cancelled and refresh driver profile
     * Call this from UI when you have access to DriverViewModel
     */
    fun handleTripCancelledWithProfileRefresh(
        tripCancelledEvent: com.kabukabu.driver.core.data.socket.TripCancelledEvent,
        driverViewModel: DriverViewModel
    ) {
        handleTripCancelled(tripCancelledEvent)

        // Fetch profile in background to update active trip state
        viewModelScope.launch {
            Log.d("TripViewModel", "Fetching driver profile in background after trip cancellation")
            driverViewModel.fetchUserProfile()
        }
    }

    //emitdriverlocation

    //

    //arrive pickup

    //start trip

    //arrived destination/ end of trip

    //rate trip

    //cancel trip


    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        SoundPlayer.stopTripAlert()
    }
}


data class DistanceInfo(
    val distanceText: String,
    val distanceMeters: Int,
    val durationText: String,
    val durationSeconds: Int
)