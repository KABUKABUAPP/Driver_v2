package com.kabukabu.driver.core.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.kabukabu.driver.R

object CustomToast {

    /**
     * Show a success toast with custom design
     * @param context The context
     * @param message The success message to display
     */
    @Suppress("DEPRECATION")
    fun showSuccess(context: Context, message: String) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.custom_toast_layout, null)

        val icon = layout.findViewById<ImageView>(R.id.toast_icon)
        val text = layout.findViewById<TextView>(R.id.toast_text)

        icon.setImageResource(R.drawable.success)
        icon.setColorFilter(context.getColor(android.R.color.holo_green_dark))
        text.text = message

        Toast(context).apply {
            duration = Toast.LENGTH_LONG
            setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 100)
            view = layout
        }.show()
    }

    /**
     * Show an error toast with custom design
     * @param context The context
     * @param message The error message to display
     */
    @Suppress("DEPRECATION")
    fun showError(context: Context, message: String) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.custom_toast_layout, null)

        val icon = layout.findViewById<ImageView>(R.id.toast_icon)
        val text = layout.findViewById<TextView>(R.id.toast_text)

        icon.setImageResource(R.drawable.error)
        icon.setColorFilter(context.getColor(android.R.color.holo_red_dark))
        text.text = message

        Toast(context).apply {
            duration = Toast.LENGTH_LONG
            setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 100)
            view = layout
        }.show()
    }
}

