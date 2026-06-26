package com.frame.zero.feature.home.tab.dashboard

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.core.collections.mapImmutable
import com.frame.zero.core.network.connectivity.ConnectivityObserver
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.dashboard.Dashboard
import com.frame.zero.domain.dashboard.DashboardStats
import com.frame.zero.domain.dashboard.DashboardTask
import com.frame.zero.feature.home.LoadErrorKind
import com.frame.zero.feature.home.usecase.GetDashboardUseCase
import com.frame.zero.feature.home.usecase.GetMeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class DashboardTabViewModel(
  private val getMeUseCase: GetMeUseCase,
  private val getDashboardUseCase: GetDashboardUseCase,
  connectivityObserver: ConnectivityObserver,
  dispatcher: CoroutineContext = Dispatchers.Main.immediate
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(DashboardTabState())
  val state: StateFlow<DashboardTabState> = _state.asStateFlow()

  init {
    load()
    // Auto-recover from an offline failure: when connectivity returns and we're
    // still showing the network-error state, reload without user action.
    scope.launch {
      connectivityObserver.isOnline
        .filter { online -> online }
        .collect {
          if (_state.value.error == LoadErrorKind.Network) load()
        }
    }
  }

  fun retry() {
    load()
  }

  @OptIn(ExperimentalTime::class)
  private fun load() {
    scope.launch {
      _state.update { DashboardTabState(isLoading = true) }
      val dashDeferred = async { getDashboardUseCase() }
      val meDeferred = async { getMeUseCase() }

      val dashResult = dashDeferred.await()
      val meResult = meDeferred.await()

      val today = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

      _state.update {
        when (dashResult) {
          is Outcome.Success -> {
            val userName = when (meResult) {
              is Outcome.Success -> meResult.data.firstName
              is Outcome.Failure -> dashResult.data.displayName.substringBefore(" ")
            }
            DashboardTabState(
              isLoading = false,
              dashboard = dashResult.data
                .toUi(today)
                .copy(displayName = userName)
            )
          }
          is Outcome.Failure -> DashboardTabState(
            isLoading = false,
            error = dashResult.error.toLoadErrorKind()
          )
        }
      }
    }
  }

  private fun DomainError.toLoadErrorKind(): LoadErrorKind =
    if (this is DomainError.Offline) LoadErrorKind.Network else LoadErrorKind.Generic

  private fun resolveUrgency(
    task: DashboardTask,
    today: LocalDate
  ): DueUrgency {
    val dueDate = task.dueDate ?: return DueUrgency.Normal
    return when {
      dueDate < today -> DueUrgency.Overdue
      dueDate == today -> DueUrgency.Today
      dueDate == today.plus(1, DateTimeUnit.DAY) -> DueUrgency.Tomorrow
      else -> DueUrgency.Normal
    }
  }

  private fun Dashboard.toUi(today: LocalDate): DashboardUi =
    DashboardUi(
      displayName = displayName,
      stats = stats.toUi(),
      myTasks = myTasks.mapImmutable { it.toUi(resolveUrgency(it, today)) }
    )

  private fun DashboardStats.toUi(): DashboardStatsUi =
    DashboardStatsUi(activeProjects = activeProjects, openTasks = openTasks)

  private fun DashboardTask.toUi(dueUrgency: DueUrgency): DashboardTaskUi =
    DashboardTaskUi(
      id = id,
      title = title,
      productionTitle = productionTitle,
      dueDate = dueDate,
      dueUrgency = dueUrgency
    )

  override fun onDestroy() {
    scope.cancel()
  }
}
