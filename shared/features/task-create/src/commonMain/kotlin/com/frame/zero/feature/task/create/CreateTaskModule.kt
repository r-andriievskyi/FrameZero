package com.frame.zero.feature.task.create

import com.frame.zero.feature.task.create.domain.CreateTaskUseCase
import com.frame.zero.feature.task.create.domain.GetAssignableMembersUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val featureTaskCreateModule: Module = module {
  factory { CreateTaskUseCase(get()) }
  factory { GetAssignableMembersUseCase(get()) }
  factory { (productionId: String, productionTitle: String) ->
    CreateTaskViewModel(
      productionId = productionId,
      productionTitle = productionTitle,
      createTaskUseCase = get(),
      getAssignableMembersUseCase = get()
    )
  }
}
