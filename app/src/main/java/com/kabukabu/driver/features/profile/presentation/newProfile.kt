import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kabukabu.driver.R
import com.kabukabu.driver.core.theme.KabukabuDriverTheme
import com.kabukabu.driver.core.theme.KabukabuYellow
import com.kabukabu.driver.features.home.presentation.viewmodel.DriverViewModel

// Color constants based on the image
val greyCardBg = Color(0xFF282828)
val greytext = Color(0xFFC2C2C2)
val LightCardBg = Color(0xFFF7F7F7)
val AccentYellow = KabukabuYellow

// Helper function to capitalize the first letter of each word
fun String.capitalizeWords(): String {
    return this.split(" ").joinToString(" ") { it.capitalize() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit , vm: DriverViewModel = viewModel()) {
    // Observe profile data from ViewModel
    val profile by vm.userDetails.collectAsState()

    // Safe getters
    val user = profile?.user
    val userProfilePicture = user?.profileImage
    val fullName = user?.fullName?.capitalizeWords() ?: ""
    val phone = user?.phoneNumber ?: "Unknown"
    val email = user?.email ?: "Unknown"
    val referralCode = user?.referralCode ?: ""
    val guarantor = user?.guarantor
    val guarantorName = guarantor?.name?.capitalizeWords() ?: "Not set"
    val car = profile?.carDetails
    val carDesc = if (car != null) {
        val brand = car.brandName ?: ""
//        val model = car.model ?: ""
        val year = car.year ?: ""
        val color = car.color ?: ""
        listOf(brand, year, color).filter { it.isNotBlank() }.joinToString(", ")
    } else {
        "No car details"
    }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(innerPadding)
        ) {
            // --- Main Dark Card ---
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(),


                colors = CardDefaults.cardColors(containerColor = Color.Black),
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp)
                ) {

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 0.dp),
                        colors = CardDefaults.cardColors(containerColor = greyCardBg),
                        shape = RoundedCornerShape(10.dp)
                    ) {

                        Column(modifier = Modifier.padding(20.dp)) {
                            // Profile Image & Name
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (userProfilePicture.isNullOrBlank()) {
                                    Box {
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF3D3D3D)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val initial =
                                                fullName.firstOrNull()?.uppercaseChar()?.toString()
                                                    ?: "?"
                                            Text(
                                                text = initial,
                                                color = Color.White,
                                                fontWeight = FontWeight.W700,
                                                fontSize = 20.sp
                                            )

                                        }
//                                        Image(
//                                            painter = painterResource(id = R.drawable.edit_n),
//                                            contentDescription = "",
//                                            modifier = Modifier
//                                                .size(24.dp)
//                                                .align(Alignment.BottomEnd)
//
//                                        )
                                    }

                                } else {
                                    Box {
                                        AsyncImage(
                                            model = userProfilePicture,
                                            contentDescription = "Profile image",
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEFEFEF), shape = CircleShape)
                                        )
//                                        Image(
//                                            painter = painterResource(id = R.drawable.edit_n),
//                                            contentDescription = "",
//                                            modifier = Modifier
//                                                .size(24.dp)
//                                                .align(Alignment.BottomEnd)
//
//                                        )
                                    }
                                }
//                                Box {
//                                    // Replace with actual image painter
//                                    Box(
//                                        modifier = Modifier
//                                            .size(80.dp)
//                                            .clip(CircleShape)
//                                            .background(Color.Gray)
//                                    )
//                                    Image(
//                                        painter = painterResource(id = R.drawable.edit_n),
//                                        contentDescription = "",
//                                        modifier = Modifier
//                                            .size(24.dp)
//                                            .align(Alignment.BottomEnd)
//
//                                    )
//                                }


                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        fullName.ifBlank { "Unknown" },
                                        color = Color.White,
                                        fontWeight = FontWeight.W700,
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = AccentYellow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        " 4.5",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.W700,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            ProfileInfoItem(R.drawable.phone_n, phone, greytext)
                            ProfileInfoItem(R.drawable.envelope, email, greytext)
