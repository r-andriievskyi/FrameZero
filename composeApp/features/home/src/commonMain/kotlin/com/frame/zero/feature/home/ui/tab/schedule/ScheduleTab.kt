package com.frame.zero.feature.home.ui.tab.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frame.zero.core.collections.mapImmutableSet
import com.frame.zero.core.collections.orEmpty
import com.frame.zero.domain.schedule.Schedule
import com.frame.zero.domain.schedule.ScheduleDay
import com.frame.zero.domain.schedule.ScheduleEvent
import com.frame.zero.domain.schedule.ScheduleEventKind
import com.frame.zero.domain.schedule.ScheduleTask
import com.frame.zero.domain.schedule.ScheduleView
import com.frame.zero.dto.task.TaskPriority
import com.frame.zero.dto.task.TaskStatus
import com.frame.zero.feature.home.tab.schedule.DueLabel
import com.frame.zero.feature.home.tab.schedule.ScheduleEventUiModel
import com.frame.zero.feature.home.tab.schedule.ScheduleTabComponent
import com.frame.zero.feature.home.tab.schedule.ScheduleTabState
import com.frame.zero.feature.home.tab.schedule.ScheduleTaskUiModel
import com.frame.zero.feature.home.ui.FloatingBottomNavClearance
import com.frame.zero.feature.home.ui.tab.schedule.components.MonthCalendar
import com.frame.zero.feature.home.ui.tab.schedule.components.ScheduleDateHeader
import com.frame.zero.feature.home.ui.tab.schedule.components.ScheduleTimeline
import com.frame.zero.feature.home.ui.tab.schedule.components.ScheduleViewSelector
import com.frame.zero.feature.home.ui.tab.schedule.components.WeekDayStrip
import com.frame.zero.feature.home.ui.tab.schedule.components.weekStartFor
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.schedule_screen_title
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

@Composable
fun ScheduleTab(component: ScheduleTabComponent) {
  val state by component.state.collectAsStateWithLifecycle()
  ScheduleContent(
    state = state,
    onViewChanged = component::onViewChanged,
    onDateSelected = component::onDateSelected,
    onMonthNavigated = component::onMonthNavigated
  )
}

@Composable
private fun ScheduleContent(
  state: ScheduleTabState,
  onViewChanged: (ScheduleView) -> Unit = {},
  onDateSelected: (LocalDate) -> Unit = {},
  onMonthNavigated: (offset: Int) -> Unit = {}
) {
  val selectedDate = state.selectedDate ?: return
  val schedule = state.schedule

  // Dates that have either events or tasks — drives the dot indicators.
  val daysWithItems = remember(schedule) {
    schedule?.days
      ?.filter { it.events.isNotEmpty() || it.tasks.isNotEmpty() }
      ?.mapImmutableSet { it.date }
      .orEmpty()
  }

  val navigationBarsBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
  val spacingSystem = AppTheme.spacingSystem
  val colorSystem = AppTheme.colorSystem
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(colorSystem.background)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = spacingSystem.space16)
      .padding(
        top = spacingSystem.space24,
        bottom = navigationBarsBottom + FloatingBottomNavClearance
      )
  ) {
    Text(
      modifier = Modifier.semantics { heading() },
      text = stringResource(Res.string.schedule_screen_title),
      style = AppTheme.typographySystem.displayMedium,
      color = colorSystem.textPrimary
    )

    VerticalSpacer(spacingSystem.space16)

    ScheduleViewSelector(
      selected = state.view,
      onViewSelected = onViewChanged
    )

    VerticalSpacer(spacingSystem.space16)

    when (state.view) {
      ScheduleView.DAY -> {
        ScheduleDateHeader(
          date = selectedDate,
          isToday = state.isSelectedDateToday
        )
      }

      ScheduleView.WEEK -> {
        val weekStart = remember(selectedDate) { weekStartFor(selectedDate) }
        WeekDayStrip(
          weekStart = weekStart,
          selectedDate = selectedDate,
          daysWithEvents = daysWithItems,
          onDayClick = onDateSelected
        )
      }

      ScheduleView.MONTH -> {
        MonthCalendar(
          year = state.displayYear,
          month = state.displayMonth,
          selectedDate = selectedDate,
          today = selectedDate,
          daysWithEvents = daysWithItems,
          onDayClick = onDateSelected,
          onPreviousMonth = { onMonthNavigated(-1) },
          onNextMonth = { onMonthNavigated(1) }
        )
      }
    }

    VerticalSpacer(spacingSystem.space24)

    if (state.selectedDayEvents.isNotEmpty() || state.selectedDayTasks.isNotEmpty()) {
      ScheduleTimeline(
        events = state.selectedDayEvents,
        tasks = state.selectedDayTasks
      )
    }
  }
}

private val previewDate = LocalDate(2026, 4, 26)

