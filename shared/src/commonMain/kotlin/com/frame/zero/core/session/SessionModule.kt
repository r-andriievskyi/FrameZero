package com.frame.zero.core.session

import com.frame.zero.core.navigation.NavigationSignal
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module

val sessionModule: Module = module {
  single<Settings> { createTokenSettings() }
  single { TokenStorage(get()) }
  single { UserCache(get()) }
  single { LogoutSignal() }
  single { NavigationSignal() }
  single { SessionManager(get(), get(), get(), get(), cleaners = getAll()) }
}

internal expect fun createTokenSettings(): Settings
