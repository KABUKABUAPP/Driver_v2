package com.kabukabu.driver.components.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun TitleText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 14, bottomPadding: Int = 8,
    color: Color = Color.Black, fontWeight: FontWeight = FontWeight.Normal,
    startPadding: Int = 0, textAlign: TextAlign = TextAlign.Start,
    topPadding: Int = 0, letterSpacing: Int = 0, lineHeight: Int = 25,
    isVisible: Boolean = true,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Visible
) {
    if (isVisible) {
        Text(
            text = text,
            fontSize = fontSize.sp,
            color = color,
            textAlign = textAlign, fontWeight = fontWeight, lineHeight = lineHeight.sp,
            letterSpacing = letterSpacing.sp,
            modifier = modifier.padding(
                bottom = bottomPadding.dp, start = startPadding.dp, top = topPadding.dp
            ),
            maxLines = maxLines,
            overflow = overflow
        )
    }
}