package com.frame.zero.feature.home.ui

import androidx.compose.runtime.Composable
import framezero.composeapp.features.home.generated.resources.Res
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.stringResource

/** Localized abbreviated month name, e.g. "Jun". */
@Composable
internal fun Month.shortLabel(): String =
  stringResource(
    when (this) {
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

/** Short due-date label, e.g. "Jun 5". */
@Composable
internal fun LocalDate.toShortDueLabel(): String = "${month.shortLabel()} $day"
