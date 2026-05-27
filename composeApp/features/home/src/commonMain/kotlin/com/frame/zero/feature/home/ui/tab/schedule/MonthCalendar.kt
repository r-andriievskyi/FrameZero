package com.frame.zero.feature.home.ui.tab.schedule

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.generated.resources.ic_chevron_left
import com.frame.zero.shared.design_system.generated.resources.ic_chevron_right
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.cal_day_fri
import framezero.composeapp.features.home.generated.resources.cal_day_mon
import framezero.composeapp.features.home.generated.resources.cal_day_sat
import framezero.composeapp.features.home.generated.resources.cal_day_sun
import framezero.composeapp.features.home.generated.resources.cal_day_thu
import framezero.composeapp.features.home.generated.resources.cal_day_tue
import framezero.composeapp.features.home.generated.resources.cal_day_wed
import framezero.composeapp.features.home.generated.resources.month_april
import framezero.composeapp.features.home.generated.resources.month_august
import framezero.composeapp.features.home.generated.resources.month_december
import framezero.composeapp.features.home.generated.resources.month_february
import framezero.composeapp.features.home.generated.resources.month_january
import framezero.composeapp.features.home.generated.resources.month_july
import framezero.composeapp.features.home.generated.resources.month_june
import framezero.composeapp.features.home.generated.resources.month_march
import framezero.composeapp.features.home.generated.resources.month_may
import framezero.composeapp.features.home.generated.resources.month_november
import framezero.composeapp.features.home.generated.resources.month_october
import framezero.composeapp.features.home.generated.resources.month_september
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.frame.zero.shared.design_system.generated.resources.Res as DesignSystemRes

private val EventDotSize = 6.dp
private val DayCellSize = 44.dp
private val NavButtonSize = 36.dp
private val MonthNavButtonBorderWidth = 1.dp

@Composable
internal fun MonthCalendar(
  year: Int,
  month: Month,
  selectedDate: LocalDate,
  today: LocalDate,
  daysWithEvents: Set<LocalDate>,
  onDayClick: (LocalDate) -> Unit,
  onPreviousMonth: () -> Unit,
  onNextMonth: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      MonthNavButton(iconRes = DesignSystemRes.drawable.ic_chevron_left, onClick = onPreviousMonth)
      val monthName = stringResource(
        when (month) {
          Month.JANUARY -> Res.string.month_january
          Month.FEBRUARY -> Res.string.month_february
          Month.MARCH -> Res.string.month_march
          Month.APRIL -> Res.string.month_april
          Month.MAY -> Res.string.month_may
          Month.JUNE -> Res.string.month_june
          Month.JULY -> Res.string.month_july
          Month.AUGUST -> Res.string.month_august
          Month.SEPTEMBER -> Res.string.month_september
          Month.OCTOBER -> Res.string.month_october
          Month.NOVEMBER -> Res.string.month_november
          Month.DECEMBER -> Res.string.month_december
        }
      )
      Text(
        text = "$monthName $year",
        style = AppTheme.typographySystem.titleMedium,
        color = AppTheme.colorSystem.textPrimary,
        textAlign = TextAlign.Center
      )
      MonthNavButton(iconRes = DesignSystemRes.drawable.ic_chevron_right, onClick = onNextMonth)
    }

    VerticalSpacer(AppTheme.spacingSystem.space16)

    val dayLabels = listOf(
      stringResource(Res.string.cal_day_mon),
      stringResource(Res.string.cal_day_tue),
      stringResource(Res.string.cal_day_wed),
      stringResource(Res.string.cal_day_thu),
      stringResource(Res.string.cal_day_fri),
      stringResource(Res.string.cal_day_sat),
      stringResource(Res.string.cal_day_sun)
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      dayLabels.forEach { label ->
        Box(
          modifier = Modifier.size(DayCellSize),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = label,
            style = AppTheme.typographySystem.labelSmall,
            color = AppTheme.colorSystem.textMuted,
            textAlign = TextAlign.Center
          )
        }
      }
    }

    VerticalSpacer(AppTheme.spacingSystem.space4)

    val firstDay = LocalDate(year, month, 1)
    val startOffset = (firstDay.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal + 7) % 7
    val daysInMonth = daysInMonth(year, month)
    val totalCells = startOffset + daysInMonth
    val rows = (totalCells + 6) / 7

    for (row in 0 until rows) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        for (col in 0 until 7) {
          val cellIndex = row * 7 + col
          val dayNumber = cellIndex - startOffset + 1
          if (dayNumber in 1..daysInMonth) {
            val date = LocalDate(year, month, dayNumber)
            MonthDayCell(
              dayNumber = dayNumber,
              isSelected = date == selectedDate,
              isToday = date == today,
              hasEvents = date in daysWithEvents,
              onClick = { onDayClick(date) }
            )
          } else {
            Box(modifier = Modifier.size(DayCellSize))
          }
        }
      }
    }
  }
}

