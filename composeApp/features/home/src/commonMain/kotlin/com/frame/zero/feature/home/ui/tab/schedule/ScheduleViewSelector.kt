package com.frame.zero.feature.home.ui.tab.schedule

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import com.frame.zero.domain.schedule.ScheduleView
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.schedule_view_day
import framezero.composeapp.features.home.generated.resources.schedule_view_month
import framezero.composeapp.features.home.generated.resources.schedule_view_week
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ScheduleViewSelector(
  selected: ScheduleView,
  onViewSelected: (ScheduleView) -> Unit,
  modifier: Modifier = Modifier
) {
  val outerShape = RoundedCornerShape(AppTheme.radiusSystem.radiusMax)
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(outerShape)
      .background(AppTheme.colorSystem.inputBackground)
      .padding(AppTheme.spacingSystem.space4)
  ) {
    ScheduleView.entries.forEach { view ->
      SegmentTab(
        label = view.label(),
        isSelected = view == selected,
        onClick = { onViewSelected(view) },
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun SegmentTab(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val backgroundColor by animateColorAsState(
    if (isSelected) AppTheme.colorSystem.accent else AppTheme.colorSystem.inputBackground
  )
  val textColor by animateColorAsState(
    if (isSelected) AppTheme.colorSystem.textOnAccent else AppTheme.colorSystem.textSecondary
  )
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radiusMax))
      .background(backgroundColor)
      .clickable(onClick = onClick)
      .padding(
        horizontal = AppTheme.spacingSystem.space16,
        vertical = AppTheme.spacingSystem.space8
      ),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      style = AppTheme.typographySystem.labelMedium,
      color = textColor,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun ScheduleView.label(): String =
  when (this) {
    ScheduleView.DAY -> stringResource(Res.string.schedule_view_day)
    ScheduleView.WEEK -> stringResource(Res.string.schedule_view_week)
    ScheduleView.MONTH -> stringResource(Res.string.schedule_view_month)
  }

@LightDarkPreview
@Composable
private fun ScheduleViewSelectorPreview() {
  AppTheme {
    Box(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      ScheduleViewSelector(
        selected = ScheduleView.WEEK,
        onViewSelected = {}
      )
    }
  }
}
