package com.kabukabu.driver.components.ui

import android.content.Context
import android.graphics.Insets.add
import android.os.Build.VERSION.SDK_INT
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.kabukabu.driver.R


@Composable
fun TitleText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 14, bottomPadding: Int = 8,
    color: Color = Color.Black, fontWeight: FontWeight = FontWeight.Normal,
    startPadding: Int = 0, textAlign: TextAlign = TextAlign.Start,
    topPadding: Int = 0, endPadding: Int = 0, letterSpacing: Int = 0, lineHeight: Int = 25,
    isVisible: Boolean = true,
    maxLines: Int = 2,
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
fun KabuDivider(
    modifier: Modifier = Modifier,
    height: Dp = 2.dp,
    width: Dp = Dp.Unspecified,
    color: Color = Color.Transparent,
    cornerRadius: Dp = 12.dp
) {
    Box(
        modifier = modifier
            .then(
                if (width != Dp.Unspecified)
                    Modifier.width(width)
                else Modifier.fillMaxWidth()
            )
            .height(height)
            .background(
                color = color,
                shape = RoundedCornerShape(cornerRadius)
            )
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
//    isAccountNumber: Boolean = false,
    focusable: Boolean? = false,
    isValidationError: Boolean = false,
//    isAmount: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    textFieldColors: TextFieldColors = TextFieldDefaults.colors()
) {
    Box(
        modifier = modifier
            .padding(bottom = if (isValidationError) 4.dp else bottomPadding.dp)
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
                        onTextChanged(newValue)
                    },
                    placeholder = {
                        Text(
                            placeholderText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.W500,
                            color = Color(0xFF9A9A9A)
                        )
                    },
                    colors = noBorderColors,
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

            "phone number" -> {
                OutlinedTextField(
                    value = value ?: "",
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() }
                        if (filtered.length <= 11) {
                            onTextChanged(filtered)
                        } else {
                            onTextChanged(filtered.take(11))
                        }
                    },
                    placeholder = {
                        Text(
                            placeholderText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.W500,
                            color = Color(0xFF9A9A9A)
                        )
                    },
                    colors = noBorderColors,
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
                        onTextChanged(newValue)
                    },
                    placeholder = {
                        Text(
                            placeholderText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.W500,
                            color = Color(0xFF9A9A9A)
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
                        onTextChanged(newValue)
                    },
                    placeholder = {
                        Text(
                            placeholderText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.W500,
                            color = Color(0xFF9A9A9A)
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
    placeholderText: String = "",
    iconTint: Color = Color.Gray,
    bottomPadding: Int = 15,
    onClick: () -> Unit = {},
    isError: Boolean = false,
    onTextChanged: (text: String) -> Unit = {},
    textFieldColors: TextFieldColors = TextFieldDefaults.colors(
        disabledTextColor = Color.Black
    )
) {
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
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W500,
                    color = Color(0xFF9A9A9A)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(10),
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.caret_down),
                    tint = iconTint,
                    contentDescription = "Dropdown button",
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

@Composable
fun KabuBottomButton(
    text: String,
    modifier: Modifier = Modifier,
    topPadding: Int = 0,
    isLoading: Boolean = false,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
) {
    Button(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = topPadding.dp)
            .height(53.dp),
        onClick = { onClick() },
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors().copy(
            disabledContainerColor = Color(0xFFB2D5C7),
            disabledContentColor = Color(0xFFE6E6E6),
            containerColor = MaterialTheme.colorScheme.primary
        )

    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            TitleText(
                text = text,
                bottomPadding = 0,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun RowScope.KabuBottomButtonRowScope(
    text: String,
    icon: Int,
    modifier: Modifier = Modifier,
    topPadding: Int = 0,
    isLoading: Boolean = false,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
) {
    Button(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = topPadding.dp)
            .weight(1f)
            .height(53.dp),
        onClick = { onClick() },
        enabled = enabled,
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors().copy(
            disabledContainerColor = Color(0xFFB2D5C7),
            disabledContentColor = Color(0xFFE6E6E6),
            containerColor = MaterialTheme.colorScheme.primary
        )

    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TitleText(
                    text = text,
                    fontSize = 14,
                    fontWeight = FontWeight.W500,
                    bottomPadding = 0,
                    endPadding = 14
                )

                Icon(
                    painter = painterResource(icon),
                    contentDescription = "arrow-right icon"
                )
            }
        }
    }
}

@Composable
fun RowScope.KabuTransparentBottomButtonRowScope(
    text: String,
    icon: Int,
    modifier: Modifier = Modifier,
    topPadding: Int = 0,
    isLoading: Boolean = false,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
) {
    Button(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = topPadding.dp)
            .weight(1f)
            .height(55.dp),
        onClick = { onClick() },
        enabled = enabled,
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors().copy(
            disabledContainerColor = Color(0xFFB2D5C7),
            disabledContentColor = Color(0xFFE6E6E6),
            containerColor = Color(0xFFF1F1F1)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = "arrow-right icon"
                )
                TitleText(
                    text = text,
                    fontSize = 14,
                    fontWeight = FontWeight.W500,
                    bottomPadding = 0,
                    startPadding = 14
                )

            }
        }
    }
}

@Composable
fun getThirtyPercentOfScreenWidth(): Dp {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    return screenWidthDp * 0.3f
}


@Composable
fun CustomLinearProgressIndicator(progress: Float, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .width(getThirtyPercentOfScreenWidth())
            .height(6.dp)
            .clip(RoundedCornerShape(6.dp)), // for rounded edges
        color = MaterialTheme.colorScheme.primary, // progress color
        trackColor = MaterialTheme.colorScheme.surfaceVariant // background track
    )

}


