package com.kabukabu.driver.features.home.presentation.views.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun PermissionHandler(
    onLocationPermissionGranted: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(false) }

    // Check if we already have permission
    LaunchedEffect(Unit) {
        hasLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        onLocationPermissionGranted(hasLocationPermission)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                     permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        onLocationPermissionGranted(granted)
    }

    // Request permission if not granted
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            val permissionsToRequest = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

            // Add notification permission for Android 13+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }

            locationPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}

@Composable
fun OverlayPermissionPrompt() {
    val context = LocalContext.current
    var showOverlayPrompt by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // If overlay is not granted, prompt user on homescreen
        if (!android.provider.Settings.canDrawOverlays(context)) {
            showOverlayPrompt = true
        }
    }

    // Launcher to open overlay settings (system has no direct result; onResume will check)
    val openOverlaySettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { /* onResume handles permission detection */ }
    )

    if (showOverlayPrompt) {
        AlertDialog(
            onDismissRequest = { showOverlayPrompt = false },
            title = { Text(text = "Enable Overlay") },
            text = {
                Text(text = "Kabukabu requires overlay permission to display incoming trip overlays. Open settings to allow overlays now?")
            },
            confirmButton = {
                TextButton(onClick = {
                    showOverlayPrompt = false
                    // Open overlay settings
                    val intent = Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        data = android.net.Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    openOverlaySettingsLauncher.launch(intent)
                }) { Text(text = "Open settings") }
            },
            dismissButton = {
                TextButton(onClick = { showOverlayPrompt = false }) { Text(text = "Cancel") }
            }
        )
    }
}

