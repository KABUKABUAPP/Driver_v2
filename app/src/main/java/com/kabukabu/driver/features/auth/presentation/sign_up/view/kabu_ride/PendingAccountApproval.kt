package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import android.annotation.SuppressLint
import android.content.Intent
import androidx.activity.compose.BackHandler

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kabukabu.driver.KabukabuDriverApp
import com.kabukabu.driver.R
import com.kabukabu.driver.components.ui.GIFImage
import com.kabukabu.driver.components.ui.KabuBottomButton
import com.kabukabu.driver.components.ui.TitleText
import com.kabukabu.driver.core.data.local.DataPersistenceViewModel
import com.kabukabu.driver.core.navigation.Navigator
import com.kabukabu.driver.features.auth.data.entity.response.VideoClipsResponse
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.AuthViewModel
import com.kabukabu.driver.features.home.presentation.viewmodel.DriverViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.core.net.toUri
import com.kabukabu.driver.components.ui.displayToastMessage
import com.kabukabu.driver.core.navigation.ApprovalStatus
import kotlinx.coroutines.delay

@Composable
fun KabuRidePendingAccountApprovalScreen(
    navigator: Navigator
) {
    val context = LocalContext.current
    val activityOwner = context as ViewModelStoreOwner
    val driverViewModel: DriverViewModel = viewModel(viewModelStoreOwner = activityOwner)
    val dataPersistenceViewModel: DataPersistenceViewModel = koinViewModel()
    val authViewModel: AuthViewModel = koinViewModel()
    val userPreferences = KabukabuDriverApp.getInstance().userPreferences
    val userDetails by userPreferences.userDetails.collectAsState(initial = null)
    val listOfClips = dataPersistenceViewModel.videoClips.collectAsState().value
    var showAccountApprovedModal by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = true) {}

    LaunchedEffect(Unit) {
        authViewModel.fetchVideoClips()
    }

    // Uncomment this block if you want to re-enable the periodic profile checks later.

    LaunchedEffect(Unit) {
        var time = 1
        while (true) {
            driverViewModel.fetchUserProfile()

            val documents = userDetails?.documents ?: emptyList()

            if (documents.any { it.status == "DECLINED" }) {
                navigator.navToKabuRideAccountDeclinedScreen()
                return@LaunchedEffect
            }

            if (userDetails?.user?.guarantorStatus?.lowercase() == ApprovalStatus.declined.name) {
                navigator.navToKabuRideReuploadGuarantorDetails()
                return@LaunchedEffect
            }

            if (userDetails?.user?.driver?.adminApproval?.lowercase() == ApprovalStatus.approved.name) {
                showAccountApprovedModal = true
                context.displayToastMessage("Account approved")
                navigator.navToKabuRideInspection()
                return@LaunchedEffect
            }
//            if (documents.isNotEmpty() && documents.all { it.status == "APPROVED" }) {
//                delay(500)
//                showAccountApprovedModal = true
//                return@LaunchedEffect
//            }

            println("called endpoint $time")
            time++
            delay(5000)
        }
    }


    if (showAccountApprovedModal) {
//        AccountApprovedModal(
//            onClick = {
//                navigator.navToKabuRideInspection()
//            }
//        )
    }

    Scaffold(
//        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GIFImage(
                gifImage = R.drawable.splash_light, // your GIF file (splash.gif)
                modifier = Modifier
                  .matchParentSize()
//                    .fillMaxWidth()
//                    .fillMaxHeight(0.6f)
//                    .alpha(0.3f) // transparency to keep focus on content
            )

            // ✅ Foreground content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
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
                            println("documents status.......$declinedDocuments")
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
}


//@PreviewParameter
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccountApprovedModal(
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
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    // 2. Calculate the min and max heights as a percentage of the screen height.
    val minWidth = screenWidth* 0.384f // 40% of screen height
    val minHeight = screenHeight* 0.106f


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
                        .width(minWidth)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, video.clip.toUri())
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .height(minHeight)
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
