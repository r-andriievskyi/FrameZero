package com.frame.zero.feature.home.tab.dashboard

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.dashboard.DashboardTask
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
  dispatcher: CoroutineContext = Dispatchers.Main.immediate
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(DashboardTabState())
  val state: StateFlow<DashboardTabState> = _state.asStateFlow()

  init {
    load()
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
              is Outcome.Success -> {
                val user = meResult.data
                "${user.firstName} ${user.lastName}".trim()
              }
              is Outcome.Failure -> dashResult.data.displayName
            }
            DashboardTabState(
              isLoading = false,
              dashboard = dashResult.data
                .toUi { task -> resolveUrgency(task, today) }
                .copy(displayName = userName)
            )
          }
          is Outcome.Failure -> DashboardTabState(isLoading = false, isError = true)
        }
      }
    }
  }

  private fun resolveUrgency(task: DashboardTask, today: LocalDate): DueUrgency {
    val dueDate = task.dueDate ?: return DueUrgency.Normal
    return when {
      dueDate < today -> DueUrgency.Overdue
      dueDate == today -> DueUrgency.Today
      dueDate == today.plus(1, DateTimeUnit.DAY) -> DueUrgency.Tomorrow
      else -> DueUrgency.Normal
    }
  }

  override fun onDestroy() {
    scope.cancel()
  }
}
