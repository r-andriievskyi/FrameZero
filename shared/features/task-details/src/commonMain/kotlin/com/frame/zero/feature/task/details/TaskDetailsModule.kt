package com.frame.zero.feature.task.details

import com.frame.zero.feature.task.details.data.TasksRepositoryImpl
import com.frame.zero.feature.task.details.usecase.CompleteTaskUseCase
import com.frame.zero.feature.task.details.usecase.GetTaskDetailsUseCase
import com.frame.zero.repository.tasks.TasksRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val featureTaskDetailsModule: Module =
  module {
    single<TasksRepository> { TasksRepositoryImpl(get(), get(), get(), get()) }
    factory { GetTaskDetailsUseCase(get()) }
    factory { CompleteTaskUseCase(get()) }
    factory { (taskId: String) ->
      TaskDetailsViewModel(
        taskId = taskId,
        getTaskDetailsUseCase = get(),
        completeTaskUseCase = get(),
        tasksRepository = get(),
        attachmentFileManager = get()
      )
    }
  }
