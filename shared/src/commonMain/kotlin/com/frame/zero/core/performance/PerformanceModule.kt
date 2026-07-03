package com.frame.zero.core.performance

import org.koin.core.module.Module
import org.koin.dsl.module

val performanceModule: Module =
  module {
    single<Performance> { PerformanceImpl(getAll()) }
  }
