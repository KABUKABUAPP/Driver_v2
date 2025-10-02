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

class SupportDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SupportDetailUiState())
    val uiState: StateFlow<SupportDetailUiState> = _uiState

    fun load(supportId: String) {
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
                val res = ApiClient.rideService.viewSupportTicket(
                    bearerToken = bearer,
                    userId = userId,
                    supportId = supportId
                )
                if (!res.isSuccessful) throw Exception(res.errorBody()?.string() ?: "Failed to load ticket")
                val body = res.body()?.string() ?: "{}"
                val root = JSONObject(body)
                val data = root.optJSONObject("data")
                val subject = data?.optString("subject")
                val messagesArr = data?.optJSONArray("messages") ?: data?.optJSONArray("chats")
                val list = mutableListOf<SupportMessage>()
                if (messagesArr != null) {
                    for (i in 0 until messagesArr.length()) {
                        val m = messagesArr.optJSONObject(i)
                        if (m != null) {
                            val msg = m.optString("message", m.optString("msg"))
                            val sender = m.optString("sender", m.optString("from"))
                            val createdAt = m.optString("created_at", m.optString("createdAt"))
                            list.add(SupportMessage(message = msg, sender = sender, createdAt = createdAt))
                        }
                    }
                }
                _uiState.value = _uiState.value.copy(isLoading = false, subject = subject ?: "Support", messages = list)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Error")
            }
        }
    }

    fun sendReply(supportId: String, message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSending = true, sendError = null)
                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""
                val bearer = "Bearer $token"
                val res = ApiClient.rideService.replySupportTicket(
                    bearerToken = bearer,
                    userId = userId,
                    supportId = supportId,
                    message = message
                )
                if (!res.isSuccessful) throw Exception(res.errorBody()?.string() ?: "Failed to send")
                _uiState.value = _uiState.value.copy(isSending = false, draft = "")
                // reload thread
                load(supportId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSending = false, sendError = e.message ?: "Send error")
            }
        }
    }

    fun updateDraft(text: String) {
        _uiState.value = _uiState.value.copy(draft = text)
    }
}

data class SupportMessage(
    val message: String,
    val sender: String?,
    val createdAt: String?
)

data class SupportDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val subject: String = "Support",
    val messages: List<SupportMessage> = emptyList(),
    val draft: String = "",

    val isSending: Boolean = false,
    val sendError: String? = null
)
