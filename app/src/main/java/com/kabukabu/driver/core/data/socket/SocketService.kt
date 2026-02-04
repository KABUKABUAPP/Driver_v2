package com.kabukabu.driver.core.data.socket

import android.content.Intent
import android.util.Log
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.services.CustomFullScreenOverlayService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.net.URISyntaxException
import java.util.concurrent.atomic.AtomicBoolean


object SocketService {
    @Volatile
    private var mSocket: Socket? = null
    private const val RIDE_URL = "https://rideservice-dev.up.railway.app"

    // Mutex for synchronizing connect()
    private val connectionMutex = Mutex()

    // Prevent overlapping connection attempts
    private val isConnecting = AtomicBoolean(false)

    // Avoid storing a static context reference; use a getter to fetch preferences when needed
    private val userPreferences get() = KabukabuDriverApp.getInstance().userPreferences
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

    // Support message events
    private val _supportMessageEvent = MutableSharedFlow<JSONObject>()
    val supportMessageEvent = _supportMessageEvent.asSharedFlow()

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
            // Avoid re-entrancy: if another connect is in progress, skip
            if (isConnecting.get()) {
                Log.d("SocketService", "connect() called but a connection attempt is already in progress")
                _socketEvents.emit("[${getCurrentTime()}] Skipped connect - already connecting")
                return@launch
            }

