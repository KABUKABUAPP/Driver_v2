//package com.kabu.kabukabu_driver
//
//import android.app.ActivityManager
//import android.app.Dialog
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.app.Service
//import android.content.Context
//import android.content.Intent
//import android.graphics.PixelFormat
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.os.IBinder
//import android.provider.Settings
//import android.view.LayoutInflater
//import android.view.MotionEvent
//import android.view.View
//import android.view.WindowManager
//import android.widget.Button
//import android.widget.TextView
//import android.widget.Toast
//import androidx.annotation.NonNull
//import androidx.annotation.RequiresApi
//import io.flutter.embedding.android.FlutterFragmentActivity
//import io.flutter.embedding.engine.FlutterEngine
//import io.flutter.plugin.common.MethodChannel
//
//
//class MainActivity: FlutterFragmentActivity() {
//    private lateinit var windowManager: WindowManager
//    private lateinit var layoutParams: WindowManager.LayoutParams
//    private var draggableCircleView: View? = null
//
//    private var hasOverlayPermission = false
//    private val CHANNEL = "com.kabu.kabukabu_driver/chathead"
//    private val CHANNELFULL = "com.kabu.kabukabu_driver/full"
//
//    companion object {
//        private const val REQUEST_OVERLAY_PERMISSION = 1
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        checkOverlayPermission()
//    }
//
//    private fun checkOverlayPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!Settings.canDrawOverlays(this)) {
//                showOverlayPermissionDialog()
//            } else {
//                hasOverlayPermission = true
//            }
//        } else {
//            hasOverlayPermission = true
//        }
//    }
//
//    private fun showOverlayPermissionDialog() {
//        val inflater = LayoutInflater.from(this)
//        val dialogView = inflater.inflate(R.layout.custom_alert_dialog, null)
//
//        // Get references to the views within the layout
//        val titleTextView = dialogView.findViewById<TextView>(R.id.alert_title)
//        val messageTextView = dialogView.findViewById<TextView>(R.id.alert_message)
//        val positiveButton = dialogView.findViewById<Button>(R.id.positive_button)
//        val negativeButton = dialogView.findViewById<Button>(R.id.negative_button)
//
//        // Set the text for the title and message
//        titleTextView.text = "Overlay Permission Required"
//        messageTextView.text = "This app needs permission to display overlays for chat heads and custom views. Would you like to grant permission?"
//
//        // Create a custom dialog with the inflated layout
//        val dialog = Dialog(this, android.R.style.Theme_Translucent_NoTitleBar)
//        dialog.setContentView(dialogView)
//        dialog.setCancelable(false)
//
//        // Set click listeners for the buttons
//        positiveButton.setOnClickListener {
//            requestOverlayPermission()
//            dialog.dismiss()
//        }
//        negativeButton.setOnClickListener {
//            dialog.dismiss()
//        }
//
//        // Show the custom dialog
//        dialog.show()
//    }
//
//    private fun requestOverlayPermission() {
//       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!Settings.canDrawOverlays(this)) {
//                val intent = Intent(
//                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                    Uri.parse("package:$packageName")
//                )
//                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
//            }
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                hasOverlayPermission = Settings.canDrawOverlays(this)
//            }
//        }
//    }
//
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
//        super.configureFlutterEngine(flutterEngine)
//
//        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
//            if (call.method == "startService") {
//                startChatHeadService()
//            } else if (call.method == "stopService") {
//                stopChatHeadService()
//            }
//        }
//
//        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "com.kabu.kabukabu_driver/custom_overlay")
//            .setMethodCallHandler { call, result ->
//                if (call.method == "showOverlay") {
//
//                    startCustomFullScreenOverlayService()
//            } else if (call.method == "stopOverlayService") {
//                stopCustomFullScreenOverlayService()
//            }
//            }
//
//        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNELFULL).setMethodCallHandler { call, result ->
//            if (call.method == "triggerForegroundService") {
//                triggerForegroundService()
//                result.success(null)
//            } else {
//                result.notImplemented()
//            }
//        }
//
//        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "CustomForegroundService")
//                .setMethodCallHandler { call, result ->
//                    when (call.method) {
//                        "startForegroundService" -> {
//                            startForegroundServiceMethod() // start the foreground service
//                            result.success("Foreground service started successfully")
//                        }
//
//                         "stopForegroundService" -> {
//                            stopForegroundServiceMethod()
//                            result.success("Foreground service stopped successfully")
//                        }
//
//                        "showDraggableCircle" -> {
//                            showDraggableCircle()
//                            result.success("Show Draggable Circle....")
//                        }
//                        "hideDraggableCircle" -> {
//                            hideDraggableCircle()
//                            result.success("hide Draggable Circle....")
//                        }
//                        "hasOverlaysPermission" -> {
//                            hasOverlaysPermission()
//                            result.success(hasOverlaysPermission())
//                        }
//
//                        "requestOverlayPermission" -> {
//                            requestOverlayPermission()
//                        }
//
//                        else -> result.notImplemented()
//                    }
//                }
//    }
//
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun showDraggableCircle() {
//        if (draggableCircleView != null) {
//            return
//        }
//        // Initialize WindowManager and LayoutParams
//        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
//        layoutParams = WindowManager.LayoutParams(
//            WindowManager.LayoutParams.WRAP_CONTENT,
//            WindowManager.LayoutParams.WRAP_CONTENT,
//            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//            PixelFormat.TRANSLUCENT
//        )
//
//        // Set the initial position to top-left corner
//       var width = getWindowManager().getDefaultDisplay().getWidth()
//       var height = getWindowManager().getDefaultDisplay().getHeight()
//        //val width = windowManager.defaultDisplay.width
//    //val height = windowManager.defaultDisplay.height
//
//        // Get the screen width
//        val displayMetrics = resources.displayMetrics
//        val screenWidth = displayMetrics.widthPixels
//
//
//
//        layoutParams.x = screenWidth - 250 - width
//        layoutParams.y =  -height
//        // Inflate the draggable circle layout
//        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        draggableCircleView = inflater.inflate(R.layout.new_logo_layout, null)
//
//        // Set up the onTouchListener for dragging
//        draggableCircleView?.setOnTouchListener(object : View.OnTouchListener {
//            private var initialX = 0
//            private var initialY = 0
//            private var initialTouchX = 0f
//            private var initialTouchY = 0f
//
//
//
//            override fun onTouch(v: View, event: MotionEvent): Boolean {
//                when (event.action) {
//                    MotionEvent.ACTION_DOWN -> {
//                        initialX = layoutParams.x
//                        initialY = layoutParams.y
//                        initialTouchX = event.rawX
//                        initialTouchY = event.rawY
//                        return true
//                    }
//                    MotionEvent.ACTION_MOVE -> {
//                        layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
//                        layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
//                        windowManager.updateViewLayout(draggableCircleView, layoutParams)
//                        return true
//                    }
//
//                    MotionEvent.ACTION_UP -> {
//                        // Snap the circle back to the top left corner (0, 0)
////                        layoutParams.x =
////                            initialX
////                        layoutParams.y =
////                            initialY
//                        //windowManager.updateViewLayout(draggableCircleView, layoutParams)
//                        //openMainApp()
//                        // If the touch didn't move much, consider it a click
//                         val clickThreshold = 10
//                        if (Math.abs(event.rawX - initialTouchX) < clickThreshold && Math.abs(event.rawY - initialTouchY) < clickThreshold) {
//                            openMainApp()
//                        }
//                        return true
//                    }
//
//
//                }
//                return false
//            }
//        })
//
//        // Add the view to the window
//        windowManager.addView(draggableCircleView, layoutParams)
//    }
//
//    private fun hideDraggableCircle() {
//        if (draggableCircleView != null) {
//            windowManager.removeView(draggableCircleView)
//            draggableCircleView = null
//        }
//    }
//
//    private fun openMainApp() {
//        val intent = Intent().apply {
//            setClassName("com.kabu.kabukabu_driver", "com.kabu.kabukabu_driver.MainActivity")
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            action = Intent.ACTION_MAIN
//            addCategory(Intent.CATEGORY_LAUNCHER)
//        }
//        //context.startActivity(intent)
//         startActivity(intent)
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun startForegroundServiceMethod() {
//        if (!isForegroundServiceRunning()) {
//            val serviceIntent = Intent(applicationContext, com.kabu.kabukabu_driver.ForegroundService::class.java)
//            applicationContext.startForegroundService(serviceIntent)
//        }
//    }
//
//    private fun hasOverlaysPermission(): Boolean {
//        return hasOverlayPermission
//    }
//
//
//    private fun stopForegroundServiceMethod() {
//        val serviceIntent = Intent(applicationContext, com.kabu.kabukabu_driver.ForegroundService::class.java)
//        applicationContext.stopService(serviceIntent)
//    }
//
//    private fun isForegroundServiceRunning(): Boolean {
//        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
//            if (ForegroundService::class.java.name == service.service.className) {
//                return true
//            }
//        }
//        return false
//    }
//
//    private fun getWindowManager(context: Context): WindowManager {
//        return context.applicationContext
//            .getSystemService(WINDOW_SERVICE) as WindowManager
//    }
//    private fun startChatHeadService() {
//        val intent = Intent(this, ChatHeadService::class.java)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(intent)
//        } else {
//            startService(intent)
//        }
//    }
//
//    private fun stopChatHeadService() {
//        val intent = Intent(this, ChatHeadService::class.java)
//        stopService(intent)
//    }
//
//    private fun startCustomFullScreenOverlayService() {
//        val intent = Intent(this, CustomFullScreenOverlayService::class.java)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(intent)
//        } else {
//            startService(intent)
//        }
//    }
//
//    private fun stopCustomFullScreenOverlayService() {
//        val intent = Intent(this, CustomFullScreenOverlayService::class.java)
//        stopService(intent)
//    }
//
//    private fun checkDrawOverlayPermission() {
//        if (!Settings.canDrawOverlays(this)) {
//            Toast.makeText(this, "Please allow overlay permission", Toast.LENGTH_LONG).show()
//            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
//            startActivity(intent)
//        }
//    }
//    // private fun startMyForegroundService() {
//    //     val intent = Intent(this, MyForegroundService::class.java)
//    //     ContextCompat.startForegroundService(this, intent)
//    // }
//
//    // Method to bring the app to the foreground
//    fun bringAppToForeground() {
//        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
//        launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
//        startActivity(launchIntent)
//    }
//
//    private fun triggerForegroundService() {
//        val intent = Intent(this, BackgroundService::class.java)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(intent)
//        } else {
//            startService(intent)
//        }
//    }
//    // private fun openFlutterScreen() {
//    //     val intent = Intent(this, FlutterActivity::class.java)
//    //     intent.putExtra("open_screen", "your_screen_name") // Pass any extra data if needed
//    //     startActivity(intent)
//    // }
//
//    // override fun onDestroy() {
//    //     super.onDestroy()
//    //     unregisterReceiver(receiver)
//    // }
//}
//
//class BackgroundService : Service() {
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        createNotificationChannel()
//
//        // Create intent to open the app when the notification is clicked
//        val notificationIntent = Intent(this, MainActivity::class.java)
//        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
//
//        val notification = Notification.Builder(this, "BackgroundServiceChannel")
//            .setContentTitle("Driver App")
//            .setContentText("Rider is requesting a ride")
//            .setSmallIcon(R.drawable.ic_stat_onesignal_default)
//            .setContentIntent(pendingIntent)
//            .build()
//
//        startForeground(1, notification)
//
//        // Bring the app to the foreground
//        val launchIntent = Intent(this, MainActivity::class.java)
//        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        startActivity(launchIntent)
//
//        return START_NOT_STICKY
//    }
//
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val serviceChannel = NotificationChannel(
//                "BackgroundServiceChannel",
//                "Background Service Channel",
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