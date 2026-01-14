package com.kabukabu.driver.features.support.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import com.kabukabu.driver.features.wallet.presentation.KabuRed
import com.kabukabu.driver.features.wallet.presentation.PinModalConfig
import com.kabukabu.driver.features.wallet.presentation.StackedBottomSheet

// --- Data Models ---
enum class TicketStatus { OPEN, CLOSED }

data class Ticket(
    val id: String,
    val category: String,
    val date: String,
    val closedDate: String? = null,
    val status: TicketStatus,
    val indicatorColor: Color = KabukabuYellow // Default Yellow
)

// Helper: use pointerInput tap gestures for preview-safe clickable behaviour
fun Modifier.previewSafeClickable(onClick: () -> Unit): Modifier = this.pointerInput(Unit) {
    detectTapGestures(onTap = { onClick() })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketListScreen(
    onBack: () -> Unit,
    onNavigateToTripSupport: () -> Unit,
    selectedTrip: SupportTrip?,
    onTicketClick: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(TicketStatus.OPEN) }
    var searchQuery by remember { mutableStateOf("") }
    var showSupportTypeModal by remember { mutableStateOf(false) }
    var showSubjectEntryModal by remember { mutableStateOf(false) }
    var showSelectSubjectModal by remember { mutableStateOf(false) }
    var subjectText by remember { mutableStateOf("") }

    LaunchedEffect(selectedTrip) {
        if (selectedTrip != null) {
            showSelectSubjectModal = true
        }
    }

    // Dummy Data
    val tickets = remember {
        listOf(
            Ticket(
                "#Case-764322",
                "Stolen Property",
                "Jan 1, 2023 at 3:40pm",
                status = TicketStatus.OPEN
            ),
            Ticket(
                "#Case-764322",
                "Stolen Property",
                "Jan 1, 2023 at 3:40pm",
                status = TicketStatus.OPEN,
                indicatorColor = Color(0xFF4CAF50)
            ),
            Ticket(
                "#Case-764322",
                "Stolen Property",
                "Jan 1, 2023 at 3:40pm",
                "Jan 1, 2023 at 3:40pm",
                TicketStatus.CLOSED
            ),
        )
    }

    val filteredTickets = tickets.filter { it.status == selectedTab }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Support", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    Surface(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = KabukabuYellow
                    ) {
                        IconButton(onClick = {
                            showSupportTypeModal = true
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }, containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // --- Segmented Tab Control ---
            Spacer(modifier = Modifier.height(16.dp))
            CustomSegmentedControl(
                selectedTab = selectedTab, onTabSelected = { selectedTab = it })

            // --- Search Bar ---
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Search for an ${selectedTab.name.lowercase()} ticket",
                        color = KabuGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W500
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF8F8F8),
                    focusedContainerColor = Color(0xFFF8F8F8),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )

            // --- Tickets List ---
            Spacer(modifier = Modifier.height(24.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(filteredTickets) { ticket ->
                    TicketCard(ticket, onTicketClick)
                }
            }
        }
    }

    if (showSupportTypeModal) {
        SupportTypeModal(onDismiss = { showSupportTypeModal = false }, onTripSupportSelect = {
            showSupportTypeModal = false
            onNavigateToTripSupport()
        }, onGeneralSupportSelect = {
            showSupportTypeModal = false
            showSubjectEntryModal = true
        })
    }

    if (showSelectSubjectModal) {
        SelectSubjectModal(onDismiss = { showSelectSubjectModal = false }, onSubmit = {
            showSelectSubjectModal = false
            // Handle submission
        })
    }

    if (showSubjectEntryModal) {
        SubjectEntryModal(
            text = subjectText,
            onTextChange = { subjectText = it },
            onDismiss = { showSubjectEntryModal = false },
            onSubmit = {
                showSubjectEntryModal = false
                // Handle submission
            })
    }
}

