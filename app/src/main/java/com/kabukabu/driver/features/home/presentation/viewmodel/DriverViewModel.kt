package com.kabukabu.driver.features.home.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.core.data.remote.ApiClient
import com.kabukabu.driver.core.data.remote.FcmTokenPayload
import com.kabukabu.driver.core.data.remote.NotificationApiClient
import com.kabukabu.driver.core.data.socket.SocketService
import com.kabukabu.driver.core.data.socket.TripFoundEvent
import com.kabukabu.driver.features.home.data.EndTripResponse
import com.kabukabu.driver.features.home.data.OnlineStatusRequest
import com.kabukabu.driver.features.home.data.RatingRequest
import com.kabukabu.driver.features.home.data.TripTodayData
import com.kabukabu.driver.features.home.presentation.views.components.Coordinate
import com.kabukabu.driver.features.home.presentation.views.components.distanceTo
import com.kabukabu.driver.features.profile.data.ActiveTrip
import com.kabukabu.driver.features.profile.data.PreferredPaymentMethods
import com.kabukabu.driver.features.profile.data.PreferredPaymentMethodsRequest
import com.kabukabu.driver.features.profile.data.ProfileData
import com.kabukabu.driver.features.profile.data.ProfileResponse
import com.kabukabu.driver.features.profile.data.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Enum representing the different states of an active trip
 */


class DriverViewModel(application: Application) : AndroidViewModel(application) {

//    private val userPreferences = UserPreferences(application)

    val userPreferences = KabukabuDriverApp.getInstance().userPreferences


    private val _isOnline = MutableStateFlow(false)
    val isOnline = _isOnline.asStateFlow()

    private val _activeTrip = MutableStateFlow<ActiveTrip?>(null)
    val activeTrip = _activeTrip.asStateFlow()

    // Hold TripFoundEvent data for immediate display before profile fetch completes
    private val _pendingTripEvent = MutableStateFlow<TripFoundEvent?>(null)
    val pendingTripEvent = _pendingTripEvent.asStateFlow()

    // Add manual trip status override
    private val _manualTripStatus = MutableStateFlow<TripStatus?>(null)

    /**
     * Current trip status derived from manual override or active trip
     * Manual status takes precedence when set
     * Returns STANDBY if there's no active trip and no manual status
     */
    val currentTripStatus: StateFlow<TripStatus> = combine(
        _manualTripStatus,
        _activeTrip
    ) { manualStatus, trip ->
        // Manual status takes precedence if set
        manualStatus ?: trip?.status?.let { TripStatus.fromStatus(it) } ?: TripStatus.STANDBY
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TripStatus.STANDBY
    )

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    private val _userDetails = MutableStateFlow<ProfileData?>(null)
    // Expose the correct StateFlow for profile data (ProfileData contains user, carDetails, etc.)
    val userDetails = _userDetails.asStateFlow()

    private val _userPaymentMethod = MutableStateFlow<PreferredPaymentMethods?>(null)
    val userPaymentMethods = _userPaymentMethod.asStateFlow()

    // Track the last saved payment methods from server to detect changes made on UI
    private var lastSavedPaymentMethods: PreferredPaymentMethods? = null

    // Dirty flag indicating user changed preference on UI and it hasn't been pushed to server
    private val _isPaymentPreferenceDirty = MutableStateFlow(false)
    val isPaymentPreferenceDirty = _isPaymentPreferenceDirty.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isUpdatingOnlineStatus = MutableStateFlow(false)
    val isUpdatingOnlineStatus = _isUpdatingOnlineStatus.asStateFlow()

    private val _isArrivedButtonLoading = MutableStateFlow(false)
    val isArrivedButtonLoading = _isArrivedButtonLoading.asStateFlow()

    private val _isStartTripLoading = MutableStateFlow(false)
    val isStartTripLoading = _isStartTripLoading.asStateFlow()

    private val _isEndTripLoading = MutableStateFlow(false)
    val isEndTripLoading = _isEndTripLoading.asStateFlow()

    private val _showTripCompletionSheet = MutableStateFlow(false)
    val showTripCompletionSheet = _showTripCompletionSheet.asStateFlow()

    private val _endTripData = MutableStateFlow<com.kabukabu.driver.features.home.data.EndTripData?>(null)
    val endTripData = _endTripData.asStateFlow()

