package com.kabukabu.driver.features.support.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.core.data.remote.ApiClient
import com.kabukabu.driver.features.support.presentation.AddressInfo
import com.kabukabu.driver.features.support.presentation.DriverInfo
import com.kabukabu.driver.features.support.presentation.TripInfo
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

    private fun parseSupportTicketItem(obj: JSONObject): SupportTicketItem {
        val title = obj.optString("title")
        val statusStr = obj.optString("status")
        val createdAt = obj.optString("createdAt")
        val ticketId = obj.optString("ticket_id")
        val isAnswered = obj.optBoolean("is_answered", false)
        val fromSos = obj.optBoolean("from_sos", false)

        // Parse trip info if available
        val tripObj = obj.optJSONObject("trip")
        var tripInfo: TripInfo? = null
        if (tripObj != null && !tripObj.isNull("_id")) {
            val startAddrObj = tripObj.optJSONObject("start_address")
            val startAddress = if (startAddrObj != null) {
                AddressInfo(
                    country = startAddrObj.optString("country").takeIf { it.isNotBlank() },
                    state = startAddrObj.optString("state").takeIf { it.isNotBlank() },
                    city = startAddrObj.optString("city").takeIf { it.isNotBlank() },
                    street = startAddrObj.optString("street").takeIf { it.isNotBlank() },
                    fullAddress = startAddrObj.optString("full_address").takeIf { it.isNotBlank() }
                )
            } else null

            val endAddrObj = tripObj.optJSONObject("end_address")
            val endAddress = if (endAddrObj != null) {
                AddressInfo(
                    country = endAddrObj.optString("country").takeIf { it.isNotBlank() },
                    state = endAddrObj.optString("state").takeIf { it.isNotBlank() },
                    city = endAddrObj.optString("city").takeIf { it.isNotBlank() },
                    street = endAddrObj.optString("street").takeIf { it.isNotBlank() },
                    fullAddress = endAddrObj.optString("full_address").takeIf { it.isNotBlank() }
                )
            } else null

            val driverObj = tripObj.optJSONObject("driver")
            val driverInfo = if (driverObj != null) {
                DriverInfo(
                    id = driverObj.optString("_id").takeIf { it.isNotBlank() },
                    fullName = driverObj.optString("full_name").takeIf { it.isNotBlank() },
                    email = driverObj.optString("email").takeIf { it.isNotBlank() }
                )
            } else null

            tripInfo = TripInfo(
                id = tripObj.optString("_id", tripObj.optString("id")).takeIf { it.isNotBlank() },
                startAddress = startAddress,
                endAddress = endAddress,
                driver = driverInfo,
                price = if (tripObj.has("price")) tripObj.optInt("price") else null,
                paymentType = tripObj.optString("payment_type").takeIf { it.isNotBlank() },
                tripType = tripObj.optString("trip_type").takeIf { it.isNotBlank() },
                startTime = tripObj.optString("start_time").takeIf { it.isNotBlank() },
                endTime = tripObj.optString("end_time").takeIf { it.isNotBlank() }
            )
        }

        return SupportTicketItem(
            id = obj.optString("_id", obj.optString("id")),
            title = if (title.isNullOrBlank()) "Support Ticket" else title,
            ticketId = ticketId,
            status = statusStr,
            isAnswered = isAnswered,
            fromSos = fromSos,
            createdAt = createdAt,
            trip = tripInfo
        )
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
                            items.add(parseSupportTicketItem(obj))
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
                _uiState.value = _uiState.value.copy(isCreating = true, createError = null, createSuccess = null, newlyCreatedTicket = null)
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

                // Parse the response to get the created ticket
                val body = res.body()?.string() ?: "{}"
                val root = JSONObject(body)
                val dataObj = root.optJSONObject("data")

                if (dataObj != null) {
                    val newTicket = parseSupportTicketItem(dataObj)

                    // Add the new ticket to the beginning of the open list
                    val updatedOpenList = listOf(newTicket) + _uiState.value.open

                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        createSuccess = true,
                        newlyCreatedTicket = newTicket,
                        open = updatedOpenList
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isCreating = false, createSuccess = true)
                    // Fallback: refresh open tickets
                    openPage = 1
                    load(status = "open", reset = true)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isCreating = false, createError = e.message ?: "Error")
            }
        }
    }

    fun openNewTicketByTrip(subject: String, message: String, tripId: String) {
        if (subject.isBlank() || message.isBlank()) return
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isCreating = true, createError = null, createSuccess = null, newlyCreatedTicket = null)
                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""
                val bearer = "Bearer $token"
                val res = ApiClient.rideService.openSupportTicketByTrip(
                    bearerToken = bearer,
                    userId = userId,
                    subject = subject,
                    tripId = tripId,
                    message = message
                )
                if (!res.isSuccessful) throw Exception(res.errorBody()?.string() ?: "Failed to open ticket")

                // Parse the response to get the created ticket
                val body = res.body()?.string() ?: "{}"
                val root = JSONObject(body)
                val dataObj = root.optJSONObject("data")

                if (dataObj != null) {
                    val newTicket = parseSupportTicketItem(dataObj)

                    // Add the new ticket to the beginning of the open list
                    val updatedOpenList = listOf(newTicket) + _uiState.value.open

                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        createSuccess = true,
                        newlyCreatedTicket = newTicket,
                        open = updatedOpenList
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isCreating = false, createSuccess = true)
                    // Fallback: refresh open tickets
                    openPage = 1
                    load(status = "open", reset = true)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isCreating = false, createError = e.message ?: "Error")
            }
        }
    }

    fun clearNewlyCreatedTicket() {
        _uiState.value = _uiState.value.copy(newlyCreatedTicket = null, createSuccess = null)
    }
}

data class SupportTicketItem(
    val id: String?,
    val title: String,
    val ticketId: String?,
    val status: String?,
    val isAnswered: Boolean,
    val fromSos: Boolean,
    val createdAt: String?,
    val trip: TripInfo? = null
)

//data class TripInfo(
//    val id: String?,
//    val startAddress: AddressInfo?,
//    val endAddress: AddressInfo?,
//    val driver: DriverInfo?,
//    val price: Int?,
//    val paymentType: String?,
//    val tripType: String?,
//    val startTime: String?,
//    val endTime: String?
//)
//
//data class AddressInfo(
//    val country: String?,
//    val state: String?,
//    val city: String?,
//    val street: String?,
//    val fullAddress: String?
//)
//
//data class DriverInfo(
//    val id: String?,
//    val fullName: String?,
//    val email: String?
//)

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
    val newlyCreatedTicket: SupportTicketItem? = null
)
