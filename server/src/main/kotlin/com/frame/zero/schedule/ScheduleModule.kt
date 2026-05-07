package com.frame.zero.schedule

import org.koin.dsl.module

fun scheduleModule() =
  module {
    single<ScheduleEventRepository> { ScheduleEventRepositoryExposed() }
    single { ScheduleService(get(), get(), get()) }
  }
