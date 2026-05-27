package com.frame.zero.feature.home.ui.tab.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.frame.zero.domain.schedule.ScheduleEventKind
import com.frame.zero.domain.schedule.ScheduleItem
import com.frame.zero.domain.schedule.ScheduleView
import com.frame.zero.dto.schedule.ScheduleItemSource
import com.frame.zero.feature.home.tab.schedule.ScheduleTabComponent
import com.frame.zero.feature.home.tab.schedule.ScheduleTabState
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

  // Collect dates that have events for dot indicators
  val daysWithEvents by remember(schedule) {
    derivedStateOf {
      schedule?.days
        ?.filter { it.items.isNotEmpty() }
        ?.map { it.date }
        ?.toSet()
        .orEmpty()
    }
  }

  // Items for the currently selected date
  val selectedDayItems by remember(schedule, selectedDate) {
    derivedStateOf {
      schedule?.days?.find { it.date == selectedDate }?.items.orEmpty()
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(AppTheme.colorSystem.background)
      .verticalScroll(rememberScrollState())
      .padding(
        horizontal = AppTheme.spacingSystem.space16,
        vertical = AppTheme.spacingSystem.space24
      )
  ) {
    // Title
    Text(
      text = stringResource(Res.string.schedule_screen_title),
      style = AppTheme.typographySystem.displayMedium,
      color = AppTheme.colorSystem.textPrimary
    )

    VerticalSpacer(AppTheme.spacingSystem.space16)

    // View selector (Day / Week / Month)
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
          today = selectedDate,
          daysWithEvents = daysWithEvents,
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
          daysWithEvents = daysWithEvents,
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

    // Timeline
    if (selectedDayItems.isNotEmpty()) {
      ScheduleTimeline(items = selectedDayItems)
    }
  }
}

// ── Previews ────────────────────────────────────────────────────────────────

private val previewDate = LocalDate(2026, 4, 26)

private val previewItems = listOf(
  ScheduleItem(
    id = "1",
    source = ScheduleItemSource.EVENT,
    title = "Scene 14 – Interior Office",
    productionId = "p1",
    productionTitle = "Film",
    startsAt = Instant.fromEpochSeconds(1745647200),
    endsAt = null,
    dueDate = null,
    location = "Studio A",
    eventKind = ScheduleEventKind.SHOOT,
    taskStatus = null
  ),
  ScheduleItem(
    id = "2",
    source = ScheduleItemSource.EVENT,
    title = "Cast lunch & script review",
    productionId = "p1",
    productionTitle = "Film",
    startsAt = Instant.fromEpochSeconds(1745663600),
    endsAt = null,
    dueDate = null,
    location = "Green Room",
    eventKind = ScheduleEventKind.MEETING,
    taskStatus = null
  ),
  ScheduleItem(
    id = "3",
    source = ScheduleItemSource.EVENT,
    title = "ADR Session – Maya Rivera",
    productionId = "p1",
    productionTitle = "Film",
    startsAt = Instant.fromEpochSeconds(1745672800),
    endsAt = null,
    dueDate = null,
    location = "Sound Stage",
    eventKind = ScheduleEventKind.SHOOT,
    taskStatus = null
  ),
  ScheduleItem(
    id = "4",
    source = ScheduleItemSource.EVENT,
    title = "Director dailies review",
    productionId = "p1",
    productionTitle = "Film",
    startsAt = Instant.fromEpochSeconds(1745681800),
    endsAt = null,
    dueDate = null,
    location = "Screening Room",
    eventKind = ScheduleEventKind.REVIEW,
    taskStatus = null
  )
)

private val previewSchedule = Schedule(
  rangeStart = previewDate,
  rangeEnd = previewDate,
  days = listOf(ScheduleDay(date = previewDate, items = previewItems))
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
              items = listOf(previewItems[1])
            ),
            ScheduleDay(
              date = LocalDate(2026, 4, 24),
              items = listOf(previewItems[0])
            ),
            ScheduleDay(
              date = previewDate,
              items = previewItems
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
            ScheduleDay(LocalDate(2026, 4, 8), listOf(previewItems[0])),
            ScheduleDay(LocalDate(2026, 4, 14), listOf(previewItems[1])),
            ScheduleDay(LocalDate(2026, 4, 15), listOf(previewItems[2])),
            ScheduleDay(LocalDate(2026, 4, 18), listOf(previewItems[0])),
            ScheduleDay(LocalDate(2026, 4, 22), listOf(previewItems[1])),
            ScheduleDay(LocalDate(2026, 4, 24), listOf(previewItems[3])),
            ScheduleDay(previewDate, previewItems),
            ScheduleDay(LocalDate(2026, 4, 29), listOf(previewItems[2]))
          )
        )
      )
    )
  }
}
