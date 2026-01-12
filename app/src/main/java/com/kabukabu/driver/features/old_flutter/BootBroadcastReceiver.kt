//package com.kabu.kabukabu_driver
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.util.Log
//import io.flutter.embedding.android.FlutterActivity
//
//class BootBroadcastReceiver : BroadcastReceiver() {
//
//    override fun onReceive(context: Context, intent: Intent) {
//        // Check if the received intent action is BOOT_COMPLETED
//        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
//            Log.d("BootReceiver", "Device booted up!")
//
//            // Start the Flutter activity after boot completion
//            val flutterIntent = Intent(context, FlutterActivity::class.java)
//            flutterIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            context.startActivity(flutterIntent)
//        }
//    }
//}
