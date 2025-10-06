package com.kabukabu.driver.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kabukabu.driver.R
import com.kabukabu.driver.components.utils_functions.priceFilter


@Composable
fun TitleText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 14, bottomPadding: Int = 8,
    color: Color = Color.Black, fontWeight: FontWeight = FontWeight.Normal,
    startPadding: Int = 0, textAlign: TextAlign = TextAlign.Start,
    topPadding: Int = 0, endPadding: Int = 0, letterSpacing: Int = 0, lineHeight: Int = 25,
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
                bottom = bottomPadding.dp, start = startPadding.dp, top = topPadding.dp,
                end = endPadding.dp
            ),
            maxLines = maxLines,
            overflow = overflow
        )
    }
}

@Composable
fun KabuDivider(modifier: Modifier = Modifier, height: Double, color: Color = Color.Transparent) {
    HorizontalDivider(
        modifier
            .height(height.dp),
        DividerDefaults.Thickness, color = color
    )
}

@Composable
fun KabuOutlinedTextField(
    modifier: Modifier = Modifier,
    keyboardType: String? = "",
    value: String? = "",
    onTextChanged: (text: String) -> Unit = {},
    placeholderText: String = "",
    bottomPadding: Int = 15,
    isClickable: Boolean = true,
    isAccountNumber: Boolean = false,
    focusable: Boolean? = false,
    isValidationError: Boolean = false,
    isAmount: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    textFieldColors: TextFieldColors = TextFieldDefaults.colors()
) {
    Box(
        modifier = modifier
            .padding(bottom = bottomPadding.dp)
            .focusable(enabled = focusable!!)
            .background(color = Color.Transparent)
    ) {
        // Base color configuration: everything default, except border/outline is transparent
        val noBorderColors = textFieldColors.copy(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent
        )

        when (keyboardType) {
            "number" -> {
                OutlinedTextField(
                    value = value ?: "",
                    onValueChange = { newValue ->
                        if (isAccountNumber) {
                            val trimmedValue = newValue.take(12)
                            onTextChanged(trimmedValue)
                        } else if (isAmount) {
                            if ((Regex("\\.").findAll(newValue).count() > 1).not()) {
                                onTextChanged(newValue)
                            }
                        } else {
                            onTextChanged(newValue)
                        }
                    },
                    placeholder = {
                        Text(
                            placeholderText,
                            fontSize = 14.sp
                        )
                    },
                    colors = noBorderColors,
                    visualTransformation = {
                        priceFilter(it, isAmount)
                    },
                    singleLine = true,
                    isError = isValidationError,
                    enabled = isClickable,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = imeAction
                    ),
                )
            }

            "number password" -> {
                OutlinedTextField(
                    value = value ?: "",
                    onValueChange = { newValue ->
                        if (isAccountNumber) {
                            val trimmedValue = newValue.take(12)
                            onTextChanged(trimmedValue)
                        } else if (isAmount) {
                            if ((Regex("\\.").findAll(newValue).count() > 1).not()) {
                                onTextChanged(newValue)
                            }
                        } else {
                            onTextChanged(newValue)
                        }
                    },
                    placeholder = {
                        Text(
                            placeholderText,
                            fontSize = 14.sp
                        )
                    },
                    enabled = isClickable,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = imeAction
                    ),
                    isError = isValidationError,
                    colors = noBorderColors
                )
            }

            else -> {
                OutlinedTextField(
                    value = value ?: "",
                    onValueChange = { newValue ->
                        if (isAccountNumber) {
                            val trimmedValue = newValue.take(12)
                            onTextChanged(trimmedValue)
                        } else if (isAmount) {
                            if ((Regex("\\.").findAll(newValue).count() > 1).not()) {
                                onTextChanged(newValue)
                            }
                        } else {
                            onTextChanged(newValue)
                        }
                    },
                    placeholder = {
                        Text(
                            placeholderText,
                            fontSize = 14.sp
                        )
                    },
                    enabled = isClickable,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = imeAction
                    ),
                    isError = isValidationError,
                    colors = noBorderColors
                )
            }
        }
    }
}


@Composable
fun KabuOutlinedTextFieldWithTrailingIconButton(
    value: String? = "",
    placeholderText: String,
    iconTint: Color = Color.Gray,
    bottomPadding: Int = 15,
    onClick: () -> Unit = {},
    isError: Boolean = false,
    onTextChanged: (text: String) -> Unit = {},
    textFieldColors: TextFieldColors = TextFieldDefaults.colors()
) {
    // Use base theme but make only the border invisible
    val noBorderColors = textFieldColors.copy(
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
        disabledContainerColor = Color(0xFFF1F1F1),

    )

    Box(
        modifier = Modifier
            .padding(bottom = bottomPadding.dp)
            .background(Color.Transparent)
    ) {
        OutlinedTextField(
            value = value ?: "",
            onValueChange = { newValue ->
                onTextChanged(newValue)
            },
            readOnly = true,
            enabled = false,
            isError = isError,
            colors = noBorderColors,
            placeholder = {
                Text(
                    text = placeholderText,
                    fontSize = 14.sp
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(10),
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.chevron_down),
                    tint = iconTint,
                    contentDescription = null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable { onClick() }
        )
    }
}


@Composable
fun KabuSpacer(width: Int) {
    Spacer(modifier = Modifier.width(width.dp))
}