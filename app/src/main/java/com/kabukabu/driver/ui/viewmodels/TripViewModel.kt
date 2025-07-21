package com.kabukabu.driver.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.data.socket.SocketEvent
import com.kabukabu.driver.data.socket.SocketService
import com.kabukabu.driver.utils.TripUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TripViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<TripUiState>(TripUiState.Idle)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            SocketService.tripFoundEvent.collect { tripFoundEvent ->
                _uiState.value = TripUiState.TripRequest(tripFoundEvent)
            }
        }
    }

    fun acceptTrip() {
        // TODO: Implement accept trip logic
        _uiState.value = TripUiState.Idle
    }

    fun declineTrip() {
        // TODO: Implement decline trip logic
        _uiState.value = TripUiState.Idle
    }
} 