package com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel

import android.app.Application
import com.kabukabu.driver.core.data.local.DataPersistenceViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {

    single { DataPersistenceViewModel() }
    viewModel { AuthViewModel(get()) }

}

