package com.kabukabu.driver.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.kabukabu.driver.MainActivity
import com.kabukabu.driver.R
import com.kabukabu.driver.core.data.socket.SocketService
import com.kabukabu.driver.core.data.socket.TripFoundEvent
import com.kabukabu.driver.core.data.TripRequestData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

/**
 * Foreground service that listens for trip requests and displays full-screen notifications
 * when the app is in background
 */
class TripRequestService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var isTripRequestActive = false

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "trip_request_channel"
        private const val CHANNEL_NAME = "Trip Requests"

        const val ACTION_START_SERVICE = "START_SERVICE"
        const val ACTION_STOP_SERVICE = "STOP_SERVICE"
        const val ACTION_ACCEPT_TRIP = "ACCEPT_TRIP"
        const val ACTION_DECLINE_TRIP = "DECLINE_TRIP"

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
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVICE -> {
                startForegroundService()
                startListeningForTripRequests()
            }
            ACTION_STOP_SERVICE -> {
                stopSelf()
            }
            ACTION_ACCEPT_TRIP -> {
                // Handle accept action - this will be processed by the activity
                sendBroadcast(Intent("com.kabukabu.driver.ACCEPT_TRIP"))
            }
            ACTION_DECLINE_TRIP -> {
                // Handle decline action
                sendBroadcast(Intent("com.kabukabu.driver.DECLINE_TRIP"))
                isTripRequestActive = false
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundService() {
        val notification = createOnlineNotification()
        startForeground(NOTIFICATION_ID, notification)
        Log.d("TripRequestService", "Foreground service started")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Use IMPORTANCE_MAX to ensure heads-up notifications
            val importance = NotificationManager.IMPORTANCE_MAX
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications for trip requests"
                setSound(null, null) // We'll handle sound in the app
                setShowBadge(true)
                enableLights(true)
                enableVibration(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
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
            .setSmallIcon(R.drawable.ic_red_marker) // Use your app icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startListeningForTripRequests() {
        serviceScope.launch {
            SocketService.tripFoundEvent.collectLatest { tripEvent ->
                Log.d("TripRequestService", "Trip request received: ${tripEvent.eventId}")
                if (!isTripRequestActive) {
                    isTripRequestActive = true
                    showTripRequestFullScreenNotification(tripEvent)
                }
            }
        }
    }

    private fun showTripRequestFullScreenNotification(tripEvent: TripFoundEvent) {
        // Store the trip event for the activity to access
        TripRequestData.currentTripEvent = tripEvent

        // Create intent to launch TripRequestActivity with proper flags
        val fullScreenIntent = Intent(this, com.kabukabu.driver.TripRequestActivity::class.java).apply {
            // Add flags to bypass background launch restrictions
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            addFlags(Intent.FLAG_FROM_BACKGROUND)
            putExtra("trip_id", tripEvent.eventId)
            putExtra("trip_details", tripEvent.toString())
        }


        // Create PendingIntent with full-screen intent
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(), // Unique request code
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create notification with full-screen intent (this is the key to bypass restrictions)
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_red_marker)
            .setContentTitle("New Trip Request!")
            .setContentText("Tap to view and accept trip")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)  // This triggers the activity
            .setContentIntent(fullScreenPendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setTimeoutAfter(30000) // 30 seconds timeout

        // Add action buttons for quick response
        val acceptIntent = Intent(this, TripRequestService::class.java).apply {
            action = ACTION_ACCEPT_TRIP
        }
        val acceptPendingIntent = PendingIntent.getService(
            this, 1, acceptIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val declineIntent = Intent(this, TripRequestService::class.java).apply {
            action = ACTION_DECLINE_TRIP
        }
        val declinePendingIntent = PendingIntent.getService(
            this, 2, declineIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        notificationBuilder
            .addAction(R.drawable.ic_red_marker, "Accept", acceptPendingIntent)
            .addAction(R.drawable.ic_red_marker, "Decline", declinePendingIntent)

        val notificationManager = getSystemService(NotificationManager::class.java)
        val notification = notificationBuilder.build()

        // Show notification which will trigger full-screen intent
        notificationManager.notify(NOTIFICATION_ID + 1, notification)

        Log.d("TripRequestService", "Trip request notification shown with full-screen intent")

        // For Android 14+, we need to check if USE_FULL_SCREEN_INTENT is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (!notificationManager.canUseFullScreenIntent()) {
                Log.w("TripRequestService", "Full-screen intent permission not granted - notification will show in tray")
                // Try to send the pending intent manually
                try {
                    fullScreenPendingIntent.send()
                    Log.d("TripRequestService", "Manually triggered pending intent")
                } catch (e: Exception) {
                    Log.e("TripRequestService", "Failed to send pending intent: ${e.message}")
                }
            } else {
                Log.d("TripRequestService", "Full-screen intent permission granted - activity should launch")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d("TripRequestService", "Service destroyed")
    }
}

