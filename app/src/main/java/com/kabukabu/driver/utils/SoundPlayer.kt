package com.kabukabu.driver.utils

import android.content.Context
import android.media.MediaPlayer
import com.kabukabu.driver.R

object SoundPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun playTripAlert(context: Context) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, R.raw.find_driver) // Corrected file name
                mediaPlayer?.setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                }
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
} 