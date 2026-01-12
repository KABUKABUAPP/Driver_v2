package com.kabukabu.driver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.kabukabu.driver.core.data.local.UserPreferences
import com.kabukabu.driver.services.ChatHeadService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class BootBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootBroadcastReceiver", "Device boot completed - checking chat-head boot opt-in")
            try {
                val prefs = UserPreferences.getInstance(context)
                // Read preference in a coroutine
                CoroutineScope(Dispatchers.Default).launch {
                    val enabled = prefs.bootStartEnabled.first()
                    if (enabled) {
                        // Start chat head service on boot if permission already granted
                        if (android.provider.Settings.canDrawOverlays(context)) {
                            val svcIntent = Intent(context, ChatHeadService::class.java)
                            ContextCompat.startForegroundService(context, svcIntent)
                            Log.d("BootBroadcastReceiver", "ChatHeadService started on boot (boot opt-in)")
                        } else {
                            Log.d("BootBroadcastReceiver", "Boot opt-in true but overlay permission not granted; not starting service")
                        }
                    } else {
                        Log.d("BootBroadcastReceiver", "Boot-start opt-in is false; not starting service on boot")
                    }
                }
            } catch (e: Exception) {
                Log.e("BootBroadcastReceiver", "Error checking boot prefs: ${e.message}")
            }
        }
    }
}
