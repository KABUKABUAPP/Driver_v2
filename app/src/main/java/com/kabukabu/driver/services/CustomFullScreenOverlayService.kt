package com.kabukabu.driver.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.core.app.NotificationCompat
import com.kabukabu.driver.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CustomFullScreenOverlayService : Service() {

    private val CHANNEL_ID = "overlay_service_channel"
    private val NOTIF_ID = 4231

    // Coroutine scope for this service. It's cancelled in onDestroy.
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Keep references so we can remove the view in onDestroy
    private var overlayView: View? = null
    private var wm: WindowManager? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(startIntent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification(startIntent)

        // Start the service in the foreground, as required for overlays post-Android 8.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIF_ID, notification)
        }

        Log.d("CustomFullScreenOverlayService", "Service started in foreground.")

        // Show the overlay UI
        showOverlay(startIntent)

        return START_NOT_STICKY
    }

    private fun showOverlay(startIntent: Intent?) {
        if (!Settings.canDrawOverlays(this)) {
            Log.w("CustomFullScreenOverlayService", "Overlay permission not granted. Stopping service.")
            stopSelf()
            return
        }

        if (overlayView != null) {
            Log.d("CustomFullScreenOverlayService", "Overlay view is already showing.")
            return
        }

        try {
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.custom_overlay, null)
            overlayView = view

            // These parameters create a modal, dimmed overlay.
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
                dimAmount = 0.75f // 75% black dimming
            }

            wm?.addView(view, params)
            Log.d("CustomFullScreenOverlayService", "Overlay view added successfully.")

            // --- Button Click Listener ---
            val btn = view.findViewById<Button>(R.id.overlay_button)
            btn.setOnClickListener {
                Log.d("CustomFullScreenOverlayService", "'View Request' button clicked.")
                try {
                    val intentToApp = Intent(this, com.kabukabu.driver.MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        putExtra("from_overlay", true)
                        startIntent?.getStringExtra("trip_id")?.let { putExtra("trip_id", it) }
                        startIntent?.getStringExtra("trip_details")?.let { putExtra("trip_details", it) }
                    }
                    startActivity(intentToApp)
                    Log.d("CustomFullScreenOverlayService", "startActivity called.")
                } catch (e: Exception) {
                    Log.e("CustomFullScreenOverlayService", "Failed to start activity from overlay: ${e.message}", e)
                } finally {
                    // This is the key: stop the service. onDestroy() will handle the cleanup.
                    stopSelf()
                }
            }

            // Auto-dismiss the overlay after 30 seconds.
            serviceScope.launch {
                delay(20000)
                Log.d("CustomFullScreenOverlayService", "Overlay timing out after 20s.")
                stopSelf() // This will trigger onDestroy for cleanup.
            }

        } catch (e: Exception) {
            Log.e("CustomFullScreenOverlayService", "Fatal error showing overlay view: ${e.message}", e)
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // This is the single, definitive place to clean up the UI.
        try {
            overlayView?.let {
                if (it.isAttachedToWindow) {
                    Log.d("CustomFullScreenOverlayService", "Removing overlay view from window manager.")
                    wm?.removeView(it)
                }
            }
        } catch (e: Exception) {
            Log.e("CustomFullScreenOverlayService", "Error removing view during onDestroy: ${e.message}", e)
        }
        overlayView = null
        wm = null
        serviceScope.cancel() // Cancel all coroutines.
        Log.d("CustomFullScreenOverlayService", "Service destroyed and resources cleaned up.")
    }

    private fun createNotification(startIntent: Intent?): Notification {
        val fullScreenIntent = Intent(this, com.kabukabu.driver.MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startIntent?.extras?.let { putExtras(it) }
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Kabukabu Driver")
            .setContentText("Incoming trip request")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(pendingIntent, true) // Fallback for some devices
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Incoming Trip Requests",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Displays incoming trip requests over other apps."
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}