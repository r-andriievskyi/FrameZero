package com.frame.zero.feature.task.details

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

  init {
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    lifecycle.doOnDestroy { scope.cancel() }
    scope.launch {
      viewModel.events.collect { }
    }
  }

  fun onIntent(intent: TaskDetailsIntent) = viewModel.onIntent(intent)
}
