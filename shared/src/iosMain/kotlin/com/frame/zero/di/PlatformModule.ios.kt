package com.frame.zero.di

import com.frame.zero.IOSPlatform
import com.frame.zero.Platform
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
  single<Platform> { IOSPlatform() }
}
