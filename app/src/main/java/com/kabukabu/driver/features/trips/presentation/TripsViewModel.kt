package com.kabukabu.driver.features.trips.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.features.trips.data.TripHistoryData
import com.kabukabu.driver.features.trips.data.TripItem
import com.kabukabu.driver.core.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TripsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TripsUiState())
    val uiState: StateFlow<TripsUiState> = _uiState

    private var page = 1
    private val limit = 10

    init { refresh() }

    fun refresh() {
        page = 1
        loadTrips(reset = true)
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || _uiState.value.noMore) return
        page += 1
        loadTrips(reset = false)
    }

    private fun loadTrips(reset: Boolean) {
        viewModelScope.launch {
            try {
                if (reset) _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                else _uiState.value = _uiState.value.copy(isLoadingMore = true, error = null)

                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""
                if (token.isBlank() || userId.isBlank()) {
                    _uiState.value = _uiState.value.copy(isLoading = false, isLoadingMore = false, error = "Missing auth")
                    return@launch
                }
                val bearer = "Bearer $token"
                val res = ApiClient.rideService.getMyTrips(
                    bearerToken = bearer,
                    userId = userId,
                    page = page,
                    limit = limit
                )
                val newItems = res.data?.data ?: emptyList()
                val hasNext = res.data?.pagination?.hasNext ?: false

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    items = if (reset) newItems else _uiState.value.items + newItems,
                    noMore = !hasNext
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isLoadingMore = false, error = e.message ?: "Failed to load trips")
            }
        }
    }
}

data class TripsUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val items: List<TripItem> = emptyList(),
    val noMore: Boolean = false
)
