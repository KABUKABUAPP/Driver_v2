package com.kabukabu.driver.features.support.presentation

import android.os.Parcelable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabukabu.driver.R
import com.kabukabu.driver.core.theme.KabuGray
import com.kabukabu.driver.core.theme.KabukabuDriverTheme
import kotlinx.parcelize.Parcelize


@Parcelize
enum class TripStatus : Parcelable { COMPLETED, CANCELLED }

@Parcelize
data class SupportTrip(
    val id: String,
    val destination: String,
    val date: String,
    val time: String,
    val amount: String,
    val paymentMethod: String,
    val status: TripStatus
) : Parcelable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectSupportTripScreen(onBack: () -> Unit, onTripSelected: (SupportTrip) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

    val trips = remember {
        listOf(
            SupportTrip(
                id = "1",
                destination = "Film house Cinemas IMAX Lekki",
                date = "1 November",
                time = "3:30pm",
                amount = "₦1,300",
                paymentMethod = "cash payment",
                status = TripStatus.COMPLETED
            ),
            SupportTrip(
                id = "2",
                destination = "Film house Cinemas IMAX Lekki",
                date = "1 November",
                time = "3:30pm",
                amount = "₦0",
                paymentMethod = "cancelled",
                status = TripStatus.CANCELLED
            ),
            SupportTrip(
                id = "3",
                destination = "Film house Cinemas IMAX Lekki",
                date = "1 November",
                time = "3:30pm",
                amount = "₦10,000",
                paymentMethod = "wallet payment",
                status = TripStatus.COMPLETED
            ),
            SupportTrip(
                id = "4",
                destination = "Filmhouse Cinemas IMAX Lekki",
                date = "1 November",
                time = "3:30pm",
                amount = "₦10,000",
                paymentMethod = "cash payment",
                status = TripStatus.COMPLETED
            )
        )
    }

    Scaffold(
//        modifier = Modifier.navigationBarsPadding(),
        topBar = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text("New Support", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.size(24.dp)) // Placeholder for alignment
                }
                Text(
                    text = "Select a trip you need support for",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        },
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Search for a date", color = KabuGray)
                },
                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF8F8F8),
                    focusedContainerColor = Color(0xFFF8F8F8),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Today", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(trips) { trip ->
                    SupportTripItem(trip = trip, onTripSelected = onTripSelected)
                }
            }
        }
    }
}


@Composable
fun SupportTripItem(trip: SupportTrip, onTripSelected: (SupportTrip) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.trip_car),
            contentDescription = "Activity Icon",
            modifier = Modifier.size(50.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = trip.destination, fontWeight = FontWeight.W600, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "${trip.date}, ${trip.time}", fontSize = 12.sp, color = KabuGray)
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val amountColor = if (trip.status == TripStatus.CANCELLED) Color.Gray else Color.Black
                Text(text = trip.amount, fontWeight = FontWeight.W700, fontSize = 12.sp, color = amountColor)
                Text(text = " • ", fontSize = 14.sp, color = Color.LightGray)
                Text(text = trip.paymentMethod, fontSize = 12.sp, color = KabuGray)
            }
        }
        Button(
            onClick = { onTripSelected(trip) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF2F2F2), 
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Select", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectSupportTripScreenPreview() {
    KabukabuDriverTheme {
        SelectSupportTripScreen(onBack = {}, onTripSelected = {})
    }
}
