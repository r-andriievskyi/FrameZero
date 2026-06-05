package com.frame.zero.feature.home.tab.schedule

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.schedule.Schedule
import com.frame.zero.domain.schedule.ScheduleEvent
import com.frame.zero.domain.schedule.ScheduleTask
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class ScheduleTabViewModel(
  private val getScheduleUseCase: GetScheduleUseCase,
  dispatcher: CoroutineContext = Dispatchers.Main.immediate
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(
    today().let { t ->
      ScheduleTabState(
        selectedDate = t,
        isSelectedDateToday = true,
        displayYear = t.year,
        displayMonth = t.month
      )
    }
  )
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
    val todayDate = today()
    _state.update {
      it.copy(
        selectedDate = date,
        isSelectedDateToday = date == todayDate,
        displayYear = date.year,
        displayMonth = date.month,
        selectedDayEvents = it.schedule.eventsFor(date),
        selectedDayTasks = it.schedule.tasksFor(date, todayDate)
      )
    }
    load(view = _state.value.view, date = date)
  }

  fun onMonthNavigated(offset: Int) {
    _state.update { state ->
      val current = LocalDate(state.displayYear, state.displayMonth, 1)
      val next = current.plus(offset, DateTimeUnit.MONTH)
      state.copy(displayYear = next.year, displayMonth = next.month)
    }
  }

  private fun load(
    view: ScheduleView,
    date: LocalDate
  ) {
    loadJob?.cancel()
    loadJob = scope.launch {
      _state.update { it.copy(isLoading = true, error = null) }
      when (val outcome = getScheduleUseCase(GetScheduleUseCase.Params(view, date))) {
        is Outcome.Success -> {
          val todayDate = today()
          val selectedDate = _state.value.selectedDate ?: date
          _state.update {
            it.copy(
              isLoading = false,
              schedule = outcome.data,
              selectedDayEvents = outcome.data.eventsFor(selectedDate),
              selectedDayTasks = outcome.data.tasksFor(selectedDate, todayDate)
            )
          }
        }

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
  private fun today(): LocalDate =
    Clock.System.now()
      .toLocalDateTime(TimeZone.currentSystemDefault())
      .date

  private fun Schedule?.eventsFor(date: LocalDate): List<ScheduleEventUiModel> =
    this?.days?.find { it.date == date }?.events?.map { it.toUiModel() }.orEmpty()

  private fun Schedule?.tasksFor(
    date: LocalDate,
    today: LocalDate
  ): List<ScheduleTaskUiModel> = this?.days?.find { it.date == date }?.tasks?.map { it.toUiModel(today) }.orEmpty()

  private fun ScheduleEvent.toUiModel() =
    ScheduleEventUiModel(
      id = id,
      title = title,
      productionTitle = productionTitle,
      location = location,
      eventKind = kind,
      timeRangeLabel = "${startsAt.formatTime()} – ${endsAt.formatTime()}"
    )

  private fun ScheduleTask.toUiModel(today: LocalDate) =
    ScheduleTaskUiModel(
      id = id,
      title = title,
      productionTitle = productionTitle,
      priority = priority,
      dueLabel = if (dueDate == today) {
        DueLabel.Today
      } else {
        DueLabel.OtherDate(dueDate)
      }
    )

  private fun Instant.formatTime(): String {
    val local = toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = local.hour.toString().padStart(2, '0')
    val minute = local.minute.toString().padStart(2, '0')
    return "$hour:$minute"
  }
}
