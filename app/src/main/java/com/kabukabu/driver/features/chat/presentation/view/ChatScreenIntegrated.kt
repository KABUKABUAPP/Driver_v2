package com.kabukabu.driver.features.chat.presentation.view

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.R
import com.kabukabu.driver.core.theme.KabukabuDriverTheme
import com.kabukabu.driver.features.chat.data.ChatData
import com.kabukabu.driver.features.chat.presentation.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Chat Screen with ViewModel integration
 * Shows chat messages between driver and rider for a specific trip
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenIntegrated(
    orderId: String,
    riderName: String = "Rider",
    riderPhone: String? = null,
    onBackClick: () -> Unit = {},
    viewModel: ChatViewModel = viewModel()
) {

    // This is the list of predefined quick replies
    val suggestionMessages = listOf(
        "Hello",
        "Where are you?",
        "I've arrived.",
        "I'm on my way."
    )

    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    // Use passed currentUserId parameter instead of loading from ViewModel
    // This eliminates the race condition on first load
    // val currentUserId by viewModel.currentUserId.collectAsState()

    var inputText by remember { mutableStateOf("") }

    // LazyColumn state for auto-scrolling
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Load chats ONLY ONCE when ViewModel is first created
    LaunchedEffect(Unit) {
        Log.d("ChatScreen", "LaunchedEffect triggered for orderId: $orderId")
        viewModel.loadChats(orderId)
        // Mark messages as read when chat screen is opened (clear badge)
        viewModel.markMessagesAsRead()
    }

    // Theme Colors
    val accentColor = Color(0xFFFFC107) // Amber/Yellow
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TopBar - Fixed at top
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = riderName,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (!riderPhone.isNullOrBlank()) {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:$riderPhone")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Log.e("ChatScreen", "Failed to open dialer: ${e.message}")
                            }
                        }
                    }) {
                        Image(
                            painter = painterResource(id = R.drawable.call),
                            contentDescription = "Call",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
            // Messages area - takes remaining space
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    loading && messages.isEmpty() -> LoadingView()
                    error != null && messages.isEmpty() -> ErrorView(
                        error = error ?: "Unknown error",
                        onRetry = { viewModel.loadChats(orderId) }
                    )
                    messages.isEmpty() -> ChatEmptyStateView()
                    else -> MessageListView(messages, accentColor, listState)
                }
            }

            // Suggestion chips area
            QuickReplyChips(
                suggestions = suggestionMessages,
                accentColor = accentColor,
                onChipClick = { suggestion: String ->
                    // When a chip is clicked, send the message immediately
                    viewModel.sendMessage(orderId, suggestion)
                }
            )

            // Bottom input area
            ChatMessageInput(
                text = inputText,
                onTextChange = { newText ->
                    inputText = newText
                    if (newText.isNotBlank()) {
                        viewModel.onUserTyping(orderId)
                    }
                },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(orderId, inputText)
                        inputText = ""
                    }
                },
                accentColor = accentColor
            )
        }
    }
}


@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFFFFC107))
    }
}

@Composable
fun ErrorView(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Error: $error")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun ChatEmptyStateView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.empty_m),
            contentDescription = "Message",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(65.dp)
                .width(90.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Communicate with your driver here",
            color = Color(0xff9A9A9A),
            fontSize = 14.sp,
            fontWeight = FontWeight.W400
        )
    }
}

@Composable
fun MessageListView(
    messages: List<ChatData>,
    accentColor: Color,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
    ) {
        items(messages) { message ->
            MessageBubble(message, accentColor)
        }
    }
}

@Composable
fun MessageBubble(message: ChatData, accentColor: Color) {
    // Use the type field from ChatUser to determine if this is from the driver (me)
    val userType = message.user?.type?.lowercase()
    val isMe = userType != "rider"

    // Debug logging to help diagnose alignment issues
    LaunchedEffect(message.id) {
        Log.d("MessageBubble", "Message: '${message.content}' | UserType: $userType | IsMe: $isMe")
    }

    val align = if (isMe) Alignment.End else Alignment.Start
    val color = if (isMe) Color(0xffF1F1F1) else Color(0xFF2C3FEF)
    val textColor = if (isMe) Color.Black else Color.White
    val shape = if (isMe) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = align
    ) {
        Box(
            modifier = Modifier
                .background(color, shape)
                .padding(12.dp)
        ) {
            Text(
                text = message.content ?: "",
                color = textColor
            )
        }
        // Use message.createdAt and handle nullability
        if (message.createdAt != null) {
            Text(
                text = formatTimestamp(message.getCreatedAtTimestamp()),
                color = Color.Gray,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp)
            )
        }
    }
}

@Composable
fun ChatMessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFFFDFDFD)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...", color = Color.Gray) },
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1F1F1),
                    unfocusedContainerColor = Color(0xFFF1F1F1),
                    // Changed text color to be visible on a light background
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = accentColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(accentColor)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

// THIS WAS THE BROKEN FUNCTION. IT IS NOW FIXED.
private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun QuickReplyChips(
    suggestions: List<String>,
    accentColor: Color,
    onChipClick: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(suggestions) { text ->
            Surface(
                modifier = Modifier.clickable { onChipClick(text) },
                color = Color(0xffE5EDFF),

                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = text,
                    color = Color(0xff2C3FEF),
                    fontWeight = FontWeight.W500,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ChatScreenIntegratedPreview() {
    KabukabuDriverTheme {
        ChatScreenIntegrated(orderId = "preview_order_id")
    }
}