    // Waiting time countdown (5 minutes = 300 seconds)
    private val _waitingTimeSeconds = MutableStateFlow<Int?>(null)
    val waitingTimeSeconds = _waitingTimeSeconds.asStateFlow()

    // Today's trip analytics
    private val _todayTripData = MutableStateFlow(TripTodayData(
        totalTrips = 0,
        totalKMToday = 0.0,
        totalEarnedToday = 0.0
    ))
    val todayTripData = _todayTripData.asStateFlow()

    // Access to LocationRepository for driver location
    private val locationRepository = com.kabukabu.driver.core.data.location.LocationRepository.getInstance(application.applicationContext)
    val driverLocation = locationRepository.currentLocation

    // Start countdown timer when trip status becomes ARRIVED_PICKUP
    init {
        viewModelScope.launch {
            currentTripStatus.collect { status ->
                when (status) {
                    TripStatus.ARRIVED_PICKUP -> {
                        // Start 5-minute countdown
                        startWaitingTimeCountdown()
                    }
                    TripStatus.TRIP_STARTED -> {
                        // Stop countdown when trip starts
                        _waitingTimeSeconds.value = null
                    }
                    else -> {
                        // Reset countdown for other statuses
                        if (status != TripStatus.ARRIVED_PICKUP) {
                            _waitingTimeSeconds.value = null
                        }
                    }
                }
            }
        }

        // Emit driver location during active trip - runs continuously in ViewModel
//        viewModelScope.launch {
//            combine(activeTrip, driverLocation) { trip, location ->
//                Pair(trip, location)
//            }.collect { (trip, location) ->
//                if ( location != null) {
//                    // Emit location to backend every 5 seconds
//                    SocketService.emitLocation(
//                        lat = location.latitude,
//                        long = location.longitude,
//                        orderId = trip?.order ?: ""
//                    )
//                    Log.d("DriverViewModel", "Emitted location is (${location.latitude}, ${location.longitude}) for trip ${trip?.id}")
//                    delay(5000)
//                }
//            }
//        }

//        viewModelScope.launch {
//            SocketService.tripCancelledEvent.collect { tripCancelledEvent ->
//                awaitableFetchUserProfile()
//            }
//        }
    }

