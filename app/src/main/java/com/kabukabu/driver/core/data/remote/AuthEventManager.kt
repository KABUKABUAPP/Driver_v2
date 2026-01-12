package com.kabukabu.driver.core.data.remote

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Singleton to manage authentication events across the app.
 * When a 401 response is received, it emits an event that can be observed
 * to trigger navigation to the login screen.
 */
object AuthEventManager {

    sealed class AuthEvent {
        object Unauthorized : AuthEvent()  // 401 received
        object SessionExpired : AuthEvent() // Token expired
    }

    private val _authEvents = MutableSharedFlow<AuthEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val authEvents: SharedFlow<AuthEvent> = _authEvents.asSharedFlow()

    /**
     * Call this when a 401 response is received
     */
    fun onUnauthorized() {
        _authEvents.tryEmit(AuthEvent.Unauthorized)
    }

    /**
     * Call this when the session/token has expired
     */
    fun onSessionExpired() {
        _authEvents.tryEmit(AuthEvent.SessionExpired)
    }
}

