import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabukabu.driver.core.theme.KabuGray
import com.kabukabu.driver.core.theme.KabukabuDriverTheme
import com.kabukabu.driver.core.theme.KabukabuYellow
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
fun Modifier.previewSafeClickable(onClick: () -> Unit): Modifier =
    this.pointerInput(Unit) {
        detectTapGestures(onTap = { onClick() })
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketListScreen() {
    var selectedTab by remember { mutableStateOf(TicketStatus.CLOSED) }
    var searchQuery by remember { mutableStateOf("") }

    // Dummy Data
    val tickets = remember {
        listOf(
            Ticket("#Case-764322", "Stolen Property", "Jan 1, 2023 at 3:40pm", status = TicketStatus.OPEN),
            Ticket("#Case-764322", "Stolen Property", "Jan 1, 2023 at 3:40pm", status = TicketStatus.OPEN, indicatorColor = Color(0xFF4CAF50)),
            Ticket("#Case-764322", "Stolen Property", "Jan 1, 2023 at 3:40pm", "Jan 1, 2023 at 3:40pm", TicketStatus.CLOSED),
        )
    }

    val filteredTickets = tickets.filter { it.status == selectedTab }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Support", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(20.dp))
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
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
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
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            // --- Search Bar ---
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Search for an ${selectedTab.name.lowercase()} ticket", color = KabuGray,
                        fontSize = 14.sp, fontWeight = FontWeight.W500)
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
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
                    TicketCard(ticket
                    )
                }
            }
        }
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
            // Use explicit interactionSource to avoid runtime missing-default-method issues in layoutlib
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
                    fontWeight =  FontWeight.W500,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun TicketCard(ticket: Ticket) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
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
                        Box(modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ticket.indicatorColor))
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    }
                }

                // Status Badge
                Surface(
                    color = if (ticket.status == TicketStatus.OPEN) Color(0xFFFFF5D8) else Color(0xFFE6F9E6),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (ticket.status == TicketStatus.OPEN) "Open" else "Closed",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = if (ticket.status == TicketStatus.OPEN) KabukabuYellow else Color(0xFF4CAF50),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

//            Spacer(modifier = Modifier.height(8.dp))
            Text(ticket.category, fontWeight = FontWeight.W600, fontSize = 12.sp, color = Color.Black)
            Text(ticket.date, fontWeight = FontWeight.W600, fontSize = 12.sp, color = Color.Black)

            if (ticket.status == TicketStatus.CLOSED && ticket.closedDate != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Closed on ${ticket.closedDate}", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun SupportTypeModal(onDismiss: () -> Unit, onSelect: () -> Unit) {
    // Use app's stacked bottom sheet design for consistency
    StackedBottomSheet(
        heightFraction = 0.4f,
        onDismiss = onDismiss) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            ModalHeader("New Support", "What type of support do you need?", onDismiss)
            Spacer(modifier = Modifier.height(24.dp))

            ModalOptionItem(Icons.Default.Star, "Trip support", "Get help about a trip (e.g report stolen property, etc.)", onSelect)
            Spacer(modifier = Modifier.height(12.dp))
            ModalOptionItem(Icons.Default.ShoppingCart, "General support", "Get help about a wallet, account, etc", onSelect)
        }
    }
}

@Composable
fun SubjectEntryModal(text: String, onTextChange: (String) -> Unit, onDismiss: () -> Unit, onSubmit: () -> Unit) {
    StackedBottomSheet(
        heightFraction = 0.5f,
        onDismiss = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
            Column(modifier = Modifier.padding(24.dp)) {
                ModalHeader("Enter Subject", "This will give clarity on the support type", onDismiss)
                Spacer(modifier = Modifier.height(24.dp))

                Text("Subject", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("Tell us what happened", color = Color.Gray) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KabukabuYellow,
                        unfocusedBorderColor = KabukabuYellow
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KabukabuYellow),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Submit", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ModalHeader(title: String, subtitle: String, onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopEnd).size(15.dp).background(Color(0xFFEB5757), CircleShape)
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun ModalOptionItem(icon: ImageVector, title: String, desc: String, onClick: () -> Unit) {
    // Use explicit interactionSource here as well to avoid layoutlib clickable default-method issues
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF9F9F9))
            .previewSafeClickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(24.dp))
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
        // Preview the support type modal with no-op handlers
        SupportTypeModal(onDismiss = {}, onSelect = {})
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
            onSubmit = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TicketListPreview() {
    KabukabuDriverTheme {
        TicketListScreen()
    }
}