package com.frame.zero.repository.force_update

import org.koin.dsl.module

val forceUpdateModule = module {
  single<ForceUpdateRepository> { RemoteConfigForceUpdateRepository() }
}
