package com.kabukabu.driver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.kabukabu.driver.core.theme.KabukabuDriverTheme
import androidx.core.view.WindowCompat
import com.kabukabu.driver.core.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            // Get the current density from the local composition.
            val currentDensity = LocalDensity.current
            // Create a new density with the font scale forced to 1.0f.
            // This prevents fonts from scaling with system settings.
            val newDensity = Density(density = currentDensity.density, fontScale = 1.0f)

            // Provide the new, non-scalable density to the entire composable hierarchy.
            CompositionLocalProvider(LocalDensity provides newDensity) {
                KabukabuDriverTheme {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding(), // Respect bottom safe area only
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}

