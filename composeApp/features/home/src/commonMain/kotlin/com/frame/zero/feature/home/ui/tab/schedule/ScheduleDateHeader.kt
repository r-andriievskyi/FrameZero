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
import androidx.compose.ui.tooling.preview.Preview
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.HorizontalSpacer
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

private fun LocalDate.dayOfWeekName(): String = when (dayOfWeek) {
  DayOfWeek.MONDAY -> "Monday"
  DayOfWeek.TUESDAY -> "Tuesday"
  DayOfWeek.WEDNESDAY -> "Wednesday"
  DayOfWeek.THURSDAY -> "Thursday"
  DayOfWeek.FRIDAY -> "Friday"
  DayOfWeek.SATURDAY -> "Saturday"
  DayOfWeek.SUNDAY -> "Sunday"
}

private fun LocalDate.shortDayOfWeekName(): String = when (dayOfWeek) {
  DayOfWeek.MONDAY -> "Mon"
  DayOfWeek.TUESDAY -> "Tue"
  DayOfWeek.WEDNESDAY -> "Wed"
  DayOfWeek.THURSDAY -> "Thu"
  DayOfWeek.FRIDAY -> "Fri"
  DayOfWeek.SATURDAY -> "Sat"
  DayOfWeek.SUNDAY -> "Sun"
}

internal fun Month.shortName(): String = when (this) {
  Month.JANUARY -> "Jan"
  Month.FEBRUARY -> "Feb"
  Month.MARCH -> "Mar"
  Month.APRIL -> "Apr"
  Month.MAY -> "May"
  Month.JUNE -> "Jun"
  Month.JULY -> "Jul"
  Month.AUGUST -> "Aug"
  Month.SEPTEMBER -> "Sep"
  Month.OCTOBER -> "Oct"
  Month.NOVEMBER -> "Nov"
  Month.DECEMBER -> "Dec"
}

internal fun Month.fullName(): String = when (this) {
  Month.JANUARY -> "January"
  Month.FEBRUARY -> "February"
  Month.MARCH -> "March"
  Month.APRIL -> "April"
  Month.MAY -> "May"
  Month.JUNE -> "June"
  Month.JULY -> "July"
  Month.AUGUST -> "August"
  Month.SEPTEMBER -> "September"
  Month.OCTOBER -> "October"
  Month.NOVEMBER -> "November"
  Month.DECEMBER -> "December"
}

/**
 * Date header row showing "Saturday, Apr 26, 2026 [Today]" for day view
 * or "Sat, Apr 26 [Today]" for week view, or "Apr 26 [Today]" for month view.
 */
@Composable
internal fun ScheduleDateHeader(
  date: LocalDate,
  isToday: Boolean,
  compact: Boolean = false,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically
  ) {
    val dateText = if (compact) {
      "${date.shortDayOfWeekName()}, ${date.month.shortName()} ${date.day}"
    } else {
      "${date.dayOfWeekName()},  ${date.month.shortName()}  ${date.day}, ${date.year}"
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
        width = AppTheme.spacingSystem.space2,
        color = AppTheme.colorSystem.textSecondary,
        shape = shape
      )
      .padding(
        horizontal = AppTheme.spacingSystem.space8,
        vertical = AppTheme.spacingSystem.space2
      ),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "Today",
      style = AppTheme.typographySystem.labelSmall,
      color = AppTheme.colorSystem.textPrimary
    )
  }
}

@Preview
@Composable
private fun ScheduleDateHeaderPreview() {
  AppTheme(darkTheme = true) {
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

@Preview
@Composable
private fun ScheduleDateHeaderCompactPreview() {
  AppTheme(darkTheme = true) {
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



