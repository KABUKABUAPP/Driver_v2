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
import kotlinx.coroutines.delay

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
    
    init {
        // Start a periodic connection check
        coroutineScope.launch {
            while (true) {
                try {
                    if (mSocket == null || !mSocket!!.connected()) {
                        Log.d("SocketService", "Periodic check: Socket is null or disconnected. Reconnecting...")
                        connect()
                    }
                } catch (e: Exception) {
                    Log.e("SocketService", "Error in periodic connection check: ${e.message}", e)
                }
                delay(30000) // Check every 30 seconds
            }
        }
    }
    
    fun connect() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                if (mSocket != null && mSocket!!.connected()) {
                    Log.d("SocketService", "Socket already connected")
                    return@launch
                }

                val userId = userPreferences.userId.first()
                if (userId.isNullOrEmpty()) {
                    Log.d("SocketService", "No user ID available, skipping socket connection")
                    return@launch
                }

                val options = IO.Options()
                options.forceNew = true
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
            Log.d("SocketService", "RAW_DATA: 'tripFound' Received: ${args.getOrNull(0)}")
            try {
                val json = args.getOrNull(0)?.toString() ?: return@on
                val adapter = moshi.adapter(TripFoundEvent::class.java)
                val tripFoundData = adapter.fromJson(json)

                if (tripFoundData != null) {
                    Log.d("SocketService", "Successfully parsed 'tripFound' event: ${tripFoundData.eventId}")
                    coroutineScope.launch {
                        _tripFoundEvent.emit(tripFoundData)
                    }
                    emitAcknowledge(tripFoundData.eventId)
                }
            } catch (e: Exception) {
                Log.e("SocketService", "Error parsing 'tripFound' event", e)
            }
        }
        
        socket.on("trip-found") { args ->
            Log.d("SocketService", "RAW_DATA: 'trip-found' Received: ${args.getOrNull(0)}")
            try {
                val json = args.getOrNull(0)?.toString() ?: return@on
                val adapter = moshi.adapter(TripFoundEvent::class.java)
                val tripFoundData = adapter.fromJson(json)

                if (tripFoundData != null && tripFoundData.status == "pending") {
                    Log.d("SocketService", "Successfully parsed 'trip-found' event: ${tripFoundData.eventId}")
                    coroutineScope.launch {
                        _tripFoundEvent.emit(tripFoundData)
                    }
                    emitAcknowledge(tripFoundData.eventId)
                }
            } catch (e: Exception) {
                Log.e("SocketService", "Error parsing 'trip-found' event", e)
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

        // Add listener for ping event
        socket.on("pong") { args ->
            Log.d("SocketService", "RAW_DATA: 'pong' Received: ${args.getOrNull(0)}")
        }
    }

    suspend fun joinTripRoom(tripId: String) {
        try {
            if (mSocket == null || !mSocket!!.connected()) {
                Log.e("SocketService", "Cannot join room: Socket is null or not connected. Attempting to reconnect...")
                connect()
                delay(500) // Give it a moment to connect
            }
            
            if (mSocket == null) {
                Log.e("SocketService", "Failed to join room: Socket is still null after reconnection attempt")
                return
            }
            
            if (!mSocket!!.connected()) {
                Log.e("SocketService", "Failed to join room: Socket is still disconnected after reconnection attempt")
                return
            }
            
            Log.d("SocketService", "Socket connected status before join-room: ${mSocket?.connected()}")
            
            coroutineScope.launch {
                try {
                    // Try both event names to ensure one works
                    mSocket?.emit("join-room", tripId)
                    Log.d("SocketService", "EMITTED join-room event for trip: $tripId")
                    
                    // Also try without the dash
                    mSocket?.emit("join-room", tripId)
                    Log.d("SocketService", "EMITTED joinRoom event (no dash) for trip: $tripId")
                    
                    // Force a direct message to verify socket is working
                    mSocket?.emit("ping", "test")
                    Log.d("SocketService", "EMITTED ping event as test")
                } catch (e: Exception) {
                    Log.e("SocketService", "Error emitting join-room event: ${e.message}", e)
                }
            }.join()
        } catch (e: Exception) {
            Log.e("SocketService", "Error in joinTripRoom: ${e.message}", e)
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