//package com.kabukabu.driver.features.chat.presentation.view
//
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Send
//import androidx.compose.material.icons.outlined.MailOutline
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.kabukabu.driver.core.theme.KabukabuDriverTheme
//
//// 1. Data Model
//data class Message(val id: Int, val text: String, val isMe: Boolean)
//
//// 2. Main Orchestrator Screen
//@Composable
//fun UnifiedChatScreen() {
//    // STATE
//    var messages by remember {
//        mutableStateOf(
//            listOf(
//                Message(4, "Great, I'm on my way!", false),
//                Message(3, "I'm just around the corner.", true),
//                Message(2, "Okay, I'm wearing a blue shirt.", false),
//                Message(1, "I've arrived.", true),
//                Message(0, "Hello!", true)
//            )
//        )
//    }
//    var inputText by remember { mutableStateOf("") }
//
//    // Hardcoded suggestions (You can fetch these from a ViewModel later)
//    val suggestions = listOf("I've arrived", "I'm here", "Where are you?", "On my way")
//
//    // Theme Colors
//    val backgroundColor = Color(0xFF1E1E1E)
//    val accentColor = Color(0xFFFFC107)
//
//    // Helper function to send a message (reused by Input and Chips)
//    fun sendMessage(text: String) {
//        if (text.isNotBlank()) {
//            val newMessage = Message(messages.size, text, true)
//            messages = messages + newMessage
//        }
//    }
//
//    Scaffold(
//        containerColor = backgroundColor,
//        bottomBar = {
//            // Stack the Chips vertically above the Input Area
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(backgroundColor) // Match background so it doesn't look floating
//            ) {
//                // THE NEW CHIPS SECTION
//                SuggestionChipsRow(
//                    suggestions = suggestions,
//                    accentColor = accentColor,
//                    onChipClick = { selectedText ->
//                        sendMessage(selectedText)
//                    }
//                )
//
//                // THE INPUT SECTION
//                MessageInput(
//                    text = inputText,
//                    onTextChange = { inputText = it },
//                    onSend = {
//                        sendMessage(inputText)
//                        inputText = ""
//                    },
//                    accentColor = accentColor
//                )
//            }
//        }
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//        ) {
//            if (messages.isEmpty()) {
//                EmptyStateView(accentColor)
//            } else {
//                MessageListView(messages, accentColor)
//            }
//        }
//    }
//}
//
//// 3. The New Chips Component
//@Composable
//fun SuggestionChipsRow(
//    suggestions: List<String>,
//    accentColor: Color,
//    onChipClick: (String) -> Unit
//) {
//    LazyRow(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp, vertical = 8.dp), // Add padding around the strip
//        horizontalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        items(suggestions) { text ->
//            Surface(
//                modifier = Modifier
//                    .clip(RoundedCornerShape(20.dp))
//                    .clickable { onChipClick(text) },
//                color = Color.Transparent,
//                border = BorderStroke(1.dp, accentColor), // Outline style
//                shape = RoundedCornerShape(20.dp)
//            ) {
//                Text(
//                    text = text,
//                    color = Color.White,
//                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
//                    fontSize = 14.sp
//                )
//            }
//        }
//    }
//}
//
//// 4. The Empty State
//@Composable
//fun EmptyStateView(accentColor: Color) {
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Icon(
//            imageVector = Icons.Outlined.MailOutline,
//            contentDescription = "No messages",
//            tint = accentColor,
//            modifier = Modifier.size(100.dp)
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(
//            text = "No Messages Yet",
//            color = Color.White,
//            fontSize = 20.sp,
//            fontWeight = FontWeight.Bold
//        )
//        Text(
//            text = "Send a message to start the chat!",
//            color = Color.Gray,
//            fontSize = 14.sp
//        )
//    }
//}
//
//// 5. The Messaging State
//@Composable
//fun MessageListView(messages: List<Message>, accentColor: Color) {
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(horizontal = 16.dp),
//        reverseLayout = true
//    ) {
//        items(messages.reversed()) { message ->
//            MessageBubble(message, accentColor)
//        }
//    }
//}
//
//@Composable
//fun MessageBubble(message: Message, accentColor: Color) {
//    val align = if (message.isMe) Alignment.End else Alignment.Start
//    val color = if (message.isMe) accentColor else Color(0xFF333333)
//    val textColor = if (message.isMe) Color.Black else Color.White
//    val shape = if (message.isMe) {
//        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
//    } else {
//        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
//    }
//
//    Column(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalAlignment = align
//    ) {
//        Box(
//            modifier = Modifier
//                .padding(vertical = 4.dp)
//                .background(color, shape)
//                .padding(12.dp)
//        ) {
//            Text(text = message.text, color = textColor)
//        }
//    }
//}
//
//// 6. Input Component
//@Composable
//fun MessageInput(
//    text: String,
//    onTextChange: (String) -> Unit,
//    onSend: () -> Unit,
//    accentColor: Color
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp), // Added bottom padding
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        TextField(
//            value = text,
//            onValueChange = onTextChange,
//            modifier = Modifier.weight(1f),
//            placeholder = { Text("Type a message...") },
//            shape = RoundedCornerShape(24.dp),
//            colors = TextFieldDefaults.colors(
//                focusedIndicatorColor = Color.Transparent,
//                unfocusedIndicatorColor = Color.Transparent
//            )
//        )
//        Spacer(modifier = Modifier.width(8.dp))
//        IconButton(
//            onClick = onSend,
//            modifier = Modifier
//                .clip(CircleShape)
//                .background(accentColor)
//        ) {
//            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black)
//        }
//    }
//}
//
//@Preview(showBackground = true, backgroundColor = 0xFF1E1E1E)
//@Composable
//fun UnifiedChatScreenPreview() {
//    KabukabuDriverTheme {
//        UnifiedChatScreen()
//    }
//}