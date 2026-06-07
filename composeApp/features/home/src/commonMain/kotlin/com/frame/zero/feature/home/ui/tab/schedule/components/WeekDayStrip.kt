package com.frame.zero.feature.home.ui.tab.schedule.components

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

private val EventDotSize = 6.dp

@Composable
internal fun WeekDayStrip(
  weekStart: LocalDate,
  selectedDate: LocalDate,
  daysWithEvents: ImmutableSet<LocalDate>,
  onDayClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space4)
  ) {
    for (offset in 0 until 7) {
      val date = weekStart.plus(offset, DateTimeUnit.DAY)
      WeekDayCell(
        date = date,
        isSelected = date == selectedDate,
        hasEvents = date in daysWithEvents,
        onClick = { onDayClick(date) },
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun WeekDayCell(
  date: LocalDate,
  isSelected: Boolean,
  hasEvents: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
  val background = when {
    isSelected -> AppTheme.colorSystem.accent
    else -> AppTheme.colorSystem.cardBackground
  }
  val borderColor = when {
    isSelected -> AppTheme.colorSystem.accent
    else -> AppTheme.colorSystem.cardBorder
  }
  val dayLabelColor = when {
    isSelected -> AppTheme.colorSystem.textOnAccent
    else -> AppTheme.colorSystem.textMuted
  }
  val numberColor = when {
    isSelected -> AppTheme.colorSystem.textOnAccent
    else -> AppTheme.colorSystem.textPrimary
  }

  Column(
    modifier = modifier
      .clip(shape)
      .border(
        width = AppTheme.borderSystem.hairline,
        color = borderColor,
        shape = shape
      )
      .background(background)
      // No ripple: selected state is conveyed by background color change
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
      )
      .padding(vertical = AppTheme.spacingSystem.space8),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = date.shortDayOfWeekLabel(),
      style = AppTheme.typographySystem.labelSmall,
      color = dayLabelColor,
      textAlign = TextAlign.Center
    )
    VerticalSpacer(AppTheme.spacingSystem.space4)
    Text(
      text = date.day.toString(),
      style = AppTheme.typographySystem.titleMedium,
      color = numberColor,
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
    } else {
      VerticalSpacer(AppTheme.spacingSystem.space2)
      Box(modifier = Modifier.size(EventDotSize))
    }
  }
}

private fun LocalDate.shortDayOfWeekLabel(): String =
  when (dayOfWeek) {
    DayOfWeek.MONDAY -> "MON"
    DayOfWeek.TUESDAY -> "TUE"
    DayOfWeek.WEDNESDAY -> "WED"
    DayOfWeek.THURSDAY -> "THU"
    DayOfWeek.FRIDAY -> "FRI"
    DayOfWeek.SATURDAY -> "SAT"
    DayOfWeek.SUNDAY -> "SUN"
  }

internal fun weekStartFor(date: LocalDate): LocalDate {
  val daysFromMonday = (date.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal + 7) % 7
  return date.plus(-daysFromMonday, DateTimeUnit.DAY)
}

@LightDarkPreview
@Composable
private fun WeekDayStripPreview() {
  AppTheme {
    val today = LocalDate(2026, 4, 26)
    Box(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      WeekDayStrip(
        weekStart = weekStartFor(today),
        selectedDate = today,
        daysWithEvents = persistentSetOf(
          LocalDate(2026, 4, 22),
          LocalDate(2026, 4, 24),
          LocalDate(2026, 4, 26)
        ),
        onDayClick = {}
      )
    }
  }
}
