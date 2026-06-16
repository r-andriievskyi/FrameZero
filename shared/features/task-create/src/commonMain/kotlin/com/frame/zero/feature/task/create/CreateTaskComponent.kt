package com.frame.zero.feature.task.create

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CreateTaskComponent(
  componentContext: ComponentContext,
  val productionId: String,
  val productionTitle: String,
  val onBack: () -> Unit,
  val onCreated: (taskId: String) -> Unit,
  viewModelFactory: (
    productionId: String,
    productionTitle: String
  ) -> CreateTaskViewModel
) : ComponentContext by componentContext {
  private val viewModel: CreateTaskViewModel = instanceKeeper.getOrCreate {
    viewModelFactory(productionId, productionTitle)
  }
  private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

  val state: StateFlow<CreateTaskState>
    get() = viewModel.state

  init {
    lifecycle.doOnDestroy { scope.cancel() }
    viewModel.events
      .onEach { event ->
        when (event) {
          is CreateTaskEvent.Created -> onCreated(event.taskId)
        }
      }.launchIn(scope)
  }

  fun onIntent(intent: CreateTaskIntent) = viewModel.onIntent(intent)
}
