package com.kabukabu.driver.features.support.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.core.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject

class SupportViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState

    private var openPage = 1
    private var closedPage = 1
    private val limit = 10

    init {
        refresh()
    }

    fun refresh() {
        openPage = 1
        closedPage = 1
        load(status = "open", reset = true)
        load(status = "closed", reset = true)
    }

    fun loadMore(status: String) {
        if (status == "open") {
            if (_uiState.value.isLoadingMoreOpen || _uiState.value.noMoreOpen) return
            openPage += 1
            load(status, reset = false)
        } else {
            if (_uiState.value.isLoadingMoreClosed || _uiState.value.noMoreClosed) return
            closedPage += 1
            load(status, reset = false)
        }
    }

    private fun load(status: String, reset: Boolean) {
        viewModelScope.launch {
            try {
                if (status == "open") {
                    _uiState.value = if (reset) _uiState.value.copy(isLoadingOpen = true, errorOpen = null)
                    else _uiState.value.copy(isLoadingMoreOpen = true, errorOpen = null)
                } else {
                    _uiState.value = if (reset) _uiState.value.copy(isLoadingClosed = true, errorClosed = null)
                    else _uiState.value.copy(isLoadingMoreClosed = true, errorClosed = null)
                }

                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""
                if (token.isBlank() || userId.isBlank()) {
                    if (status == "open") _uiState.value = _uiState.value.copy(isLoadingOpen = false, isLoadingMoreOpen = false, errorOpen = "Missing auth")
                    else _uiState.value = _uiState.value.copy(isLoadingClosed = false, isLoadingMoreClosed = false, errorClosed = "Missing auth")
                    return@launch
                }
                val bearer = "Bearer $token"
                val page = if (status == "open") openPage else closedPage

                val res = ApiClient.rideService.getSupportTickets(
                    bearerToken = bearer,
                    userId = userId,
                    status = status,
                    page = page,
                    limit = limit
                )
                if (!res.isSuccessful) throw Exception(res.errorBody()?.string() ?: "Support error")
                val body = res.body()?.string() ?: "{}"
                val root = JSONObject(body)
                val data = root.optJSONObject("data")
                val list = data?.optJSONArray("data")
                val pagination = data?.optJSONObject("pagination")
                val hasNext = pagination?.optBoolean("hasNext") ?: false
                val items = mutableListOf<SupportTicketItem>()
                if (list != null) {
                    for (i in 0 until list.length()) {
                        val obj = list.optJSONObject(i)
                        if (obj != null) {
                            val subject = obj.optString("subject")
                            val statusStr = obj.optString("status")
                            val createdAt = obj.optString("created_at", obj.optString("createdAt"))
                            val lastMsg = obj.optJSONObject("last_message")?.optString("message")
                            items.add(
                                SupportTicketItem(
                                    id = obj.optString("_id", obj.optString("id")),
                                    subject = if (subject.isNullOrBlank()) "Support" else subject,
                                    status = statusStr,
                                    lastMessage = lastMsg,
                                    createdAt = createdAt
                                )
                            )
                        }
                    }
                }

                if (status == "open") {
                    _uiState.value = _uiState.value.copy(
                        isLoadingOpen = false,
                        isLoadingMoreOpen = false,
                        open = if (reset) items else _uiState.value.open + items,
                        noMoreOpen = !hasNext
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoadingClosed = false,
                        isLoadingMoreClosed = false,
                        closed = if (reset) items else _uiState.value.closed + items,
                        noMoreClosed = !hasNext
                    )
                }
            } catch (e: Exception) {
                if (status == "open") {
                    _uiState.value = _uiState.value.copy(isLoadingOpen = false, isLoadingMoreOpen = false, errorOpen = e.message)
                } else {
                    _uiState.value = _uiState.value.copy(isLoadingClosed = false, isLoadingMoreClosed = false, errorClosed = e.message)
                }
            }
        }
    }

    fun openNewTicket(subject: String, message: String) {
        if (subject.isBlank() || message.isBlank()) return
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isCreating = true, createError = null, createSuccess = null)
                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""
                val bearer = "Bearer $token"
                val res = ApiClient.rideService.openNewSupportTicket(
                    bearerToken = bearer,
                    userId = userId,
                    subject = subject,
                    message = message
                )
                if (!res.isSuccessful) throw Exception(res.errorBody()?.string() ?: "Failed to open ticket")
                _uiState.value = _uiState.value.copy(isCreating = false, createSuccess = true)
                // refresh open tickets
                openPage = 1
                load(status = "open", reset = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isCreating = false, createError = e.message ?: "Error")
            }
        }
    }
}

data class SupportTicketItem(
    val id: String?,
    val subject: String,
    val status: String?,
    val lastMessage: String?,
    val createdAt: String?
)

data class SupportUiState(
    val isLoadingOpen: Boolean = false,
    val isLoadingMoreOpen: Boolean = false,
    val errorOpen: String? = null,
    val open: List<SupportTicketItem> = emptyList(),
    val noMoreOpen: Boolean = false,

    val isLoadingClosed: Boolean = false,
    val isLoadingMoreClosed: Boolean = false,
    val errorClosed: String? = null,
    val closed: List<SupportTicketItem> = emptyList(),
    val noMoreClosed: Boolean = false,
    // Create new ticket
    val isCreating: Boolean = false,
    val createError: String? = null,
    val createSuccess: Boolean? = null,
)
