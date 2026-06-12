package com.frame.zero.schedule

import org.koin.dsl.module

fun scheduleModule() =
  module {
    single<ScheduleEventRepository> { ScheduleEventRepositoryImpl() }
    single { ScheduleService(get(), get(), get(), get()) }
  }
