package com.kabukabu.driver.core.data

import com.kabukabu.driver.core.data.socket.TripFoundEvent

/**
 * Singleton to temporarily hold trip request data when launching overlay activity
 */
object TripRequestData {
    var currentTripEvent: TripFoundEvent? = null
        set(value) {
            field = value
            android.util.Log.d("TripRequestData", "Trip event set: ${value?.eventId}")
        }

    fun clear() {
        currentTripEvent = null
        android.util.Log.d("TripRequestData", "Trip event cleared")
    }
}

