package com.kabukabu.driver

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import com.kabukabu.driver.core.data.local.UserPreferences
import com.kabukabu.driver.core.data.remote.FcmTokenPayload
import com.kabukabu.driver.core.data.remote.NotificationApiClient
import com.kabukabu.driver.core.data.socket.SocketService
import com.kabukabu.driver.core.di.initKoin
import com.kabukabu.driver.services.ChatHeadService
import com.kabukabu.driver.util.NotificationUtils
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.onesignal.notifications.INotificationClickEvent
import com.onesignal.notifications.INotificationClickListener
import com.onesignal.notifications.INotificationLifecycleListener
import com.onesignal.notifications.INotificationWillDisplayEvent
import com.onesignal.notifications.IPermissionObserver
import com.onesignal.user.subscriptions.IPushSubscriptionObserver
import com.onesignal.user.subscriptions.PushSubscriptionChangedState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.logger.AndroidLogger
import org.koin.core.logger.Level

class KabukabuDriverApp : Application(), Application.ActivityLifecycleCallbacks {
    // Lazy initialization of UserPreferences

    val userPreferences: UserPreferences by lazy {
        UserPreferences.getInstance(applicationContext)
    }

    // Track foreground state
    private var startedActivities = 0
    var isInForeground: Boolean = false
        private set

    companion object {
        private lateinit var instance: KabukabuDriverApp

        // TODO: Replace with your OneSignal App ID from the OneSignal dashboard
        private const val ONESIGNAL_APP_ID = "7e36f37b-d129-49fb-8ec6-9f3b0caca194"

        fun getInstance(): KabukabuDriverApp {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Register lifecycle callbacks to detect foreground/background
        registerActivityLifecycleCallbacks(this)

        // Create notification channels early
        NotificationUtils.createNotificationChannels(this)

        // Initialize OneSignal
        initializeOneSignal()

        // Initialize and connect the socket when app start
        SocketService.connect()

        //initialize dependency injection
        initKoin {
            androidContext(this@KabukabuDriverApp)
            AndroidLogger(Level.INFO)
        }

        // TODO: [Temporary Fix] Remove this block after one successful run.
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("KabukabuDriverApp", "Forcing chatHeadEnabled to true for one-time fix.")
            userPreferences.saveChatHeadEnabled(true)
        }

        // NOTE: Removed auto-start of ChatHeadService on app start to avoid showing the overlay while app is foregrounded.
        // ChatHeadService will be started when the app transitions to background (onActivityStopped) or when the
        // overlay permission observer detects permission being granted while the app is backgrounded.

        // Observe overlayPermissionRequested flag and start ChatHeadService when permission is granted
        CoroutineScope(Dispatchers.Default).launch {
            try {
                userPreferences.overlayPermissionRequested.collect { requested ->
                    if (!requested) return@collect
                    Log.d("KabukabuDriverApp", "overlayPermissionRequested=true; awaiting permission grant...")
                    // Poll briefly until permission is granted
                    val maxAttempts = 60
                    var attempts = 0
                    while (attempts < maxAttempts) {
                        if (Settings.canDrawOverlays(this@KabukabuDriverApp)) {
                            Log.d("KabukabuDriverApp", "Overlay permission detected as granted (observer)")
                            try { userPreferences.saveOverlayPermissionRequested(false) } catch (_: Throwable) {}
                            try {
                                // Only start ChatHeadService if the app is NOT in foreground
                                if (!isInForeground) {
                                    val svcIntent = Intent(this@KabukabuDriverApp, ChatHeadService::class.java)
                                    ContextCompat.startForegroundService(this@KabukabuDriverApp, svcIntent)
                                    Log.d("KabukabuDriverApp", "Started ChatHeadService after overlay grant (observer)")
                                } else {
                                    Log.d("KabukabuDriverApp", "Overlay granted but app is foregrounded - deferring ChatHeadService start until backgrounded")
                                }
                            } catch (e: Exception) {
                                Log.e("KabukabuDriverApp", "Failed to start ChatHeadService after overlay grant: ${e.message}")
                            }
                            break
                        }
                        attempts++
                        kotlinx.coroutines.delay(2000)
                    }
                }
            } catch (e: Exception) {
                Log.e("KabukabuDriverApp", "Error while observing overlayPermissionRequested: ${e.message}")
            }
        }
    }

