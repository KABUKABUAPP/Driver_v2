package com.kabukabu.driver.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kabukabu.driver.R
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("About", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.angle_right),
                            contentDescription = "Back",
                            tint = Color(0xFF9A9A9A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))) {
                Column(Modifier.padding(12.dp)) {
                    Text("About", color = Color.Black, fontWeight = FontWeight.W600, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        // Rate App (opens store listing)
                        val url = "https://apps.apple.com/app/id6457205132"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }, modifier = Modifier.fillMaxWidth()) { Text("Rate App") }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        val url = "https://www.kabukabu.com.ng/about-us"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }, modifier = Modifier.fillMaxWidth()) { Text("About Kabukabu") }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        val url = "https://www.kabukabu.com.ng/terms-of-service"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }, modifier = Modifier.fillMaxWidth()) { Text("Legal") }
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))) {
                Column(Modifier.padding(12.dp)) {
                    Text("Follow us", color = Color.Black, fontWeight = FontWeight.W600, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        val url = "https://www.facebook.com/profile.php?id=100091741160703&mibextid=LQQJ4d"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }, modifier = Modifier.fillMaxWidth()) { Text("Facebook") }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        val url = "https://x.com/getkabukabu?s=21"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }, modifier = Modifier.fillMaxWidth()) { Text("Twitter/X") }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        val url = "https://instagram.com/getkabukabu?igshid=MzMyNGUyNmU2YQ=="
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }, modifier = Modifier.fillMaxWidth()) { Text("Instagram") }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        val url = "https://www.linkedin.com/company/kabukabu-app?originalSubdomain=ng"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }, modifier = Modifier.fillMaxWidth()) { Text("LinkedIn") }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Made in 🇳🇬", color = Color(0xFF6A6A6A), fontSize = 12.sp)
        }
    }
}
