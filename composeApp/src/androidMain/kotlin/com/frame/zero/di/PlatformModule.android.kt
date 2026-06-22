package com.frame.zero.di

import android.content.Context
import com.frame.zero.core.network.connectivity.AndroidConnectivityObserver
import com.frame.zero.core.network.connectivity.ConnectivityObserver
import com.frame.zero.core.security.ActivityHolder
import com.frame.zero.core.security.AndroidBiometricAuthenticator
import com.frame.zero.core.security.AppLifecycleObserver
import com.frame.zero.core.security.BiometricAuthenticator
import com.frame.zero.push.PushNotificationsRouter
import com.frame.zero.repository.productions.local.AndroidDatabaseBuilderFactory
import com.frame.zero.repository.productions.local.DatabaseBuilderFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module =
  module {
    single<DatabaseBuilderFactory> { AndroidDatabaseBuilderFactory(get()) }
    single<ConnectivityObserver> { AndroidConnectivityObserver(get()) }
    single { PushNotificationsRouter(get()) }
    single { ActivityHolder() }
    single { AppLifecycleObserver(get()) }
    single<BiometricAuthenticator> { AndroidBiometricAuthenticator(get()) }
  }

fun androidContextModule(context: Context): Module = module { single<Context> { context } }
