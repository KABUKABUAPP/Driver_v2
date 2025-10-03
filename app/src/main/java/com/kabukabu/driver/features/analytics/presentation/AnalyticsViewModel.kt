package com.kabukabu.driver.features.analytics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.features.analytics.data.DriverAnalysisData
import com.kabukabu.driver.core.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AnalyticsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState

    init { fetchAnalysis(timeFrame = "this_week") }

    fun fetchAnalysis(timeFrame: String? = null, customDate: String? = null) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""
                if (token.isBlank() || userId.isBlank()) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Missing auth")
                    return@launch
                }
                val bearer = "Bearer $token"
                val tf = timeFrame ?: "this_week"
                val res = ApiClient.rideService.getDriverAnalysis(
                    bearerToken = bearer,
                    userId = userId,
                    timeFrame = tf,
                    customDate = customDate
                )
                _uiState.value = _uiState.value.copy(isLoading = false, data = res.data)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to load analytics")
            }
        }
    }
}

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val data: DriverAnalysisData? = null
)
