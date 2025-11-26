package com.kabukabu.driver.core.data.socket

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
    
    private val _tripCancelledEvent = MutableSharedFlow<TripCancelledEvent>()
    val tripCancelledEvent: SharedFlow<TripCancelledEvent> = _tripCancelledEvent.asSharedFlow()

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

        socket.on("trip-cancelled") { args ->
            Log.d("SocketService", "RAW_DATA: 'trip-cancelled' Received: ${args.getOrNull(0)}")
            try {
                val json = args.getOrNull(0)?.toString() ?: return@on
                val adapter = moshi.adapter(TripCancelledEvent::class.java)
                val tripCancelledData = adapter.fromJson(json)

                if (tripCancelledData != null) {
                    Log.d("SocketService", "Successfully parsed 'trip-cancelled' event: Trip ID: ${tripCancelledData.order.id}, Status: ${tripCancelledData.status}, EventId: ${tripCancelledData.eventId}")
                    coroutineScope.launch {
                        _tripCancelledEvent.emit(tripCancelledData)
                    }
                } else {
                    Log.w("SocketService", "Failed to parse trip-cancelled event - tripCancelledData is null")
                }
            } catch (e: Exception) {
                Log.e("SocketService", "Error parsing 'trip-cancelled' event: ${e.message}", e)
            }
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
    
    /**
     * Emit driver location updates during trip
     * @param lat Driver's current latitude
     * @param long Driver's current longitude
     * @param orderId The active trip/order ID
     * @param time Time duration (in seconds or minutes depending on backend)
     * @param distance Distance traveled (in meters or km depending on backend)
     */
    suspend fun emitLocation(
        lat: Double,
        long: Double,
        orderId: String = "",
        time: Int = 0,
        distance: Double = 0.0
    ) {
        try {
            val userId = userPreferences.userId.first()
            if (userId.isNullOrEmpty()) {
                Log.w("SocketService", "Cannot emit location: User ID is null or empty")
                return
            }

            if (mSocket == null || !mSocket!!.connected()) {
                Log.w("SocketService", "Cannot emit location: Socket is null or not connected")
                return
            }

            val json = JSONObject().apply {
                put("lat", lat)
                put("long", long)
                put("userId", userId)
                put("user_type", "driver")
                put("order", orderId)
                put("time", time)
                put("distance", distance)
            }

            mSocket?.emit("location", json)
            Log.d("SocketService", "Emitting location: distance: $distance, time: $time, lat: $lat, long: $long, orderId: $orderId")
        } catch (e: Exception) {
            Log.e("SocketService", "Error emitting location: ${e.message}", e)
        }
    }

    /**
     * Emit driver arrived at pickup location
     * @param orderId The active trip/order ID
     */
    suspend fun emitArrivePickup(orderId: String = "") {
        try {
            val userId = userPreferences.userId.first()
            if (userId.isNullOrEmpty()) {
                Log.w("SocketService", "Cannot emit arrive pickup: User ID is null or empty")
                return
            }

            if (mSocket == null || !mSocket!!.connected()) {
                Log.w("SocketService", "Cannot emit arrive pickup: Socket is null or not connected")
                return
            }

            val json = JSONObject().apply {
                put("userId", userId)
                put("user_type", "driver")
                put("order", orderId)
            }

            mSocket?.emit("driver-arrived", json)
            Log.d("SocketService", "Emitting driver arrive: orderId: $json")
        } catch (e: Exception) {
            Log.e("SocketService", "Error emitting arrive pickup: ${e.message}", e)
        }
    }

    /**
     * Emit driver arrived at destination
     * @param orderId The active trip/order ID
     */
    suspend fun emitArriveDestination(orderId: String = "") {
        try {
            val userId = userPreferences.userId.first()
            if (userId.isNullOrEmpty()) {
                Log.w("SocketService", "Cannot emit arrive destination: User ID is null or empty")
                return
            }

            if (mSocket == null || !mSocket!!.connected()) {
                Log.w("SocketService", "Cannot emit arrive destination: Socket is null or not connected")
                return
            }

            val json = JSONObject().apply {
                put("userId", userId)
                put("user_type", "driver")
                put("order", orderId)
            }

            mSocket?.emit("driver-arrived_destination", json)
            Log.d("SocketService", "Emitting driver arrived destination: orderId: $orderId")
        } catch (e: Exception) {
            Log.e("SocketService", "Error emitting arrive destination: ${e.message}", e)
        }
    }

} 