    // Activity lifecycle callbacks
    override fun onActivityStarted(activity: Activity) {
        Log.d("KabukabuDriverApp", "onActivityStarted: ${activity.localClassName}")
        startedActivities++
        val wasInBackground = !isInForeground
        isInForeground = startedActivities > 0

        if (isInForeground && wasInBackground) {
            Log.d("KabukabuDriverApp", "App has returned to foreground.")
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    // Stop ChatHeadService if it was running
                    Log.d("KabukabuDriverApp", "Attempting to stop ChatHeadService from onActivityStarted.")
                    val svcIntent = Intent(applicationContext, com.kabukabu.driver.services.ChatHeadService::class.java)
                    applicationContext.stopService(svcIntent)
                } catch (e: Exception) {
                    Log.e("KabukabuDriverApp", "Error stopping ChatHeadService in onActivityStarted: ${e.message}", e)
                }
            }
        }
    }

    override fun onActivityStopped(activity: Activity) {
        Log.d("KabukabuDriverApp", "onActivityStopped: ${activity.localClassName}")
        startedActivities = (startedActivities - 1).coerceAtLeast(0)
        isInForeground = startedActivities > 0

        if (!isInForeground) {
            Log.d("KabukabuDriverApp", "App has gone to background.")
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    val chatEnabled = userPreferences.chatHeadEnabled.first()
                    val canDrawOverlays = Settings.canDrawOverlays(this@KabukabuDriverApp)
                    Log.d("KabukabuDriverApp", "Background check: chatEnabled=$chatEnabled, canDrawOverlays=$canDrawOverlays")

                    if (chatEnabled && canDrawOverlays) {
                        Log.d("KabukabuDriverApp", "Conditions met, attempting to start ChatHeadService.")
                        try {
                            val svcIntent = Intent(applicationContext, com.kabukabu.driver.services.ChatHeadService::class.java)
                            ContextCompat.startForegroundService(applicationContext, svcIntent)
                            Log.d("KabukabuDriverApp", "Successfully called startForegroundService for ChatHeadService.")
                        } catch (e: Exception) {
                            Log.e("KabukabuDriverApp", "Error starting ChatHeadService: ${e.message}", e)
                        }
                    } else {
                        Log.w("KabukabuDriverApp", "Conditions not met for starting ChatHeadService.")
                    }
                } catch (e: Exception) {
                    Log.e("KabukabuDriverApp", "Error in onActivityStopped background tasks: ${e.message}", e)
                }
            }
        }
    }


    // Other callbacks we don't use
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    /**
     * Initialize OneSignal Push Notification SDK
     * Equivalent to the Flutter configOneSignal() implementation
     */
    private fun initializeOneSignal() {
        // Enable verbose logging for debugging (remove in production)
        OneSignal.Debug.logLevel = LogLevel.VERBOSE

        // Initialize OneSignal with your App ID
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID)

        // Request notification permission (for Android 13+)
        CoroutineScope(Dispatchers.Main).launch {
            val accepted = OneSignal.Notifications.requestPermission(true)
            Log.d("KabukabuDriverApp", "Accepted push notification permission: $accepted")
        }

        // Set notification will show in foreground handler
        // Equivalent to Flutter's setNotificationWillShowInForegroundHandler
        OneSignal.Notifications.addForegroundLifecycleListener(object : INotificationLifecycleListener {
            override fun onWillDisplay(event: INotificationWillDisplayEvent) {
                Log.d("KabukabuDriverApp", "Received notification in foreground => ${event.notification.body}")
                // Display the notification (pass null to event.preventDefault() to not display)
                // event.preventDefault() // Call this to prevent notification from showing
                // Letting it display by not calling preventDefault()
            }
        })

        // Set notification opened handler
        // Equivalent to Flutter's setNotificationOpenedHandler
        OneSignal.Notifications.addClickListener(object : INotificationClickListener {
            override fun onClick(event: INotificationClickEvent) {
                val notification = event.notification
                Log.d("KabukabuDriverApp", "======== NOTIFICATION OPENED ============")
                Log.d("KabukabuDriverApp", "Notification ID: ${notification.notificationId}")
                Log.d("KabukabuDriverApp", "Notification Body: ${notification.body}")

                try {
                    // Get additional data from the notification
                    val additionalData = notification.additionalData
                    Log.d("KabukabuDriverApp", "Additional Data: $additionalData")

                    if (additionalData != null) {
                        val actionId = additionalData.optString("action", "")
                        val notificationActionName = additionalData.optString("type", "")

                        Log.d("KabukabuDriverApp", "Action ID: $actionId")
                        Log.d("KabukabuDriverApp", "Notification Type: $notificationActionName")

                        when (notificationActionName) {
                            "SUPPORT_MESSAGE" -> {
                                Log.d("KabukabuDriverApp", "SUPPORT_MESSAGE notification clicked")
                                // TODO: Uncomment when ready to implement navigation
                                // Navigate to support chat screen with actionId
                                /*
                                CoroutineScope(Dispatchers.Main).launch {
                                    // Get support message data
                                    // Navigate to SupportChatScreen
                                    Log.d("KabukabuDriverApp", "Would navigate to Support Chat with ID: $actionId")
                                }
                                */
                            }
                            "MESSAGE" -> {
                                Log.d("KabukabuDriverApp", "MESSAGE notification clicked")
                                // TODO: Uncomment when ready to implement chat count update
                                // Update chat badge count
                                /*
                                CoroutineScope(Dispatchers.Main).launch {
                                    // Update chat count/badge
                                    Log.d("KabukabuDriverApp", "Would update chat count")
                                }
                                */
                            }
                            else -> {
                                Log.d("KabukabuDriverApp", "Unknown notification type: $notificationActionName")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("KabukabuDriverApp", "Error processing notification click: ${e.message}", e)
                }
            }
        })

        // Set permission observer
        // Equivalent to Flutter's setPermissionObserver
        OneSignal.Notifications.addPermissionObserver(object : IPermissionObserver {
            override fun onNotificationPermissionChange(permission: Boolean) {
                Log.d("KabukabuDriverApp", "Notification permission changed: $permission")
            }
        })

        // Set subscription observer
        // Equivalent to Flutter's setSubscriptionObserver
        OneSignal.User.pushSubscription.addObserver(object : IPushSubscriptionObserver {
            override fun onPushSubscriptionChange(state: PushSubscriptionChangedState) {
                val current = state.current
                val previous = state.previous

                Log.d("KabukabuDriverApp", "Push subscription changed")
                Log.d("KabukabuDriverApp", "Previous optedIn: ${previous.optedIn}, ID: ${previous.id}")
                Log.d("KabukabuDriverApp", "Current optedIn: ${current.optedIn}, ID: ${current.id}")

                // Save player ID when user gets registered with OneSignal
                val playerId = current.id
                if (!playerId.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            userPreferences.saveOneSignalPlayerId(playerId)
                            Log.d("KabukabuDriverApp", "Saved OneSignal Player ID: $playerId")

                            // Also update the player ID on the notification backend
//                            updatePlayerIdOnBackend(playerId)
                        } catch (e: Exception) {
                            Log.e("KabukabuDriverApp", "Failed to save OneSignal Player ID: ${e.message}")
                        }
                    }
                }
            }
        })

        // Get and save initial device state / player ID
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val playerId = OneSignal.User.pushSubscription.id
                Log.d("KabukabuDriverApp", "Initial OneSignal Player ID: $playerId")

                if (!playerId.isNullOrEmpty()) {
                    userPreferences.saveOneSignalPlayerId(playerId)
                    Log.d("KabukabuDriverApp", "Saved initial OneSignal Player ID: $playerId")

                    // Also update the player ID on the notification backend
//                    updatePlayerIdOnBackend(playerId)
                }
            } catch (e: Exception) {
                Log.e("KabukabuDriverApp", "Error getting initial player ID: ${e.message}")
            }
        }

        Log.d("KabukabuDriverApp", "OneSignal initialized with all handlers")
    }

    /**
     * Set the external user ID for OneSignal (call this after user login)
     * This allows you to target push notifications to specific users
     */
    fun setOneSignalExternalUserId(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                OneSignal.login(userId)
                Log.d("KabukabuDriverApp", "OneSignal external user ID set: $userId")
            } catch (e: Exception) {
                Log.e("KabukabuDriverApp", "Failed to set OneSignal external user ID: ${e.message}")
            }
        }
    }

    /**
     * Remove the external user ID from OneSignal (call this on user logout)
     */
    fun removeOneSignalExternalUserId() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                OneSignal.logout()
                Log.d("KabukabuDriverApp", "OneSignal external user ID removed")
            } catch (e: Exception) {
                Log.e("KabukabuDriverApp", "Failed to remove OneSignal external user ID: ${e.message}")
            }
        }
    }

    /**
     * Update the player ID on the notification backend.
     * This should be called when the user logs in or when the OneSignal player ID changes.
     *
     * @param playerId The OneSignal player ID
     */
