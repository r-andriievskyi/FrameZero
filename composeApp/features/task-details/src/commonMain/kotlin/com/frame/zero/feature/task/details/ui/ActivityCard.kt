package com.frame.zero.feature.task.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.frame.zero.feature.task.details.ActivityEntry
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.task_details.generated.resources.Res
import framezero.composeapp.features.task_details.generated.resources.task_details_activity
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ActivityCard(
  activity: List<ActivityEntry>,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius16)
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(AppTheme.colorSystem.cardBackground)
      .padding(AppTheme.spacingSystem.space16)
  ) {
    SectionLabel(text = stringResource(Res.string.task_details_activity))
    VerticalSpacer(AppTheme.spacingSystem.space16)
    activity.forEachIndexed { index, entry ->
      ActivityRow(entry = entry)
      if (index < activity.lastIndex) {
        VerticalSpacer(AppTheme.spacingSystem.space16)
      }
    }
  }
}

@Composable
private fun ActivityRow(
  entry: ActivityEntry,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    val avatarColor = entry.avatarColorHex
      ?.let { parseHexColor(it) }
      ?: AppTheme.colorSystem.accentDim
    Box(
      modifier = Modifier
        .size(AvatarSize)
        .clip(CircleShape)
        .background(avatarColor),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = entry.initials,
        style = AppTheme.typographySystem.labelMedium,
        color = AppTheme.colorSystem.textOnAccent
      )
    }
    HorizontalSpacer(AppTheme.spacingSystem.space16)
    Text(
      text = entry.text,
      style = AppTheme.typographySystem.bodyMedium,
      color = AppTheme.colorSystem.textPrimary,
      modifier = Modifier.weight(1f)
    )
    HorizontalSpacer(AppTheme.spacingSystem.space8)
    Text(
      text = entry.timestamp,
      style = AppTheme.typographySystem.bodySmall,
      color = AppTheme.colorSystem.textMuted
    )
  }
}

@LightDarkPreview
@Composable
private fun ActivityCardPreview() {
  AppTheme {
    ActivityCard(
      activity = listOf(
        ActivityEntry(
          id = "act1",
          initials = "TE",
          avatarColorHex = "#7B1FA2",
          text = "TE assigned this to Maya",
          timestamp = "4d ago"
        ),
        ActivityEntry(
          id = "act2",
          initials = "MR",
          avatarColorHex = "#0097A7",
          text = "MR started reviewing pages",
          timestamp = "2h ago"
        )
      )
    )
  }
}

@LightDarkPreview
@Composable
private fun ActivityCardSingleEntryPreview() {
  AppTheme {
    ActivityCard(
      activity = listOf(
        ActivityEntry(
          id = "act1",
          initials = "JM",
          avatarColorHex = "#009688",
          text = "JM created this task",
          timestamp = "1w ago"
        )
      )
    )
  }
}

