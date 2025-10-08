package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kabukabu.driver.R
import com.kabukabu.driver.components.ui.AnnotatedTextfieldTitle
import com.kabukabu.driver.components.ui.CustomLinearProgressIndicator
import com.kabukabu.driver.components.ui.FormTextfield
import com.kabukabu.driver.components.ui.GrayBackgroundContainer
import com.kabukabu.driver.components.ui.KabuBottomButtonRowScope
import com.kabukabu.driver.components.ui.KabuDivider
import com.kabukabu.driver.components.ui.KabuTransparentBottomButtonRowScope
import com.kabukabu.driver.components.ui.ScreenTitleText
import com.kabukabu.driver.components.ui.TitleText

@Composable
fun KabuRideCarDocumentsUploadScreen() {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 30.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            CustomLinearProgressIndicator(
                progress = 0.67f,
                modifier = Modifier.padding(bottom = 50.dp)
            )

            ScreenTitleText(
                title = "Documents",
                subtitle = "Upload your car documents",
                bottomPadding = 16
            )

            GrayBackgroundContainer {

                UploadDocumentItem(
                    title = "Vehicle License",
                    label = "Tap here to capture",
                    onClick = {}
                )

                FormTextfield(
                    title = "Vehicle License Number",
                    text = "",
                    hintText = "ABC1234567",
                    onTextChanged = {}
                )

                UploadDocumentItem(
                    title = "Driver’s License",
                    label = "Tap here to capture",
                    onClick = {}
                )

                FormTextfield(
                    title = "Driver's License Number",
                    text = "",
                    hintText = "ABC1234567",
                    onTextChanged = {}
                )

                UploadDocumentItem(
                    title = "Issuance Certificate",
                    label = "Tap here to capture",
                    onClick = {}
                )

                FormTextfield(
                    title = "Issuance Certificate Number",
                    text = "",
                    hintText = "ABC1234567",
                    onTextChanged = {}
                )

                UploadDocumentItem(
                    title = "Proof of Ownership",
                    label = "Tap here to capture",
                    onClick = {}
                )

                FormTextfield(
                    title = "Proof of Ownership Number",
                    text = "",
                    hintText = "ABC1234567",
                    isCompulsory = false,
                    onTextChanged = {}
                )

                UploadDocumentItem(
                    title = "Road Worthiness Certificate",
                    label = "Tap here to capture",
                    onClick = {}
                )

                FormTextfield(
                    title = "Road Worthiness Certificate",
                    text = "",
                    hintText = "ABC1234567",
                    isCompulsory = false,
                    onTextChanged = {}
                )


                UploadDocumentItem(
                    title = "Hackney Permit",
                    label = "Tap here to capture",
                    isCompulsoryField = false,
                    onClick = {}
                )

                TitleText(
                    "Tap to reupload",
                    color = Color.Gray,
                    fontWeight = FontWeight.W500
                )

                FormTextfield(
                    title = "Hackney Permit Number",
                    text = "",
                    hintText = "Doc-IMHG-0088",
                    isCompulsory = false,
                    onTextChanged = {}
                )
            }

            Row(
                modifier = Modifier.padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                KabuTransparentBottomButtonRowScope(
                    "Previous",
                    icon = R.drawable.arrow_left
                )

                KabuBottomButtonRowScope(
                    "Next",
                    icon = R.drawable.arrow_right
                )
            }

        }

    }

}

@Composable
fun UploadDocumentItem(
    title: String,
    label: String,
    isCompulsoryField: Boolean = true,
    onClick: () -> Unit
) {
    Column {
        AnnotatedTextfieldTitle(title, isCompulsoryField)
        UploadDocumentBox(
            label = label,
            onClick = onClick
        )
    }
}

@Composable
fun UploadDocumentBox(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
//            .height(100.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF1F1F1))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.doc_upload),
                contentDescription = "image placeholder icon",
                modifier = Modifier.size(22.dp),
                tint = Color.Gray
            )
            TitleText(
                label,
                topPadding = 12
            )


        }
    }

}

