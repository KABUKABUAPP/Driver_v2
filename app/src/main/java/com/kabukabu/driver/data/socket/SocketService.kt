package com.kabukabu.driver.data.socket

import android.util.Log
import com.kabukabu.driver.KabukabuDriverApp
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URISyntaxException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object SocketService {
    @Volatile
    private var mSocket: Socket? = null
    private const val RIDE_URL = "https://rideservice-dev.up.railway.app"

    private val userPreferences = KabukabuDriverApp.getInstance().userPreferences
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    // Create a custom CoroutineScope
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _tripFoundEvent = MutableSharedFlow<SocketEvent.TripFound>()
    val tripFoundEvent: SharedFlow<SocketEvent.TripFound> = _tripFoundEvent.asSharedFlow()

    fun connect() {
        // Launch a coroutine to handle connection asynchronously
        coroutineScope.launch {
            val userId = userPreferences.userId.first()
            if (userId.isNullOrBlank()) {
                Log.e("SocketService", "Cannot connect, user ID is null or blank.")
                return@launch
            }

            try {
                val options = IO.Options().apply {
                    transports = arrayOf("websocket")
                    forceNew = true
                }
                val socket = IO.socket("$RIDE_URL?authid=$userId", options)
                mSocket = socket

                // Setup listeners before connecting
                setupListeners(socket)

                socket.connect()
                Log.d("SocketService", "Connecting to socket...")

            } catch (e: URISyntaxException) {
                Log.e("SocketService", "URISyntaxException: ${e.message}")
            }
        }
    }

    private fun setupListeners(socket: Socket) {
        socket.on(Socket.EVENT_CONNECT) {
            Log.d("SocketService", "Socket Connected!")
            coroutineScope.launch {
                val userId = userPreferences.userId.first()
                if (userId != null) {
                    socket.emit("joinRoom", userId)
                }
            }
        }

        socket.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Log.e("SocketService", "Socket Connect Error: ${args.getOrNull(0)}")
        }

        socket.on("tripFound") { args ->
            Log.d("SocketService", "Trip Found: ${args[0]}")
            try {
                val json = args[0].toString()
                val adapter = moshi.adapter(SocketEvent.TripFound::class.java)
                val tripFoundData = adapter.fromJson(json)

                if (tripFoundData != null && tripFoundData.status == "pending") {
                    coroutineScope.launch {
                        _tripFoundEvent.emit(tripFoundData)
                    }
                    emitAcknowledge(tripFoundData.eventId)
                }
            } catch (e: Exception) {
                Log.e("SocketService", "Error parsing tripFound event", e)
            }
        }

        socket.on("onMessage") { args ->
            Log.d("SocketService", "onMessage Received: ${args.getOrNull(0)}")
            // TODO: Parse and handle message
        }

        socket.on("onTripCancelled") { args ->
            Log.d("SocketService", "onTripCancelled Received: ${args.getOrNull(0)}")
            // TODO: Parse and handle trip cancellation
        }

        socket.on("onTyping") { args ->
            Log.d("SocketService", "onTyping Received: ${args.getOrNull(0)}")
            // TODO: Parse and handle typing indicator
        }

        socket.on("supportMessage") { args ->
            Log.d("SocketService", "supportMessage Received: ${args.getOrNull(0)}")
            // TODO: Parse and handle support message
        }
    }

    fun disconnect() {
        mSocket?.disconnect()
        Log.d("SocketService", "Socket Disconnected!")
    }

    fun emitAcknowledge(eventId: String) {
        val json = JSONObject().put("eventId", eventId)
        mSocket?.emit("received", json)
        Log.d("SocketService", "Emitting acknowledge for eventId: $eventId")
    }
    
    // TODO: Add all other event emitters from the Flutter code

} 