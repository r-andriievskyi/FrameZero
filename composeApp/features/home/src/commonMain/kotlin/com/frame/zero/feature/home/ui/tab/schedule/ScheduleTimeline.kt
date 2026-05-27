package com.frame.zero.feature.home.ui.tab.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.frame.zero.domain.schedule.ScheduleEvent
import com.frame.zero.domain.schedule.ScheduleEventKind
import com.frame.zero.domain.schedule.ScheduleTask
import com.frame.zero.dto.task.TaskPriority
import com.frame.zero.dto.task.TaskStatus
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.ic_calendar_clock
import framezero.composeapp.features.home.generated.resources.ic_task
import framezero.composeapp.features.home.generated.resources.schedule_due_today
import framezero.composeapp.features.home.generated.resources.schedule_events_header
import framezero.composeapp.features.home.generated.resources.schedule_tasks_due_header
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

@Composable
internal fun ScheduleTimeline(
  events: List<ScheduleEvent>,
  tasks: List<ScheduleTask>,
  selectedDate: LocalDate?,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    if (events.isNotEmpty()) {
      ScheduleSectionHeader(
        icon = Res.drawable.ic_calendar_clock,
        title = stringResource(Res.string.schedule_events_header),
        count = events.size
      )
      VerticalSpacer(AppTheme.spacingSystem.space16)
      EventsTimeline(events = events)
    }

    if (tasks.isNotEmpty()) {
      if (events.isNotEmpty()) {
        VerticalSpacer(AppTheme.spacingSystem.space24)
      }
      ScheduleSectionHeader(
        icon = Res.drawable.ic_task,
        title = stringResource(Res.string.schedule_tasks_due_header),
        count = tasks.size
      )
      VerticalSpacer(AppTheme.spacingSystem.space16)
      TasksList(tasks = tasks, selectedDate = selectedDate)
    }
  }
}

@Composable
private fun EventsTimeline(
  events: List<ScheduleEvent>,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    events.forEachIndexed { index, event ->
      TimelineRow(
        timeLabel = event.startsAt.formatTime()
      ) {
        ScheduleEventCard(
          title = event.title,
          location = event.location,
          eventKind = event.kind
        )
      }
      if (index < events.lastIndex) {
        VerticalSpacer(AppTheme.spacingSystem.space16)
      }
    }
  }
}

@Composable
private fun TasksList(
  tasks: List<ScheduleTask>,
  selectedDate: LocalDate?,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    tasks.forEachIndexed { index, task ->
      ScheduleTaskCard(
        title = task.title,
        dueLabel = task.dueDate.toDueLabel(selectedDate),
        productionTitle = task.productionTitle,
        priority = task.priority
      )
      if (index < tasks.lastIndex) {
        VerticalSpacer(AppTheme.spacingSystem.space8)
      }
    }
  }
}

@Composable
private fun TimelineRow(
  timeLabel: String,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = timeLabel,
      style = AppTheme.typographySystem.monoSmall,
      color = AppTheme.colorSystem.textMuted
    )

    HorizontalSpacer(AppTheme.spacingSystem.space16)

    Box(modifier = Modifier.weight(1f)) {
      content()
    }
  }
}

@Composable
private fun LocalDate.toDueLabel(selectedDate: LocalDate?): String? {
  if (selectedDate == null) return null
  return when {
    this == selectedDate -> stringResource(Res.string.schedule_due_today)
    else -> {
      val monthStr = month.name.take(3).lowercase()
        .replaceFirstChar { it.uppercase() }
      "$monthStr $day"
    }
  }
}

private fun Instant.formatTime(): String {
  val local = toLocalDateTime(TimeZone.currentSystemDefault())
  val hour = local.hour.toString().padStart(2, '0')
  val minute = local.minute.toString().padStart(2, '0')
  return "$hour:$minute"
}

// ── Previews ────────────────────────────────────────────────────────────────

@LightDarkPreview
@Composable
private fun ScheduleTimelinePreview() {
  AppTheme {
    Box(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      ScheduleTimeline(
        selectedDate = LocalDate(2026, 4, 26),
        events = listOf(
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
        ),
        tasks = listOf(
          ScheduleTask(
            id = "5",
            title = "Review Scene 12 script revisions",
            productionId = "p2",
            productionTitle = "Echoes of Silence",
            dueDate = LocalDate(2026, 4, 26),
            status = TaskStatus.OPEN,
            priority = TaskPriority.HIGH
          )
        )
      )
    }
  }
}