    /**
     * StateFlow that determines if the "Arrived" button should be visible
     * Button is shown when:
     * - There's an active trip
     * - Trip status is ACCEPTED (driver is heading to pickup)
     * - Driver is within 500m of pickup location
     */
    val showArrivedButton: StateFlow<Boolean> = combine(
        _activeTrip,
        currentTripStatus,
        driverLocation
    ) { trip, status, location ->
        // Only show button if there's an active trip and status is ACCEPTED
        if (trip == null || status != TripStatus.ACCEPTED || location == null) {
            return@combine false
        }

        // Calculate distance to pickup location
        val pickupCoordinate = Coordinate(trip.startPoint[1], trip.startPoint[0])
        val driverCoordinate = Coordinate(location.latitude, location.longitude)
        val distance = driverCoordinate.distanceTo(pickupCoordinate)

        // Show button when within 500m (with 50m buffer to prevent flickering)
        val isWithinRange = distance <= 550.0 // 50m buffer

        Log.d("DriverViewModel", "Distance to pickup: ${distance}m, Button visible: $isWithinRange")

        isWithinRange
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    /**
     * StateFlow that determines if the "End Trip" button should be visible
     * Button is shown when:
     * - There's an active trip
     * - Trip status is TRIP_STARTED (trip is in progress)
     * - Driver is within 500m of destination location
     */
    val showEndTripButton: StateFlow<Boolean> = combine(
        _activeTrip,
        currentTripStatus,
        driverLocation
    ) { trip, status, location ->
        // Only show button if there's an active trip and status is TRIP_STARTED
        if (trip == null || status != TripStatus.TRIP_STARTED || location == null) {
            return@combine false
        }

        // Ensure endPoint exists and has valid coordinates
        if (trip.endPoint.size < 2) {
            return@combine false
        }

        // Calculate distance to destination location
        val destinationCoordinate = Coordinate(trip.endPoint[1], trip.endPoint[0])
        val driverCoordinate = Coordinate(location.latitude, location.longitude)
        val distance = driverCoordinate.distanceTo(destinationCoordinate)

        // Show button when within 500m (with 50m buffer to prevent flickering)
        val isWithinRange = distance <= 550.0 // 50m buffer

        Log.d("DriverViewModel", "Distance to destination: ${distance}m, End Trip button visible: $isWithinRange")

        isWithinRange
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        // Fetch early so UI like the drawer can consume cached state immediately
        fetchUserProfile()
        fetchTodayTripAnalytics()
        updateFcmId()

        viewModelScope.launch(Dispatchers.IO) {
            userPreferences.userDetails.collect {
                Log.i("DataStoreDebug", "userDetails active trip is: ${it?.activeTrip}")
            }
        }

    }

    fun cancelTrippedByRider(){
        _activeTrip.value = null
        _pendingTripEvent.value = null

        viewModelScope.launch {
            delay(2000)
            fetchUserProfile(false)
        }
//        fetchUserProfile()
    }

    fun fetchUserProfile( useLocal : Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = userPreferences.authToken.firstOrNull()
            if (token.isNullOrBlank()) {
                Log.e("DriverViewModel", "Cannot fetch profile, token is missing.")
                return@launch
            }
            try {
                val savedPreference = userPreferences.userDetails.firstOrNull()
                if(useLocal) _activeTrip.value = savedPreference?.activeTrip
                _userDetails.value = savedPreference
                val onlineStatus = savedPreference?.user?.onlineStatus
                _isOnline.value = onlineStatus == "online"
                _userPaymentMethod.value = savedPreference?.user?.driver?.preferredPaymentMethods
                Log.d("DriverViewModel", "Fetching user profile with token: $token")
                val profileResponse = ApiClient.authService.getProfile("Bearer $token")
                if (profileResponse.isSuccessful) {
                    val rawResponse = profileResponse.body()?.string()

                    // Now, parse the response and update the online status
                    if (rawResponse != null) {
                        val moshi = ApiClient.moshi
                        val adapter = moshi.adapter(ProfileResponse::class.java)

                        val parsedResponse = adapter.fromJson(rawResponse)
                        val user = parsedResponse?.data?.user
                        val userDetails = parsedResponse?.data
                        val userPaymentMethod =
                            parsedResponse?.data?.user?.driver?.preferredPaymentMethods

                        // Update the in-memory current payment methods
                        _userPaymentMethod.value = userPaymentMethod

                        // Initialize last saved copy and reset dirty flag
                        lastSavedPaymentMethods = userPaymentMethod
                        _isPaymentPreferenceDirty.value = false

                        val onlineStatus = user?.onlineStatus
                        _isOnline.value = onlineStatus == "online"
                        Log.d(
                            "DriverViewModel",
                            "Driver online status updated to: ${isOnline.value}"
                        )

                        // Update active trip
                        _activeTrip.value = parsedResponse?.data?.activeTrip

                        // Clear pending trip event once activeTrip is populated
                        if (_activeTrip.value != null) {
                            SocketService.joinTripRoom(_activeTrip.value!!.order)
                            clearPendingTripEvent()
                        }

                        Log.d("DriverViewModel", "Active trip updated: ${_activeTrip.value}")
                        // Update user profile
//                        _userProfile.value = user

                        _userDetails.value = userDetails
                        userPreferences.saveUserDetails(userDetails)
                        Log.d(
                            "DriverViewModel, Datastore",
                            "user details saved to datastore: $userDetails"
                        )

                        userPreferences.saveFullName(userDetails?.user?.fullName ?: "")
//                        Log.d("DriverViewModel", "User profile updated: ${_userProfile.value}")
                    }
                } else {
                    Log.e(
                        "DriverViewModel",
                        "Failed to fetch profile: ${profileResponse.errorBody()?.string()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("DriverViewModel", "Exception fetching profile: ${e.message}", e)
            }
        }
    }

    /**
     * Set pending trip event for immediate display
     * Called when trip is accepted, before profile fetch completes
     */
    fun setPendingTripEvent(tripEvent: TripFoundEvent?) {
        _pendingTripEvent.value = tripEvent
        Log.d("DriverViewModel", "Pending trip event set: ${tripEvent?.eventId}")
    }

    fun setActiveTripEvent() {
        _activeTrip.value = null
        Log.d("DriverViewModel", "active trip event set to null")
    }

    /**
     * Clear pending trip event once activeTrip is populated from profile
     */
    private fun clearPendingTripEvent() {
        _pendingTripEvent.value = null
        Log.d("DriverViewModel", "Pending trip event cleared")
    }

    fun fetchTodayTripAnalytics() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = userPreferences.authToken.firstOrNull()
                if (token.isNullOrBlank()) {
                    Log.e("DriverViewModel", "Cannot fetch today's trip analytics, token is missing.")
                    return@launch
                }

                Log.d("DriverViewModel", "Fetching today's trip analytics...")
                val response = ApiClient.rideService.getTodayTripStats("Bearer $token")

                if (response.isSuccessful) {
                    val tripTodayResponse = response.body()
                    if (tripTodayResponse?.data != null) {
                        _todayTripData.value = tripTodayResponse.data
                        Log.d("DriverViewModel", "Today's trip analytics updated: ${tripTodayResponse.data}")
                    } else {
                        Log.w("DriverViewModel", "Today's trip analytics data is null")
                    }


                } else {
                    Log.e("DriverViewModel", "Failed to fetch today's trip analytics: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("DriverViewModel", "Exception fetching today's trip analytics: ${e.message}", e)
            }
        }
    }

    fun rateRider(orderId: String, rating: Double, comment: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = userPreferences.authToken.firstOrNull()
                if (token.isNullOrBlank()) {
                    Log.e("DriverViewModel", "Cannot rate rider, token is missing.")
                    return@launch
                }

                if (orderId.isBlank()) {
                    Log.e("DriverViewModel", "Cannot rate rider, orderId is missing.")
                    return@launch
                }

                Log.d("DriverViewModel", "Rating rider for order: $orderId with rating: $rating")

                val ratingRequest = RatingRequest(
                    rating = rating,
                    comment = comment
                )

                val response = ApiClient.authService.rateRider(
                    bearerToken = "Bearer $token",
                    orderId = orderId,
                    request = ratingRequest
                )

                if (response.isSuccessful) {
                    Log.d("DriverViewModel", "Rider rated successfully")
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Failed to rate rider"
                    Log.e("DriverViewModel", "Failed to rate rider: $errorMessage")
                }
            } catch (e: Exception) {
                Log.e("DriverViewModel", "Exception rating rider: ${e.message}", e)
            }
        }
    }

    fun arrivedPickup() {
        viewModelScope.launch(Dispatchers.IO) {
            val activeTrip = _activeTrip.value

            // Set loading state to true
            _isArrivedButtonLoading.value = true

            // Manually set status to ARRIVED_PICKUP immediately for testing and instant UI feedback


            try {
                Log.d("DriverViewModel", "Manually set trip status to ARRIVED_PICKUP")

                SocketService.emitArrivePickup(orderId = activeTrip!!.order)
                _manualTripStatus.value = TripStatus.ARRIVED_PICKUP
                Log.d("DriverViewModel", "Emitted arrive pickup socket event")

//                 Step 3: Refresh user profile to get updated trip state
                val profileRefreshed = awaitableFetchUserProfile()
                if (profileRefreshed) {
                    Log.d("DriverViewModel", "Profile refreshed successfully after arriving at pickup")
                    // Clear manual override once we have fresh data from server
                    _manualTripStatus.value = null
                    _errorMessage.value = null
                } else {
                    Log.w("DriverViewModel", "Profile refresh failed after arriving at pickup")
                }
            } catch (e: Exception) {
                // Revert manual status on error
                _manualTripStatus.value = null
                _errorMessage.value = e.message ?: "Error updating trip status"
                Log.e("DriverViewModel", "Exception in arrivedPickup: ${e.message}", e)
            } finally {
                // Always reset loading state
                _isArrivedButtonLoading.value = false
            }
        }
    }


    //start trip
    fun startTrip(){
        viewModelScope.launch(Dispatchers.IO) {
            // Set loading state to true
            _isStartTripLoading.value = true

            // Manually set status to TRIP_STARTED immediately for testing and instant UI feedback


            try {
                val token = userPreferences.authToken.firstOrNull()
                if (token.isNullOrBlank()) {
                    Log.e("DriverViewModel", "Cannot start trip, token is missing.")
                    _errorMessage.value = "Authentication token is missing"
                    _manualTripStatus.value = null // Revert on error
                    return@launch
                }

                val activeTrip = _activeTrip.value
                if (activeTrip == null) {
                    Log.e("DriverViewModel", "Cannot start trip, no active trip found.")
                    _errorMessage.value = "No active trip found"
                    _manualTripStatus.value = null // Revert on error
                    return@launch
                }

                Log.d("DriverViewModel", "Manually set trip status to TRIP_STARTED")

                val tripResponse = ApiClient.authService.startTrip("Bearer $token", activeTrip.id)
                if (tripResponse.isSuccessful) {
                    Log.d("DriverViewModel", "Trip started successfully")
                    _manualTripStatus.value = TripStatus.TRIP_STARTED
                   fetchUserProfile()

                } else {
                    val errorMessage = tripResponse.errorBody()?.string() ?: "Failed to start trip"
                    _errorMessage.value = errorMessage
                    _manualTripStatus.value = null // Revert on error
                    Log.e("DriverViewModel", "Failed to start trip: $errorMessage")
                }
            } catch (e: Exception) {
                _manualTripStatus.value = null // Revert on error
                _errorMessage.value = e.message ?: "Error starting trip"
                Log.e("DriverViewModel", "Exception in startTrip: ${e.message}", e)
            } finally {
                // Always reset loading state
                _isStartTripLoading.value = false
            }
        }

    }


    /**
     * End the current trip
     * Called when driver arrives at destination and clicks "End Trip"
     * Shows trip completion sheet on success
     */
    fun endTrip(){
        viewModelScope.launch(Dispatchers.IO) {
            // Set loading state to true
            _isEndTripLoading.value = true

            try {
                val token = userPreferences.authToken.firstOrNull()
                if (token.isNullOrBlank()) {
                    Log.e("DriverViewModel", "Cannot end trip, token is missing.")
                    _errorMessage.value = "Authentication token is missing"
                    return@launch
                }

                val activeTrip = _activeTrip.value
                if (activeTrip == null) {
                    Log.e("DriverViewModel", "Cannot end trip, no active trip found.")
                    _errorMessage.value = "No active trip found"
                    return@launch
                }

                Log.d("DriverViewModel", "Ending trip...")

                val tripResponse = ApiClient.authService.endTrip("Bearer $token", activeTrip.id)
                if (tripResponse.isSuccessful) {
                    val endTripResponse = tripResponse.body()
                    Log.d("DriverViewModel", "Trip ended successfully: ${endTripResponse?.message}")

                    // Store the end trip data for display in completion sheet
                    _endTripData.value = endTripResponse?.data
                    Log.d("DriverViewModel", "End trip data stored: price=${endTripResponse?.data?.endedTrip?.price}, payment=${endTripResponse?.data?.endedTrip?.paymentType}")
//                    _activeTrip.value = null
                    // Refresh profile to get updated data (clears activeTrip)
                    val result = awaitableFetchUserProfile()
                    if (result) {
                        Log.d("DriverViewModel", "Profile refreshed successfully after ending trip")
                        _errorMessage.value = null
                    } else {
                        Log.w("DriverViewModel", "Profile refresh failed after ending trip")
                    }

                    // Refresh today's trip analytics to update statistics
                    fetchTodayTripAnalytics()

                    // Show trip completion bottom sheet
                    _showTripCompletionSheet.value = true
                } else {
                    val errorMessage = tripResponse.errorBody()?.string() ?: "Failed to end trip"
                    _errorMessage.value = errorMessage
                    Log.e("DriverViewModel", "Failed to end trip: $errorMessage")
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error ending trip"
                Log.e("DriverViewModel", "Exception in endTrip: ${e.message}", e)
            } finally {
                // Always reset loading state
                _isEndTripLoading.value = false
            }
        }

    }

    //arrived destination

    /**
     * Updates payment preference on the server.
     * Returns true when the update was successful, false otherwise.
     */
    private suspend fun updatePaymentPreferenceOnServer(token: String): Boolean {
        try {
            Log.d("DriverViewModel", "Updating payment preference first...")
            val cashPaymentStatus = if (_userPaymentMethod.value?.cash == true) "selected" else "unselected"
            val cardPaymentStatus = if (_userPaymentMethod.value?.card == true) "selected" else "unselected"
            val walletPaymentStatus = if (_userPaymentMethod.value?.wallet == true) "selected" else "unselected"
            val paymentResponse = ApiClient.authService.updatePaymentPreference(
                bearerToken = "Bearer $token",
                request = PreferredPaymentMethodsRequest(
                    cashPaymentStatus,
                    walletPaymentStatus,
                    cardPaymentStatus
                )
            )

            if (paymentResponse.isSuccessful) {
                Log.d("DriverViewModel", "Payment preference updated successfully")

                // Update last saved copy and clear dirty flag on success
                lastSavedPaymentMethods = _userPaymentMethod.value
                _isPaymentPreferenceDirty.value = false
                return true
            } else {
                Log.w(
                    "DriverViewModel",
                    "Payment preference update failed: ${paymentResponse.errorBody()?.string()}"
                )
                return false
            }
        } catch (e: Exception) {
            // Log error but continue - this function never throws
            Log.e(
                "DriverViewModel",
                "Error updating payment preference (continuing anyway): ${e.message}",
                e
            )
            return false
        }
    }

    fun updateOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            // Set loading state to true at the start
            _isUpdatingOnlineStatus.value = true

            try {
                val token = userPreferences.authToken.firstOrNull()
                val userId = userPreferences.userId.firstOrNull()

                if (token.isNullOrBlank() || userId.isNullOrBlank()) {
                    Log.e("DriverViewModel", "Cannot update status, token or userId is missing.")
                    return@launch
                }

                // Validation: Check if user has at least one payment method selected when going online
                if (isOnline) {
                    val hasAtLeastOnePaymentMethod = _userPaymentMethod.value?.let { prefs ->
                        prefs.cash == true || prefs.wallet == true || prefs.card == true
                    } ?: false

                    if (!hasAtLeastOnePaymentMethod) {
                        _errorMessage.value = "Please select at least one payment method before going online"
                        Log.w("DriverViewModel", "Cannot go online: No payment method selected")
                        return@launch
                    }
                }

                val status = if (isOnline) "online" else "offline"

                // Step 1: Update payment preference first if going online and preferences changed
                if (isOnline && _isPaymentPreferenceDirty.value) {
                    val updated = updatePaymentPreferenceOnServer(token)
                    if (!updated) {
                        Log.w("DriverViewModel", "Payment preference update failed; continuing to update online status")
                        // keep dirty flag true so future attempts still try again
                    }
                }

                // Step 2: Update online status (always runs, regardless of payment preference result)
                try {
                    Log.d("DriverViewModel", "Updating online status to $status...")
                    val statusResponse = ApiClient.rideService.updateOnlineStatus(
                        token = "Bearer $token",
                        userId = userId,
                        request = OnlineStatusRequest(onlineStatus = status)
                    )

                    if (statusResponse.isSuccessful) {
                        val body = statusResponse.body()
                        if (body?.status == "success") {

                            _isOnline.value = !isOnline
                            // Start or stop TripRequestService based on the new online state
                            try {
                                val appContext = getApplication<com.kabukabu.driver.KabukabuDriverApp>().applicationContext
                                if (isOnline) {
                                    // We just set the driver to online
                                    Log.d("DriverViewModel", "Driver is now online - starting TripRequestService")
                                    com.kabukabu.driver.services.TripRequestService.startService(appContext)
                                } else {
                                    Log.d("DriverViewModel", "Driver is now offline - stopping TripRequestService")
                                    com.kabukabu.driver.services.TripRequestService.stopService(appContext)
                                }
                            } catch (e: Exception) {
                                Log.e("DriverViewModel", "Failed to start/stop TripRequestService: ${e.message}")
                            }
                             _errorMessage.value = null // Clear any previous errors
                             awaitableFetchUserProfile()
                             Log.d(
                                 "DriverViewModel",
                                 "Successfully updated online status to $status"
                             )

                        } else {
                            val message = body?.message ?: "Failed to update online status"
                            _errorMessage.value = message
                            Log.e("DriverViewModel", "Failed to update online status: $message")
                        }
                    } else {
                        val errorMessage = parseErrorMessage(statusResponse.errorBody()?.string())
                        _errorMessage.value = errorMessage
                        Log.e("DriverViewModel", "Failed to update online status: $errorMessage")
                    }
                } catch (e: Exception) {
                    // Show network/exception error in snackbar
                    _errorMessage.value = e.message ?: "Network error occurred"
                    Log.e("DriverViewModel", "Error updating online status: ${e.message}", e)
                }
            } finally {
                // Always set loading state to false when done (success or failure)
                _isUpdatingOnlineStatus.value = false
            }
        }
    }

    //setPaymentMethodPreference
    fun setPaymentMethodPreference(
        cash: Boolean, wallet: Boolean, bankTransfer: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = userPreferences.authToken.firstOrNull()
            if (token.isNullOrBlank()) {
                _errorMessage.value = "Authentication token is missing."
                return@launch
            }

            if(_isOnline.value){
                _errorMessage.value = "You can not change payment method while online."
                return@launch
            }

            val newPreferences = PreferredPaymentMethods(
                cash = cash, wallet = wallet, card = bankTransfer
            )

            // Update the current preference shown on UI
            _userPaymentMethod.value = newPreferences

            // Mark dirty if it differs from last saved preference
            val last = lastSavedPaymentMethods
            val changed = !paymentMethodsEqual(last, newPreferences)
            _isPaymentPreferenceDirty.value = changed

            Log.d("DriverViewModel", "Payment preference changed: $changed")

//            try {
//                val response = ApiClient.authService.updatePaymentPreference(
//                    bearerToken = "Bearer $token",
//                    request = UpdatePaymentMethodRequest(newPreferences)
//                )
//
//                if (response.isSuccessful) {
//                    _userPaymentMethod.value = newPreferences
//                    _errorMessage.value = "Payment methods updated."
//                } else {
//                    val errorBody = response.errorBody()?.string()
//                    _errorMessage.value = parseErrorMessage(errorBody)
//                }
//            } catch (e: Exception) {
//                _errorMessage.value = e.message ?: "An error occurred."
//            }
        }
    }

    // Helper to compare two PreferredPaymentMethods safely
    private fun paymentMethodsEqual(a: PreferredPaymentMethods?, b: PreferredPaymentMethods?): Boolean {
        if (a === b) return true
        if (a == null && b == null) return true
        if (a == null || b == null) return false
        return (a.cash ?: false) == (b.cash ?: false)
                && (a.wallet ?: false) == (b.wallet ?: false)
                && (a.card ?: false) == (b.card ?: false)
    }

    fun updateFcmId() {
        viewModelScope.launch {
            try {
                // Get player ID from userPreferences
                val playerId = userPreferences.oneSignalPlayerId.firstOrNull()
                val token = userPreferences.authToken.firstOrNull()
                val userId = userPreferences.userId.firstOrNull()

                if (playerId.isNullOrEmpty()) {
                    Log.w("DriverViewModel", "Cannot update FCM ID: Player ID is null or empty")
                    return@launch
                }

                if (token.isNullOrEmpty()) {
                    Log.w("DriverViewModel", "Cannot update FCM ID: Auth token is null or empty")
                    return@launch
                }

                if (userId.isNullOrEmpty()) {
                    Log.w("DriverViewModel", "Cannot update FCM ID: User ID is null or empty")
                    return@launch
                }

                Log.d("DriverViewModel", "Updating FCM token for user: $userId with playerId: $playerId")

                // Create payload
                val payload = FcmTokenPayload(
                    playerId = playerId,
                    platform = "android",
                    userId = userId,
                    type = "driver"
                )

                // Send to notification service
                val response = withContext(Dispatchers.IO) {
                    NotificationApiClient.service.updateFcmToken(
                        bearerToken = "Bearer $token",
                        userId = userId,
                        payload = payload
                    )
                }

                if (response.isSuccessful) {
                    Log.d("DriverViewModel", "FCM token updated successfully")
                } else {
                    Log.e("DriverViewModel", "Failed to update FCM token: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("DriverViewModel", "Error updating FCM token: ${e.message}", e)
            }
        }
    }


    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Manually set trip status for testing purposes or immediate UI feedback
     * Pass null to clear the manual override and use the active trip status
     *
     * @param status The trip status to set, or null to clear the override
     */
    fun setManualTripStatus(status: TripStatus?) {
        _manualTripStatus.value = status
        Log.d("DriverViewModel", "Manual trip status set to: $status")
    }

    /**
     * Dismiss the trip completion bottom sheet and reset trip-related state
     * Called when user clicks "Continue" on the trip completion sheet
     */
    fun dismissTripCompletionSheet() {
        _showTripCompletionSheet.value = false
        _manualTripStatus.value = null
        _endTripData.value = null
        _activeTrip.value = null
        _pendingTripEvent.value = null

        // Refresh analytics when returning to main screen
        fetchTodayTripAnalytics()
        fetchUserProfile()

        Log.d("DriverViewModel", "Trip completion sheet dismissed, state reset")
    }

    /**
     * Start the 5-minute waiting time countdown
     * Runs when driver arrives at pickup location
     */
    private fun startWaitingTimeCountdown() {
        viewModelScope.launch {
            _waitingTimeSeconds.value = 300 // 5 minutes = 300 seconds
            Log.d("DriverViewModel", "Starting 5-minute waiting time countdown")

            while (_waitingTimeSeconds.value != null && _waitingTimeSeconds.value!! > 0) {
                delay(1000) // Wait 1 second

                val current = _waitingTimeSeconds.value
                if (current != null && current > 0) {
                    _waitingTimeSeconds.value = current - 1

                    if (current - 1 == 0) {
                        Log.d("DriverViewModel", "Waiting time countdown finished")
                        _waitingTimeSeconds.value = null
                      }
                }
            }
        }
    }

    private fun parseErrorMessage(rawError: String?): String {
        if (rawError.isNullOrBlank()) return "Failed to update online status"
        return try {
            val json = JSONObject(rawError)
            json.optString("message", rawError)
        } catch (e: Exception) {
            Log.e("DriverViewModel", "Error parsing error message: ${e.message}")
            rawError
        }
    }

    // In DriverViewModel.kt

    // This new function can be awaited and returns a Boolean indicating success.
    suspend fun awaitableFetchUserProfile(): Boolean {
        // Use withContext to switch to an I/O thread and wait for the result.
        return withContext(Dispatchers.IO) {
            val token = userPreferences.authToken.firstOrNull()
            if (token.isNullOrBlank()) {
                Log.e("DriverViewModel", "Cannot fetch profile, token is missing.")
                return@withContext false // Failure
            }

            try {
                val profileResponse = ApiClient.authService.getProfile("Bearer $token")
                if (profileResponse.isSuccessful) {
                    val rawResponse = profileResponse.body()?.string()

                    // Now, parse the response and update the online status
                    if (rawResponse != null) {
                        val moshi = ApiClient.moshi
                        val adapter = moshi.adapter(ProfileResponse::class.java)

                        val parsedResponse = adapter.fromJson(rawResponse)
                        val user = parsedResponse?.data?.user
                        val userDetails = parsedResponse?.data
                        val userPaymentMethod =
                            parsedResponse?.data?.user?.driver?.preferredPaymentMethods
                        _userPaymentMethod.value = userPaymentMethod

                        val onlineStatus = user?.onlineStatus
                        _isOnline.value = onlineStatus == "online"
                        Log.d(
                            "DriverViewModel",
                            "Driver online status updated to: ${isOnline.value}"
                        )

                        // Update active trip
                        _activeTrip.value = parsedResponse?.data?.activeTrip
                        Log.d("DriverViewModel", "Active trip updated: ${_activeTrip.value}")

                        // Update user profile
//                        _userProfile.value = user

                        _userDetails.value = userDetails
                        userPreferences.saveUserDetails(userDetails)
                        Log.d(
                            "DriverViewModel, Datastore",
                            "user details saved to datastore: $userDetails"
                        )

                        userPreferences.saveFullName(userDetails?.user?.fullName ?: "")
                        return@withContext true // Success
                    }
                    return@withContext false // Failure

                } else {
                    Log.e(
                        "DriverViewModel",
                        "Failed to fetch profile: ${profileResponse.errorBody()?.string()}"
                    )
                    return@withContext false // Failure
                }
            } catch (e: Exception) {
                Log.e("DriverViewModel", "Exception fetching profile: ${e.message}", e)
                return@withContext false // Failure
            }
        }
    }
}




enum class TripStatus {
    STANDBY,
    ACCEPTED,
    ARRIVED_PICKUP,
    TRIP_STARTED,
    ARRIVED_DESTINATION;

    companion object {
        /**
         * Converts backend status string to TripStatus enum
         * @param status The status string from the backend
         * @return The corresponding TripStatus enum value
         */
        fun fromStatus(status: String): TripStatus {
            return when (status.lowercase()) {
                "initiated" -> ACCEPTED
                "accepted" -> TRIP_STARTED
                "started" -> TRIP_STARTED
                "driver_arrived" -> ARRIVED_PICKUP
                "driver_arrived_destination" -> ARRIVED_DESTINATION
                else -> {
                    Log.w("TripStatus", "Unknown status: $status, defaulting to STANDBY")
                    STANDBY
                }
            }
        }
    }
}