//                            ProfileInfoItem(R.drawable.pen, "View profile", greytext)

//                        Spacer(modifier = Modifier.height(20.dp))

                            // Bottom Referral Section

                        }
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 0.dp),
                        colors = CardDefaults.cardColors(containerColor = greyCardBg),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Your Code: ${referralCode.ifBlank { "Driver001" }}",
                                    color = Color.White,
                                    fontWeight = FontWeight.W600,
                                    fontSize = 14.sp
                            )
//                            Text(
//                                "Tap to view referral analytics",
//                                color = Color.Gray,
//                                fontSize = 12.sp,
//                                fontWeight = FontWeight.W400,
//                            )
                            }
                            // Interactive share and copy icons
                            // Use LocalContext and LocalClipboardManager to perform actions
                            val context = androidx.compose.ui.platform.LocalContext.current
                            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    // Share referral code via Android share sheet
                                    val code = referralCode.ifBlank { "Driver001" }
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(android.content.Intent.EXTRA_SUBJECT, "My referral code")
                                            putExtra(android.content.Intent.EXTRA_TEXT, "My referral code: $code")
                                        }
                                        context.startActivity(android.content.Intent.createChooser(intent, "Share referral code"))
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Unable to share referral code", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Image(
                                        painter = painterResource(id = R.drawable.upload_n),
                                        contentDescription = "Share referral code",
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                IconButton(onClick = {
                                    // Copy referral code to clipboard
                                    val code = referralCode.ifBlank { "Driver001" }
                                    try {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(code))
                                        android.widget.Toast.makeText(context, "Referral code copied", android.widget.Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Unable to copy referral code", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Image(
                                        painter = painterResource(id = R.drawable.copy_n),
                                        contentDescription = "Copy referral code",
                                    )
                                }
                            }
                         }
                     }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Guarantor Card ---
            InfoCard(title = "Guarantor") {
                Text(guarantorName, fontWeight = FontWeight.W700, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                ProfileInfoItem(R.drawable.user_a, "Mother", Color.Black)
                ProfileInfoItem(R.drawable.phone_a, "0909 888 7655", Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
//                Button(
//                    onClick = {},
//                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEDEDED)),
//                    shape = RoundedCornerShape(8.dp),
//                    contentPadding = PaddingValues(horizontal = 24.dp)
//                ) {
//                    Text("View", color = Color.Black, fontWeight = FontWeight.W600, fontSize = 14.sp)
//                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Car Details Card ---
            InfoCard(title = "Car details") {
                Image(
                    painter = painterResource(id = R.drawable.car_a),
                    contentDescription = "",


                )
                Text(carDesc.capitalizeWords() ?: "", fontWeight = FontWeight.W700, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                ProfileInfoItem(R.drawable.taxi_a, car?.plateNumber ?: "AB JHK 1234", Color.Black)
                ProfileInfoItem(R.drawable.file_a, "${profile?.documents?.size ?: 0} car documents uploaded", Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
//                Button(
//                    onClick = {},
//                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEDEDED)),
//                    shape = RoundedCornerShape(8.dp),
//                    contentPadding = PaddingValues(horizontal = 24.dp)
//                ) {
//                    Text("View", color = Color.Black, fontWeight = FontWeight.W600, fontSize = 14.sp)
//                }
            }
        }
    }
}

@Composable
fun ProfileInfoItem(iconRes: Int, text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = text,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, color = color, fontSize = 14.sp, fontWeight = FontWeight.W500)
    }
}

@Composable
fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightCardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = Color.Gray, fontSize = 12.sp,
                fontWeight = FontWeight.W500,)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

// Preview for IDE
@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    KabukabuDriverTheme {
        ProfileScreen(onBack = {})
    }
}
