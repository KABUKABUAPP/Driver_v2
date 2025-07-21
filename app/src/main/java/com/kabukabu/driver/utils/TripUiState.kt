package com.kabukabu.driver.utils

import com.kabukabu.driver.data.socket.SocketEvent

sealed class TripUiState {
    object Idle : TripUiState()
    data class TripRequest(val tripDetails: SocketEvent.TripFound) : TripUiState()
    object TripInProgress : TripUiState()
    data class Error(val message: String) : TripUiState()
} 