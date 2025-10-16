package com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single { AuthViewModel(androidContext() as Application) }
}