@Composable
private fun MonthNavButton(
  iconRes: DrawableResource,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
  Box(
    modifier = modifier
      .size(NavButtonSize)
      .clip(shape)
      .background(color = AppTheme.colorSystem.inputBackground, shape = shape)
      .border(width = MonthNavButtonBorderWidth, color = AppTheme.colorSystem.border, shape = shape)
      .clickableWithRipple(color = AppTheme.colorSystem.accentDim, onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Image(
      painter = painterResource(iconRes),
      colorFilter = ColorFilter.tint(AppTheme.colorSystem.textPrimary),
      contentDescription = null
    )
  }
}

@Composable
private fun MonthDayCell(
  dayNumber: Int,
  isSelected: Boolean,
  isToday: Boolean,
  hasEvents: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val background = when {
    isSelected -> AppTheme.colorSystem.accent
    else -> androidx.compose.ui.graphics.Color.Transparent
  }
  val textColor = when {
    isSelected -> AppTheme.colorSystem.textOnAccent
    isToday -> AppTheme.colorSystem.accentText
    hasEvents -> AppTheme.colorSystem.accentText
    else -> AppTheme.colorSystem.textPrimary
  }

  Column(
    modifier = modifier
      .size(DayCellSize)
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
      .background(background)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
      ),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = dayNumber.toString(),
      style = AppTheme.typographySystem.bodyMedium,
      color = textColor,
      textAlign = TextAlign.Center
    )
    if (hasEvents) {
      VerticalSpacer(AppTheme.spacingSystem.space2)
      Box(
        modifier = Modifier
          .size(EventDotSize)
          .clip(CircleShape)
          .background(
            if (isSelected) {
              AppTheme.colorSystem.textOnAccent
            } else {
              AppTheme.colorSystem.accent
            }
          )
      )
    }
  }
}

private fun daysInMonth(
  year: Int,
  month: Month
): Int {
  val first = LocalDate(year, month, 1)
  val nextMonth = first.plus(1, DateTimeUnit.MONTH)
  return (nextMonth.toEpochDays() - first.toEpochDays()).toInt()
}

@LightDarkPreview
@Composable
private fun MonthCalendarPreview() {
  AppTheme {
    val today = LocalDate(2026, 4, 26)
    Box(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      MonthCalendar(
        year = 2026,
        month = Month.APRIL,
        selectedDate = today,
        today = today,
        daysWithEvents = setOf(
          LocalDate(2026, 4, 8),
          LocalDate(2026, 4, 14),
          LocalDate(2026, 4, 15),
          LocalDate(2026, 4, 18),
          LocalDate(2026, 4, 22),
          LocalDate(2026, 4, 24),
          LocalDate(2026, 4, 26),
          LocalDate(2026, 4, 29)
        ),
        onDayClick = {},
        onPreviousMonth = {},
        onNextMonth = {}
      )
    }
  }
}
