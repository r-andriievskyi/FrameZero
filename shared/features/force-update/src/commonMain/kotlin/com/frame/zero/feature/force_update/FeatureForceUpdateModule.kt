package com.frame.zero.feature.force_update

import org.koin.dsl.module

val featureForceUpdateModule = module {
  factory { CheckForceUpdateUseCase(get(), get()) }
  single { ForceUpdateController(get(), get(), get()) }
}
