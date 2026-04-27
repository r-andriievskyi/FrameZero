package com.frame.zero.core.session

import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module

val sessionModule: Module = module {
  single<Settings> { createTokenSettings() }
  single { TokenStorage(get()) }
  single { LogoutSignal() }
  single { SessionManager(get(), get(), get()) }
}

internal expect fun createTokenSettings(): Settings