@Composable
fun CustomSegmentedControl(selectedTab: TicketStatus, onTabSelected: (TicketStatus) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF2F2F2))
    ) {
        TicketStatus.entries.forEach { status ->
            val isSelected = selectedTab == status
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) KabukabuYellow else Color.Transparent)
                    .previewSafeClickable { onTabSelected(status) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = status.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W500,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun TicketCard(ticket: Ticket, onTicketClick: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .previewSafeClickable { onTicketClick(ticket.id) },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE6E6E6)),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(ticket.id, fontWeight = FontWeight.W500, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    if (ticket.status == TicketStatus.OPEN) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(ticket.indicatorColor)
                        )
                    } else {
                        Icon(painter = painterResource(id = R.drawable.chats_dots), contentDescription = "chat icon", tint = Color.Unspecified)
                    }
                }

                Surface(
                    color = if (ticket.status == TicketStatus.OPEN) Color(0xFFFFF5D8) else Color(
                        0xFFE6F9E6
                    ), shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (ticket.status == TicketStatus.OPEN) "Open" else "Closed",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = if (ticket.status == TicketStatus.OPEN) KabukabuYellow else Color(
                            0xFF4CAF50
                        ),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                ticket.category,
                fontWeight = FontWeight.W600,
                fontSize = 12.sp,
                color = Color.Black
            )
            Text(ticket.date, fontWeight = FontWeight.W600, fontSize = 12.sp, color = Color.Black)

            if (ticket.status == TicketStatus.CLOSED && ticket.closedDate != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Closed on ${ticket.closedDate}",
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SupportTypeModal(
    onDismiss: () -> Unit, onTripSupportSelect: () -> Unit, onGeneralSupportSelect: () -> Unit
) {
    StackedBottomSheet(
        heightFraction = 0.4f, onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ModalHeader("New Support", "What type of support do you need?", onDismiss)
            Spacer(modifier = Modifier.height(24.dp))

            ModalOptionItem(
                R.drawable.route_1,
                "Trip support",
                "Get help about a trip (e.g report stolen property, etc.)",
                onTripSupportSelect
            )
            Spacer(modifier = Modifier.height(12.dp))
            ModalOptionItem(
                R.drawable.star_magic,
                "General support",
                "Get help about a wallet, account, etc",
                onGeneralSupportSelect
            )
        }
    }
}

@Composable
fun SelectSubjectModal(onDismiss: () -> Unit, onSubmit: () -> Unit) {
    val subjects = listOf(
        "I lost my phone",
        "I forgot my bag",
        "I took the rider's charger",
        "I transferred to the driver twice",
        "Rider stole from me",
        "Rider called to harass me",
        "Rider keeps calling me",
        "Other"
    )
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    var otherSubjectText by remember { mutableStateOf("") }

    StackedBottomSheet(
        onDismiss = onDismiss, heightFraction = 0.8f
    ) {
        Column(modifier = Modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
        ) {
            ModalHeader("Select Subject", "This will give clarity on the support type", onDismiss)
            Spacer(modifier = Modifier.height(24.dp))

            subjects.forEach { subject ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .previewSafeClickable { selectedSubject = subject }
                        .padding(vertical = 10.dp)) {
                    CustomRadioButton(
                        selected = selectedSubject == subject,
                        onClick = { selectedSubject = subject })
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(subject, fontSize = 16.sp)
                }
            }

            if (selectedSubject == "Other") {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Subject",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = otherSubjectText,
                    onValueChange = { otherSubjectText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Tell us what happened", color = Color.Gray) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KabukabuYellow, unfocusedBorderColor = KabukabuYellow
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KabukabuYellow),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Submit", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}


@Composable
fun CustomRadioButton(selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
//            .background(if (selected) Color.Black else Color.Transparent)
            .border(
                BorderStroke(1.dp, if (selected) Color.Black else Color(0xffD8D8D8)), CircleShape
            )
            .previewSafeClickable { onClick() }, contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(if (selected) Color.Black else Color(0xffD8D8D8))
        )
    }
}


@Composable
fun SubjectEntryModal(
    text: String,
    onTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    StackedBottomSheet(
        heightFraction = 0.5f, onDismiss = onDismiss
    ) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
            Column(modifier = Modifier.padding(24.dp)) {
                ModalHeader(
                    "Enter Subject",
                    "This will give clarity on the support type",
                    onDismiss
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Subject",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Tell us what happened", color = Color.Gray) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KabukabuYellow, unfocusedBorderColor = KabukabuYellow
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KabukabuYellow),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Submit",
                        color = Color.Black,
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ModalHeader(title: String, subtitle: String, onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.W700)
            Text(
                subtitle,
                fontSize = 14.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.W400
            )
        }
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(KabuRed.copy(alpha = 0.1f), CircleShape)
                .size(PinModalConfig.closeButtonSize)
//            modifier = Modifier.align(Alignment.TopEnd).size(15.dp).background(Color(0xFFEB5757), CircleShape)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = KabuRed,
                modifier = Modifier.size(PinModalConfig.closeIconSize)
            )
        }
    }
}

@Composable
fun ModalOptionItem(iconRes: Int, title: String, desc: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF9F9F9))
            .previewSafeClickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = title,
            modifier = Modifier
                .size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(desc, fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
        }
    }
}

// --- Modal Previews ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SupportTypeModalPreview() {
    KabukabuDriverTheme {
        SupportTypeModal(onDismiss = {}, onTripSupportSelect = {}, onGeneralSupportSelect = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SelectSubjectModalPreview() {
    KabukabuDriverTheme {
        SelectSubjectModal(onDismiss = {}, onSubmit = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SubjectEntryModalPreview() {
    KabukabuDriverTheme {
        SubjectEntryModal(
            text = "This is a sample subject message",
            onTextChange = {},
            onDismiss = {},
            onSubmit = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TicketListPreview() {
    KabukabuDriverTheme {
        TicketListScreen(onBack = {}, onNavigateToTripSupport = {}, selectedTrip = null, onTicketClick = {})
    }
}
