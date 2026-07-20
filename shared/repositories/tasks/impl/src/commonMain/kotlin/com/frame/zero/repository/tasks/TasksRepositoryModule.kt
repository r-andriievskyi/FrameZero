package com.frame.zero.repository.tasks

import com.frame.zero.core.session.SessionCleaner
import com.frame.zero.database.FrameZeroDatabase
import com.frame.zero.repository.tasks.network.TasksApi
import com.frame.zero.repository.tasks.network.TasksApiImpl
import org.koin.dsl.bind
import org.koin.dsl.module

val tasksRepositoryModule = module {
  single<TasksApi> { TasksApiImpl(get(), get()) }
  single<TasksRepository> {
    TasksRepositoryImpl(get(), get(), get(), get(), get(), get())
  }
  single {
    TasksSessionCleaner(get<FrameZeroDatabase>().taskSummariesDao())
  } bind SessionCleaner::class
}
