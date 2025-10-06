package com.kabukabu.driver.components.utils_functions

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

fun priceFilter(text: AnnotatedString, isAmount: Boolean = false): TransformedText {

    val out = if (!isAmount) text.toString() else formatAmount(text.toString())

    val offsetMapping = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            if (!isAmount) return offset
            val rightOffset = text.lastIndex - offset
            val commasToTheRight = rightOffset / 3
            return out.lastIndex - rightOffset - commasToTheRight
        }

        override fun transformedToOriginal(offset: Int): Int {
            if (!isAmount) return offset
            val totalCommas = ((text.length - 1) / 3).coerceAtLeast(0)
            val rightOffset = out.length - offset
            val commasToTheRight = rightOffset / 4
            return offset - (totalCommas - commasToTheRight)
        }

    }
    return TransformedText(AnnotatedString(out), offsetMapping)
}

fun formatAmount(amount: String): String {
    if (amount.isBlank() || amount == "₦")
        return ""
    // remove unwanted characters
    var result = amount
        .replace(",", "") // remove possible commas
        .replace("₦", "") // remove naira symbol

    // separate fractional parts
    val split = result.split(".")

    // convert to currency format
    result = formatCurrency(split[0].toDouble())

    // retrieve decimal part
    val decimalPart = if (split.size == 1) "00" else split[1]

    // concatenate
    result = "$result.$decimalPart"
    // remove unwanted characters
    result = result
        .replace(".00", "") // remove trailing .00
        .substring(1) // remove naira prefix

    return result
}

fun formatCurrency(amount: Double): String {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "NG"))
    currencyFormat.currency = Currency.getInstance("NGN")
    return currencyFormat.format(amount)
}
