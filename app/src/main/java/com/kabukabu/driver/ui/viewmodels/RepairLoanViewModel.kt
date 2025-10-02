package com.kabukabu.driver.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject

class RepairLoanViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RepairLoanUiState())
    val uiState: StateFlow<RepairLoanUiState> = _uiState

    private var page = 1
    private val limit = 10

    init { refresh() }

    fun refresh() {
        page = 1
        loadLoans(reset = true)
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || _uiState.value.noMore) return
        page += 1
        loadLoans(reset = false)
    }

    private fun loadLoans(reset: Boolean) {
        viewModelScope.launch {
            try {
                if (reset) _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                else _uiState.value = _uiState.value.copy(isLoadingMore = true, error = null)
                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""
                val bearer = "Bearer $token"
                val res = ApiClient.rideService.getRepairLoans(bearer, userId, page, limit)
                if (!res.isSuccessful) throw Exception(res.errorBody()?.string() ?: "Loan error")
                val body = res.body()?.string() ?: "{}"
                val root = JSONObject(body)
                val data = root.optJSONObject("data")
                val arr = data?.optJSONArray("data")
                val pagination = data?.optJSONObject("pagination")
                val hasNext = pagination?.optBoolean("hasNext") ?: false
                val items = mutableListOf<RepairLoanItem>()
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val o = arr.optJSONObject(i) ?: continue
                        items.add(
                            RepairLoanItem(
                                id = o.optString("_id", o.optString("id")),
                                amount = o.optInt("amount"),
                                status = o.optString("status"),
                                reason = o.optString("reason"),
                                createdAt = o.optString("created_at", o.optString("createdAt"))
                            )
                        )
                    }
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    loans = if (reset) items else _uiState.value.loans + items,
                    noMore = !hasNext
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isLoadingMore = false, error = e.message ?: "Error")
            }
        }
    }

    fun requestLoan(amount: Int, reason: String) {
        if (amount <= 0 || reason.isBlank()) return
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isRequesting = true, requestError = null, requestSuccess = null)
                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""
                val bearer = "Bearer $token"
                val res = ApiClient.rideService.requestRepairLoan(bearer, userId, amount, reason)
                if (!res.isSuccessful) throw Exception(res.errorBody()?.string() ?: "Request failed")
                _uiState.value = _uiState.value.copy(isRequesting = false, requestSuccess = true)
                refresh()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isRequesting = false, requestError = e.message ?: "Error")
            }
        }
    }
}

data class RepairLoanItem(
    val id: String?,
    val amount: Int,
    val status: String?,
    val reason: String?,
    val createdAt: String?
)

data class RepairLoanUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val loans: List<RepairLoanItem> = emptyList(),
    val noMore: Boolean = false,

    val isRequesting: Boolean = false,
    val requestError: String? = null,
    val requestSuccess: Boolean? = null,
)
