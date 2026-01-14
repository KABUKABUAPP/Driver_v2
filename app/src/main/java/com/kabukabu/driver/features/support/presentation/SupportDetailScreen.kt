package com.kabukabu.driver.features.support.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabukabu.driver.R
import com.kabukabu.driver.core.theme.KabuGray
import com.kabukabu.driver.core.theme.KabukabuDriverTheme
import com.kabukabu.driver.core.theme.KabukabuYellow

data class Message(
    val id: String,
    val text: String,
    val timestamp: String,
    val isFromCurrentUser: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportDetailScreen(supportId: String, onBack: () -> Unit, onViewTrip: () -> Unit) {
    var messageText by remember { mutableStateOf("") }
    val messages = remember {
        listOf(
            Message("1", "I picked this ride at Kuvuki Land yesterday and i forgot my AirPods in the car after i got to my destination. I called the driver and he denied seeing my AirPods but i was charging it. That’s how i forgot it", "", false),
            Message("2", "Please look into it for me", "", false),
            Message("3", "Hello John, we’re sorry you had to go through that. We do not tolerate theft on Kabukabu. Give us a moment, We’ll get back to you", "", true),
            Message("4", "Thank you.", "", false)
        )
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 1.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", modifier = Modifier.size(24.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(supportId, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(painter = painterResource(id = R.drawable.chats_dots), contentDescription = "chat icon", tint = Color.Unspecified)
                    }
                    Spacer(modifier = Modifier.width(16.dp))

                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Stolen Property", fontWeight = FontWeight.W600, fontSize = 12.sp, color = Color.Black)
                        Text("Jan 1, 2023 at 3:40pm", color = Color.Black, fontSize = 12.sp,fontWeight = FontWeight.W600 )
                    }
                    Button(
                        onClick = onViewTrip,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KabukabuYellow.copy(alpha = 0.2f),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("View Trip", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
            }
        },
        bottomBar = {
            MessageInputBar(messageText = messageText, onMessageChange = { messageText = it })
        },
        containerColor = Color.White
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = false
        ) {
            item { 
                Text(text = "Ticket created on Jan 1, 2023 at 3:40pm", color = KabuGray, textAlign = TextAlign.Center, modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp), fontSize = 12.sp)
            }
            items(messages) { message ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (message.isFromCurrentUser) Arrangement.End else Arrangement.Start
                ) {
                    MessageBubble(message = message)
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val backgroundColor = if (message.isFromCurrentUser) Color(0xFFE5EDFF) else Color(0xFFF2F2F2)
    val textColor = if (message.isFromCurrentUser) Color(0xFF2C3FEF) else Color(0xFF161616)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .widthIn(max = 300.dp)
    ) {
        Text(
            text = message.text,
            modifier = Modifier.padding(12.dp),
            color = textColor,
            fontSize = 14.sp
        )
    }
}

@Composable
fun MessageInputBar(messageText: String, onMessageChange: (String) -> Unit) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message", color = KabuGray) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(25.dp)
                            .clip(CircleShape)
                            .background(KabukabuYellow),
                        contentAlignment = Alignment.Center
                    ){
                        Icon(Icons.Default.Add, contentDescription = "add", tint= Color.White)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF8F8F8),
                    focusedContainerColor = Color(0xFFF8F8F8),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SupportDetailScreenPreview() {
    KabukabuDriverTheme {
        SupportDetailScreen("#Case-764322", onBack = {}, onViewTrip = {})
    }
}
