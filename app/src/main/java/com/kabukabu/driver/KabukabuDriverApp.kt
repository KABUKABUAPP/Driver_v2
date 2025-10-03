package com.kabukabu.driver

import android.app.Application
import com.kabukabu.driver.core.data.local.UserPreferences
import com.kabukabu.driver.core.data.socket.SocketService

class KabukabuDriverApp : Application() {
    // Lazy initialization of UserPreferences
    val userPreferences: UserPreferences by lazy {
        UserPreferences(applicationContext)
    }
    
    companion object {
        private lateinit var instance: KabukabuDriverApp
        
        fun getInstance(): KabukabuDriverApp {
            return instance
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize and connect the socket when app start
        SocketService.connect()
    }
} 