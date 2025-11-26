package com.kabukabu.driver.features.home.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.core.data.remote.ApiClient
import com.kabukabu.driver.features.home.data.OnlineStatusRequest
import com.kabukabu.driver.features.home.presentation.views.components.Coordinate
import com.kabukabu.driver.features.home.presentation.views.components.distanceTo
import com.kabukabu.driver.features.profile.data.ActiveTrip
import com.kabukabu.driver.features.profile.data.PreferredPaymentMethods
import com.kabukabu.driver.features.profile.data.PreferredPaymentMethodsRequest
import com.kabukabu.driver.features.profile.data.ProfileData
import com.kabukabu.driver.features.profile.data.ProfileResponse
import com.kabukabu.driver.features.profile.data.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
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

    /**
     * Current trip status derived from the active trip
     * Returns STANDBY if there's no active trip
     */
    val currentTripStatus: StateFlow<TripStatus> = _activeTrip.map { trip ->
        trip?.status?.let { TripStatus.fromStatus(it) } ?: TripStatus.STANDBY
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TripStatus.STANDBY
    )

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    private val _userDetails = MutableStateFlow<ProfileData?>(null)
    val userDetails = _userProfile.asStateFlow()

    private val _userPaymentMethod = MutableStateFlow<PreferredPaymentMethods?>(null)
    val userPaymentMethods = _userPaymentMethod.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isUpdatingOnlineStatus = MutableStateFlow(false)
    val isUpdatingOnlineStatus = _isUpdatingOnlineStatus.asStateFlow()

    private val _isArrivedButtonLoading = MutableStateFlow(false)
    val isArrivedButtonLoading = _isArrivedButtonLoading.asStateFlow()

    private val _isStartTripLoading = MutableStateFlow(false)
    val isStartTripLoading = _isStartTripLoading.asStateFlow()

    // Access to LocationRepository for driver location
    private val locationRepository = com.kabukabu.driver.core.data.location.LocationRepository.getInstance(application.applicationContext)
    val driverLocation = locationRepository.currentLocation

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

    init {
        // Fetch early so UI like the drawer can consume cached state immediately
        fetchUserProfile()

        viewModelScope.launch(Dispatchers.IO) {
            userPreferences.userDetails.collect {
                Log.i("DataStoreDebug", "userDetails emitted: $it")
            }
        }

    }

    fun fetchUserProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            val token = userPreferences.authToken.firstOrNull()
            if (token.isNullOrBlank()) {
                Log.e("DriverViewModel", "Cannot fetch profile, token is missing.")
                return@launch
            }
            try {
                val savedPreference = userPreferences.userDetails.firstOrNull()
                _activeTrip.value = savedPreference?.activeTrip
                val onlineStatus = savedPreference?.user?.onlineStatus
                _isOnline.value = onlineStatus == "online"
                _userPaymentMethod.value = savedPreference?.user?.driver?.preferredPaymentMethods
                Log.d("DriverViewModel", "Fetching user profile with token: $token")
                val profileResponse = ApiClient.authService.getProfile("Bearer $token")
                if (profileResponse.isSuccessful) {
                    val rawResponse = profileResponse.body()?.string()
                    Log.d("DriverViewModel", "User Profile Response: $rawResponse")

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

    fun arrivedPickup() {
        viewModelScope.launch(Dispatchers.IO) {
            val activeTrip = _activeTrip.value

            // Set loading state to true
            _isArrivedButtonLoading.value = true

            try {
                Log.d("DriverViewModel", "Successfully updated trip status to driver_arrived")

                com.kabukabu.driver.core.data.socket.SocketService.emitArrivePickup(orderId = activeTrip!!.id)
                Log.d("DriverViewModel", "Emitted arrive pickup socket event")

                // Step 3: Refresh user profile to get updated trip state
                val profileRefreshed = awaitableFetchUserProfile()
                if (profileRefreshed) {
                    Log.d("DriverViewModel", "Profile refreshed successfully after arriving at pickup")
                    _errorMessage.value = null
                } else {
                    Log.w("DriverViewModel", "Profile refresh failed after arriving at pickup")
                }
            } catch (e: Exception) {
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

            try {
                val token = userPreferences.authToken.firstOrNull()
                if (token.isNullOrBlank()) {
                    Log.e("DriverViewModel", "Cannot start trip, token is missing.")
                    _errorMessage.value = "Authentication token is missing"
                    return@launch
                }

                val activeTrip = _activeTrip.value
                if (activeTrip == null) {
                    Log.e("DriverViewModel", "Cannot start trip, no active trip found.")
                    _errorMessage.value = "No active trip found"
                    return@launch
                }

                val tripResponse = ApiClient.authService.startTrip("Bearer $token", activeTrip.id)
                if (tripResponse.isSuccessful) {
                    Log.d("DriverViewModel", "Trip started successfully")
                    val result = awaitableFetchUserProfile()
                    if (result) {
                        Log.d("DriverViewModel", "Profile refreshed successfully after starting trip")
                        _errorMessage.value = null
                    } else {
                        Log.w("DriverViewModel", "Profile refresh failed after starting trip")
                    }
                } else {
                    val errorMessage = tripResponse.errorBody()?.string() ?: "Failed to start trip"
                    _errorMessage.value = errorMessage
                    Log.e("DriverViewModel", "Failed to start trip: $errorMessage")
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error starting trip"
                Log.e("DriverViewModel", "Exception in startTrip: ${e.message}", e)
            } finally {
                // Always reset loading state
                _isStartTripLoading.value = false
            }
        }

    }

    //arrived destination

    /**
     * Updates payment preference on the server.
     * This function never throws exceptions - it always logs and continues.
     */
    private suspend fun updatePaymentPreferenceOnServer(token: String) {
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
            } else {
                Log.w(
                    "DriverViewModel",
                    "Payment preference update failed: ${paymentResponse.errorBody()?.string()}"
                )
            }
        } catch (e: Exception) {
            // Log error but continue - this function never throws
            Log.e(
                "DriverViewModel",
                "Error updating payment preference (continuing anyway): ${e.message}",
                e
            )
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

                // Step 1: Update payment preference first if going online
                if (isOnline) {
                    updatePaymentPreferenceOnServer(token)
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
                            awaitableFetchUserProfile()
                            _isOnline.value = isOnline
                            _errorMessage.value = null // Clear any previous errors
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

            val newPreferences = PreferredPaymentMethods(
                cash = cash, wallet = wallet, card = bankTransfer
            )
            _userPaymentMethod.value = newPreferences

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

    //getPaymentMethodPreference


    fun clearErrorMessage() {
        _errorMessage.value = null
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
                    Log.d("DriverViewModel", "User Profile Response: $rawResponse")

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