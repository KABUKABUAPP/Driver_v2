package com.kabukabu.driver.features.support.presentation

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kabukabu.driver.R
import com.kabukabu.driver.core.theme.KabuGray
import com.kabukabu.driver.core.theme.KabukabuDriverTheme
import com.kabukabu.driver.core.theme.KabukabuYellow
import com.kabukabu.driver.features.support.viewmodel.SupportSharedViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SupportDetailScreen(
    supportId: String,
    onBack: () -> Unit,
    onViewTrip: () -> Unit,
    viewModel: SupportDetailViewModel = viewModel(),
    supportSharedViewModel: SupportSharedViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showTripDetailsModal by remember { mutableStateOf(false) }

    // Get the selected ticket from shared ViewModel (still used for navigation context)
    val selectedTicket by supportSharedViewModel.selectedTicket.collectAsState()

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.setSelectedImage(it) }
    }

    // Load ticket data on first composition
    LaunchedEffect(supportId) {
        viewModel.load(supportId)
    }

    // Auto-scroll to bottom when messages first load
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty() && !uiState.isLoading) {
            coroutineScope.launch {
                listState.animateScrollToItem(uiState.messages.size - 1)
            }
        }
    }

    // Clear active support ID on dispose
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearActiveSupportId()
        }
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars), topBar = {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 1.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        "Back",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        uiState.ticketId.ifBlank { selectedTicket?.ticketId ?: "" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
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
                    Column {
                        Text(
                            selectedTicket?.title ?: "",
                            fontWeight = FontWeight.W600,
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                        Text(
                            formatTicketDate( selectedTicket?.createdAt),
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.W600
                        )
                    }
                }
                // Only show "View Trip" button if trip is available
//                if (uiState.trip != null || selectedTicket?.trip != null) {
                    Button(
                        onClick = {
//                            showTripDetailsModal = true
                                  },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xffFFF5D8),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("View Trip", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
//                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
        }
    }, bottomBar = {
        Column {
            // Reply preview
//            uiState.replyingTo?.let { replyMsg ->
//                ReplyPreview(
//                    message = replyMsg, onClose = { viewModel.clearReply() })
//            }

            // Image preview
            uiState.selectedImageUri?.let { uri ->
                ImagePreview(
                    imageUri = uri, onClose = { viewModel.setSelectedImage(null) })
            }

            MessageInputBar(
                messageText = uiState.draft,
                onMessageChange = { viewModel.updateDraft(it) },
                onSend = {
                    if (uiState.draft.isNotBlank() || uiState.selectedImageUri != null) {
                        Log.d("SupportDetailVM", "got here 4")
                        viewModel.sendReply(
                            supportId = supportId,
                            message = uiState.draft,
                            replyToId = uiState.replyingTo?.id
                        )
                    }
                },
                onPickImage = {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                isSending = uiState.isSending
            )
        }
    }, containerColor = Color.White
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center), color = KabukabuYellow
                    )
                }

                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error ?: "Error loading messages",
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.load(supportId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KabukabuYellow
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .imePadding(),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            uiState.createdAt?.let { dateStr ->
                                Text(
                                    text = "Ticket created on ${formatSupportDate(dateStr)}",
                                    color = KabuGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp), color = KabukabuYellow
                                    )
                                }
                            }
                        }

                        items(uiState.messages, key = { it.id ?: it.hashCode() }) { message ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (message.admin != null) Arrangement.Start else Arrangement.End
                            ) {
                                MessageBubble(
                                    message = message, onReply = { viewModel.setReplyTo(message) })
                            }
                        }
                    }
                }
            }
        }

        // Show trip details modal
        val tripToShow = uiState.trip ?: selectedTicket?.trip
        if (showTripDetailsModal && tripToShow != null) {
            TripDetailsModal(
                trip = tripToShow, onDismiss = { showTripDetailsModal = false })
        }
    }
}

