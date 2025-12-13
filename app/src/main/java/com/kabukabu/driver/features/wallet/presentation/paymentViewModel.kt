package com.kabukabu.driver.features.wallet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.features.wallet.data.PaymentHistoryResponse
import com.kabukabu.driver.features.wallet.data.TransactionRow
import com.kabukabu.driver.core.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState

    private var currentPage = 0
    private val limit = 20

    init {
        loadPaymentHistory(reset = true)
    }

    fun refresh() {
        currentPage = 1
        loadPaymentHistory(reset = true)
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || _uiState.value.noMore) return
        currentPage += 1
        loadPaymentHistory(reset = false)
    }

    private fun loadPaymentHistory(reset: Boolean) {
        viewModelScope.launch {
            try {
                // Set loading state
                if (reset) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        error = null,
                        isRefreshing = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = true,
                        error = null
                    )
                }

                // Get auth credentials
                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""

                if (token.isBlank() || userId.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        isRefreshing = false,
                        error = "Authentication required"
                    )
                    return@launch
                }

                // Make API call
                val bearer = "Bearer $token"
                val response: PaymentHistoryResponse = ApiClient.rideService.getPaymentHistory(
                    bearerToken = bearer,
                    userId = userId,
                    page = currentPage,
                    limit = limit
                )

                // Extract transaction rows
                val newTransactions = response.data?.data?.rows ?: emptyList()
                val hasNext = response.data?.hasNext ?: false
                val total = response.data?.total ?: 0

                // Update state with data
                val updatedTransactions = if (reset) {
                    newTransactions
                } else {
                    _uiState.value.transactions + newTransactions
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    isRefreshing = false,
                    error = null,
                    transactions = updatedTransactions,
                    noMore = !hasNext,
                    totalCount = total,
                    currentPage = currentPage
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    isRefreshing = false,
                    error = e.message ?: "Failed to load payment history"
                )
            }
        }
    }

    fun onRefresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        refresh()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class PaymentUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val transactions: List<TransactionRow> = emptyList(),
    val noMore: Boolean = false,
    val totalCount: Int = 0,
    val currentPage: Int = 1
)


