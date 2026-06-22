package com.frame.zero.core.security

import org.koin.core.module.Module
import org.koin.dsl.module

val securityModule: Module = module {
  single { AppLockController(get(), get()) }
}