private val previewEvents = listOf(
  ScheduleEvent(
    id = "1",
    title = "Scene 14 – Interior Office",
    productionId = "p1",
    productionTitle = "Film",
    startsAt = Instant.fromEpochSeconds(1745647200),
    endsAt = Instant.fromEpochSeconds(1745650800),
    location = "Studio A",
    kind = ScheduleEventKind.SHOOT
  ),
  ScheduleEvent(
    id = "2",
    title = "Cast lunch & script review",
    productionId = "p1",
    productionTitle = "Film",
    startsAt = Instant.fromEpochSeconds(1745663600),
    endsAt = Instant.fromEpochSeconds(1745667200),
    location = "Green Room",
    kind = ScheduleEventKind.MEETING
  ),
  ScheduleEvent(
    id = "3",
    title = "ADR Session – Maya Rivera",
    productionId = "p1",
    productionTitle = "Film",
    startsAt = Instant.fromEpochSeconds(1745672800),
    endsAt = Instant.fromEpochSeconds(1745676400),
    location = "Sound Stage",
    kind = ScheduleEventKind.SHOOT
  ),
  ScheduleEvent(
    id = "4",
    title = "Director dailies review",
    productionId = "p1",
    productionTitle = "Film",
    startsAt = Instant.fromEpochSeconds(1745681800),
    endsAt = Instant.fromEpochSeconds(1745685400),
    location = "Screening Room",
    kind = ScheduleEventKind.REVIEW
  )
)

private val previewTasks = listOf(
  ScheduleTask(
    id = "5",
    title = "Review Scene 12 script revisions",
    productionId = "p2",
    productionTitle = "Echoes of Silence",
    dueDate = previewDate,
    status = TaskStatus.OPEN,
    priority = TaskPriority.HIGH
  )
)

private val previewSchedule = Schedule(
  rangeStart = previewDate,
  rangeEnd = previewDate,
  days = listOf(
    ScheduleDay(date = previewDate, events = previewEvents, tasks = previewTasks)
  )
)

private val previewEventUiModels = persistentListOf(
  ScheduleEventUiModel(
    id = "1",
    title = "Scene 14 – Interior Office",
    productionTitle = "Film",
    location = "Studio A",
    eventKind = ScheduleEventKind.SHOOT,
    timeRangeLabel = "08:00 – 09:00"
  ),
  ScheduleEventUiModel(
    id = "2",
    title = "Cast lunch & script review",
    productionTitle = "Film",
    location = "Green Room",
    eventKind = ScheduleEventKind.MEETING,
    timeRangeLabel = "12:00 – 13:00"
  ),
  ScheduleEventUiModel(
    id = "3",
    title = "ADR Session – Maya Rivera",
    productionTitle = "Film",
    location = "Sound Stage",
    eventKind = ScheduleEventKind.SHOOT,
    timeRangeLabel = "14:00 – 15:00"
  ),
  ScheduleEventUiModel(
    id = "4",
    title = "Director dailies review",
    productionTitle = "Film",
    location = "Screening Room",
    eventKind = ScheduleEventKind.REVIEW,
    timeRangeLabel = "16:30 – 17:30"
  )
)

private val previewTaskUiModels = persistentListOf(
  ScheduleTaskUiModel(
    id = "5",
    title = "Review Scene 12 script revisions",
    productionTitle = "Echoes of Silence",
    priority = TaskPriority.HIGH,
    dueLabel = DueLabel.Today
  )
)

@LightDarkPreview
@Composable
private fun ScheduleContentDayPreview() {
  AppTheme {
    ScheduleContent(
      state = ScheduleTabState(
        view = ScheduleView.DAY,
        selectedDate = previewDate,
        schedule = previewSchedule,
        selectedDayEvents = previewEventUiModels,
        selectedDayTasks = previewTaskUiModels
      )
    )
  }
}

@LightDarkPreview
@Composable
private fun ScheduleContentWeekPreview() {
  AppTheme {
    ScheduleContent(
      state = ScheduleTabState(
        view = ScheduleView.WEEK,
        selectedDate = previewDate,
        schedule = Schedule(
          rangeStart = LocalDate(2026, 4, 20),
          rangeEnd = LocalDate(2026, 4, 27),
          days = listOf(
            ScheduleDay(
              date = LocalDate(2026, 4, 22),
              events = listOf(previewEvents[1]),
              tasks = emptyList()
            ),
            ScheduleDay(
              date = LocalDate(2026, 4, 24),
              events = listOf(previewEvents[0]),
              tasks = emptyList()
            ),
            ScheduleDay(
              date = previewDate,
              events = previewEvents,
              tasks = previewTasks
            )
          )
        )
      )
    )
  }
}

@LightDarkPreview
@Composable
private fun ScheduleContentMonthPreview() {
  AppTheme {
    ScheduleContent(
      state = ScheduleTabState(
        view = ScheduleView.MONTH,
        selectedDate = previewDate,
        schedule = Schedule(
          rangeStart = LocalDate(2026, 4, 1),
          rangeEnd = LocalDate(2026, 4, 30),
          days = listOf(
            ScheduleDay(LocalDate(2026, 4, 8), listOf(previewEvents[0]), emptyList()),
            ScheduleDay(LocalDate(2026, 4, 14), listOf(previewEvents[1]), emptyList()),
            ScheduleDay(LocalDate(2026, 4, 15), listOf(previewEvents[2]), emptyList()),
            ScheduleDay(LocalDate(2026, 4, 18), listOf(previewEvents[0]), emptyList()),
            ScheduleDay(LocalDate(2026, 4, 22), listOf(previewEvents[1]), emptyList()),
            ScheduleDay(LocalDate(2026, 4, 24), listOf(previewEvents[3]), emptyList()),
            ScheduleDay(previewDate, previewEvents, previewTasks),
            ScheduleDay(LocalDate(2026, 4, 29), listOf(previewEvents[2]), emptyList())
          )
        )
      )
    )
  }
}
