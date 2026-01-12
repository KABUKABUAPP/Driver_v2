package com.kabukabu.driver.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationUtils {
    const val CHANNEL_TRIP_REQUEST = "trip_request_channel"
    const val CHANNEL_CHAT_HEAD = "chat_head_channel"
    const val CHANNEL_FOREGROUND = "foreground_service_channel"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Trip request channel - high importance (used with fullScreenIntent)
        val tripChannel = NotificationChannel(
            CHANNEL_TRIP_REQUEST,
            "Trip Requests",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for incoming trip requests"
        }

        // Chat head service channel - low importance
        val chatHeadChannel = NotificationChannel(
            CHANNEL_CHAT_HEAD,
            "Chat Head Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Background service for chat head overlay"
        }

        // Foreground service channel - default importance
        val foregroundChannel = NotificationChannel(
            CHANNEL_FOREGROUND,
            "Foreground Service",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "General foreground service notifications"
        }

        // Create or update channels
        manager.createNotificationChannel(tripChannel)
        manager.createNotificationChannel(chatHeadChannel)
        manager.createNotificationChannel(foregroundChannel)
    }
}

