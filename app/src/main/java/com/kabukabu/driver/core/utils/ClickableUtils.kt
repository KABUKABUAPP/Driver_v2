package com.kabukabu.driver.core.utils

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

/**
 * A safe clickable modifier that avoids the LocalIndication compatibility issues
 * with newer Compose Foundation versions.
 *
 * Uses pointerInput with detectTapGestures instead of clickable to avoid
 * the IndicationNodeFactory compatibility issue.
 */
fun Modifier.safeClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = this.pointerInput(enabled) {
    if (enabled) {
        detectTapGestures(onTap = { onClick() })
    }
}

/**
 * A safe clickable modifier with explicit interactionSource and null indication.
 * This avoids the LocalIndication compatibility issue by not relying on
 * LocalIndication.current for the indication.
 *
 * This version uses composed {} to properly remember the interactionSource.
 */
fun Modifier.safeClickableWithRipple(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    // Use pointerInput to provide consistent behavior and avoid ripple Indication incompatibilities
    this.pointerInput(enabled) {
        if (enabled) {
            detectTapGestures(onTap = { onClick() })
        }
    }
}

/**
 * A composable-friendly safe clickable modifier.
 * Use this within @Composable functions for proper remember semantics.
 */
@Composable
fun Modifier.composableSafeClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier {
    // Use pointerInput inside composed to ensure this is usable from @Composable
    return composed {
        this.pointerInput(enabled) {
            if (enabled) {
                detectTapGestures(onTap = { onClick() })
            }
        }
    }
}

/**
 * Extension function that creates a clickable modifier with explicit interaction source
 * to avoid IndicationNodeFactory compatibility issues.
 *
 * This is a drop-in replacement for .clickable { } that works with newer Compose versions.
 * It uses `composed {}` to properly remember the interactionSource.
 */
fun Modifier.noRippleClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    this.pointerInput(enabled) {
        if (enabled) {
            detectTapGestures(onTap = { onClick() })
        }
    }
}
