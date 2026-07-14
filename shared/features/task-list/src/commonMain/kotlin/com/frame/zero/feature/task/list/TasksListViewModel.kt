package com.frame.zero.feature.task.list

import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.repository.tasks.TasksRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlin.coroutines.CoroutineContext

class TasksListViewModel(
  private val productionId: String,
  private val tasksRepository: TasksRepository,
  dispatcher: CoroutineContext = Dispatchers.Main.immediate
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(TasksListState())
  val state: StateFlow<TasksListState> = _state.asStateFlow()

  private val filter = MutableStateFlow(_state.value.filter)
  private val sort = MutableStateFlow(_state.value.sort)

  @OptIn(ExperimentalCoroutinesApi::class)
  val tasks: Flow<PagingData<TaskListItemUi>> =
    combine(filter, sort) { currentFilter, currentSort -> currentFilter to currentSort }
      .flatMapLatest { (currentFilter, currentSort) ->
        // TODO: replace with an offline-first Room + Paging3 source once TasksRepository exposes
        // a paged stream for a production (mirror shared/repositories/productions). Until then
        // this is a stub so the module compiles.
        val pagingFlow: Flow<PagingData<TaskListItemUi>> = TODO(
          "Wire TasksRepository paging source: productionId=$productionId, " +
            "filter=$currentFilter, sort=$currentSort"
        )
        pagingFlow
      }
      .cachedIn(scope)

  fun onIntent(intent: TasksListIntent) {
    when (intent) {
      is TasksListIntent.FilterChanged -> {
        _state.update { it.copy(filter = intent.filter) }
        filter.value = intent.filter
      }
      is TasksListIntent.SortChanged -> {
        _state.update { it.copy(sort = intent.sort) }
        sort.value = intent.sort
      }
    }
  }

  override fun onDestroy() {
    scope.cancel()
  }
}
