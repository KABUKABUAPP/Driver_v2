package com.kabukabu.driver.features.home.presentation.views.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.kabukabu.driver.core.utils.safeClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kabukabu.driver.R
import com.kabukabu.driver.core.data.local.UserPreferences
import com.kabukabu.driver.features.home.presentation.viewmodel.DriverViewModel
import kotlinx.coroutines.flow.firstOrNull
import com.kabukabu.driver.features.profile.data.ProfileData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
 fun FullScreenDrawer(
    onClose: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToMyTrips: () -> Unit,
    onNavigateToPromotions: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToRepairLoan: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSocketDebug: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        color = Color.White
    ) {
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            try {
                val userPreferences = UserPreferences.getInstance(context)
                val token = userPreferences.authToken.firstOrNull()
                val savedPreference = userPreferences.userDetails.firstOrNull()
                Log.d("AuthToken", "FULL TOKEN: $token")
            } catch (e: Exception) {
                Log.e("AuthToken", "Failed to read token: ${e.message}")
            }
        }

        val userPreferences = UserPreferences.getInstance(context)
        val user = userPreferences.userDetails.collectAsState(initial = null).value
        val userProfile = user?.user


        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "My Account",
                        color = Color.Black,
                        fontWeight = FontWeight.W600,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(36.dp)
                            .border(
                                width = 1.dp,
                                color = Color(0xFFE1E1E1),
                                shape = CircleShape
                            )
                            .safeClickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF9A9A9A),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Profile header container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(Color(0xFFF9F9F9), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp)
                        .safeClickable {
                            onNavigateToProfile()
                                   },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Avatar + Name/Link
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Compute display name with first letter capitalized
                            val displayName = userProfile?.fullName?.trim()?.let { name ->
                                if (name.isEmpty()) "" else name.replaceFirstChar { ch -> ch.uppercaseChar() }
                            } ?: ""

                            // Avatar: if profileImage is blank, show initial in circle; else show image
                            if (userProfile?.profileImage.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF3D3D3D)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val initial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                                    Text(
                                        text = initial,
                                        color = Color.White,
                                        fontWeight = FontWeight.W700,
                                        fontSize = 20.sp
                                    )
                                }
                            } else {
                                AsyncImage(
                                    model = userProfile.profileImage,
                                    contentDescription = "Profile image",
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEFEFEF), shape = CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = displayName,
                                    color = Color(0xFF161616),
                                    fontWeight = FontWeight.W600,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "view profile",
                                    color = Color(0xFF3D3D3D),
                                    fontWeight = FontWeight.W400,
                                    fontSize = 12.sp,
                                )
                            }
                        }

                        // Right: rating
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFFBF00),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = (userProfile?.averageRating?.value ?: 0.0).let { java.util.Locale.getDefault().let { loc -> String.format(loc, "%.1f", it) } },
                                color = Color(0xFF161616),
                                fontWeight = FontWeight.W700,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Drawer menu items
                Column(modifier = Modifier.fillMaxWidth()) {
                    // General group
                    Text(
                        text = "General",
                        color = Color(0xFF161616),
                        fontWeight = FontWeight.W700,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                            DrawerMenuItem(
                                iconRes = R.drawable.wallet,
                                title = "Wallet",
                                onClick = {
                                    onNavigateToWallet()
                                }
                            )
                            DrawerMenuItem(
                                iconRes = R.drawable.chart_bar,
                                title = "Analytics",
                                onClick = { onNavigateToAnalytics() }
                            )
                            DrawerMenuItem(
                                iconRes = R.drawable.taxi_d,
                                title = "Trip Activities",
                                onClick = { onNavigateToMyTrips() }
                            )
                            DrawerMenuItem(
                                iconRes = R.drawable.receipt_percent,
                                title = "Promotions",
                                onClick = { onNavigateToPromotions() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Support group
                    Text(
                        text = "Support",
                        color = Color(0xFF161616),
                        fontWeight = FontWeight.W700,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                            DrawerMenuItem(
                                iconRes = R.drawable.info_circle,
                                title = "About",
                                onClick = { onNavigateToAbout() }
                            )
//                            DrawerMenuItem(
//                                iconRes = R.drawable.mynaui_shield_solid,
//                                title = "Repair Loan",
//                                onClick = { onNavigateToRepairLoan() }
//                            )
                            DrawerMenuItem(
                                iconRes = R.drawable.messages_text,
                                title = "Support",
                                onClick = { onNavigateToSupport() }
                            )
//                            DrawerMenuItem(
//                                iconRes = R.drawable.ic_launcher_foreground, // Using default icon
//                                title = "🔧 Socket Debug",
//                                onClick = { onNavigateToSocketDebug() }
//                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Final unnamed group with only Logout
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                            DrawerMenuItem(
                                iconRes = R.drawable.log_out,
                                title = "Logout",
                                onClick = {
                                    onLogout()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    iconRes: Int,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .safeClickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = title,
            modifier = Modifier
                .size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = Color.Black,
            fontWeight = FontWeight.W600,
            fontSize = 15.sp
        )
    }
}
