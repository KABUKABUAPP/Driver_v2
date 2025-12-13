package com.kabukabu.driver.features.debug

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabukabu.driver.core.data.socket.SocketService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocketDebugScreen(
    onBack: () -> Unit
) {
    // Handle Android back button
    BackHandler {
        onBack()
    }

    val scope = rememberCoroutineScope()

    // Connection status
    var connectionStatus by remember { mutableStateOf("Unknown") }
    var isConnected by remember { mutableStateOf(false) }
    var socketId by remember { mutableStateOf("N/A") }

    // Events and errors
    val events = remember { mutableStateListOf<String>() }
    val errors = remember { mutableStateListOf<String>() }

    // Tab selection
    var selectedTab by remember { mutableStateOf(0) }

    // Collect socket status and events
    LaunchedEffect(Unit) {
        // Collect connection status
        launch {
            SocketService.connectionStatus.collectLatest { status ->
                connectionStatus = status
                isConnected = SocketService.isSocketConnected()
                socketId = SocketService.getSocketId()
            }
        }

        // Collect events
        launch {
            SocketService.socketEvents.collectLatest { event ->
                events.add(0, event) // Add to beginning for most recent first
                if (events.size > 100) events.removeAt(events.lastIndex)
            }
        }

        // Collect errors
        launch {
            SocketService.socketErrors.collectLatest { error ->
                errors.add(0, error) // Add to beginning for most recent first
                if (errors.size > 50) errors.removeAt(errors.lastIndex)
            }
        }

        // Initial status check
        isConnected = SocketService.isSocketConnected()
        socketId = SocketService.getSocketId()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Socket Debug Monitor") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            SocketService.connect()
                        }
                    }) {
                        Icon(Icons.Default.Refresh, "Reconnect")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Connection Status Card
            ConnectionStatusCard(
                isConnected = isConnected,
                status = connectionStatus,
                socketId = socketId
            )
            Divider()
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Events")
                            if (events.isNotEmpty()) {
                                Spacer(Modifier.width(8.dp))
                                Badge { Text("${events.size}") }
                            }
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Errors")
                            if (errors.isNotEmpty()) {
                                Spacer(Modifier.width(8.dp))
                                Badge(containerColor = MaterialTheme.colorScheme.error) {
                                    Text("${errors.size}")
                                }
                            }
                        }
                    }
                )
            }
            // Content based on selected tab
            when (selectedTab) {
                0 -> EventsList(events = events, onClear = { events.clear() })
                1 -> ErrorsList(errors = errors, onClear = { errors.clear() })
            }
        }
    }
}

@Composable
fun ConnectionStatusCard(
    isConnected: Boolean,
    status: String,
    socketId: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected)
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            else
                Color(0xFFF44336).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Clear,
                    contentDescription = null,
                    tint = if (isConnected) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isConnected) "Connected" else "Disconnected",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                    Text(
                        text = status,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))

            StatusRow(label = "Socket ID", value = socketId)
            StatusRow(label = "URL", value = "rideservice-dev.up.railway.app")
            StatusRow(
                label = "Status",
                value = if (isConnected) "✅ Online & Listening" else "❌ Offline"
            )
        }
    }
}

@Composable
fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun EventsList(
    events: List<String>,
    onClear: () -> Unit,
) {
    val listState = rememberLazyListState()
    Column(modifier = Modifier.fillMaxSize()) {
        if (events.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Events (${events.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onClear) {
                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear")
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events) { event ->
                    EventItem(event = event)
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No events yet.\nWaiting for socket activity...",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ErrorsList(
    errors: List<String>,
    onClear: () -> Unit
) {
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        if (errors.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Errors (${errors.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onClear) {
                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear")
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(errors) { error ->
                    ErrorItem(error = error)
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "No errors! 🎉",
                        color = Color(0xFF4CAF50),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Socket is operating normally",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EventItem(event: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = when {
            event.contains("✅") || event.contains("CONNECTED") -> Color(0xFF4CAF50).copy(alpha = 0.1f)
            event.contains("🚗") || event.contains("TRIP REQUEST") -> Color(0xFF2196F3).copy(alpha = 0.1f)
            event.contains("❌") -> Color(0xFFF44336).copy(alpha = 0.1f)
            else -> Color(0xFFF5F5F5)
        }
    ) {
        Text(
            text = event,
            modifier = Modifier.padding(12.dp),
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun ErrorItem(error: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF44336).copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF44336).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Clear,
                contentDescription = null,
                tint = Color(0xFFF44336),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = error,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp,
                color = Color(0xFFC62828)
            )
        }
    }
}

