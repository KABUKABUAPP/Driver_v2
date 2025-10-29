package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.components.ui.KabuBottomButton
import com.kabukabu.driver.features.home.presentation.DriverViewModel


@Composable
fun KabuRideTermsAndConditionsScreen(onNavToSelfieVerification: () -> Unit) {

    val context = LocalContext.current
    // Create DriverViewModel at Activity scope so it's shared across Splash and Home
    val activityOwner = context as ViewModelStoreOwner
    val driverViewModel: DriverViewModel = viewModel(viewModelStoreOwner = activityOwner)

    BackHandler { true }


    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Kabukabu T&C",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp, top = 12.dp)
            )

            SectionHeader("Subscription")
            SectionBody("You agree to pay to Kabukabu a commission fee of 5%, as consideration for your access to Kabukabu’s services and platforms.")

            SectionHeader("VAT on services")
            SectionBody("You agree to the deduction and remittance of Value Added Tax and other applicable taxes in accordance with relevant laws in the Federal Republic of Nigeria.")

            SectionHeader("Before the ride;")
            SectionBody("Have change ready. A driver always needs to be able to make change, even if the passenger wants to pay using large bills.")
            SectionBody("Don't pick up other people besides the passenger and co-travellers (maximum of 4 in total where there are co-travellers). Drivers must work alone; third parties are not permitted to be in the car during the ride.")
            SectionBody("Keep your car clean and air out the interior. Check that the car is in good working order before the ride.")
            SectionBody("Check the mechanical condition of your vehicle regularly. The driver must ensure the passengers' safety en route.")

            SectionHeader("When accepting a ride request;")
            SectionBody("Accept requests when the price and the route are convenient for you. Pay attention to the passenger's options and comments, and also to their rating.")
            SectionBody("Accept orders in neighbourhoods you know, and use the GPS to save your own time and your passenger's.")
            SectionBody("If you are running late, call the passenger and let them know.")
            SectionBody("Try to pull up as close as possible to the pickup and destination points.")
            SectionBody("Only tell the passenger you have arrived when you are at the pickup point. Mark the ride as completed when you reach the address specified, not earlier.")

            SectionHeader("During the ride;")
            SectionBody("Things that make passengers leave 5 stars and that raise your Priority:\n- friendliness and politeness\n- helping the passenger with their luggage\n- safe and careful driving\n\nThings that lower your Priority:\n- talking on the phone during the trip\n- talking with passengers about your personal life, politics, religion, or national differences\n- being disrespectful to the passenger — for example, by playing music too loudly\n- smoking while driving\n- transporting other people or passengers from another ride\n- making personal stops during a ride")

            SectionHeader("After the ride;")
            SectionBody("If the passenger left any belongings behind contact the passenger or contact support@kabukabu.com.ng.")

            SectionHeader("Parcels")
            SectionBody("If you are asked to deliver a parcel:\n1. Ask the sender to show you exactly what you will be transporting.\n2. Ask them to seal the parcel in your presence.")
            SectionBody("Do not accept a parcel if:\n1. The sender refuses to show you the contents.\n2. The contents strike you as illegal or suspicious.")
            SectionBody("If you suspect that the sender is breaking the law or the rules of our service, please contact support@kabukabu.com.ng.")
            KabuBottomButton("Agree and Continue", topPadding = 20,
                onClick = onNavToSelfieVerification
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Color.Black,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
fun SectionBody(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = Color.Black,
        lineHeight = 20.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
