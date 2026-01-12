//package com.kabu.kabukabu_driver
//
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.app.Service
//import android.content.Context
//import android.content.Intent
//import android.graphics.PixelFormat
//import android.os.Build
//import android.os.IBinder
//import android.view.LayoutInflater
//import android.view.View
//import android.view.WindowManager
//import android.widget.Button
//import androidx.core.app.NotificationCompat
//import android.view.Gravity
//
//class CustomFullScreenOverlayService : Service() {
//
//    private lateinit var windowManager: WindowManager
//    private lateinit var overlayView: View
//    private val CHANNEL_ID = "OverlayServiceChannel"
//
//    override fun onCreate() {
//        super.onCreate()
//
//        // Create the notification channel
//        createNotificationChannel()
//
//        // Build the notification
//        val notificationIntent = Intent(this, MainActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(
//            this,
//            0,
//            notificationIntent,
//            PendingIntent.FLAG_IMMUTABLE // Added FLAG_IMMUTABLE for Android 12+
//        )
//
//        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
//            .setContentTitle("Kabukabu Driver")
//            .setContentText("Waiting for trip request...")
//            .setSmallIcon(R.mipmap.ic_launcher)
//            .setContentIntent(pendingIntent)
//            .build()
//
//        // Start the service in the foreground
//        startForeground(1, notification)
//
//        // Inflate the custom full-screen layout
//        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        overlayView = inflater.inflate(R.layout.custom_overlay, null)
//
//        // Set the layout parameters for the overlay
//        val params = WindowManager.LayoutParams(
//            WindowManager.LayoutParams.MATCH_PARENT,
//            WindowManager.LayoutParams.MATCH_PARENT,
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
//            } else {
//                WindowManager.LayoutParams.TYPE_PHONE
//            },
//            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
//            PixelFormat.TRANSLUCENT
//        )
//
//        params.gravity = Gravity.CENTER
//        // Add the overlay to the window manager
//        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
//        windowManager.addView(overlayView, params)
//
//        // Handle button click to open the app
//        overlayView.findViewById<Button>(R.id.overlay_button).setOnClickListener {
//            val intent = Intent(this, MainActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//            stopSelf() // Stop the service once the app is opened
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        if (::overlayView.isInitialized) {
//            windowManager.removeView(overlayView)
//        }
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }
//
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val serviceChannel = NotificationChannel(
//                CHANNEL_ID,
//                "Foreground Service Channel",
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
//            val manager = getSystemService(NotificationManager::class.java)
//            manager.createNotificationChannel(serviceChannel)
//        }
//    }
//}