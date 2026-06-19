package com.frame.zero.repository.device_token

import com.frame.zero.core.session.SessionCleaner
import com.frame.zero.core.session.SessionManager
import org.koin.dsl.bind
import org.koin.dsl.module

val deviceTokenModule = module {
  single<DeviceTokenRepository> { DeviceTokenRepositoryImpl(get(), get()) }
  // eager so it starts observing the session at app start, not on first injection.
  single(createdAtStart = true) {
    DeviceTokenSynchronizer(get<SessionManager>().state, get(), get(), get())
  }
  single { DeviceTokenSessionCleaner(get(), get()) } bind SessionCleaner::class
}
