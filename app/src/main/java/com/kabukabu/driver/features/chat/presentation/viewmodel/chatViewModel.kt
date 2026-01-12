package com.kabukabu.driver.features.chat.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.core.data.socket.SocketService
import com.kabukabu.driver.features.chat.data.ChatData
import com.kabukabu.driver.features.chat.data.ChatRepository
import com.kabukabu.driver.features.chat.data.ChatUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

/**
 * ChatViewModel
 *
 * Exposes:
 * - messages: StateFlow<List<ChatData>> (newest first — matching Flutter reverse: true)
 * - loading: StateFlow<Boolean>
 * - error: StateFlow<String?>
 * - isTyping: StateFlow<Boolean>
 *
 * Responsibilities:
 * - Load chats via repository
 * - Send message (push local + emit socket)
 * - Listen to socket events via SocketService and update StateFlow
 * - Handle typing indicator with a short timeout (3s)
 */
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository()
    private val userPreferences = KabukabuDriverApp.getInstance().userPreferences

    private val _messages = MutableStateFlow<List<ChatData>>(emptyList())
    val messages: StateFlow<List<ChatData>> = _messages.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private var typingClearJob: Job? = null

    // Cache messages per orderId for instant display when returning to chat
    private val messageCache = mutableMapOf<String, List<ChatData>>()
    private var currentOrderId: String? = null

    // Track which orders have been loaded to avoid re-fetching on screen revisit
    private val loadedOrders = mutableSetOf<String>()

    // Track unread message count per order
    private val _unreadMessageCount = MutableStateFlow<Int>(0)
    val unreadMessageCount: StateFlow<Int> = _unreadMessageCount.asStateFlow()

    // Track last seen message count to detect new messages
    private var lastSeenMessageCount = 0

    init {
        // Get current user ID immediately (not in a coroutine to ensure it's loaded first)
        viewModelScope.launch {
            // Use runBlocking to ensure userId is loaded before anything else
            val userId = userPreferences.userId.firstOrNull()
            _currentUserId.value = userId
            Log.d("ChatViewModel", "Current user ID loaded: $userId")
        }

        // Listen to socket events
        viewModelScope.launch {
            SocketService.chatMessageEvent.collect { jsonObject ->
                val chatData = mapToChatData(jsonObject)
                onSocketMessageReceived(chatData)
            }
        }

        viewModelScope.launch {
            SocketService.typingEvent.collect { jsonObject ->
                handleRemoteTyping(jsonObject)
            }
        }
    }

    /**
     * Loads chat history for orderId.
     * Only loads once per orderId - subsequent calls use cache without refetching.
     * Clears old data when loading a different trip's chat.
     */
    fun loadChats(orderId: String, page: Int = 1, limit: Int = 20) {
        Log.d("ChatViewModel", "========================================")
        Log.d("ChatViewModel", "loadChats called for orderId: $orderId")
        Log.d("ChatViewModel", "ViewModel instance: ${this.hashCode()}")
        Log.d("ChatViewModel", "Current orderId: $currentOrderId")
        Log.d("ChatViewModel", "loadedOrders contains orderId: ${loadedOrders.contains(orderId)}")
        Log.d("ChatViewModel", "loadedOrders set: $loadedOrders")
        Log.d("ChatViewModel", "messageCache has orderId: ${messageCache.containsKey(orderId)}")
        Log.d("ChatViewModel", "========================================")

        // If this is a DIFFERENT trip than the current one, clear old data
        if (currentOrderId != null && currentOrderId != orderId) {
            Log.d("ChatViewModel", "🔄 Different trip detected! Clearing old data from $currentOrderId")
            clearChatData()
            // Clear cache and loaded orders for the old trip
            messageCache.remove(currentOrderId)
            loadedOrders.remove(currentOrderId)
        }

        // If this order has already been loaded in this ViewModel lifecycle, just show cache
        if (loadedOrders.contains(orderId)) {
            val cachedMessages = messageCache[orderId]
            if (cachedMessages != null) {
                _messages.value = cachedMessages
                currentOrderId = orderId
                Log.d("ChatViewModel", "✅ Using cached messages for order: $orderId (already loaded) - NO API CALL")
                return
            }
        }

        // Check cache first
        val cachedMessages = messageCache[orderId]
        if (cachedMessages != null) {
            // Show cached messages immediately
            _messages.value = cachedMessages
            currentOrderId = orderId
            Log.d("ChatViewModel", "Loaded ${cachedMessages.size} cached messages for order: $orderId")

            // Mark as loaded and fetch fresh data in background
            loadedOrders.add(orderId)
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val response = repository.fetchChats(orderId, page = page, limit = limit)
                    val list = response.data.reversed()
                    _messages.value = list
                    messageCache[orderId] = list
                    Log.d("ChatViewModel", "Refreshed ${list.size} messages from server")
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Error refreshing chats: ${e.message}", e)
                    // Don't show error if we have cached data
                }
            }
        } else {
            // No cache - show loading and fetch from server
            Log.d("ChatViewModel", "🔴 NO CACHE FOUND - Making API call for orderId: $orderId")
            viewModelScope.launch(Dispatchers.IO) {
                _loading.value = true
                _error.value = null
                currentOrderId = orderId
                try {
                    Log.d("ChatViewModel", "📡 API CALL STARTED for orderId: $orderId")
                    val response = repository.fetchChats(orderId, page = page, limit = limit)
                    val list = response.data.reversed()
                    _messages.value = list
                    messageCache[orderId] = list
                    loadedOrders.add(orderId)
                    _loading.value = false
                    Log.d("ChatViewModel", "✅ Loaded ${list.size} messages from server and cached")
                } catch (e: Exception) {
                    _loading.value = false
                    _error.value = e.message ?: "Unknown error loading chats"
                    Log.e("ChatViewModel", "❌ Error loading chats: ${e.message}", e)
                }
            }
        }
    }

    /**
     * Send message: pushes local ChatData immediately then emits via Socket.
     */
    fun sendMessage(orderId: String, text: String) {
        viewModelScope.launch {
            val userId = _currentUserId.value ?: userPreferences.userId.firstOrNull() ?: return@launch

            if (text.isBlank()) {
                Log.w("ChatViewModel", "Attempted to send blank message")
                return@launch
            }

            // Build local message and immediately update UI
            val localMessage = repository.sendMessageLocalAndEmit(orderId, userId, text)

            // Add to end (bottom of chat) for chronological order
            val updated = _messages.value.toMutableList()
            updated.add(localMessage)
            _messages.value = updated

            // Update cache
            messageCache[orderId] = updated

            Log.d("ChatViewModel", "Message sent: $text")
        }
    }

    /**
     * Called on text changed in input — emits typing event.
     * Debounce handled on server or via SocketService.
     */
    fun onUserTyping(orderId: String) {
        viewModelScope.launch {
            val userId = _currentUserId.value ?: userPreferences.userId.firstOrNull() ?: return@launch
            repository.emitTyping(orderId, userId)
        }
    }

    /**
     * Called when a message arrives from socket (listener registered in init).
     */
    private fun onSocketMessageReceived(chatData: ChatData) {
        viewModelScope.launch {
            // Avoid duplicates: check if message id already in list
            val messageId = chatData.idOrLocal()
            val existing = _messages.value.any { it.idOrLocal() == messageId }

            // Also check if this is from current user (already added locally)
            val isFromCurrentUser = chatData.user?._id == _currentUserId.value || chatData.user?.id == _currentUserId.value

            if (!existing && !isFromCurrentUser) {
                // Add to end (bottom of chat) for chronological order
                val updated = _messages.value.toMutableList()
                updated.add(chatData)
                _messages.value = updated

                // Update cache for current order
                currentOrderId?.let { orderId ->
                    messageCache[orderId] = updated
                }

                // Update unread count (new message arrived)
                updateUnreadCount()

                Log.d("ChatViewModel", "New message received via socket: ${chatData.content}")
            } else {
                Log.d("ChatViewModel", "Duplicate message ignored or from current user")
            }
        }
    }

    /**
     * Handle remote typing event. Expects payload to contain userId and room.
     * Show typing indicator for a fixed time (3s) similar to Flutter.
     */
    private fun handleRemoteTyping(jsonObject: JSONObject) {
        viewModelScope.launch {
            try {
                val typingUserId = jsonObject.optString("userId")

                // Only show typing if it's NOT from current user
                if (typingUserId != _currentUserId.value && typingUserId.isNotBlank()) {
                    _isTyping.value = true
                    Log.d("ChatViewModel", "Other user is typing")

                    // Cancel previous timer and start new one
                    typingClearJob?.cancel()
                    typingClearJob = viewModelScope.launch {
                        delay(3000) // 3 seconds timeout
                        _isTyping.value = false
                        Log.d("ChatViewModel", "Typing indicator cleared")
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error handling typing event: ${e.message}", e)
            }
        }
    }

    /**
     * Utility: parse payload from socket into ChatData.
     */
    private fun mapToChatData(jsonObject: JSONObject): ChatData {
        return try {
            val id = jsonObject.optString("_id").takeIf { it.isNotBlank() }
                ?: jsonObject.optString("id").takeIf { it.isNotBlank() }
                ?: UUID.randomUUID().toString()

            val room = jsonObject.optString("room").takeIf { it.isNotBlank() }
                ?: jsonObject.optString("room_id")

            val content = jsonObject.optString("content")

            val userJson = jsonObject.optJSONObject("user")
            val user = if (userJson != null) {
                ChatUser(
                    type = userJson.optString("type").takeIf { it.isNotBlank() },
                    _id = userJson.optString("_id").takeIf { it.isNotBlank() },
                    id = userJson.optString("id").takeIf { it.isNotBlank() },
                    name = userJson.optString("name").takeIf { it.isNotBlank() },
                    profileImage = userJson.optString("profileImage").takeIf { it.isNotBlank() }
                )
            } else null

            // Parse createdAt as ISO 8601 string, fallback to current time
            val createdAt = jsonObject.optString("createdAt").takeIf { it.isNotBlank() }
                ?: run {
                    val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                    format.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    format.format(java.util.Date())
                }

            ChatData(
                _id = id,
                id = id,
                room = room,
                content = content,
                user = user,
                createdAt = createdAt
            )
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error parsing chat data: ${e.message}", e)
            // Fallback
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            format.timeZone = java.util.TimeZone.getTimeZone("UTC")
            ChatData(
                id = UUID.randomUUID().toString(),
                content = jsonObject.toString(),
                createdAt = format.format(java.util.Date())
            )
        }
    }

    fun clearError() {
        _error.value = null
    }

    /**
     * Mark all messages as read for the current order (clear badge)
     * Call this when user opens the chat screen
     */
    fun markMessagesAsRead() {
        lastSeenMessageCount = _messages.value.size
        _unreadMessageCount.value = 0
        Log.d("ChatViewModel", "Marked messages as read, count: $lastSeenMessageCount")
    }

    /**
     * Update unread count based on new messages
     * Called when new messages arrive via socket or API
     */
    private fun updateUnreadCount() {
        val currentMessageCount = _messages.value.size
        if (currentMessageCount > lastSeenMessageCount) {
            val newMessagesCount = currentMessageCount - lastSeenMessageCount
            _unreadMessageCount.value = newMessagesCount
            Log.d("ChatViewModel", "New unread messages: $newMessagesCount (Total: $currentMessageCount, Last seen: $lastSeenMessageCount)")
        }
    }

    /**
     * Clear chat data for previous trip
     * Called when loading a different trip's chat
     */
     fun clearChatData() {
        _messages.value = emptyList()
        _unreadMessageCount.value = 0
        lastSeenMessageCount = 0
        _loading.value = false
        _error.value = null
        Log.d("ChatViewModel", "Cleared chat data for previous trip")
    }

    override fun onCleared() {
        super.onCleared()
        typingClearJob?.cancel()
        Log.d("ChatViewModel", "ChatViewModel cleared")
    }
}

