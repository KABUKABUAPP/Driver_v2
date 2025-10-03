package com.kabukabu.driver.features.promotions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.core.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject

class PromotionsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PromotionsUiState())
    val uiState: StateFlow<PromotionsUiState> = _uiState

    private var ongoingPage = 1
    private var completedPage = 1
    private val limit = 10

    init {
        refresh()
    }

    fun refresh() {
        ongoingPage = 1
        completedPage = 1
        load(status = "ongoing", reset = true)
        load(status = "completed", reset = true)
    }

    fun loadMore(status: String) {
        if (status == "ongoing") {
            if (_uiState.value.isLoadingMoreOngoing || _uiState.value.noMoreOngoing) return
            ongoingPage += 1
            load(status = status, reset = false)
        } else {
            if (_uiState.value.isLoadingMoreCompleted || _uiState.value.noMoreCompleted) return
            completedPage += 1
            load(status = status, reset = false)
        }
    }

    private fun load(status: String, reset: Boolean) {
        viewModelScope.launch {
            try {
                if (status == "ongoing") {
                    _uiState.value = if (reset) _uiState.value.copy(isLoadingOngoing = true, errorOngoing = null)
                    else _uiState.value.copy(isLoadingMoreOngoing = true, errorOngoing = null)
                } else {
                    _uiState.value = if (reset) _uiState.value.copy(isLoadingCompleted = true, errorCompleted = null)
                    else _uiState.value.copy(isLoadingMoreCompleted = true, errorCompleted = null)
                }
                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""
                if (token.isBlank() || userId.isBlank()) {
                    if (status == "ongoing") _uiState.value = _uiState.value.copy(isLoadingOngoing = false, isLoadingMoreOngoing = false, errorOngoing = "Missing auth")
                    else _uiState.value = _uiState.value.copy(isLoadingCompleted = false, isLoadingMoreCompleted = false, errorCompleted = "Missing auth")
                    return@launch
                }
                val bearer = "Bearer $token"
                val page = if (status == "ongoing") ongoingPage else completedPage
                val res = ApiClient.rideService.getPromotions(
                    bearerToken = bearer,
                    userId = userId,
                    status = status,
                    page = page,
                    limit = limit
                )
                if (!res.isSuccessful) throw Exception(res.errorBody()?.string() ?: "Promo error")
                val body = res.body()?.string() ?: "{}"
                val root = JSONObject(body)
                val data = root.optJSONObject("data")
                val list = data?.optJSONArray("data")
                val pagination = data?.optJSONObject("pagination")
                val hasNext = pagination?.optBoolean("hasNext") ?: false
                val items = mutableListOf<PromotionItem>()
                if (list != null) {
                    for (i in 0 until list.length()) {
                        val obj = list.optJSONObject(i)
                        if (obj != null) {
                            // Resilient key mapping similar to Flutter
                            val statusStr = obj.optString("status")
                            val promo = obj.optJSONObject("promotion")
                            val coupon = obj.optJSONObject("coupon")
                            val name = promo?.optString("name")
                                ?: coupon?.optString("name")
                                ?: obj.optString("name")
                            val expiry = promo?.optString("expiryDate")
                                ?: obj.optString("expiryDate")
                            items.add(PromotionItem(name = name.ifBlank { "Promo" }, status = statusStr, expiryDate = expiry))
                        }
                    }
                }

                if (status == "ongoing") {
                    _uiState.value = _uiState.value.copy(
                        isLoadingOngoing = false,
                        isLoadingMoreOngoing = false,
                        ongoing = if (reset) items else _uiState.value.ongoing + items,
                        noMoreOngoing = !hasNext
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoadingCompleted = false,
                        isLoadingMoreCompleted = false,
                        completed = if (reset) items else _uiState.value.completed + items,
                        noMoreCompleted = !hasNext
                    )
                }
            } catch (e: Exception) {
                if (status == "ongoing") {
                    _uiState.value = _uiState.value.copy(isLoadingOngoing = false, isLoadingMoreOngoing = false, errorOngoing = e.message)
                } else {
                    _uiState.value = _uiState.value.copy(isLoadingCompleted = false, isLoadingMoreCompleted = false, errorCompleted = e.message)
                }
            }
        }
    }
}

data class PromotionItem(
    val name: String,
    val status: String?,
    val expiryDate: String?
)

data class PromotionsUiState(
    val isLoadingOngoing: Boolean = false,
    val isLoadingMoreOngoing: Boolean = false,
    val errorOngoing: String? = null,
    val ongoing: List<PromotionItem> = emptyList(),
    val noMoreOngoing: Boolean = false,

    val isLoadingCompleted: Boolean = false,
    val isLoadingMoreCompleted: Boolean = false,
    val errorCompleted: String? = null,
    val completed: List<PromotionItem> = emptyList(),
    val noMoreCompleted: Boolean = false,
)
