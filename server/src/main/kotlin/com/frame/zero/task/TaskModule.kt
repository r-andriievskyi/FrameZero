package com.frame.zero.task

import org.koin.dsl.module

fun taskModule() =
  module {
    single<TaskRepository> { TaskRepositoryExposed() }
    single { TaskService(get(), get()) }
  }
