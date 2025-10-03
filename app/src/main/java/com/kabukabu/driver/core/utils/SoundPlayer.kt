package com.kabukabu.driver.core.utils

import android.content.Context
import android.media.MediaPlayer
import com.kabukabu.driver.R

object SoundPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun playTripAlert(context: Context) {
        try {
            if (mediaPlayer?.isPlaying == true) {
                return // Don't start a new one if it's already playing
            }
            // Release any previous instance
            mediaPlayer?.release()

            mediaPlayer = MediaPlayer.create(context, R.raw.find_driver)
            mediaPlayer?.isLooping = true // Enable looping
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopTripAlert() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
} 