package com.frame.zero.core.upload

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Platform-specific upload wiring. The Android actual registers [UploadTaskUseCase] (driven by the
 * WorkManager worker); iOS uploads through a background `NSURLSession` and contributes nothing.
 */
expect fun uploadPlatformModule(): Module

val uploadModule: Module =
  module {
    includes(uploadPlatformModule())
    single { PendingUploadStore(get()) }
  }
