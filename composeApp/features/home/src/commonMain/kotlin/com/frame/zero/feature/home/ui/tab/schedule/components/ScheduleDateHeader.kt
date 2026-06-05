package com.frame.zero.feature.home.ui.tab.schedule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.frame.zero.core.format.formatMonthShort
import com.frame.zero.core.format.fullName
import com.frame.zero.core.format.shortName
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.today
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ScheduleDateHeader(
  date: LocalDate,
  isToday: Boolean,
  compact: Boolean = false,
  modifier: Modifier = Modifier
) {
  val fullDayName = date.dayOfWeek.fullName()
  val shortDayName = date.dayOfWeek.shortName()
  val shortMonthName = date.formatMonthShort()

  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically
  ) {
    val dateText = if (compact) {
      "$shortDayName, $shortMonthName ${date.day}"
    } else {
      "$fullDayName,  $shortMonthName  ${date.day}, ${date.year}"
    }
    Text(
      text = dateText,
      style = AppTheme.typographySystem.monoMedium,
      color = AppTheme.colorSystem.textSecondary
    )
    if (isToday) {
      HorizontalSpacer(AppTheme.spacingSystem.space8)
      TodayBadge()
    }
  }
}

@Composable
internal fun TodayBadge(modifier: Modifier = Modifier) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radiusMax)
  Box(
    modifier = modifier
      .clip(shape)
      .border(
        width = AppTheme.borderSystem.hairline,
        color = AppTheme.colorSystem.border,
        shape = shape
      )
      .padding(
        horizontal = AppTheme.spacingSystem.space8,
        vertical = AppTheme.spacingSystem.space4
      ),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = stringResource(Res.string.today),
      style = AppTheme.typographySystem.labelSmall,
      color = AppTheme.colorSystem.textPrimary
    )
  }
}

@LightDarkPreview
@Composable
private fun ScheduleDateHeaderPreview() {
  AppTheme {
    Box(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      ScheduleDateHeader(
        date = LocalDate(2026, 4, 26),
        isToday = true
      )
    }
  }
}

@LightDarkPreview
@Composable
private fun ScheduleDateHeaderCompactPreview() {
  AppTheme {
    Box(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      ScheduleDateHeader(
        date = LocalDate(2026, 4, 26),
        isToday = true,
        compact = true
      )
    }
  }
}
