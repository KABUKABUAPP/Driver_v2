import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose.compiler)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
    // OneSignal Gradle Plugin
    id("com.onesignal.androidsdk.onesignal-gradle-plugin")
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.kabukabu.driver"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kabukabu.driver"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val storeFilePath = keystoreProperties["storeFile"] as? String
                ?: error("Missing 'storeFile' entry in keystore.properties")
            val storePasswordValue = keystoreProperties["storePassword"] as? String
                ?: error("Missing 'storePassword' entry in keystore.properties")
            val keyAliasValue = keystoreProperties["keyAlias"] as? String
                ?: error("Missing 'keyAlias' entry in keystore.properties")
            val keyPasswordValue = keystoreProperties["keyPassword"] as? String
                ?: error("Missing 'keyPassword' entry in keystore.properties")

            storeFile = file(storeFilePath)
            storePassword = storePasswordValue
            keyAlias = keyAliasValue
            keyPassword = keyPasswordValue
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true // Enable resource shrinking

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    
    // Compose
    implementation(libs.compose.activity)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.navigation)
    implementation(libs.androidx.compose.ui.text)
    debugImplementation(libs.compose.ui.tooling)
    
    // Splash Screen
    implementation(libs.androidx.splashscreen)

    // Lifecycle
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp.logging.interceptor)
    
    // DataStore for preferences
    implementation(libs.datastore.preferences)
    
    // Coil for GIF support in Compose
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    
    // Glide for GIF support in traditional Views
    implementation(libs.glide)

    // Mapbox Maps
    implementation(libs.mapbox.maps)
    implementation(libs.play.services.location)
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-services:5.8.0")

    // Socket.IO
    implementation(libs.socket.io.client)

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
//    implementation("com.google.firebase:firebase-storage")
//    implementation("com.google.firebase:firebase-auth")


    //cameraX extension
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)


    //koin for di
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.core)
//    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.compose.viewmodel.v401)

    // OneSignal Push Notifications
    implementation("com.onesignal:OneSignal:[5.0.0, 5.99.99]")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}