@Composable
internal fun ScreenTitleText(
    title: String,
    subtitle: String,
    titleFontSize: Int = 22,
    subtitleFontSize: Int = 15,
    bottomPadding: Int = 0,
    topPadding: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = topPadding.dp, bottom = bottomPadding.dp)
    ) {
        TitleText(
            text = title,
            fontSize = titleFontSize,
            fontWeight = FontWeight.W600,
            bottomPadding = 5,
        )
        TitleText(
            text = subtitle,
            fontSize = subtitleFontSize,
            bottomPadding = 12,
            lineHeight = 22
        )
    }
}


@Composable
internal fun GrayBackgroundContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = Color(0x5DF1F1F1),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Column {
            content()
        }
    }
}


@Composable
fun FormTextfield(
    title: String,
    value: String,
    hintText: String,
    onTextChanged: (String) -> Unit,
    isCompulsory: Boolean = true,
    keyboardType: String = "",
    imeAction: ImeAction = ImeAction.Next,
    validationError: Boolean = false,
    validationErrorMessage: String = "",
) {
    Box(
        modifier = Modifier
            .background(color = Color(0x4DF1F1F1))
            .padding(vertical = 6.dp)
            .fillMaxWidth()
    ) {
        Column {
            AnnotatedTextfieldTitle(title = title, isCompulsory = isCompulsory)
            KabuOutlinedTextField(
                value = value,
                onTextChanged = onTextChanged,
                placeholderText = hintText,
                imeAction = imeAction,
                isValidationError = validationError,
                keyboardType = keyboardType,
                textFieldColors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF1F1F1),
                    focusedContainerColor = Color(0xFFF1F1F1),
                )
            )
            if (validationError) {
                TitleText(
                    validationErrorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12
                )
            }
        }
    }
}

@Composable
internal fun RowScope.RowScopeFormTextfield(
    title: String,
    value: String,
    hintText: String,
    isDropdown: Boolean,
    imeAction: ImeAction = ImeAction.Next,
    onTextChanged: (String) -> Unit,
    onClick: () -> Unit = {},
    validationError: Boolean = false,
    validationErrorMessage: String = "",
) {
    Box(
        modifier = Modifier
//            .background(color = Color(0x4DF1F1F1))
            .padding(vertical = 12.dp)
            .weight(1f),
    ) {
        Column {
            AnnotatedTextfieldTitle(title = title)
            if (isDropdown) {
                KabuOutlinedTextFieldWithTrailingIconButton(
                    value = value,
                    onTextChanged = onTextChanged,
                    onClick = onClick,
                    placeholderText = hintText,
                    textFieldColors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF1F1F1),
                        focusedContainerColor = Color(0xFFF1F1F1),
                        disabledTextColor = Color.Black
                    )
                )
            } else {
                KabuOutlinedTextField(
                    value = value,
                    onTextChanged = onTextChanged,
                    placeholderText = hintText,
                    imeAction = imeAction,
                    textFieldColors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF1F1F1),
                        focusedContainerColor = Color(0xFFF1F1F1),
                        disabledTextColor = Color.Black
                    )
                )
            }
            if (validationError) {
                TitleText(
                    validationErrorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12
                )
            }

        }
    }
}


@Composable
internal fun AnnotatedTextfieldTitle(title: String, isCompulsory: Boolean = true) {
    Row {
        TitleText(
            title,
            fontWeight = FontWeight.W500,
            endPadding = 3, fontSize = 13
        )
        if (isCompulsory)
            TitleText(
                "*",
                fontSize = 12,
                fontWeight = FontWeight.W500,
                color = Color(0xFFEF2C5B)
            )

    }
}

@Composable
fun FormTextfieldDropdown(
    title: String,
    value: String = "",
    isCompulsory: Boolean = true,
    validationError: Boolean = false,
    validationErrorMessage: String = "",
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        AnnotatedTextfieldTitle(title = title, isCompulsory = isCompulsory)
        KabuOutlinedTextFieldWithTrailingIconButton(
            value = value,
            placeholderText = title,
            onClick = onClick,
            isError = validationError,
        )
        if (validationError) {
            TitleText(
                validationErrorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12
            )
        }
    }
}

fun Context.displayToastMessage(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}


@Composable
fun KabuSearchBar(
    placeholderText: String,
    modifier: Modifier = Modifier,
    filterText: String? = "",
    onTextChanged: (text: String) -> Unit = {},
//    onCancelClicked: () -> Unit = {}
) {
    OutlinedTextField(
        value = filterText ?: "",
        onValueChange = onTextChanged,
        placeholder = {
            Text(
                placeholderText,
                fontSize = 14.sp,
            )
        },
        shape = RoundedCornerShape(12),
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                tint = Color.Gray,
                contentDescription = null,
                modifier = Modifier
                    .size(25.dp)
                    .clickable { },

                )
        },
//        trailingIcon = {
//            Icon(
//                painter = painterResource(id = R.drawable.cancel),
//                contentDescription = "Back button",
//                modifier = Modifier
//                    .size(13.dp)
//                    .clickable { onCancelClicked() }
//            )
//        },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
        ),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    )
}

@Composable
fun GIFImage(
    @DrawableRes gifImage: Int,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()
    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context).data(data = gifImage).apply(block = {
//                size(Size.)
            }).build(), imageLoader = imageLoader
        ),
        contentDescription = "gif image",
        modifier = modifier.fillMaxWidth(),
    )
}