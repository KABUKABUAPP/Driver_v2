package com.kabukabu.driver.utils

import com.kabukabu.driver.data.socket.TripFoundEvent

sealed class TripUiState {
    object Idle : TripUiState()
    object NoTrip : TripUiState()
    data class TripRequest(
        val tripDetails: TripFoundEvent,
        val remainingTime: Int = 20
    ) : TripUiState()
    data class TripAccepted(val message: String) : TripUiState()
    data class Error(val message: String) : TripUiState()
} 