package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.bumptech.glide.Glide
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.R
import com.kabukabu.driver.components.ui.GIFImage
import com.kabukabu.driver.components.ui.KabuBottomButton
import com.kabukabu.driver.components.ui.KabuSpacer
import com.kabukabu.driver.components.ui.TitleText
import com.kabukabu.driver.core.data.local.DataPersistenceViewModel
import com.kabukabu.driver.core.data.local.LocalDataSource
import com.kabukabu.driver.core.navigation.Navigator
import com.kabukabu.driver.features.auth.data.entity.response.VideoClipsResponse
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.AuthViewModel
import com.kabukabu.driver.features.home.presentation.DriverViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.core.net.toUri
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun KabuRidePendingAccountApprovalScreen(
    navigator: Navigator
) {

    val context = LocalContext.current
    // Create DriverViewModel at Activity scope so it's shared across Splash and Home
    val activityOwner = context as ViewModelStoreOwner
    val driverViewModel: DriverViewModel = viewModel(viewModelStoreOwner = activityOwner)
    val dataPersistenceViewModel: DataPersistenceViewModel = koinViewModel()
    val authViewModel: AuthViewModel = koinViewModel()
    val coroutineScope = rememberCoroutineScope()
    val userPreferences = KabukabuDriverApp.getInstance().userPreferences
    val userDetails by userPreferences.userDetails.collectAsState(initial = null)
    val listOfClips = dataPersistenceViewModel.videoClips.collectAsState().value
    var showAccountApprovedModal by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {}

    val declinedDocuments = userDetails?.documents
        ?.filter { it.status == "DECLINED" }
        ?: emptyList()

    //listen to state update and nav to Declined screen if status had been changed.
    LaunchedEffect(Unit) {
        while (true) {
            driverViewModel.fetchUserProfile()
            declinedDocuments.forEach { document ->
                when (document.status) {
                    "DECLINED" -> {
                        navigator.navToKabuRideAccountDeclinedScreen()
                        return@LaunchedEffect // stop further checks if navigated away
                    }
                    "APPROVED" -> {
                        delay(500)
                        showAccountApprovedModal = true
                    }
                }
            }
            delay(3000)
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
//                .verticalScroll(rememberScrollState())
        ) {
            Column {
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

            LearnMoreAboutKabukabu(listOfClips)

        }
    }
}

//@PreviewParameter
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountApprovedModal(
    onClick: () -> Unit,
) {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = {},
        sheetState = sheetState,
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


@SuppressLint("UseKtx")
@Composable
fun LearnMoreAboutKabukabu(videos: List<VideoClipsResponse>) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {

        TitleText("Learn more about Kabukabu",
            fontSize = 16,
            fontWeight = FontWeight.W500,
            bottomPadding = 16
        )

        LazyRow(
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(videos) { video ->
                Column(
                    modifier = Modifier
                        .width(160.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, video.clip.toUri())
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .height(100.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = video.thumbnail,
                            contentDescription = video.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.play_square),
                            contentDescription = "Play video",
                            tint = Color.White,
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.Center)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                    Text(
                        text = video.duration,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }
            }
        }
    }
}

//data class VideoItem(
//    val duration: String,
//    val thumbnail: String,
//    val title: String,
//    val clip: String
//)
