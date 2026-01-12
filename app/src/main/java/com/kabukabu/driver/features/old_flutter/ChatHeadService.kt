//package com.kabukabu.driver.services
//
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.Service
//import android.content.Context
//import android.content.Intent
//import android.content.pm.ServiceInfo
//import android.graphics.PixelFormat
//import android.os.Build
//import android.os.IBinder
//import android.util.Log
//import android.view.Gravity
//import android.view.MotionEvent
//import android.view.View
//import android.view.WindowManager
//import androidx.appcompat.widget.AppCompatImageView
//import androidx.core.app.NotificationCompat
//import com.kabukabu.driver.R
//
//class ChatHeadService : Service() {
//
//    private lateinit var windowManager: WindowManager
//    private lateinit var chatHeadView: AppCompatImageView
//
//    // Threshold for detecting drag vs click
//    private val CLICK_THRESHOLD = 20
//
//    override fun onCreate() {
//        super.onCreate()
//
//        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
//
//        // Create a notification to comply with foreground service requirements and start foreground first
//        val notificationChannelId = com.kabukabu.driver.util.NotificationUtils.CHANNEL_CHAT_HEAD
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                notificationChannelId,
//                "Chat Head Service Channel",
//                NotificationManager.IMPORTANCE_LOW
//            )
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        val notification: Notification = NotificationCompat.Builder(this, notificationChannelId)
//            .setContentTitle("Kabukabu Service")
//            .setContentText("Kabukabu is running")
//            .setSmallIcon(R.drawable.ic_launcher_foreground)
//            .build()
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
//            try {
//                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
//            } catch (_: Throwable) {
//                startForeground(1, notification)
//            }
//        } else {
//            startForeground(1, notification)
//        }
//
//        // Override performClick to define the action when the chat head is tapped
//        chatHeadView = object : AppCompatImageView(this) {
//            override fun performClick(): Boolean {
//                super.performClick()
//                try {
//                    // This intent, combined with launchMode="singleTask" in the manifest,
//                    // will bring the existing app task to the foreground, preserving the back stack.
//                    val intent = Intent(applicationContext, com.kabukabu.driver.MainActivity::class.java).apply {
//                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
//                    }
//                    applicationContext.startActivity(intent)
//
//                } catch (e: Exception) {
//                    Log.e("ChatHeadService", "performClick failed to start activity: ${e.message}")
//                }
//
//                // The service's job is done once the app is open, so stop it.
//                stopSelf()
//
//                return true
//            }
//        }
//
//        // Make sure the view reports clickable to satisfy accessibility lint when using touch listener
//        chatHeadView.isClickable = true
//
//        // Use app launcher foreground drawable as chat head icon
//        chatHeadView.setImageResource(R.drawable.ic_overlay_logo_foreground)
//
//        // Set the size of the chat head icon
//        val iconSize = resources.getDimensionPixelSize(R.dimen.chat_head_size)
//
//        val layoutParams = WindowManager.LayoutParams(
//            iconSize,
//            iconSize,
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
//            } else {
//                @Suppress("DEPRECATION")
//                WindowManager.LayoutParams.TYPE_PHONE
//            },
//            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                    or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//            PixelFormat.TRANSLUCENT
//        )
//
//        layoutParams.gravity = Gravity.START or Gravity.TOP
//
//        // Ensure overlay permission exists before adding the view
//        if (!android.provider.Settings.canDrawOverlays(this)) {
//            Log.w("ChatHeadService", "Overlay permission is not granted - stopping ChatHeadService")
//            stopSelf()
//            return
//        }
//
//        try {
//            windowManager.addView(chatHeadView, layoutParams)
//            Log.d("ChatHeadService", "Chat head view added to WindowManager")
//        } catch (t: Throwable) {
//            Log.e("ChatHeadService", "Failed to add view to window manager: ${t.message}")
//            // If adding view failed, stop the service to avoid orphaned foreground notification
//            try { stopSelf() } catch (_: Throwable) {}
//            return
//        }
//
//        // Handle touch events for dragging
//        chatHeadView.setOnTouchListener(object : View.OnTouchListener {
//            private var initialX = 0
//            private var initialY = 0
//            private var xOffset = 0
//            private var yOffset = 0
//            private var isDragging = false
//
//            override fun onTouch(v: View, event: MotionEvent): Boolean {
//                when (event.action) {
//                    MotionEvent.ACTION_DOWN -> {
//                        initialX = event.rawX.toInt()
//                        initialY = event.rawY.toInt()
//                        xOffset = (layoutParams.x - event.rawX).toInt()
//                        yOffset = (layoutParams.y - event.rawY).toInt()
//                        isDragging = false
//                        Log.d("ChatHeadService", "ACTION_DOWN")
//                        return true
//                    }
//                    MotionEvent.ACTION_MOVE -> {
//                        val xMovement = kotlin.math.abs(event.rawX.toInt() - initialX)
//                        val yMovement = kotlin.math.abs(event.rawY.toInt() - initialY)
//
//                        if (xMovement > CLICK_THRESHOLD || yMovement > CLICK_THRESHOLD) {
//                            isDragging = true
//                        }
//
//                        layoutParams.x = (event.rawX + xOffset).toInt()
//                        layoutParams.y = (event.rawY + yOffset).toInt()
//                        try {
//                            windowManager.updateViewLayout(chatHeadView, layoutParams)
//                        } catch (t: Throwable) {
//                            Log.e("ChatHeadService", "Failed to update layout: ${t.message}")
//                        }
//                        Log.d("ChatHeadService", "ACTION_MOVE")
//                        return true
//                    }
//                    MotionEvent.ACTION_UP -> {
//                        Log.d("ChatHeadService", "ACTION_UP")
//                        if (!isDragging) {
//                            // Launch the app when the chat head is clicked
//                            v.performClick()
//                        }
//                        return true
//                    }
//                }
//                return false
//            }
//        })
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        try {
//            if (::chatHeadView.isInitialized) {
//                windowManager.removeView(chatHeadView)
//            }
//        } catch (t: Throwable) {
//            // ignore
//        }
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }
//}
