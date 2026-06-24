package com.frame.zero.core.upload

import org.koin.core.module.Module
import org.koin.dsl.module

// iOS drives uploads through a background NSURLSession (BackgroundUrlSessionTaskUploadScheduler),
// so there is no UploadTaskUseCase to register here.
actual fun uploadPlatformModule(): Module = module { }
