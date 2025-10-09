package com.kabukabu.driver.features.auth.presentation.sign_up.view.kabu_ride

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kabukabu.driver.components.ui.FormTextfield
import com.kabukabu.driver.components.ui.KabuBottomButton
import com.kabukabu.driver.components.ui.KabuDivider
import com.kabukabu.driver.components.ui.RowScopeFormTextfield
import com.kabukabu.driver.components.ui.ScreenTitleText
import com.kabukabu.driver.components.ui.TitleText

@Composable
fun AccountDeclinedScreen() {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {

                TitleText(
                    text = "Your account was \ndeclined",
                    fontSize = 25,
                    fontWeight = FontWeight.W500,
                    bottomPadding = 12,
                    topPadding = 24
                )

                TitleText(
                    text = "Here’s why your account was declined",
                    bottomPadding = 16,
                    topPadding = 8,
                    fontWeight = FontWeight.W500,
                )

            }

            KabuBottomButton(
                text = "Resubmit",
                onClick = {}
            )
        }
    }

}