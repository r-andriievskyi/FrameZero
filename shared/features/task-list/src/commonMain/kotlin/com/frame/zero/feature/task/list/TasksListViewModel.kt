package com.frame.zero.feature.task.list

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.repository.tasks.TasksRepository
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@AssistedInject
class TasksListViewModel(
  @Assisted private val productionId: String?,
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

  /**
   * A null production id means "all tasks assigned to me" rather than one production.
   */
  @AssistedFactory
  fun interface Factory {
    fun create(productionId: String?): TasksListViewModel
  }
}
