package com.frame.zero.feature.appupdate

import org.koin.dsl.module

val featureAppUpdateModule = module {
  factory { CheckAppUpdateUseCase(get(), get()) }
  single { AppUpdateController(get(), get(), get()) }
}
