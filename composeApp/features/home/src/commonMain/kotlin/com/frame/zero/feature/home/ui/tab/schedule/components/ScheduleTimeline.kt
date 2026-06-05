package com.frame.zero.feature.home.ui.tab.schedule.components

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
import com.frame.zero.domain.schedule.ScheduleEventKind
import com.frame.zero.dto.task.TaskPriority
import com.frame.zero.feature.home.tab.schedule.DueLabel
import com.frame.zero.feature.home.tab.schedule.ScheduleEventUiModel
import com.frame.zero.feature.home.tab.schedule.ScheduleTaskUiModel
import com.frame.zero.feature.home.ui.toShortDueLabel
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
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ScheduleTimeline(
  events: List<ScheduleEventUiModel>,
  tasks: List<ScheduleTaskUiModel>,
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
      TasksList(tasks = tasks)
    }
  }
}

@Composable
private fun EventsTimeline(
  events: List<ScheduleEventUiModel>,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    events.forEachIndexed { index, event ->
      TimelineRow(timeLabel = event.timeRangeLabel) {
        ScheduleEventCard(
          title = event.title,
          location = event.location,
          eventKind = event.eventKind
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
  tasks: List<ScheduleTaskUiModel>,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    tasks.forEachIndexed { index, task ->
      ScheduleTaskCard(
        title = task.title,
        dueLabel = when (val label = task.dueLabel) {
          is DueLabel.Today -> stringResource(Res.string.schedule_due_today)
          is DueLabel.OtherDate -> label.date.toShortDueLabel()
        },
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
        events = listOf(
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
        ),
        tasks = listOf(
          ScheduleTaskUiModel(
            id = "5",
            title = "Review Scene 12 script revisions",
            productionTitle = "Echoes of Silence",
            priority = TaskPriority.HIGH,
            dueLabel = DueLabel.Today
          )
        )
      )
    }
  }
}
