import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.core.theme.KabukabuDriverTheme
import com.kabukabu.driver.core.utils.safeClickable
import com.kabukabu.driver.features.support.presentation.SupportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreenNew(onBack: () -> Unit, onOpenTicket: (String) -> Unit, onClickSupport: () -> Unit = {}, vm: SupportViewModel = viewModel()) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Support",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Main Heading
            Text(
                text = "What can we help\nyou with?",
                fontSize = 25.sp,
                lineHeight = 29.sp,
                fontWeight = FontWeight.W700,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Support Action
            SupportOptionCard(
                icon = Icons.Default.Phone,
                title = "Contact support",
                description = "Get help about a trip (e.g report stolen property, etc.)",
                onClick = onClickSupport
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Secondary Heading
            Text(
                text = "Other issues?",
                fontSize = 25.sp,
                lineHeight = 29.sp,
                fontWeight = FontWeight.W700,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Mail Action
            SupportOptionCard(
                icon = Icons.Default.Email,
                title = "Mail Us",
                description = "Send us a mail"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Call Action
            SupportOptionCard(
                icon = Icons.Default.Phone,
                title = "Call Us",
                description = "Send us a mail" // Kept as "mail" to match the screenshot typo
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SupportOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth().safeClickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF7F7F7) // Light gray background per design
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.Black
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W700,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color(0xff9A9A9A),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// --- Preview Implementation ---

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SupportScreenPreview() {
    KabukabuDriverTheme {
        SupportScreenNew(onBack = {}, onOpenTicket = {})
    }
}