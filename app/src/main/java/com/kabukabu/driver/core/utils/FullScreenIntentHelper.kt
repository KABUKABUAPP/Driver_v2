package com.kabukabu.driver.core.utils

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi

object FullScreenIntentHelper {

    /**
     * Check if the app can use full-screen intents
     */
    fun canUseFullScreenIntent(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val canUse = notificationManager.canUseFullScreenIntent()
            Log.d("FullScreenIntentHelper", "Can use full-screen intent: $canUse")
            canUse
        } else {
            // Granted by default on Android < 14
            true
        }
    }

    /**
     * Open settings page where user can grant full-screen intent permission
     */
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun openFullScreenIntentSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Log.d("FullScreenIntentHelper", "Opened full-screen intent settings")
        } catch (e: Exception) {
            Log.e("FullScreenIntentHelper", "Failed to open settings: ${e.message}")
            // Fallback to app details settings
            openAppDetailsSettings(context)
        }
    }

    /**
     * Open app details settings as fallback
     */
    fun openAppDetailsSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Log.d("FullScreenIntentHelper", "Opened app details settings")
        } catch (e: Exception) {
            Log.e("FullScreenIntentHelper", "Failed to open app details: ${e.message}")
        }
    }

    /**
     * Check if we should show permission request to user
     */
    fun shouldRequestPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return false // Not needed on Android < 14
        }

        if (canUseFullScreenIntent(context)) {
            return false // Already granted
        }

        // Check if we've already asked (you can implement SharedPreferences flag here)
        return true
    }
}