@Composable
fun MessageBubble(
    message: SupportMessageData, onReply: () -> Unit
) {
    val isFromAdmin = message.admin != null
    val backgroundColor = if (!isFromAdmin) Color(0xFFF2F2F2) else Color(0xFFE5EDFF)
    val textColor = if (!isFromAdmin) Color(0xFF161616) else Color(0xFF2C3FEF)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .widthIn(max = 300.dp)
            .clickable { onReply() }) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Reply indicator
            if (message.isReply && message.replyTo != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Gray.copy(alpha = 0.2f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(modifier = Modifier.padding(8.dp)) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(40.dp)
                                .background(KabukabuYellow)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = message.replyTo.admin?.fullName ?: "You",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = KabuGray
                            )
                            Text(
                                text = message.replyTo.content ?: "Attachment",
                                fontSize = 11.sp,
                                color = KabuGray,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Attachments
            message.attachments?.let { attachments ->
                if (attachments.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = if (message.content != null) 8.dp else 0.dp)
                    ) {
                        attachments.forEach { imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Attachment",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            // Message text
            message.content?.let { content ->
                val displayText = if (isFromAdmin && message.admin != null) {
                    "$content"
//                    "$content -${getUserInitials(message.admin.fullName ?: "Admin")}"
                } else {
                    content
                }

                Text(
                    text = displayText, color = textColor, fontSize = 14.sp, fontWeight = if (isFromAdmin) FontWeight.W500 else FontWeight.W400
                )
            }
        }
    }
}

@Composable
fun ReplyPreview(
    message: SupportMessageData, onClose: () -> Unit
) {
    Surface(
        color = Color(0xFFF5F5F5), modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .background(KabukabuYellow)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Replying to ${message.admin?.fullName ?: "yourself"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = KabuGray
                )
                Text(
                    text = message.content ?: "Attachment",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, "Close", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun ImagePreview(
    imageUri: Uri, onClose: () -> Unit
) {
    Surface(
        color = Color(0xFFF5F5F5), modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Selected image",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Image selected", modifier = Modifier.weight(1f), fontSize = 14.sp
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, "Remove", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun MessageInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    onPickImage: () -> Unit,
    isSending: Boolean
) {
    Surface(
        color = Color.White, shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
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
                            .background(KabukabuYellow)
                            .clickable(onClick = onPickImage), contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "add image",
                            tint = Color.White
                        )
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF8F8F8),
                    focusedContainerColor = Color(0xFFF8F8F8),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                ),
                enabled = !isSending
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSend, enabled = !isSending
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp), color = KabukabuYellow
                    )
                } else {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (messageText.isNotBlank()) KabukabuYellow else Color.Gray
                    )
                }
            }
        }
    }
}

// Helper function to format date
fun formatSupportDate(dateString: String?): String {
    if (dateString == null) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mma", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

// Helper function to get user initials
fun getUserInitials(name: String): String {
    val parts = name.trim().split(" ")
    return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
        parts.size == 1 && parts[0].length >= 2 -> parts[0].substring(0, 2).uppercase()
        parts.size == 1 -> parts[0].first().uppercase()
        else -> "AD"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsModal(
    trip: TripInfo, onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss, containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Trip Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Trip ID
            trip.id?.let {
                DetailRow(label = "Trip ID", value = it)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Pickup Address
            trip.startAddress?.fullAddress?.let {
                DetailRow(label = "Pickup", value = it)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Dropoff Address
            trip.endAddress?.fullAddress?.let {
                DetailRow(label = "Dropoff", value = it)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Price
            trip.price?.let {
                DetailRow(label = "Price", value = "₦${it}")
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Payment Type
            trip.paymentType?.let {
                DetailRow(label = "Payment Type", value = it.uppercase())
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Trip Type
            trip.tripType?.let {
                DetailRow(label = "Trip Type", value = it.replace("_", " "))
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Start Time
            trip.startTime?.let {
                DetailRow(label = "Start Time", value = formatSupportDate(it))
                Spacer(modifier = Modifier.height(12.dp))
            }

            // End Time
            trip.endTime?.let {
                DetailRow(label = "End Time", value = formatSupportDate(it))
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Driver info
            trip.driver?.fullName?.let {
                DetailRow(label = "Driver", value = it.split(" ").joinToString(" ") { word ->
                    word.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() }
                })
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KabukabuYellow
                )
            ) {
                Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.W500
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value, fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.W600
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SupportDetailScreenPreview() {
    KabukabuDriverTheme {
        SupportDetailScreen("#Case-764322", onBack = {}, onViewTrip = {})
    }
}
