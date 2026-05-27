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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.frame.zero.domain.schedule.Schedule
import com.frame.zero.domain.schedule.ScheduleDay
import com.frame.zero.domain.schedule.ScheduleEvent
import com.frame.zero.domain.schedule.ScheduleEventKind
import com.frame.zero.domain.schedule.ScheduleTask
import com.frame.zero.domain.schedule.ScheduleView
import com.frame.zero.dto.task.TaskPriority
import com.frame.zero.dto.task.TaskStatus
import com.frame.zero.feature.home.tab.schedule.ScheduleTabComponent
import com.frame.zero.feature.home.tab.schedule.ScheduleTabState
import com.frame.zero.feature.home.ui.FloatingBottomNavClearance
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.schedule_screen_title
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

@Composable
fun ScheduleTabContent(component: ScheduleTabComponent) {
  LaunchedEffect(Unit) { component.onAppeared() }
  val state by component.state.collectAsState()
  ScheduleContent(
    state = state,
    onViewChanged = component::onViewChanged,
    onDateSelected = component::onDateSelected
  )
}

@Composable
private fun ScheduleContent(
  state: ScheduleTabState,
  onViewChanged: (ScheduleView) -> Unit = {},
  onDateSelected: (LocalDate) -> Unit = {}
) {
  val selectedDate = state.selectedDate ?: return
  val schedule = state.schedule

  // Local month navigation state for the month view
  var displayMonth by remember(selectedDate) { mutableStateOf(selectedDate.month) }
  var displayYear by remember(selectedDate) { mutableStateOf(selectedDate.year) }

  // Dates that have either events or tasks — drives the dot indicators.
  val daysWithItems by remember(schedule) {
    derivedStateOf {
      schedule?.days
        ?.filter { it.events.isNotEmpty() || it.tasks.isNotEmpty() }
        ?.map { it.date }
        ?.toSet()
        .orEmpty()
    }
  }

  val selectedDay by remember(schedule, selectedDate) {
    derivedStateOf { schedule?.days?.find { it.date == selectedDate } }
  }

  val navigationBarsBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(AppTheme.colorSystem.background)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = AppTheme.spacingSystem.space16)
      .padding(
        top = AppTheme.spacingSystem.space24,
        bottom = navigationBarsBottom + FloatingBottomNavClearance
      )
  ) {
    Text(
      text = stringResource(Res.string.schedule_screen_title),
      style = AppTheme.typographySystem.displayMedium,
      color = AppTheme.colorSystem.textPrimary
    )

    VerticalSpacer(AppTheme.spacingSystem.space16)

    ScheduleViewSelector(
      selected = state.view,
      onViewSelected = onViewChanged
    )

    VerticalSpacer(AppTheme.spacingSystem.space16)

    when (state.view) {
      ScheduleView.DAY -> {
        ScheduleDateHeader(
          date = selectedDate,
          isToday = true
        )
      }

      ScheduleView.WEEK -> {
        WeekDayStrip(
          weekStart = weekStartFor(selectedDate),
          selectedDate = selectedDate,
          daysWithEvents = daysWithItems,
          onDayClick = onDateSelected
        )
        VerticalSpacer(AppTheme.spacingSystem.space16)
        ScheduleDateHeader(
          date = selectedDate,
          isToday = true,
          compact = true
        )
      }

      ScheduleView.MONTH -> {
        MonthCalendar(
          year = displayYear,
          month = displayMonth,
          selectedDate = selectedDate,
          today = selectedDate,
          daysWithEvents = daysWithItems,
          onDayClick = onDateSelected,
          onPreviousMonth = {
            val prev = LocalDate(displayYear, displayMonth, 1)
              .plus(-1, DateTimeUnit.MONTH)
            displayYear = prev.year
            displayMonth = prev.month
          },
          onNextMonth = {
            val next = LocalDate(displayYear, displayMonth, 1)
              .plus(1, DateTimeUnit.MONTH)
            displayYear = next.year
            displayMonth = next.month
          }
        )
        VerticalSpacer(AppTheme.spacingSystem.space16)
        ScheduleDateHeader(
          date = selectedDate,
          isToday = true,
          compact = true
        )
      }
    }

    VerticalSpacer(AppTheme.spacingSystem.space24)

    val day = selectedDay
    if (day != null && (day.events.isNotEmpty() || day.tasks.isNotEmpty())) {
      ScheduleTimeline(
        events = day.events,
        tasks = day.tasks,
        selectedDate = selectedDate
      )
    }
  }
}

// ── Previews ────────────────────────────────────────────────────────────────

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

@LightDarkPreview
@Composable
private fun ScheduleContentDayPreview() {
  AppTheme {
    ScheduleContent(
      state = ScheduleTabState(
        view = ScheduleView.DAY,
        selectedDate = previewDate,
        schedule = previewSchedule
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
