package com.frame.zero.dashboard

import org.koin.dsl.module

fun dashboardModule() =
  module {
    single { DashboardService(get(), get(), get()) }
  }
