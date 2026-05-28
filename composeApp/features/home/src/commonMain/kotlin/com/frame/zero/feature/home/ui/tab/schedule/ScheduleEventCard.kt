package com.frame.zero.feature.home.ui.tab.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.frame.zero.domain.schedule.ScheduleEventKind
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.event_kind_meeting
import framezero.composeapp.features.home.generated.resources.event_kind_other
import framezero.composeapp.features.home.generated.resources.event_kind_review
import framezero.composeapp.features.home.generated.resources.event_kind_shoot
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ScheduleEventCard(
  title: String,
  location: String?,
  eventKind: ScheduleEventKind?,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius16)
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .border(
        width = AppTheme.borderSystem.hairline,
        color = AppTheme.colorSystem.border,
        shape = shape
      )
      .background(AppTheme.colorSystem.cardBackground)
      .padding(AppTheme.spacingSystem.space16),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = AppTheme.typographySystem.titleSmall,
        color = AppTheme.colorSystem.textPrimary
      )
      if (location != null) {
        VerticalSpacer(AppTheme.spacingSystem.space4)
        Text(
          text = location,
          style = AppTheme.typographySystem.bodySmall,
          color = AppTheme.colorSystem.textMuted
        )
      }
    }
    if (eventKind != null) {
      EventKindBadge(kind = eventKind)
    }
  }
}

@Composable
private fun EventKindBadge(
  kind: ScheduleEventKind,
  modifier: Modifier = Modifier
) {
  val colors = kind.badgeColors()
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius4)
  Box(
    modifier = modifier
      .clip(shape)
      .background(colors.first)
      .padding(
        horizontal = AppTheme.spacingSystem.space8,
        vertical = AppTheme.spacingSystem.space4
      ),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = kind.displayLabel(),
      style = AppTheme.typographySystem.labelSmall,
      color = colors.second
    )
  }
}

@Composable
private fun ScheduleEventKind.badgeColors(): Pair<Color, Color> =
  when (this) {
    ScheduleEventKind.SHOOT -> AppTheme.colorSystem.accentSurface to AppTheme.colorSystem.accentText
    ScheduleEventKind.MEETING -> AppTheme.colorSystem.successSurface to AppTheme.colorSystem.successText
    ScheduleEventKind.REVIEW -> AppTheme.colorSystem.warningSurface to AppTheme.colorSystem.warningText
    ScheduleEventKind.OTHER -> AppTheme.colorSystem.inputBackground to AppTheme.colorSystem.textSecondary
  }

@Composable
private fun ScheduleEventKind.displayLabel(): String =
  when (this) {
    ScheduleEventKind.SHOOT -> stringResource(Res.string.event_kind_shoot)
    ScheduleEventKind.MEETING -> stringResource(Res.string.event_kind_meeting)
    ScheduleEventKind.REVIEW -> stringResource(Res.string.event_kind_review)
    ScheduleEventKind.OTHER -> stringResource(Res.string.event_kind_other)
  }

@LightDarkPreview
@Composable
private fun ScheduleEventCardPreview() {
  AppTheme {
    Column(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      ScheduleEventCard(
        title = "Scene 14 – Interior Office",
        location = "Studio A",
        eventKind = ScheduleEventKind.SHOOT
      )
      VerticalSpacer(AppTheme.spacingSystem.space8)
      ScheduleEventCard(
        title = "Cast lunch & script review",
        location = "Green Room",
        eventKind = ScheduleEventKind.MEETING
      )
      VerticalSpacer(AppTheme.spacingSystem.space8)
      ScheduleEventCard(
        title = "Director dailies review",
        location = "Screening Room",
        eventKind = ScheduleEventKind.REVIEW
      )
    }
  }
}
