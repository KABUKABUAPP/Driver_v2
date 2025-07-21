package com.kabukabu.driver.utils

import com.kabukabu.driver.data.socket.TripFoundEvent

sealed class TripUiState {
    object Idle : TripUiState()
    data class TripRequest(
        val tripDetails: TripFoundEvent,
        val remainingTime: Int = 20
    ) : TripUiState()
    object TripInProgress : TripUiState()
    data class Error(val message: String) : TripUiState()
} 