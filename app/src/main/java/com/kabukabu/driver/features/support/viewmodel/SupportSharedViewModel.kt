package com.kabukabu.driver.features.support.viewmodel

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
        _selectedTrip.value = trip
    }

    fun selectTicket(ticket: SupportTicketItem?) {
        _selectedTicket.value = ticket
    }

    fun clear() {
        _selectedTrip.value = null
        _selectedTicket.value = null
    }
}

