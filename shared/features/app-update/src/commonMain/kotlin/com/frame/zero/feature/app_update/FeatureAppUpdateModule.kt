package com.frame.zero.feature.app_update

import org.koin.dsl.module

val featureAppUpdateModule = module {
  factory { CheckAppUpdateUseCase(get(), get()) }
  single { AppUpdateController(get(), get(), get()) }
}
