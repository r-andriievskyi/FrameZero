package com.frame.zero.task

import org.koin.dsl.module

fun taskModule() =
  module {
    single<TaskRepository> { TaskRepositoryImpl() }
    single { TaskService(get(), get(), get()) }
  }
