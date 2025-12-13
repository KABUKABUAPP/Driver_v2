package com.kabukabu.driver.features.chat.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.kabukabu.driver.core.data.remote.ApiClient
import com.kabukabu.driver.core.data.socket.SocketService
import java.util.UUID

/**
 * ChatRepository
 *
 * Responsibilities:
 * - Fetch chat history via REST (Retrofit `ApiService`)
 * - Send messages via `SocketService` and optionally persist locally
 * - Provide small helper to build local ChatData before server ack
 */
class ChatRepository {

    /**
     * Fetch chat history from server (suspend).
     */
    suspend fun fetchChats(orderId: String, page: Int = 1, limit: Int = 20): ChatResponse =
        withContext(Dispatchers.IO) {
            try {
                Log.d("ChatRepository", "Fetching chats for order: $orderId, page: $page, limit: $limit")
                val response = ApiClient.authService.fetchChats(orderId, limit = limit, page = page)
                Log.d("ChatRepository", "Fetched ${response.data.size} messages")
                response
            } catch (e: Exception) {
                Log.e("ChatRepository", "Error fetching chats: ${e.message}", e)
                throw e
            }
        }

    /**
     * Send a chat message via socket (fire-and-forget).
     * Also returns a locally-built ChatData that the UI can display immediately.
     *
     * Emitted socket payload:
     * {
     *   "room":"ORDER_ID",
     *   "userId":"USER_ID",
     *   "content":"Hello rider"
     * }
     */
    fun sendMessageLocalAndEmit(orderId: String, userId: String, content: String): ChatData {
        // Build local message object (client-side id)
        val localId = UUID.randomUUID().toString()

        // Create ISO 8601 date string for createdAt
        val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
        format.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val createdAtString = format.format(java.util.Date())

        val message = ChatData(
            id = localId,
            room = orderId,
            content = content,
            user = ChatUser(_id = userId, id = userId, name = null, profileImage = null),
            createdAt = createdAtString
        )

        // Emit through socket
        try {
            SocketService.emitMessage(room = orderId, userId = userId, content = content)
            Log.d("ChatRepository", "Message sent via socket to room: $orderId")
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error sending message via socket: ${e.message}", e)
        }

        // Return the locally-created message to let UI show it immediately
        return message
    }

    /**
     * Emit typing event to server.
     * Payload: { "room": orderId, "userId": userId }
     */
    fun emitTyping(orderId: String, userId: String) {
        try {
            SocketService.emitTyping(room = orderId, userId = userId)
            Log.d("ChatRepository", "Typing indicator sent to room: $orderId")
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error emitting typing: ${e.message}", e)
        }
    }
}

