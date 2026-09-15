package com.frame.zero.feature.task.details

import com.frame.zero.feature.task.details.usecase.CompleteTaskUseCase
import com.frame.zero.feature.task.details.usecase.GetAssignableMembersUseCase
import com.frame.zero.feature.task.details.usecase.GetTaskDetailsUseCase
import com.frame.zero.feature.task.details.usecase.UpdateTaskParticipantsUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val featureTaskDetailsModule: Module =
  module {
    factory { GetTaskDetailsUseCase(get()) }
    factory { CompleteTaskUseCase(get()) }
    factory { GetAssignableMembersUseCase(get()) }
    factory { UpdateTaskParticipantsUseCase(get()) }
    factory { (taskId: String) ->
      TaskDetailsViewModel(
        taskId = taskId,
        getTaskDetailsUseCase = get(),
        completeTaskUseCase = get(),
        getAssignableMembersUseCase = get(),
        updateTaskParticipantsUseCase = get(),
        tasksRepository = get(),
        attachmentFileManager = get()
      )
    }
  }
