package com.frame.zero.core.analytics

import org.koin.core.module.Module
import org.koin.dsl.module

val analyticsModule: Module =
  module {
    single<Analytics> { AnalyticsImpl(getAll()) }
  }
