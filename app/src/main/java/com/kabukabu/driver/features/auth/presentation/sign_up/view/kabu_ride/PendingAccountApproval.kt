package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.Glide
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.R
import com.kabukabu.driver.components.ui.GIFImage
import com.kabukabu.driver.components.ui.KabuBottomButton
import com.kabukabu.driver.components.ui.KabuSpacer
import com.kabukabu.driver.components.ui.TitleText
import com.kabukabu.driver.core.data.local.LocalDataSource
import com.kabukabu.driver.core.navigation.Navigator
import com.kabukabu.driver.features.home.presentation.DriverViewModel


@Composable
fun KabuRidePendingAccountApprovalScreen(onNavigateToLogin: () -> Unit, navigator: Navigator) {

    val context = LocalContext.current
    // Create DriverViewModel at Activity scope so it's shared across Splash and Home
    val activityOwner = context as ViewModelStoreOwner
    val driverViewModel: DriverViewModel = viewModel(viewModelStoreOwner = activityOwner)
    val userPreferences = KabukabuDriverApp.getInstance().userPreferences
    val userDetails by userPreferences.userDetails.collectAsState(initial = null)

    var showAccountApprovedModal by remember { mutableStateOf(false) }


    BackHandler(enabled = true) {}

    val declinedDocuments = userDetails?.documents
        ?.filter { it.status == "DECLINED" }
        ?: emptyList()

    //listen to state update and nav to Declined screen if status had been changed.
    LaunchedEffect(Unit) {
        declinedDocuments.forEach { document ->
            when (document.status) {
                "DECLINED" -> navigator.navToKabuRideAccountDeclinedScreen()
                "APPROVED" -> {
                    showAccountApprovedModal = true
//                    navigator.navToKabuRideInspection()
                }
            }
        }
    }

    if (showAccountApprovedModal) {
        AccountApprovedModal(
            onClick = {
                navigator.navToKabuRideInspection()
            }
        )
    }


    val bgModifier = Modifier
        .paint(
            painter = painterResource(id = R.drawable.map_bg),
            alpha = 0.1f,
            contentScale = ContentScale.FillWidth
        )
        .fillMaxWidth()

    Scaffold { paddingValues ->
        Column(
            modifier = bgModifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TitleText(
                text = "Your account is pending \napproval",
                fontSize = 25,
                fontWeight = FontWeight.W500,
                bottomPadding = 12,
                topPadding = 24,
                modifier = Modifier.clickable {
                    val declinedDocuments = userDetails?.documents
                    println("douments status.......$declinedDocuments")
                }
            )

            TitleText(
                text = "We are doing background check. We will notify you immediately afterwards",
                bottomPadding = 16,
                topPadding = 8,
                fontWeight = FontWeight.W500
            )
        }
    }
}

//@PreviewParameter
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountApprovedModal(
    onClick: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = {},
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            GIFImage(
                gifImage = R.drawable.account_approved,
                modifier = Modifier
            )
            TitleText(
                "Your account is approved!",
                fontSize = 22,
                fontWeight = FontWeight.Bold,
            )
            TitleText(
                "You can now drive and earn with us",
                fontSize = 15,
                bottomPadding = 30,
            )

            KabuBottomButton(
                "Let's go",
                onClick = onClick
            )

        }
    }
}