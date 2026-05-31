package com.frame.zero.feature.production.ui

import androidx.compose.runtime.Composable
import com.frame.zero.domain.production.Genre
import framezero.composeapp.features.production.generated.resources.Res
import framezero.composeapp.features.production.generated.resources.month_april_short
import framezero.composeapp.features.production.generated.resources.month_august_short
import framezero.composeapp.features.production.generated.resources.month_december_short
import framezero.composeapp.features.production.generated.resources.month_february_short
import framezero.composeapp.features.production.generated.resources.month_january_short
import framezero.composeapp.features.production.generated.resources.month_july_short
import framezero.composeapp.features.production.generated.resources.month_june_short
import framezero.composeapp.features.production.generated.resources.month_march_short
import framezero.composeapp.features.production.generated.resources.month_may_short
import framezero.composeapp.features.production.generated.resources.month_november_short
import framezero.composeapp.features.production.generated.resources.month_october_short
import framezero.composeapp.features.production.generated.resources.month_september_short
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.stringResource

internal fun Genre.displayLabel(): String = name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

@Composable
internal fun LocalDate.formatDisplay(): String {
  val monthName = when (month) {
    Month.JANUARY -> stringResource(Res.string.month_january_short)
    Month.FEBRUARY -> stringResource(Res.string.month_february_short)
    Month.MARCH -> stringResource(Res.string.month_march_short)
    Month.APRIL -> stringResource(Res.string.month_april_short)
    Month.MAY -> stringResource(Res.string.month_may_short)
    Month.JUNE -> stringResource(Res.string.month_june_short)
    Month.JULY -> stringResource(Res.string.month_july_short)
    Month.AUGUST -> stringResource(Res.string.month_august_short)
    Month.SEPTEMBER -> stringResource(Res.string.month_september_short)
    Month.OCTOBER -> stringResource(Res.string.month_october_short)
    Month.NOVEMBER -> stringResource(Res.string.month_november_short)
    Month.DECEMBER -> stringResource(Res.string.month_december_short)
  }
  return "$monthName $day, $year"
}


internal fun parseDateInput(raw: String): LocalDate? {
  val parts = raw.split(".")
  if (parts.size == 3) {
    val day = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val year = parts[2].toIntOrNull() ?: return null
    return runCatching { LocalDate(year, month, day) }.getOrNull()
  }
  return runCatching { LocalDate.parse(raw) }.getOrNull()
}
