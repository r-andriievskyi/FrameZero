package com.frame.zero.feature.task.list

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.flow.Flow

class TasksListComponent(
  componentContext: ComponentContext,
  val productionId: String?,
  val onTaskClick: (taskId: String) -> Unit,
  viewModelFactory: (productionId: String?) -> TasksListViewModel
) : ComponentContext by componentContext {
  private val viewModel: TasksListViewModel =
    instanceKeeper.getOrCreate { viewModelFactory(productionId) }

  val tasks: Flow<PagingData<TaskListItemUi>>
    get() = viewModel.tasks

  fun onIntent(intent: TasksListIntent) = viewModel.onIntent(intent)
}
