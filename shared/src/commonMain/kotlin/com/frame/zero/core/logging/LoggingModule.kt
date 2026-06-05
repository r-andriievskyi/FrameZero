package com.frame.zero.core.logging

import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

val loggingModule: Module =
  module {
    single { NoOpLogSink() } bind LogSink::class
    single<Logger> { LoggerImpl(getAll()) }
  }
