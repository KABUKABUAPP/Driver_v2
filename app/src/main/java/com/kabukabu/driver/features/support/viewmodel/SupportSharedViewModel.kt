package com.kabukabu.driver.features.support.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.kabukabu.driver.features.trips.data.TripItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SupportSharedViewModel : ViewModel() {
    private val _selectedTrip = MutableStateFlow<TripItem?>(null)
    val selectedTrip: StateFlow<TripItem?> = _selectedTrip

    private val _selectedTicket = MutableStateFlow<SupportTicketItem?>(null)
    val selectedTicket: StateFlow<SupportTicketItem?> = _selectedTicket

    fun selectTrip(trip: TripItem?) {
        _selectedTicket.value = null
        _selectedTrip.value = trip
        Log.d("SupportSharedViewModel", "Selected Trip: $trip")
    }

    fun selectTicket(ticket: SupportTicketItem?) {
        _selectedTrip.value = null
        _selectedTicket.value = ticket
        Log.d("SupportSharedViewModel", "✅ Selected Ticket - ID: ${ticket?.id}, TicketID: ${ticket?.ticketId}, Title: ${ticket?.title}")
    }

    fun clear() {
        Log.d("SupportSharedViewModel", "🧹 Clearing selectedTicket (was ID: ${_selectedTicket.value?.id})")
        _selectedTrip.value = null
        _selectedTicket.value = null
    }
}

