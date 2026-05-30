package com.frame.zero.feature.home.tab.schedule

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.schedule.ScheduleView
import com.frame.zero.feature.home.usecase.GetScheduleUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ScheduleTabViewModel(
  private val getScheduleUseCase: GetScheduleUseCase,
  dispatcher: CoroutineContext = Dispatchers.Main.immediate
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(ScheduleTabState(selectedDate = today()))
  val state: StateFlow<ScheduleTabState> = _state.asStateFlow()

  private var loadJob: Job? = null

  init {
    load(view = _state.value.view, date = _state.value.selectedDate!!)
  }

  fun onViewChanged(view: ScheduleView) {
    if (view == _state.value.view) return
    _state.update { it.copy(view = view) }
    load(view = view, date = _state.value.selectedDate ?: today())
  }

  fun onDateSelected(date: LocalDate) {
    _state.update { it.copy(selectedDate = date) }
    load(view = _state.value.view, date = date)
  }

  private fun load(
    view: ScheduleView,
    date: LocalDate
  ) {
    loadJob?.cancel()
    loadJob = scope.launch {
      _state.update { it.copy(isLoading = true, error = null) }
      when (val outcome = getScheduleUseCase(GetScheduleUseCase.Params(view, date))) {
        is Outcome.Success -> _state.update { it.copy(isLoading = false, schedule = outcome.data) }

        is Outcome.Failure -> _state.update {
          it.copy(isLoading = false, error = outcome.error.toString())
        }
      }
    }
  }

  override fun onDestroy() {
    scope.cancel()
  }

  @OptIn(ExperimentalTime::class)
  private fun today(): LocalDate = Clock.System
    .now()
    .toLocalDateTime(TimeZone.currentSystemDefault())
    .date
}
