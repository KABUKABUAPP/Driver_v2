package com.kabukabu.driver.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.data.socket.SocketService
import com.kabukabu.driver.utils.SoundPlayer
import com.kabukabu.driver.utils.TripUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TripViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<TripUiState>(TripUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private var countdownJob: Job? = null

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
        // TODO: Implement accept trip logic (emit socket event)
        _uiState.value = TripUiState.Idle
    }

    fun declineTrip() {
        countdownJob?.cancel()
        SoundPlayer.stopTripAlert()
        // TODO: Implement decline trip logic (emit socket event)
        _uiState.value = TripUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        SoundPlayer.stopTripAlert()
    }
} 