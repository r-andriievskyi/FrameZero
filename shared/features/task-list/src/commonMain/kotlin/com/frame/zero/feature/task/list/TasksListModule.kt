package com.frame.zero.feature.task.list

import org.koin.core.module.Module
import org.koin.dsl.module

val featureTaskListModule: Module =
  module {
    factory { (productionId: String?) ->
      TasksListViewModel(
        productionId = productionId,
        tasksRepository = get()
      )
    }
  }
