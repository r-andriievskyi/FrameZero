package com.frame.zero.feature.task.details

import org.koin.core.module.Module
import org.koin.dsl.module

val featureTaskDetailsModule: Module =
  module {
    factory { (taskId: String) ->
      TaskDetailsViewModel(taskId = taskId)
    }
  }
