package com.frame.zero.core.upload

import org.koin.core.module.Module
import org.koin.dsl.module

actual fun uploadPlatformModule(): Module =
  module {
    factory { UploadTaskUseCase(get(), get(), get(), get()) }
  }
