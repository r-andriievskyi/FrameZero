package com.frame.zero.feature.home.ui.tab.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.HorizontalSpacer
import com.discovery.playground.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.domain.schedule.ScheduleEventKind
import com.frame.zero.domain.schedule.ScheduleItem
import com.frame.zero.dto.schedule.ScheduleItemSource
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

private val TimeColumnWidth = 52.dp
private val TimelineDotSize = 8.dp

/**
 * Timeline list of schedule items for a single day — each item has a time
 * label on the left, a dot + connecting line, and the event card on the right.
 */
@Composable
internal fun ScheduleTimeline(
  items: List<ScheduleItem>,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    items.forEachIndexed { index, item ->
      TimelineRow(
        timeLabel = item.startsAt.formatTime(),
        isLast = index == items.lastIndex
      ) {
        ScheduleEventCard(
          title = item.title,
          location = item.location,
          eventKind = item.eventKind
        )
      }
      if (index < items.lastIndex) {
        VerticalSpacer(AppTheme.spacingSystem.space16)
      }
    }
  }
}

@Composable
private fun TimelineRow(
  timeLabel: String,
  isLast: Boolean,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  val lineColor = AppTheme.colorSystem.cardBorder
  val dotColor = AppTheme.colorSystem.accent

  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top
  ) {
    // Time label
    Text(
      text = timeLabel,
      style = AppTheme.typographySystem.monoSmall,
      color = AppTheme.colorSystem.textMuted,
      modifier = Modifier.width(TimeColumnWidth)
    )

    // Dot + vertical line
    Box(
      modifier = Modifier
        .width(AppTheme.spacingSystem.space16)
        .drawBehind {
          if (!isLast) {
            val centerX = size.width / 2
            drawLine(
              color = lineColor,
              start = Offset(centerX, TimelineDotSize.toPx()),
              end = Offset(centerX, size.height + 64.dp.toPx()),
              strokeWidth = 1.dp.toPx()
            )
          }
        },
      contentAlignment = Alignment.TopCenter
    ) {
      Box(
        modifier = Modifier
          .size(TimelineDotSize)
          .clip(CircleShape)
          .background(dotColor)
      )
    }

    HorizontalSpacer(AppTheme.spacingSystem.space8)

    // Card
    Box(modifier = Modifier.weight(1f)) {
      content()
    }
  }
}

private fun Instant?.formatTime(): String {
  if (this == null) return ""
  val local = this.toLocalDateTime(TimeZone.currentSystemDefault())
  val hour = local.hour.toString().padStart(2, '0')
  val minute = local.minute.toString().padStart(2, '0')
  return "$hour:$minute"
}

@Preview
@Composable
private fun ScheduleTimelinePreview() {
  AppTheme(darkTheme = true) {
    Box(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      ScheduleTimeline(
        items = listOf(
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
      )
    }
  }
}


