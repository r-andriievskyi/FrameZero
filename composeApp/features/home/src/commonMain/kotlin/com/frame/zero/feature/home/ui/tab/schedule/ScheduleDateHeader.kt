package com.frame.zero.feature.home.ui.tab.schedule

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
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.day_friday
import framezero.composeapp.features.home.generated.resources.day_friday_short
import framezero.composeapp.features.home.generated.resources.day_monday
import framezero.composeapp.features.home.generated.resources.day_monday_short
import framezero.composeapp.features.home.generated.resources.day_saturday
import framezero.composeapp.features.home.generated.resources.day_saturday_short
import framezero.composeapp.features.home.generated.resources.day_sunday
import framezero.composeapp.features.home.generated.resources.day_sunday_short
import framezero.composeapp.features.home.generated.resources.day_thursday
import framezero.composeapp.features.home.generated.resources.day_thursday_short
import framezero.composeapp.features.home.generated.resources.day_tuesday
import framezero.composeapp.features.home.generated.resources.day_tuesday_short
import framezero.composeapp.features.home.generated.resources.day_wednesday
import framezero.composeapp.features.home.generated.resources.day_wednesday_short
import framezero.composeapp.features.home.generated.resources.month_april_short
import framezero.composeapp.features.home.generated.resources.month_august_short
import framezero.composeapp.features.home.generated.resources.month_december_short
import framezero.composeapp.features.home.generated.resources.month_february_short
import framezero.composeapp.features.home.generated.resources.month_january_short
import framezero.composeapp.features.home.generated.resources.month_july_short
import framezero.composeapp.features.home.generated.resources.month_june_short
import framezero.composeapp.features.home.generated.resources.month_march_short
import framezero.composeapp.features.home.generated.resources.month_may_short
import framezero.composeapp.features.home.generated.resources.month_november_short
import framezero.composeapp.features.home.generated.resources.month_october_short
import framezero.composeapp.features.home.generated.resources.month_september_short
import framezero.composeapp.features.home.generated.resources.today
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ScheduleDateHeader(
  date: LocalDate,
  isToday: Boolean,
  compact: Boolean = false,
  modifier: Modifier = Modifier
) {
  val fullDayName = stringResource(
    when (date.dayOfWeek) {
      DayOfWeek.MONDAY -> Res.string.day_monday
      DayOfWeek.TUESDAY -> Res.string.day_tuesday
      DayOfWeek.WEDNESDAY -> Res.string.day_wednesday
      DayOfWeek.THURSDAY -> Res.string.day_thursday
      DayOfWeek.FRIDAY -> Res.string.day_friday
      DayOfWeek.SATURDAY -> Res.string.day_saturday
      DayOfWeek.SUNDAY -> Res.string.day_sunday
    }
  )
  val shortDayName = stringResource(
    when (date.dayOfWeek) {
      DayOfWeek.MONDAY -> Res.string.day_monday_short
      DayOfWeek.TUESDAY -> Res.string.day_tuesday_short
      DayOfWeek.WEDNESDAY -> Res.string.day_wednesday_short
      DayOfWeek.THURSDAY -> Res.string.day_thursday_short
      DayOfWeek.FRIDAY -> Res.string.day_friday_short
      DayOfWeek.SATURDAY -> Res.string.day_saturday_short
      DayOfWeek.SUNDAY -> Res.string.day_sunday_short
    }
  )
  val shortMonthName = stringResource(
    when (date.month) {
      Month.JANUARY -> Res.string.month_january_short
      Month.FEBRUARY -> Res.string.month_february_short
      Month.MARCH -> Res.string.month_march_short
      Month.APRIL -> Res.string.month_april_short
      Month.MAY -> Res.string.month_may_short
      Month.JUNE -> Res.string.month_june_short
      Month.JULY -> Res.string.month_july_short
      Month.AUGUST -> Res.string.month_august_short
      Month.SEPTEMBER -> Res.string.month_september_short
      Month.OCTOBER -> Res.string.month_october_short
      Month.NOVEMBER -> Res.string.month_november_short
      Month.DECEMBER -> Res.string.month_december_short
    }
  )

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
