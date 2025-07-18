package com.kabukabu.driver

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import com.bumptech.glide.Glide

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set the content view to our layout
        setContentView(R.layout.activity_splash)
        
        // Find the ImageView for the GIF
        val gifImageView = findViewById<ImageView>(R.id.gifImageView)
        
        // Load the GIF using Glide
        Glide.with(this)
            .asGif()
            .load(R.drawable.splash)  // Use your GIF file name here
            .centerCrop()
            .into(gifImageView)
        
        // Delay for 2.5 seconds to show the GIF animation, then start the main activity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            
            // Apply the fade-in/fade-out animation
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            
            finish()
        }, 2500)
    }
} 