            connectionMutex.withLock {
                try {
                    // If we already have a socket and it's connected, do nothing
                    if (mSocket != null) {
                        if (mSocket!!.connected()) {
                            Log.d("SocketService", "Socket already connected")
                            _connectionStatus.emit("Already connected")
                            _socketEvents.emit("[${getCurrentTime()}] Attempted connection - already connected")
                            return@withLock
                        } else {
                            // If socket exists but not connected, attempt to reuse it where possible
                            if (isConnecting.get()) {
                                Log.d("SocketService", "Socket exists and connection is in progress")
                                _socketEvents.emit("[${getCurrentTime()}] Socket exists and connection is in progress - skipping")
                                return@withLock
                            }

                            Log.d("SocketService", "Reusing existing socket instance to connect")
                            _connectionStatus.emit("Connecting (reusing socket)")
                            _socketEvents.emit("[${getCurrentTime()}] Reusing existing socket and calling connect()")
                            try {
                                isConnecting.set(true)
                                mSocket!!.connect()
                                Log.d("SocketService", "Called connect() on existing socket instance")
                            } catch (e: Exception) {
                                Log.e("SocketService", "Error calling connect on existing socket: ${e.message}", e)
                                _socketErrors.emit("[${getCurrentTime()}] ERROR reconnecting existing socket: ${e.message}")
                                isConnecting.set(false)
                            }
                            return@withLock
                        }
                    }

                    val userId = userPreferences.userId.first()
                    if (userId.isNullOrEmpty()) {
                        Log.d("SocketService", "No user ID available, skipping socket connection")
                        _connectionStatus.emit("No user ID - cannot connect")
                        _socketErrors.emit("[${getCurrentTime()}] ERROR: No user ID available")
                        return@withLock
                    }

                    // Prevent another concurrent connect attempt while we prepare/create the socket
                    if (!isConnecting.compareAndSet(false, true)) {
                        Log.d("SocketService", "Another connect started concurrently - skipping this attempt")
                        _socketEvents.emit("[${getCurrentTime()}] Skipped concurrent connect attempt")
                        return@withLock
                    }

                    val options = IO.Options().apply {
                        // Avoid forceNew: reusing manager/socket keeps socket identity stable
                        forceNew = false
                        reconnection = true           // Enable auto-reconnection
                        reconnectionAttempts = 10     // Try 10 times before giving up
                        reconnectionDelay = 1000      // Wait 1 second before first retry
                        reconnectionDelayMax = 5000   // Max 5 seconds between retries
                        timeout = 20000               // Connection timeout 20 seconds
                    }
                    val connectionUrl = "$RIDE_URL?authid=$userId"
                    Log.d("SocketService", "DEBUG: Connecting to URL: $connectionUrl")
                    _connectionStatus.emit("Connecting...")
                    _socketEvents.emit("[${getCurrentTime()}] Attempting connection to: $RIDE_URL")
                    _socketEvents.emit("[${getCurrentTime()}] User ID: $userId")

                    // Ensure any previous socket is cleaned up before creating new one
                    if (mSocket != null) {
                        Log.w("SocketService", "Unexpected existing socket instance found - cleaning up before creating new socket")
                        _socketEvents.emit("[${getCurrentTime()}] Cleaning up unexpected existing socket instance before new socket creation")
                        closeSocket()
                    }

                    // Create a new socket only when there is no existing instance
                    val socket = IO.socket(connectionUrl, options)

                    // Assign and set listeners
                    mSocket = socket

                    // Setup listeners before connecting
                    setupListeners(socket)

                    socket.connect()
                    Log.d("SocketService", "DEBUG: socket.connect() called.")

                } catch (e: URISyntaxException) {
                    Log.e("SocketService", "DEBUG: URISyntaxException: ${e.message}")
                    _connectionStatus.emit("Connection failed: URI error")
                    _socketErrors.emit("[${getCurrentTime()}] ERROR: URI Syntax Error - ${e.message}")
                    isConnecting.set(false)
                } catch (e: Exception) {
                    Log.e("SocketService", "DEBUG: Exception in connect: ${e.message}")
                    _connectionStatus.emit("Connection failed: ${e.message}")
                    _socketErrors.emit("[${getCurrentTime()}] ERROR: ${e.message}")
                    isConnecting.set(false)
                }
            }
        }
    }

    // Helper to safely cleanup and close existing socket instance
    private fun closeSocket() {
        try {
            mSocket?.let { socket ->
                try {
                    // Remove all listeners attached to this socket
                    socket.off()
                } catch (e: Exception) {
                    Log.w("SocketService", "Warning while removing listeners: ${e.message}")
                }
                try {
                    if (socket.connected()) {
                        socket.disconnect()
                    }
                } catch (e: Exception) {
                    Log.w("SocketService", "Warning while disconnecting socket: ${e.message}")
                }
                try {
                    // Close releases underlying resources
                    socket.close()
                } catch (e: Exception) {
                    Log.w("SocketService", "Warning while closing socket: ${e.message}")
                }
            }
        } finally {
            mSocket = null
            isConnecting.set(false)
        }
    }

    private fun getCurrentTime(): String {
        val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    private fun emitAcknowledge(eventId: String) {
        val json = JSONObject().put("eventId", eventId)
        mSocket?.emit("received", json)
        Log.d("SocketService", "Emitting acknowledge for eventId: $eventId")
    }

    private fun setupListeners(socket: Socket) {
        socket.on(Socket.EVENT_CONNECT) {
            Log.d("SocketService", "DEBUG: >>>>>>>>>> Socket.EVENT_CONNECT: Successfully connected! <<<<<<<<<<")
            coroutineScope.launch {
                // Clear connecting flag when socket is connected
                isConnecting.set(false)
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
                isConnecting.set(false)
                _connectionStatus.emit("Disconnected")
                _socketEvents.emit("[${getCurrentTime()}] ❌ DISCONNECTED - Reason: ${args.getOrNull(0)}")
            }
        }

        socket.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Log.e("SocketService", "DEBUG: >>>>>>>>>> Socket.EVENT_CONNECT_ERROR: Connection Error! Reason: ${args.getOrNull(0)} <<<<<<<<<<")
            coroutineScope.launch {
                isConnecting.set(false)
                _connectionStatus.emit("Connection error")
                _socketErrors.emit("[${getCurrentTime()}] ❌ CONNECTION ERROR: ${args.getOrNull(0)}")
            }
        }

        // Reconnection events
        socket.on("reconnect") { args ->
            Log.d("SocketService", "DEBUG: >>>>>>>>>> Socket reconnected after ${args.getOrNull(0)} attempts <<<<<<<<<<")
            coroutineScope.launch {
                isConnecting.set(false)
                _connectionStatus.emit("Reconnected")
                _socketEvents.emit("[${getCurrentTime()}] 🔄 RECONNECTED after ${args.getOrNull(0)} attempts")
            }
        }

        socket.on("reconnect_attempt") { args ->
            Log.d("SocketService", "DEBUG: >>>>>>>>>> Socket reconnection attempt ${args.getOrNull(0)} <<<<<<<<<<")
            coroutineScope.launch {
                _connectionStatus.emit("Reconnecting...")
                _socketEvents.emit("[${getCurrentTime()}] 🔄 Reconnection attempt ${args.getOrNull(0)}")
            }
        }

        socket.on("reconnect_error") { args ->
            Log.e("SocketService", "DEBUG: >>>>>>>>>> Socket reconnection error: ${args.getOrNull(0)} <<<<<<<<<<")
            coroutineScope.launch {
                isConnecting.set(false)
                _socketErrors.emit("[${getCurrentTime()}] ❌ RECONNECTION ERROR: ${args.getOrNull(0)}")
            }
        }

        socket.on("reconnect_failed") {
            Log.e("SocketService", "DEBUG: >>>>>>>>>> Socket reconnection failed after all attempts <<<<<<<<<<")
            coroutineScope.launch {
                isConnecting.set(false)
                _connectionStatus.emit("Reconnection failed")
                _socketErrors.emit("[${getCurrentTime()}] ❌ RECONNECTION FAILED - all attempts exhausted")
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
                        // If app is backgrounded, prefer the overlay-first flow: start CustomFullScreenOverlayService
                        try {
                            val app = KabukabuDriverApp.getInstance()
                            if (!app.isInForeground) {
                                val canOverlay = android.provider.Settings.canDrawOverlays(app.applicationContext)
                                if (canOverlay) {
                                    val overlayIntent = Intent(app.applicationContext, CustomFullScreenOverlayService::class.java).apply {
                                        putExtra("trip_id", tripFoundData.eventId)
                                        // Serialize tripFoundData to JSON to forward full details
                                        try {
                                            val tripJson = moshi.adapter(TripFoundEvent::class.java).toJson(tripFoundData)
                                            putExtra("trip_details", tripJson)
                                        } catch (_: Throwable) {}
                                    }
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        app.applicationContext.startForegroundService(overlayIntent)
                                    } else {
                                        app.applicationContext.startService(overlayIntent)
                                    }
                                    _socketEvents.emit("[${getCurrentTime()}] 🔔 Started CustomFullScreenOverlayService because app is backgrounded and overlay permission exists")
                                } else {
                                    com.kabukabu.driver.services.TripRequestService.startService(app.applicationContext)
                                    _socketEvents.emit("[${getCurrentTime()}] 🔔 Started TripRequestService because overlay permission not available")
                                }
                            } else {
                                _socketEvents.emit("[${getCurrentTime()}] ℹ️ App is foregrounded - UI will handle trip request presentation")
                            }
                        } catch (e: Exception) {
                            _socketErrors.emit("[${getCurrentTime()}] ❌ ERROR starting overlay/trip service: ${e.message}")
                        }
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
//                    Log.d("SocketService", "Successfully parsed 'trip-cancelled' event: Trip ID: ${tripCancelledData.order.id}, Status: ${tripCancelledData.status}, EventId: ${tripCancelledData.eventId}")
                    coroutineScope.launch {
                        _socketEvents.emit("[${getCurrentTime()}] ❌ TRIP CANCELLED - Trip ID: ${tripCancelledData.order.id}")
                        _tripCancelledEvent.emit(tripCancelledData)
                        emitAcknowledge(tripCancelledData.eventId)
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

        socket.on("support-message") { args ->
            Log.d("SocketService", "RAW_DATA: 'supportMessage' Received: ${args.getOrNull(0)}")
            coroutineScope.launch {
                _socketEvents.emit("[${getCurrentTime()}] 📥 Received 'supportMessage' event")
            }
            try {
                val data = args.getOrNull(0)
                if (data is JSONObject) {
                    Log.d("SocketService", "Support message received: ${data.toString()}")
                    coroutineScope.launch {
                        _supportMessageEvent.emit(data)
                    }
                }
            } catch (e: Exception) {
                Log.e("SocketService", "Error parsing 'supportMessage' event", e)
                coroutineScope.launch {
                    _socketErrors.emit("[${getCurrentTime()}] ❌ ERROR parsing 'supportMessage': ${e.message}")
                }
            }
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
                } catch (e: Exception) {
                    Log.e("SocketService", "Error emitting join-room event: ${e.message}", e)
                }
            }.join()
        } catch (e: Exception) {
            Log.e("SocketService", "Error in joinTripRoom: ${e.message}", e)
        }
    }

    suspend fun joinSupportRoom(supportId: String) {
        try {
            if (mSocket == null || !mSocket!!.connected()) {
                Log.e("SocketService", "Cannot join support room: Socket is null or not connected. Attempting to reconnect...")
                connect()
                delay(500) // Give it a moment to connect
            }

            if (mSocket == null) {
                Log.e("SocketService", "Failed to join support room: Socket is still null after reconnection attempt")
                return
            }

            if (!mSocket!!.connected()) {
                Log.e("SocketService", "Failed to join support room: Socket is still disconnected after reconnection attempt")
                return
            }

            Log.d("SocketService", "Socket connected status before join-support: ${mSocket?.connected()}")

            coroutineScope.launch {
                try {

                    mSocket?.emit("join-room", supportId)
                    Log.d("SocketService", "EMITTED join-support event for ticket: $supportId")
                } catch (e: Exception) {
                    Log.e("SocketService", "Error emitting join-support event: ${e.message}", e)
                }
            }.join()
        } catch (e: Exception) {
            Log.e("SocketService", "Error in joinSupportRoom: ${e.message}", e)
        }
    }

    fun disconnect() {
        // Use safe cleanup helper to fully release socket resources
        closeSocket()
        Log.d("SocketService", "Socket Disconnected and cleaned up!")
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
     * Emit a support message to a specific support ticket
     * @param ticketId The support ticket ID (room)
     * @param userId The driver's user ID
     * @param content The message content
     * @param files Optional list of file URLs/paths
     * @param replyTo Optional message ID for replying
     */
    fun emitSupportMessage(
        ticketId: String,
        userId: String,
        content: String,
        files: List<String> = emptyList(),
        replyTo: String? = null
    ) {
        val payload = JSONObject().apply {
            put("room", ticketId)
            put("userId", userId)
            put("content", content)
            if (files.isNotEmpty()) {
                put("files", org.json.JSONArray(files))
            }
            if (replyTo != null) {
                put("replyTo", replyTo)
            }
        }
        mSocket?.emit("support-message", payload)
        Log.d("SocketService", "Emitting support message to ticket: $ticketId, content: $content, files: $files, replyTo: $replyTo")
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

    /**
     * Join a support room
     * @param ticketId The support ticket ID
     */
//    fun joinSupportRoom(ticketId: String) {
//        try {
//            val json = JSONObject().apply {
//                put("room", ticketId)
//            }
//            mSocket?.emit("join-support-room", json)
//            Log.d("SocketService", "Joining support room: $ticketId")
//        } catch (e: Exception) {
//            Log.e("SocketService", "Error joining support room: ${e.message}", e)
//        }
//    }

    /**
     * Send support message via socket (text only)
     * @param ticketId Support ticket ID (room)
     * @param userId User ID
     * @param content Message content
     * @param files List of file URLs/paths (optional)
     * @param replyTo Message ID being replied to (optional)
     */
   fun sendSupportMessage(
        ticketId: String,
        userId: String,
        content: String,
        files: List<String> = emptyList(),
        replyTo: String? = null
    ) {
        try {
            if (mSocket == null || !mSocket!!.connected()) {
                Log.w("SocketService", "Cannot send support message: Socket is null or not connected")
                return
            }

            val json = JSONObject().apply {
                put("room", ticketId)
                put("userId", userId)
                put("content", content)
                if (files.isNotEmpty()) {
                    put("files", org.json.JSONArray(files))
                }
                if (replyTo != null) {
                    put("replyTo", replyTo)
                }
            }

            mSocket?.emit("support-message", json)
            Log.d("SocketService", "Sending support message to room: $ticketId, content: $content, files: $files")
        } catch (e: Exception) {
            Log.e("SocketService", "Error sending support message: ${e.message}", e)
        }
    }

}