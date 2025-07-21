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

    private val _tripFoundEvent = MutableSharedFlow<TripFoundEvent>()
    val tripFoundEvent: SharedFlow<TripFoundEvent> = _tripFoundEvent.asSharedFlow()

    fun connect() {
        Log.d("SocketService", "Attempting to connect...")
        // Launch a coroutine to handle connection asynchronously
        coroutineScope.launch {
            val userId = userPreferences.userId.first()
            if (userId.isNullOrBlank()) {
                Log.e("SocketService", "DEBUG: Cannot connect, user ID is null or blank.")
                return@launch
            }

            Log.d("SocketService", "DEBUG: User ID found: $userId")

            try {
                val options = IO.Options().apply {
                    transports = arrayOf("websocket")
                    forceNew = true
                }
                val connectionUrl = "$RIDE_URL?authid=$userId"
                Log.d("SocketService", "DEBUG: Connecting to URL: $connectionUrl")

                val socket = IO.socket(connectionUrl, options)
                mSocket = socket

                // Setup listeners before connecting
                setupListeners(socket)

                socket.connect()
                Log.d("SocketService", "DEBUG: socket.connect() called.")

            } catch (e: URISyntaxException) {
                Log.e("SocketService", "DEBUG: URISyntaxException: ${e.message}")
            }
        }
    }

    private fun setupListeners(socket: Socket) {
        socket.on(Socket.EVENT_CONNECT) {
            Log.d("SocketService", "DEBUG: >>>>>>>>>> Socket.EVENT_CONNECT: Successfully connected! <<<<<<<<<<")
            coroutineScope.launch {
                val userId = userPreferences.userId.first()
                if (userId != null) {
                    Log.d("SocketService", "DEBUG: Emitting 'joinRoom' with userId: $userId")
                    socket.emit("join-room", userId)
                }
            }
        }

        socket.on(Socket.EVENT_DISCONNECT) { args ->
            Log.e("SocketService", "DEBUG: >>>>>>>>>> Socket.EVENT_DISCONNECT: Disconnected! Reason: ${args.getOrNull(0)} <<<<<<<<<<")
        }

        socket.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Log.e("SocketService", "DEBUG: >>>>>>>>>> Socket.EVENT_CONNECT_ERROR: Connection Error! Reason: ${args.getOrNull(0)} <<<<<<<<<<")
        }

        socket.on("trip-found") { args ->
            Log.d("SocketService", "RAW_DATA: 'tripFound' Received: ${args[0]}")
            try {
                val json = args[0].toString()
                val adapter = moshi.adapter(TripFoundEvent::class.java)
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
            Log.d("SocketService", "RAW_DATA: 'onMessage' Received: ${args.getOrNull(0)}")
            // TODO: Parse and handle message
        }

        socket.on("onTripCancelled") { args ->
            Log.d("SocketService", "RAW_DATA: 'onTripCancelled' Received: ${args.getOrNull(0)}")
            // TODO: Parse and handle trip cancellation
        }

        socket.on("onTyping") { args ->
            Log.d("SocketService", "RAW_DATA: 'onTyping' Received: ${args.getOrNull(0)}")
            // TODO: Parse and handle typing indicator
        }

        socket.on("supportMessage") { args ->
            Log.d("SocketService", "RAW_DATA: 'supportMessage' Received: ${args.getOrNull(0)}")
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