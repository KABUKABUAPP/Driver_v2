package com.kabukabu.driver.features.home.presentation.views.components

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kabukabu.driver.core.data.socket.TripFoundEvent
import com.kabukabu.driver.core.theme.KabukabuDriverTheme

@Composable
 fun TripRequestCard(
    isVisible: Boolean,
    tripDetails: TripFoundEvent?,
    driverLocation: Location?,
    remainingTime: Int,
    isAccepting: Boolean,
    isDeclining: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onTimeout: () -> Unit
) {
    if (isVisible) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Trip Request",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Time remaining: ${remainingTime}s",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onDecline,
                        enabled = !isDeclining && !isAccepting,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        if (isDeclining) {
                            Text("Declining...")
                        } else {
                            Text("Decline")
                        }
                    }

                    Button(
                        onClick = onAccept,
                        enabled = !isAccepting && !isDeclining,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) {
                        if (isAccepting) {
                            Text("Accepting...")
                        } else {
                            Text("Accept")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TripRequestCardPreview() {
    KabukabuDriverTheme {
        TripRequestCard(
            isVisible = true,
            tripDetails = null,
            driverLocation = null,
            remainingTime = 25,
            isAccepting = false,
            isDeclining = false,
            onAccept = {},
            onDecline = {},
            onTimeout = {}
        )
    }
}
