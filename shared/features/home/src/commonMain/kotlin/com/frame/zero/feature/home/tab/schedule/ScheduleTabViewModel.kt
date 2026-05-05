package com.frame.zero.feature.home.tab.schedule

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.schedule.ScheduleView
import com.frame.zero.feature.home.usecase.GetScheduleUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * See [com.frame.zero.feature.home.tab.dashboard.DashboardTabViewModel] for the lifecycle contract.
 */
class ScheduleTabViewModel(
  private val getScheduleUseCase: GetScheduleUseCase,
  dispatcher: CoroutineContext = Dispatchers.Main,
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(ScheduleTabState())
  val state: StateFlow<ScheduleTabState> = _state.asStateFlow()

  private var hasLoaded = false

  fun onAppeared() {
    if (hasLoaded) return
    hasLoaded = true
    load(view = _state.value.view, date = today())
  }

  fun onViewChanged(view: ScheduleView) {
    if (view == _state.value.view) return
    _state.value = _state.value.copy(view = view)
    load(view = view, date = today())
  }

  private fun load(
    view: ScheduleView,
    date: LocalDate
  ) {
    scope.launch {
      _state.value = _state.value.copy(isLoading = true)
      when (val outcome = getScheduleUseCase(GetScheduleUseCase.Params(view, date))) {
        is Outcome.Success ->
          _state.value = _state.value.copy(isLoading = false, schedule = outcome.data)
        is Outcome.Failure -> _state.value = _state.value.copy(isLoading = false)
      }
    }
  }

  override fun onDestroy() {
    scope.cancel()
  }

  @OptIn(ExperimentalTime::class)
  private fun today(): LocalDate =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}
