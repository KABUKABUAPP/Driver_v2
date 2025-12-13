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

    // Connection status flow
    private val _connectionStatus = MutableSharedFlow<String>(replay = 1)
    val connectionStatus: SharedFlow<String> = _connectionStatus.asSharedFlow()

    // Chat message events
    private val _chatMessageEvent = MutableSharedFlow<JSONObject>()
    val chatMessageEvent = _chatMessageEvent.asSharedFlow()

    // Typing indicator events
    private val _typingEvent = MutableSharedFlow<JSONObject>()
    val typingEvent = _typingEvent.asSharedFlow()

    // Debug monitoring flows
    private val _socketEvents = MutableSharedFlow<String>(replay = 50) // Keep last 50 events
    val socketEvents: SharedFlow<String> = _socketEvents.asSharedFlow()

    private val _socketErrors = MutableSharedFlow<String>(replay = 20) // Keep last 20 errors
    val socketErrors: SharedFlow<String> = _socketErrors.asSharedFlow()

    fun isSocketConnected(): Boolean = mSocket?.connected() ?: false
    fun getSocketId(): String = mSocket?.id() ?: "Not connected"

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
                    _connectionStatus.emit("Already connected")
                    _socketEvents.emit("[${getCurrentTime()}] Attempted connection - already connected")
                    return@launch
                }

                val userId = userPreferences.userId.first()
                if (userId.isNullOrEmpty()) {
                    Log.d("SocketService", "No user ID available, skipping socket connection")
                    _connectionStatus.emit("No user ID - cannot connect")
                    _socketErrors.emit("[${getCurrentTime()}] ERROR: No user ID available")
                    return@launch
                }

                val options = IO.Options()
                options.forceNew = true
                val connectionUrl = "$RIDE_URL?authid=$userId"
                Log.d("SocketService", "DEBUG: Connecting to URL: $connectionUrl")
                _connectionStatus.emit("Connecting...")
                _socketEvents.emit("[${getCurrentTime()}] Attempting connection to: $RIDE_URL")
                _socketEvents.emit("[${getCurrentTime()}] User ID: $userId")

                val socket = IO.socket(connectionUrl, options)
                mSocket = socket

                // Setup listeners before connecting
                setupListeners(socket)

                socket.connect()
                Log.d("SocketService", "DEBUG: socket.connect() called.")

            } catch (e: URISyntaxException) {
                Log.e("SocketService", "DEBUG: URISyntaxException: ${e.message}")
                _connectionStatus.emit("Connection failed: URI error")
                _socketErrors.emit("[${getCurrentTime()}] ERROR: URI Syntax Error - ${e.message}")
            } catch (e: Exception) {
                Log.e("SocketService", "DEBUG: Exception in connect: ${e.message}")
                _connectionStatus.emit("Connection failed: ${e.message}")
                _socketErrors.emit("[${getCurrentTime()}] ERROR: ${e.message}")
            }
        }
    }

    private fun getCurrentTime(): String {
        val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    private fun setupListeners(socket: Socket) {
        socket.on(Socket.EVENT_CONNECT) {
            Log.d("SocketService", "DEBUG: >>>>>>>>>> Socket.EVENT_CONNECT: Successfully connected! <<<<<<<<<<")
            coroutineScope.launch {
                _connectionStatus.emit("Connected")
                _socketEvents.emit("[${getCurrentTime()}] ✅ CONNECTED - Socket ID: ${socket.id()}")

                val userId = userPreferences.userId.first()
                if (userId != null) {
                    Log.d("SocketService", "DEBUG: Emitting 'joinRoom' with userId: $userId")
                    socket.emit("join-room", userId)
                    _socketEvents.emit("[${getCurrentTime()}] 📤 Emitted 'join-room' with userId: $userId")
                }
            }
        }

        socket.on(Socket.EVENT_DISCONNECT) { args ->
            Log.e("SocketService", "DEBUG: >>>>>>>>>> Socket.EVENT_DISCONNECT: Disconnected! Reason: ${args.getOrNull(0)} <<<<<<<<<<")
            coroutineScope.launch {
                _connectionStatus.emit("Disconnected")
                _socketEvents.emit("[${getCurrentTime()}] ❌ DISCONNECTED - Reason: ${args.getOrNull(0)}")
            }
        }

        socket.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Log.e("SocketService", "DEBUG: >>>>>>>>>> Socket.EVENT_CONNECT_ERROR: Connection Error! Reason: ${args.getOrNull(0)} <<<<<<<<<<")
            coroutineScope.launch {
                _connectionStatus.emit("Connection error")
                _socketErrors.emit("[${getCurrentTime()}] ❌ CONNECTION ERROR: ${args.getOrNull(0)}")
            }
        }

        socket.on("trip-found") { args ->
            Log.d("SocketService", "RAW_DATA: 'tripFound' Received: ${args.getOrNull(0)}")
            coroutineScope.launch {
                _socketEvents.emit("[${getCurrentTime()}] 📥 Received 'tripFound' event")
            }
            try {
                val json = args.getOrNull(0)?.toString() ?: return@on
                val adapter = moshi.adapter(TripFoundEvent::class.java)
                val tripFoundData = adapter.fromJson(json)

                if (tripFoundData != null) {
                    Log.d("SocketService", "Successfully parsed 'tripFound' event: ${tripFoundData.eventId}")
                    coroutineScope.launch {
                        _socketEvents.emit("[${getCurrentTime()}] 🚗 TRIP REQUEST - Event ID: ${tripFoundData.eventId}, Status: ${tripFoundData.status}")
                        _tripFoundEvent.emit(tripFoundData)
                    }
                    emitAcknowledge(tripFoundData.eventId)
                }
            } catch (e: Exception) {
                Log.e("SocketService", "Error parsing 'tripFound' event", e)
                coroutineScope.launch {
                    _socketErrors.emit("[${getCurrentTime()}] ❌ ERROR parsing 'tripFound': ${e.message}")
                }
            }
        }

        socket.on("trip-found") { args ->
            Log.d("SocketService", "RAW_DATA: 'trip-found' Received: ${args.getOrNull(0)}")
            coroutineScope.launch {
                _socketEvents.emit("[${getCurrentTime()}] 📥 Received 'trip-found' event")
            }
            try {
                val json = args.getOrNull(0)?.toString() ?: return@on
                val adapter = moshi.adapter(TripFoundEvent::class.java)
                val tripFoundData = adapter.fromJson(json)

                if (tripFoundData != null && tripFoundData.status == "pending") {
                    Log.d("SocketService", "Successfully parsed 'trip-found' event: ${tripFoundData.eventId}")
                    coroutineScope.launch {
                        _socketEvents.emit("[${getCurrentTime()}] 🚗 TRIP REQUEST - Event ID: ${tripFoundData.eventId}, Status: ${tripFoundData.status}")
                        _tripFoundEvent.emit(tripFoundData)
                    }
                    emitAcknowledge(tripFoundData.eventId)
                }
            } catch (e: Exception) {
                Log.e("SocketService", "Error parsing 'trip-found' event", e)
                coroutineScope.launch {
                    _socketErrors.emit("[${getCurrentTime()}] ❌ ERROR parsing 'trip-found': ${e.message}")
                }
            }
        }

        socket.on("message") { args ->
            Log.d("SocketService", "RAW_DATA: 'onMessage' Received: ${args.getOrNull(0)}")
            coroutineScope.launch {
                _socketEvents.emit("[${getCurrentTime()}] 📥 Received 'onMessage' event")
            }
            try {
                val data = args.getOrNull(0)
                if (data is JSONObject) {
                    Log.d("SocketService", "Chat message received: ${data.toString()}")
                    coroutineScope.launch {
                        _chatMessageEvent.emit(data)
                    }
                }
            } catch (e: Exception) {
                Log.e("SocketService", "Error parsing 'onMessage' event", e)
                coroutineScope.launch {
                    _socketErrors.emit("[${getCurrentTime()}] ❌ ERROR parsing 'onMessage': ${e.message}")
                }
            }
        }

        socket.on("trip-cancelled") { args ->
            Log.d("SocketService", "RAW_DATA: 'trip-cancelled' Received: ${args.getOrNull(0)}")
            coroutineScope.launch {
                _socketEvents.emit("[${getCurrentTime()}] 📥 Received 'trip-cancelled' event")
            }
            try {
                val json = args.getOrNull(0)?.toString() ?: return@on
                val adapter = moshi.adapter(TripCancelledEvent::class.java)
                val tripCancelledData = adapter.fromJson(json)

                if (tripCancelledData != null) {
                    Log.d("SocketService", "Successfully parsed 'trip-cancelled' event: Trip ID: ${tripCancelledData.order.id}, Status: ${tripCancelledData.status}, EventId: ${tripCancelledData.eventId}")
                    coroutineScope.launch {
                        _socketEvents.emit("[${getCurrentTime()}] ❌ TRIP CANCELLED - Trip ID: ${tripCancelledData.order.id}")
                        _tripCancelledEvent.emit(tripCancelledData)
                    }
                } else {
                    Log.w("SocketService", "Failed to parse trip-cancelled event - tripCancelledData is null")
                    coroutineScope.launch {
                        _socketErrors.emit("[${getCurrentTime()}] ❌ ERROR: Failed to parse trip-cancelled - null data")
                    }
                }
            } catch (e: Exception) {
                Log.e("SocketService", "Error parsing 'trip-cancelled' event: ${e.message}", e)
                coroutineScope.launch {
                    _socketErrors.emit("[${getCurrentTime()}] ❌ ERROR parsing 'trip-cancelled': ${e.message}")
                }
            }
        }

        socket.on("onTyping") { args ->
            Log.d("SocketService", "RAW_DATA: 'onTyping' Received: ${args.getOrNull(0)}")
            coroutineScope.launch {
                _socketEvents.emit("[${getCurrentTime()}] 📥 Received 'onTyping' event")
            }
            try {
                val data = args.getOrNull(0)
                if (data is JSONObject) {
                    Log.d("SocketService", "Typing indicator received: ${data.toString()}")
                    coroutineScope.launch {
                        _typingEvent.emit(data)
                    }
                }
            } catch (e: Exception) {
                Log.e("SocketService", "Error parsing 'onTyping' event", e)
                coroutineScope.launch {
                    _socketErrors.emit("[${getCurrentTime()}] ❌ ERROR parsing 'onTyping': ${e.message}")
                }
            }
        }

        socket.on("supportMessage") { args ->
            Log.d("SocketService", "RAW_DATA: 'supportMessage' Received: ${args.getOrNull(0)}")
            coroutineScope.launch {
                _socketEvents.emit("[${getCurrentTime()}] 📥 Received 'supportMessage' event")
            }
            // TODO: Parse and handle support message
        }

        // Add listener for ping event
        socket.on("pong") { args ->
            Log.d("SocketService", "RAW_DATA: 'pong' Received: ${args.getOrNull(0)}")
            coroutineScope.launch {
                _socketEvents.emit("[${getCurrentTime()}] 🏓 Received 'pong' event")
            }
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
     * Emit a chat message to a specific room (order/trip)
     * @param room The room/order ID
     * @param userId The driver's user ID
     * @param content The message content
     */
    fun emitMessage(room: String, userId: String, content: String) {
        val payload = JSONObject().apply {
            put("room", room)
            put("userId", userId)
            put("content", content)
        }
        mSocket?.emit("message", payload)
        Log.d("SocketService", "Emitting chat message to room: $room, content: $content")
    }

    /**
     * Emit typing indicator to a specific room
     * @param room The room/order ID
     * @param userId The driver's user ID
     */
    fun emitTyping(room: String, userId: String) {
        val payload = JSONObject().apply {
            put("room", room)
            put("userId", userId)
        }
        mSocket?.emit("typing", payload)
        Log.d("SocketService", "Emitting typing indicator to room: $room")
    }


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