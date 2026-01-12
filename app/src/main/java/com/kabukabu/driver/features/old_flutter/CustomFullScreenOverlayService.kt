// ...existing code...
package com.kabukabu.driver.features.old_flutter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.core.app.NotificationCompat
import android.view.Gravity
import com.kabukabu.driver.R
import com.kabukabu.driver.TripRequestActivity
import com.kabukabu.driver.core.data.TripRequestData

class CustomFullScreenOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private val CHANNEL_ID = "OverlayServiceChannel"

    override fun onCreate() {
        super.onCreate()

        // Create the notification channel
        createNotificationChannel()

        // Build the notification so the service can run in foreground
        val notificationIntent = Intent(this, com.kabukabu.driver.MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE // Added FLAG_IMMUTABLE for Android 12+
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Kabukabu Driver")
            .setContentText("Waiting for trip request...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()

        // Start the service in the foreground
        startForeground(1, notification)

        // Inflate the custom full-screen layout
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.custom_overlay, null)

        // Set the layout parameters for the overlay
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.CENTER
        // Add the overlay to the window manager
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(overlayView, params)

        // Handle button click to open the app
        overlayView.findViewById<Button>(R.id.overlay_button).setOnClickListener {
            // Try to forward the trip data and open TripRequestActivity directly
            val tripEvent = TripRequestData.currentTripEvent
            Log.d("CustomFullScreenOverlayService", "Overlay button clicked, tripEvent=${tripEvent?.eventId}")

            val targetIntent = Intent(this, TripRequestActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("trip_id", tripEvent?.eventId)
                putExtra("trip_details", tripEvent?.toString())
            }

            try {
                startActivity(targetIntent)
            } catch (e: Exception) {
                Log.w("CustomFullScreenOverlayService", "startActivity for TripRequestActivity failed, falling back to notification: ${e.message}")
                // Fallback: build a heads-up notification that opens TripRequestActivity via a PendingIntent
                try {
                    val stackBuilder = TaskStackBuilder.create(this).apply {
                        addNextIntentWithParentStack(Intent(this@CustomFullScreenOverlayService, com.kabukabu.driver.MainActivity::class.java))
                        addNextIntent(targetIntent)
                    }

                    val pending = stackBuilder.getPendingIntent(
                        (tripEvent?.eventId ?: System.currentTimeMillis().toString()).hashCode(),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val notif = NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("New Trip Request")
                        .setContentText("Tap to open trip request")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pending)
                        .setAutoCancel(true)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .build()

                    val nm = getSystemService(NotificationManager::class.java)
                    nm.notify(9999, notif)
                } catch (inner: Exception) {
                    Log.e("CustomFullScreenOverlayService", "Fallback notification failed: ${inner.message}")
                }
            }

            // Remove overlay and stop the service
            try {
                if (::overlayView.isInitialized) windowManager.removeView(overlayView)
            } catch (e: Exception) {
                Log.w("CustomFullScreenOverlayService", "Error removing overlay view: ${e.message}")
            }
            stopSelf() // Stop the service once the app is opened
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            try { windowManager.removeView(overlayView) } catch (_: Exception) {}
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
// ...existing code...
