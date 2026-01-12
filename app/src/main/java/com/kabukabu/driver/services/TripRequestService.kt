package com.kabukabu.driver.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.MainActivity
import com.kabukabu.driver.R
import com.kabukabu.driver.core.data.TripRequestData
import com.kabukabu.driver.core.data.location.LocationRepository
import com.kabukabu.driver.core.data.socket.SocketService
import com.kabukabu.driver.core.data.socket.TripFoundEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Foreground service that listens for trip requests and handles background location updates.
 */
class TripRequestService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var isTripRequestActive = false

    private val locationRepository: LocationRepository by lazy {
        LocationRepository.getInstance(applicationContext)
    }

    private val userPreferences by lazy {
        KabukabuDriverApp.getInstance().userPreferences
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "trip_request_channel"
        private const val CHANNEL_NAME = "Trip Requests"

        const val ACTION_START_SERVICE = "START_SERVICE"
        const val ACTION_STOP_SERVICE = "STOP_SERVICE"

        fun startService(context: Context) {
            val intent = Intent(context, TripRequestService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, TripRequestService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("TripRequestService", "Service created")
        createNotificationChannel()
        locationRepository.startLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVICE -> {
                startForegroundService()
                SocketService.connect()
                startListeningForTripRequests()
                startLocationEmitter()
            }
            ACTION_STOP_SERVICE -> {
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startLocationEmitter() {
        serviceScope.launch(Dispatchers.IO) {
            locationRepository.getLocationFlow().collect { location ->
                location?.let {
                    try {
                        // Get a snapshot of userDetails to find the active trip
                        val userDetails = userPreferences.userDetails.firstOrNull()
                        val orderId = userDetails?.activeTrip?.id ?: ""

                        SocketService.emitLocation(
                            lat = it.latitude,
                            long = it.longitude,
                            orderId = orderId
                        )
                        Log.d("TripRequestService", "Emitted location: (${it.latitude}, ${it.longitude}) for order: $orderId")
                    } catch (e: Exception) {
                        Log.e("TripRequestService", "Error emitting location: ${e.message}", e)
                    }
                }
            }
        }
    }

    // ... (rest of the service remains the same)

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundService() {
        val notification = createOnlineNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } catch (t: Throwable) {
                Log.w("TripRequestService", "startForeground with type failed, falling back: ${t.message}")
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        Log.d("TripRequestService", "Foreground service started")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Notifications for trip requests"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createOnlineNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Kabukabu Driver")
            .setContentText("You are online and ready to receive trips")
            .setSmallIcon(R.drawable.ic_stat_onesignal_default) // Use your app icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startListeningForTripRequests() {
        serviceScope.launch {
            try {
                SocketService.tripFoundEvent.collectLatest { tripEvent ->
                    if (!isTripRequestActive) {
                        isTripRequestActive = true
                        showTripRequestFullScreenNotification(tripEvent)
                    } else {
                        Log.d("TripRequestService", "Ignoring trip request ${tripEvent.eventId} because a request is already active")
                    }
                }
            } catch (e: Exception) {
                Log.e("TripRequestService", "Error collecting tripFoundEvent: ${e.message}", e)
            }
        }
    }

    private fun showTripRequestFullScreenNotification(tripEvent: TripFoundEvent) {
        TripRequestData.currentTripEvent = tripEvent

        val fullScreenIntent = Intent(this, com.kabukabu.driver.TripRequestActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_FROM_BACKGROUND)
            putExtra("trip_id", tripEvent.eventId)
            putExtra("trip_details", tripEvent.toString())
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_onesignal_default)
            .setContentTitle("New Trip Request!")
            .setContentText("Tap to view and accept trip")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setTimeoutAfter(15000)

        val notificationManager = getSystemService(NotificationManager::class.java)
        val notification = notificationBuilder.build()
        notificationManager.notify(NOTIFICATION_ID + 1, notification)

        Log.d("TripRequestService", "Trip request notification shown with full-screen intent")
    }

    override fun onDestroy() {
        super.onDestroy()
        locationRepository.stopLocationUpdates()
        serviceScope.cancel()
        Log.d("TripRequestService", "Service destroyed")
    }
}
