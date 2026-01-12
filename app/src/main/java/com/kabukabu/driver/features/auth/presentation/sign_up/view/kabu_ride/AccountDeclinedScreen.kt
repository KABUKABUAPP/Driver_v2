package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import com.kabukabu.driver.core.utils.composableSafeClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.R
import com.kabukabu.driver.components.ui.TitleText
import com.kabukabu.driver.core.navigation.ApprovalStatus
import com.kabukabu.driver.core.navigation.Navigator
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.AuthViewModel
import com.kabukabu.driver.features.home.presentation.viewmodel.DriverViewModel
import com.kabukabu.driver.features.profile.data.Document
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun KabuRideAccountDeclinedScreen(
    navigator: Navigator,
    authViewModel: AuthViewModel = koinViewModel()
) {

    val context = LocalContext.current
    // Create DriverViewModel at Activity scope so it's shared across Splash and Home
    val activityOwner = context as ViewModelStoreOwner
    val driverViewModel: DriverViewModel = viewModel(viewModelStoreOwner = activityOwner)
    val userPreferences = KabukabuDriverApp.getInstance().userPreferences
    val userDetails by userPreferences.userDetails.collectAsState(initial = null)
    var showAccountApprovedModal by rememberSaveable { mutableStateOf(false) }

    //disables back button
    BackHandler(enabled = true) {}

    val declinedDocuments = userDetails?.documents
        ?.filter { it.status == "DECLINED" }
        ?: emptyList()

    val noDeclinedDocs = userDetails?.documents
        ?.filter { it.status != "DECLINED" }
        ?: emptyList()


    LaunchedEffect(Unit) {
        while (true) {

            driverViewModel.fetchUserProfile()

            val documents = userDetails?.documents ?: emptyList()

            // 1️⃣ If any document is DECLINED → go to Declined screen
            if (documents.any { it.status == "DECLINED" }) {
                delay(3000)
                return@LaunchedEffect
            }

            // 2️⃣ If any documents is PENDING → nav to pending screen
            if (documents.any { it.status == "PENDING" }) {
                navigator.navToKabuRidePendingAccountApprovalScreen()
                return@LaunchedEffect
            }

            if (userDetails?.user?.guarantorStatus?.lowercase() == ApprovalStatus.declined.name) {
                navigator.navToKabuRideReuploadGuarantorDetails()
                return@LaunchedEffect
            }


            // 3️⃣ Otherwise (no declined, not all approved → must have some pending)
            if (userDetails?.user?.driver?.adminApproval == ApprovalStatus.approved.name) {
                showAccountApprovedModal = true
                return@LaunchedEffect
            }


            delay(5000)
        }
    }

    if (showAccountApprovedModal) {
        AccountApprovedModal(
            onClick = {
                navigator.navToKabuRideInspection()
            }
        )
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
//            verticalArrangement = Arrangement.SpaceBetween
        ) {
//            Column {

            TitleText(
                text = "❌",
                color = Color.Red,
                fontSize = 25,
                fontWeight = FontWeight.W500,
                bottomPadding = 12,
                topPadding = 30
            )

            TitleText(
                text = "Your account was \ndeclined",
                fontSize = 25,
                fontWeight = FontWeight.W500,
                bottomPadding = 20,
                topPadding = 12,
                lineHeight = 30
            )

//                TitleText(
//                    text = "Here’s why your account was declined",
//                    fontSize = 16,
//                    bottomPadding = 16,
//                    topPadding = 8,
//                    fontWeight = FontWeight.W400,
//                )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White, RoundedCornerShape(12.dp))
            ) {
                DeclinedDocs(declinedDocuments, navigator)
            }


        }

    }
}


@Composable
private fun DeclinedDocs(
    declinedTitles: List<Document>, navigator: Navigator,

    ) {

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(declinedTitles) { index, data ->
            ReasonForApprovalDeclineCard(
                document = data,
                onClick = {
                    navigator.navToKabuRideDocumentsReuploadScreen(
                        data.id ?: "",
                        data.title ?: "" // pass your title here
                    )
                }
            )

        }
    }
}

internal fun formatDeclinedDocumentName(text: String): String {
    return when (text.lowercase()) {
        "vehicle_license" -> "Vehicle License"
        "insurance_certificate" -> "Insurance Certificate"
        "proof_of_ownership" -> "Proof of Ownership"
        "road_worthiness" -> "Road Worthiness"
        "hackney_permit" -> "Hackney Permit"
        "driver_license" -> "Driver License"
        else -> text.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    }
}


@Composable
private fun ReasonForApprovalDeclineCard(
    document: Document,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(color = Color(0xFFF8F8F8), RoundedCornerShape(12.dp))
            .fillMaxWidth()
            .composableSafeClickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Icon(
            painter = painterResource(id = R.drawable.document),
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier
                .padding(start = 16.dp)
                .size(30.dp)
        )


        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(all = 16.dp)
        ) {
            TitleText(
                text = formatDeclinedDocumentName(document.title ?: ""),
                fontWeight = FontWeight.W500,
                fontSize = 16, topPadding = 0,
                bottomPadding = 0
            )

            TitleText(
                text = "Tap to re-upload",
                //                text = document.status ?: "",
                color = Color(0xFF686868),
                bottomPadding = 0,
                fontSize = 13
            )
        }

        Box(modifier = Modifier.height(15.dp))


    }

}