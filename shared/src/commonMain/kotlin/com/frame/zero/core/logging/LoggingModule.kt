package com.frame.zero.core.logging

import org.koin.core.module.Module
import org.koin.dsl.module

val loggingModule: Module =
  module {
    single<Logger> { LoggerImpl(getAll()) }
  }
