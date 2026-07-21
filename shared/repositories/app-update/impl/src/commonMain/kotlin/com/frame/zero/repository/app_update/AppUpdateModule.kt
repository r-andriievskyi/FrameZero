package com.frame.zero.repository.app_update

import org.koin.dsl.module

val appUpdateModule = module {
  single<AppUpdateRepository> { RemoteConfigAppUpdateRepository() }
}
