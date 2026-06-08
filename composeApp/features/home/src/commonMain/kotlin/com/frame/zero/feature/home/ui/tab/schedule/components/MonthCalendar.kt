package com.frame.zero.feature.home.ui.tab.schedule.components

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
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.generated.resources.ic_chevron_left
import com.frame.zero.shared.design_system.generated.resources.ic_chevron_right
import com.frame.zero.core.format.calendarDayLabels
import com.frame.zero.core.format.fullName
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.cd_next_month
import framezero.composeapp.features.home.generated.resources.cd_previous_month
import org.jetbrains.compose.resources.stringResource
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import com.frame.zero.shared.design_system.generated.resources.Res as DesignSystemRes

private val EventDotSize = 6.dp
private val DayCellSize = 44.dp
private val NavButtonSize = 36.dp

@Composable
internal fun MonthCalendar(
  year: Int,
  month: Month,
  selectedDate: LocalDate,
  today: LocalDate,
  daysWithEvents: ImmutableSet<LocalDate>,
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
      MonthNavButton(
        iconRes = DesignSystemRes.drawable.ic_chevron_left,
        contentDescription = stringResource(Res.string.cd_previous_month),
        onClick = onPreviousMonth
      )
      val monthName = month.fullName()
      Text(
        modifier = Modifier.semantics { heading() },
        text = "$monthName $year",
        style = AppTheme.typographySystem.titleMedium,
        color = AppTheme.colorSystem.textPrimary,
        textAlign = TextAlign.Center
      )
      MonthNavButton(
        iconRes = DesignSystemRes.drawable.ic_chevron_right,
        contentDescription = stringResource(Res.string.cd_next_month),
        onClick = onNextMonth
      )
    }

    VerticalSpacer(AppTheme.spacingSystem.space16)

    val dayLabels = calendarDayLabels
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
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
  Box(
    modifier = modifier
      .minimumInteractiveComponentSize()
      .size(NavButtonSize)
      .clip(shape)
      .background(color = AppTheme.colorSystem.inputBackground)
      .border(width = AppTheme.borderSystem.hairline, color = AppTheme.colorSystem.border, shape = shape)
      .clickableWithRipple(
        color = AppTheme.colorSystem.accentDim,
        role = Role.Button,
        onClick = onClick
      )
      .semantics { this.contentDescription = contentDescription },
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
    else -> Color.Transparent
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
      // No ripple: selected state is conveyed by background color change
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
      )
      .semantics(mergeDescendants = true) {
        selected = isSelected
        role = Role.Button
      },
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
        daysWithEvents = persistentSetOf(
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
