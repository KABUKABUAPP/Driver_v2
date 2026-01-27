package com.kabukabu.driver.features.support.presentation

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.core.data.remote.ApiClient
import com.kabukabu.driver.core.data.socket.SocketService
import com.kabukabu.driver.features.auth.presentation.LoginScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject

class SupportDetailViewModel(
    private val socketService: SocketService = SocketService
) : ViewModel() {
    private val _uiState = MutableStateFlow(SupportDetailUiState())
    val uiState: StateFlow<SupportDetailUiState> = _uiState

    private var currentPage = 1
    private val pageLimit = 10
    private var currentSupportId: String? = null
    private var currentUserId: String? = null

    init {
        // Listen to incoming support messages via socket
        viewModelScope.launch {
            socketService.supportMessageEvent.collect { jsonObject ->
                try {
                    Log.d("SupportDetailVM", "Received support message via socket: ${jsonObject.toString()}")

                    val supportId = jsonObject.optString("support")

                    // Only process if it's for the current ticket
                    if (supportId == currentSupportId) {
                        val messageId = jsonObject.optString("_id", jsonObject.optString("id"))
                        val userId = jsonObject.optString("user")
                        val content = jsonObject.optString("content", jsonObject.optString("message"))
                        val createdAt = jsonObject.optString("createdAt", jsonObject.optString("created_at"))
                        val isReply = jsonObject.optBoolean("isReply", false)

                        // Parse attachments
                        val attachmentsArr = jsonObject.optJSONArray("attachments")
                        val attachments = mutableListOf<String>()
                        if (attachmentsArr != null) {
                            for (j in 0 until attachmentsArr.length()) {
                                attachmentsArr.optString(j)?.let { attachments.add(it) }
                            }
                        }

                        // Parse admin info
                        val adminObj = jsonObject.optJSONObject("admin")
                        val admin = if (adminObj != null) {
                            AdminInfo(fullName = adminObj.optString("full_name", adminObj.optString("fullName")))
                        } else null

                        // Parse reply info
                        val replyToObj = jsonObject.optJSONObject("replyTo")
                        val replyTo = if (replyToObj != null && isReply) {
                            val replyContent = replyToObj.optString("content", replyToObj.optString("message"))
                            val replyUserId = replyToObj.optString("user")
                            val replyAdminObj = replyToObj.optJSONObject("admin")
                            val replyAdmin = if (replyAdminObj != null) {
                                AdminInfo(fullName = replyAdminObj.optString("full_name", replyAdminObj.optString("fullName")))
                            } else null

                            ReplyToInfo(
                                content = replyContent,
                                userId = replyUserId,
                                admin = replyAdmin
                            )
                        } else null

                        val newMessage = SupportMessageData(
                            id = messageId,
                            userId = userId,
                            content = content.takeIf { it.isNotBlank() },
                            attachments = attachments.takeIf { it.isNotEmpty() },
                            admin = admin,
                            createdAt = createdAt,
                            isReply = isReply,
                            replyTo = replyTo
                        )

                        // Add message to the list if it doesn't already exist
                        val currentMessages = _uiState.value.messages
                        if (!currentMessages.any { it.id == newMessage.id }) {
                            _uiState.value = _uiState.value.copy(
                                messages = currentMessages + newMessage
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SupportDetailVM", "Error processing socket support message", e)
                }
            }
        }
    }

    fun load(supportId: String, page: Int = 1, isLoadingMore: Boolean = false) {
        viewModelScope.launch {
            try {
                currentSupportId = supportId

                if (isLoadingMore) {
                    _uiState.value = _uiState.value.copy(isLoadingMore = true)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                }

                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                // previously used authToken by mistake; should use userId
                val userId = prefs.userId.first() ?: ""
                 currentUserId = userId

                if (token.isBlank() || userId.isBlank()) {
                    _uiState.value = _uiState.value.copy(isLoading = false, isLoadingMore = false, error = "Missing auth")
                    return@launch
                }

                // Store active support ID in preferences for notification handling
                prefs.updateActiveSupportId(supportId)

                // Join support room via socket (only on initial load)
                if (!isLoadingMore) {
                    try {
                        socketService.joinSupportRoom(supportId)
                        Log.d("SupportDetailVM", "Joined support room: $supportId")
                    } catch (e: Exception) {
                        Log.e("SupportDetailVM", "Error joining support room", e)
                    }
                }

                val bearer = "Bearer $token"
                val res = ApiClient.rideService.viewSupportTicket(
                    bearerToken = bearer,
                    userId = userId,
                    supportId = supportId
                )

                if (!res.isSuccessful) throw Exception(res.errorBody()?.string() ?: "Failed to load ticket")

                val body = res.body()?.string() ?: "{}"
                val root = JSONObject(body)
                val data = root.optJSONObject("data")

                // Parse ticket info
                val ticketData = data?.optJSONObject("supportTicket") ?: data
                val title = ticketData?.optString("title") ?: "Support"
                val ticketId = ticketData?.optString("ticket_id") ?: supportId
                val status = ticketData?.optString("status") ?: "open"
                val createdAt = ticketData?.optString("createdAt")
                val isAnswered = ticketData?.optBoolean("is_answered", false)

                // Parse trip info if present
                var tripInfo: TripInfo? = null
                val tripObj = ticketData?.optJSONObject("trip")
                if (tripObj != null && !tripObj.isNull("_id")) {
                    val startAddrObj = tripObj.optJSONObject("start_address")
                    val startAddress = if (startAddrObj != null) {
                        AddressInfo(
                            country = startAddrObj.optString("country").takeIf { it.isNotBlank() },
                            state = startAddrObj.optString("state").takeIf { it.isNotBlank() },
                            city = startAddrObj.optString("city").takeIf { it.isNotBlank() },
                            street = startAddrObj.optString("street").takeIf { it.isNotBlank() },
                            fullAddress = startAddrObj.optString("full_address").takeIf { it.isNotBlank() }
                        )
                    } else null

                    val endAddrObj = tripObj.optJSONObject("end_address")
                    val endAddress = if (endAddrObj != null) {
                        AddressInfo(
                            country = endAddrObj.optString("country").takeIf { it.isNotBlank() },
                            state = endAddrObj.optString("state").takeIf { it.isNotBlank() },
                            city = endAddrObj.optString("city").takeIf { it.isNotBlank() },
                            street = endAddrObj.optString("street").takeIf { it.isNotBlank() },
                            fullAddress = endAddrObj.optString("full_address").takeIf { it.isNotBlank() }
                        )
                    } else null

                    val driverObj = tripObj.optJSONObject("driver")
                    val driverInfo = if (driverObj != null) {
                        DriverInfo(
                            id = driverObj.optString("_id").takeIf { it.isNotBlank() },
                            fullName = driverObj.optString("full_name").takeIf { it.isNotBlank() },
                            email = driverObj.optString("email").takeIf { it.isNotBlank() }
                        )
                    } else null

                    tripInfo = TripInfo(
                        id = tripObj.optString("_id", tripObj.optString("id")).takeIf { it.isNotBlank() },
                        startAddress = startAddress,
                        endAddress = endAddress,
                        driver = driverInfo,
                        price = if (tripObj.has("price")) tripObj.optInt("price") else null,
                        paymentType = tripObj.optString("payment_type").takeIf { it.isNotBlank() },
                        tripType = tripObj.optString("trip_type").takeIf { it.isNotBlank() },
                        startTime = tripObj.optString("start_time").takeIf { it.isNotBlank() },
                        endTime = tripObj.optString("end_time").takeIf { it.isNotBlank() }
                    )
                }

                // Parse messages
                val messagesData = data?.optJSONObject("messages")
                val messagesArr = messagesData?.optJSONArray("data") ?: data?.optJSONArray("messages") ?: data?.optJSONArray("chats")
                val pagination = messagesData?.optJSONObject("pagination")
                val hasNext = pagination?.optBoolean("hasNext") ?: false

                val list = mutableListOf<SupportMessageData>()
                if (messagesArr != null) {
                    for (i in 0 until messagesArr.length()) {
                        val m = messagesArr.optJSONObject(i)
                        if (m != null) {
                            val messageId = m.optString("_id", m.optString("id"))
                            val userId = m.optString("user")
                            val content = m.optString("content", m.optString("message", m.optString("msg")))
                            val createdAt = m.optString("createdAt", m.optString("created_at"))
                            val isReply = m.optBoolean("isReply", false)

                            // Parse attachments
                            val attachmentsArr = m.optJSONArray("attachments")
                            val attachments = mutableListOf<String>()
                            if (attachmentsArr != null) {
                                for (j in 0 until attachmentsArr.length()) {
                                    attachmentsArr.optString(j)?.let { attachments.add(it) }
                                }
                            }

                            // Parse admin info
                            val adminObj = m.optJSONObject("admin")
                            val admin = if (adminObj != null) {
                                AdminInfo(fullName = adminObj.optString("full_name", adminObj.optString("fullName")))
                            } else null

                            // Parse reply info
                            val replyToObj = m.optJSONObject("replyTo")
                            val replyTo = if (replyToObj != null && isReply) {
                                val replyContent = replyToObj.optString("content", replyToObj.optString("message"))
                                val replyUserId = replyToObj.optString("user")
                                val replyAdminObj = replyToObj.optJSONObject("admin")
                                val replyAdmin = if (replyAdminObj != null) {
                                    AdminInfo(fullName = replyAdminObj.optString("full_name", replyAdminObj.optString("fullName")))
                                } else null

                                ReplyToInfo(
                                    content = replyContent,
                                    userId = replyUserId,
                                    admin = replyAdmin
                                )
                            } else null

                            list.add(
                                SupportMessageData(
                                    id = messageId,
                                    userId = userId,
                                    content = content.takeIf { it.isNotBlank() },
                                    attachments = attachments.takeIf { it.isNotEmpty() },
                                    admin = admin,
                                    createdAt = createdAt,
                                    isReply = isReply,
                                    replyTo = replyTo
                                )
                            )
                        }
                    }
                }

                // Check if admin has responded (for status indicator)
                val hasAdminResponse = list.any { it.admin != null || it.userId != currentUserId }

                if (isLoadingMore) {
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                        messages = list.reversed() + _uiState.value.messages, // Prepend older messages (reversed)
                        hasMoreMessages = hasNext
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        title = title,
                        ticketId = ticketId,
                        status = status,
                        createdAt = createdAt,
                        isAnswered = isAnswered?: false,
                        // expose trip info to UI
                        trip = tripInfo,
                        messages = list.reversed(), // Reverse to show oldest first
                        hasMoreMessages = hasNext,
                        isActive = hasAdminResponse
                    )
                }

                currentPage = page
            } catch (e: Exception) {
                Log.e("SupportDetailVM", "Error loading ticket", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = e.message ?: "Error"
                )
            }
        }
    }

    fun loadMore() {
        currentSupportId?.let { supportId ->
            if (!_uiState.value.isLoadingMore && _uiState.value.hasMoreMessages) {
                load(supportId, currentPage + 1, isLoadingMore = true)
            }
        }
    }

    fun sendReply(supportId: String, message: String, replyToId: String? = null) {
        Log.d("SupportDetailVM", "got here 1")
        if (message.isBlank() && _uiState.value.selectedImageUri == null) return
        Log.d("SupportDetailVM", "got here 2")
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSending = true, sendError = null)
                val prefs = KabukabuDriverApp.getInstance().userPreferences
                val token = prefs.authToken.first() ?: ""
                val userId = prefs.userId.first() ?: ""

                if (userId.isBlank()) {
                    _uiState.value = _uiState.value.copy(isSending = false, sendError = "User ID not found")
                    return@launch
                }

                val selectedUri = _uiState.value.selectedImageUri

                if (selectedUri != null) {
                    // Send via API with file attachment
                    try {
                        // Optimistically add the message with attachment to the UI BEFORE sending
                        val optimisticMessage = SupportMessageData(
                            id = "temp_${System.currentTimeMillis()}", // Temporary ID
                            userId = userId,
                            content = message.trim().takeIf { it.isNotBlank() },
                            attachments = listOf(selectedUri.toString()), // Use local URI initially
                            admin = null,
                            createdAt = java.time.Instant.now().toString(),
                            isReply = replyToId != null,
                            replyTo = if (replyToId != null) {
                                // Find the message being replied to
                                _uiState.value.messages.find { it.id == replyToId }?.let { msg ->
                                    ReplyToInfo(
                                        content = msg.content,
                                        userId = msg.userId,
                                        admin = msg.admin
                                    )
                                }
                            } else null
                        )

                        // Add optimistic message to UI immediately
                        _uiState.value = _uiState.value.copy(
                            messages = _uiState.value.messages + optimisticMessage
                        )

                        val context = KabukabuDriverApp.getInstance()
                        val contentResolver = context.contentResolver
                        val inputStream = contentResolver.openInputStream(selectedUri)
                            ?: throw Exception("Cannot open image")

                        // Create temporary file
                        val tempFile = java.io.File.createTempFile("support_image_", ".jpg", context.cacheDir)
                        tempFile.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                        inputStream.close()

                        // Create multipart file
                        val requestFile = okhttp3.RequestBody.create(
                            "image/*".toMediaTypeOrNull(),
                            tempFile
                        )
                        val filePart = okhttp3.MultipartBody.Part.createFormData(
                            "attachments",
                            tempFile.name,
                            requestFile
                        )

                        // Create message body
                        val messageBody = okhttp3.RequestBody.create(
                            "text/plain".toMediaTypeOrNull(),
                            message.trim()
                        )

                        val bearer = "Bearer $token"
                        val res = ApiClient.rideService.sendSupportMessageWithAttachment(
                            bearerToken = bearer,
                            userId = userId,
                            supportId = supportId,
                            message = messageBody,
                            attachments = filePart
                        )

                        // Clean up temp file
                        tempFile.delete()

                        if (!res.isSuccessful) {
                            throw Exception(res.errorBody()?.string() ?: "Failed to send message")
                        }

                        Log.d("SupportDetailVM", "Support message with attachment sent successfully")

                        // Clear draft and selected image (message is already in UI)
                        _uiState.value = _uiState.value.copy(
                            isSending = false,
                            draft = "",
                            replyingTo = null,
                            selectedImageUri = null
                        )
                    } catch (e: Exception) {
                        Log.e("SupportDetailVM", "Error sending message with attachment", e)
                        _uiState.value = _uiState.value.copy(isSending = false, sendError = e.message ?: "Send error")
                    }
                } else {
                    // Send via socket (text only)
                    socketService.sendSupportMessage(
                        ticketId = supportId,
                        userId = userId,
                        content = message.trim(),
                        files = emptyList(),
                        replyTo = replyToId
                    )

                    Log.d("SupportDetailVM", "Support message sent via socket: message='$message', replyTo=$replyToId")

                    // Optimistically add the message to the UI
                    val optimisticMessage = SupportMessageData(
                        id = "temp_${System.currentTimeMillis()}", // Temporary ID
                        userId = userId,
                        content = message.trim().takeIf { it.isNotBlank() },
                        attachments = null,
                        admin = null,
                        createdAt = java.time.Instant.now().toString(),
                        isReply = replyToId != null,
                        replyTo = if (replyToId != null) {
                            // Find the message being replied to
                            _uiState.value.messages.find { it.id == replyToId }?.let { msg ->
                                ReplyToInfo(
                                    content = msg.content,
                                    userId = msg.userId,
                                    admin = msg.admin
                                )
                            }
                        } else null
                    )

                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + optimisticMessage,
                        isSending = false,
                        draft = "",
                        replyingTo = null,
                        selectedImageUri = null
                    )
                }

                // The actual message will come back via socket and replace/update the UI
            } catch (e: Exception) {
                Log.e("SupportDetailVM", "Error sending reply", e)
                _uiState.value = _uiState.value.copy(isSending = false, sendError = e.message ?: "Send error")
            }
        }
    }

    fun updateDraft(text: String) {
        _uiState.value = _uiState.value.copy(draft = text)
    }

    fun setReplyTo(message: SupportMessageData?) {
        _uiState.value = _uiState.value.copy(replyingTo = message)
    }

    fun clearReply() {
        _uiState.value = _uiState.value.copy(replyingTo = null)
    }

    fun setSelectedImage(uri: Uri?) {
        _uiState.value = _uiState.value.copy(selectedImageUri = uri)
    }

    fun clearActiveSupportId() {
        viewModelScope.launch {
            val prefs = KabukabuDriverApp.getInstance().userPreferences
            prefs.updateActiveSupportId(null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        clearActiveSupportId()
    }
}

data class SupportMessageData(
    val id: String?,
    val userId: String?,
    val content: String?,
    val attachments: List<String>?,
    val admin: AdminInfo?,
    val createdAt: String?,
    val isReply: Boolean,
    val replyTo: ReplyToInfo?
)

data class AdminInfo(
    val fullName: String?
)

data class ReplyToInfo(
    val content: String?,
    val userId: String?,
    val admin: AdminInfo?
)

data class SupportDetailUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val title: String = "Support",
    val ticketId: String = "",
    val status: String = "open",
    val createdAt: String? = null,
    val isAnswered: Boolean = false,
    val isActive: Boolean = false,
    val trip: TripInfo? = null,
    val messages: List<SupportMessageData> = emptyList(),
    val hasMoreMessages: Boolean = false,
    val draft: String = "",
    val isSending: Boolean = false,
    val sendError: String? = null,
    val replyingTo: SupportMessageData? = null,
    val selectedImageUri: Uri? = null
)

data class TripInfo(
    val id: String?,
    val startAddress: AddressInfo?,
    val endAddress: AddressInfo?,
    val driver: DriverInfo?,
    val price: Int?,
    val paymentType: String?,
    val tripType: String?,
    val startTime: String?,
    val endTime: String?
)

data class AddressInfo(
    val country: String?,
    val state: String?,
    val city: String?,
    val street: String?,
    val fullAddress: String?
)

data class DriverInfo(
    val id: String?,
    val fullName: String?,
    val email: String?
)