//    fun updatePlayerIdOnBackend(playerId: String) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                // Get the user ID from preferences
//                val userId = userPreferences.userId.first()
//
//                if (userId.isNullOrEmpty()) {
//                    Log.w("KabukabuDriverApp", "Cannot update player ID on backend: user ID is null")
//                    return@launch
//                }
//
//                val payload = FcmTokenPayload(
//                    playerId = playerId,
//                    platform = "android",
//                    userId = userId,
//                    type = "driver"
//                )
//
//                Log.d("KabukabuDriverApp", "Updating player ID on backend: playerId=$playerId, userId=$userId")
//
//                val response = NotificationApiClient.service.updateFcmToken(payload)
//
//                if (response.isSuccessful) {
//                    Log.d("KabukabuDriverApp", "Successfully updated player ID on backend")
//                } else {
//                    Log.e("KabukabuDriverApp", "Failed to update player ID on backend: ${response.code()} - ${response.message()}")
//                }
//            } catch (e: Exception) {
//                Log.e("KabukabuDriverApp", "Error updating player ID on backend: ${e.message}", e)
//            }
//        }
//    }

    /**
     * Attempt to update the player ID on backend using stored player ID.
     * Call this after login when user ID becomes available.
     */
    fun syncPlayerIdWithBackend() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val playerId = userPreferences.oneSignalPlayerId.first()
                if (!playerId.isNullOrEmpty()) {
//                    updatePlayerIdOnBackend(playerId)
                } else {
                    // Try to get from OneSignal directly
                    val currentPlayerId = OneSignal.User.pushSubscription.id
                    if (!currentPlayerId.isNullOrEmpty()) {
//                        updatePlayerIdOnBackend(currentPlayerId)
                    } else {
                        Log.w("KabukabuDriverApp", "Cannot sync player ID: no player ID available")
                    }
                }
            } catch (e: Exception) {
                Log.e("KabukabuDriverApp", "Error syncing player ID with backend: ${e.message}")
            }
        }
    }
}
