//package com.kabu.kabukabu_driver
//
//import android.app.Service
//import android.content.Context
//import android.content.Intent
//import android.os.IBinder
//import android.app.PendingIntent
//import android.app.Notification
//import android.app.NotificationManager
//import android.app.NotificationChannel
//import android.os.Build
//import android.util.Log
//import io.flutter.embedding.android.FlutterActivity
//import io.flutter.plugin.common.MethodChannel
//
//class MyForegroundService : Service() {
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        createNotificationChannel()
//        // Create intent to open the app when the notification is clicked
//        val notificationIntent = Intent(this, MainActivity::class.java)
//        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
//
//        val notification = Notification.Builder(this, "MyForegroundServiceChannel")
//            .setContentTitle("Driver App")
//            .setContentText("Rider is requesting a ride")
//            .setSmallIcon(R.drawable.ic_stat_onesignal_default)
//            .setContentIntent(pendingIntent)
//            .build()
//        startForeground(1, notification)
//        // Bring the app to the foreground
//        Log.d("ChatHeadService", "for back")
//        val launchIntent = Intent(this, MainActivity::class.java)
//        Log.d("ChatHeadService", "fore back second")
//        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        Log.d("ChatHeadService", "launchIntent")
//        startActivity(launchIntent)
//
//        return START_NOT_STICKY
//    }
//
//    private fun createNotificationChannel() {
//        Log.d("ChatHeadService", "ACTION_DOWN")
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            Log.d("ChatHeadService", "ACTION_DOWN")
//            val serviceChannel = NotificationChannel(
//                "MyForegroundServiceChannel",
//                "MyForeground Service Channel",
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
//            val manager = getSystemService(NotificationManager::class.java)
//            manager.createNotificationChannel(serviceChannel)
//        }
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }
//}