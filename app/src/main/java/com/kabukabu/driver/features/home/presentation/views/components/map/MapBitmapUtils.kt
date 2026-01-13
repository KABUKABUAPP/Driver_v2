package com.kabukabu.driver.features.home.presentation.views.components.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources

/**
 * Utility functions for converting drawables to bitmaps for map annotations
 */
object MapBitmapUtils {
    fun bitmapFromDrawable(context: Context, @DrawableRes resId: Int): Bitmap? {
        return try {
            val drawable = AppCompatResources.getDrawable(context, resId) ?: return null
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: Exception) {
            android.util.Log.e("MapBitmapUtils", "Failed to create bitmap from drawable", e)
            null
        }
    }
}

