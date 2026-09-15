package com.frame.zero.feature.task.details

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.flow.StateFlow

class TaskDetailsComponent(
  componentContext: ComponentContext,
  val taskId: String,
  val onBack: () -> Unit,
  viewModelFactory: (taskId: String) -> TaskDetailsViewModel
) : ComponentContext by componentContext {
  private val viewModel: TaskDetailsViewModel =
    instanceKeeper.getOrCreate { viewModelFactory(taskId) }

  val state: StateFlow<TaskDetailsState>
    get() = viewModel.state

  fun onIntent(intent: TaskDetailsIntent) = viewModel.onIntent(intent)
}
