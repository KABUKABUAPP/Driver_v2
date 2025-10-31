package com.kabukabu.driver

import android.app.Application
import com.kabukabu.driver.core.data.local.UserPreferences
import com.kabukabu.driver.core.data.socket.SocketService
import com.kabukabu.driver.core.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.logger.AndroidLogger
import org.koin.core.logger.Level

class KabukabuDriverApp : Application() {
    // Lazy initialization of UserPreferences

    val userPreferences: UserPreferences by lazy {
        UserPreferences.getInstance(applicationContext)
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

        //initialize dependency injection
        initKoin {
            androidContext(this@KabukabuDriverApp)
            AndroidLogger(Level.INFO)
        }

    }
} 