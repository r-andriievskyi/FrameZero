package com.frame.zero.di

import com.frame.zero.core.network.connectivity.ConnectivityObserver
import com.frame.zero.core.network.connectivity.IosConnectivityObserver
import com.frame.zero.core.security.BiometricAuthenticator
import com.frame.zero.core.security.IosBiometricAuthenticator
import com.frame.zero.repository.productions.local.DatabaseBuilderFactory
import com.frame.zero.repository.productions.local.IosDatabaseBuilderFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module =
  module {
    single<DatabaseBuilderFactory> { IosDatabaseBuilderFactory() }
    single<ConnectivityObserver> { IosConnectivityObserver() }
    single<BiometricAuthenticator> { IosBiometricAuthenticator() }
  }
