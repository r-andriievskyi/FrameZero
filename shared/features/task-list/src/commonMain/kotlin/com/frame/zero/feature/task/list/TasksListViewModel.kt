package com.frame.zero.feature.task.list

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.repository.tasks.TasksRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

class TasksListViewModel(
  private val productionId: String?,
  tasksRepository: TasksRepository,
  dispatcher: CoroutineContext = Dispatchers.Main.immediate
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  @OptIn(ExperimentalCoroutinesApi::class)
  val tasks: Flow<PagingData<TaskListItemUi>> =
    tasksRepository.observeUserTasks().map { pagingData ->
      pagingData.map { it.toUi() }
    }.cachedIn(scope)

  fun onIntent(intent: TasksListIntent) {
    // TODO
  }

  override fun onDestroy() {
    scope.cancel()
  }
}
