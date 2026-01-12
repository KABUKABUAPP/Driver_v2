package com.kabukabu.driver.features.trips.presentation

import androidx.lifecycle.ViewModel
import com.kabukabu.driver.features.trips.data.TripItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TripSharedViewModel : ViewModel() {
    private val _selectedTrip = MutableStateFlow<TripItem?>(null)
    val selectedTrip: StateFlow<TripItem?> = _selectedTrip

    fun selectTrip(trip: TripItem?) {
        _selectedTrip.value = trip
    }

    fun clear() {
        _selectedTrip.value = null
    }
}

