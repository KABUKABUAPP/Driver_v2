package com.kabukabu.driver.core.di

import com.kabukabu.driver.core.data.local.dataPersistenceModule
import com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel.authModule


val sharedKoinModules = listOf(
    authModule,
    dataPersistenceModule
)