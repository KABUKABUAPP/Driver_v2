//package com.kabu.kabukabu_driver
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.app.Service
//import android.content.Intent
//import android.os.Build
//import android.util.Log
//import android.os.IBinder
//import androidx.core.app.NotificationCompat
//import com.kabu.kabukabu_driver.R
//
//class ForegroundService : Service(){
//    companion object {
//        private const val CHANNEL_ID = "CustomForegroundService"
//    }
//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                    CHANNEL_ID,
//                    "Foreground Service",
//                    NotificationManager.IMPORTANCE_DEFAULT
//            )
//            val manager = getSystemService(NotificationManager::class.java)
//            manager.createNotificationChannel(channel)
//            // Create an intent that will launch the main activity
//            val notificationIntent = Intent(this, MainActivity::class.java)
//            notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            val pendingIntent = PendingIntent.getActivity(
//                this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//            )
//            Log.d("ForegroundService", "Service starting")
//            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
//                    .setContentTitle("Kabukabu app")
//                    .setContentText("You are online.")
//                    .setSmallIcon(R.drawable.ic_stat_onesignal_default)
//                    .setContentIntent(pendingIntent)
//                    .setVibrate(null)
//                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                    .build()
//
//            startForeground(1, notification)
//            Log.d("ForegroundService", "Service started")
//        } else {
//
//            // Create an intent that will launch the main activity
//            val notificationIntent = Intent(this, MainActivity::class.java)
//            notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            val pendingIntent = PendingIntent.getActivity(
//                this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
//            )
//            val notification = NotificationCompat.Builder(this)
//                    .setContentTitle("Kabukabu app")
//                    .setContentText("You are online.")
//                    .setSmallIcon(R.drawable.ic_stat_onesignal_default)
//                    .setContentIntent(pendingIntent)
//                    .setVibrate(null)
//                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                    .build()
//
//            startForeground(1, notification)
//        }
//
//        return START_STICKY
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        stopForeground(true)
//    }
//
//
//}