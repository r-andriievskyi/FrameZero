package com.frame.zero.feature.production.details.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import framezero.composeapp.features.production_details.generated.resources.Res
import framezero.composeapp.features.production_details.generated.resources.month_april_short
import framezero.composeapp.features.production_details.generated.resources.month_august_short
import framezero.composeapp.features.production_details.generated.resources.month_december_short
import framezero.composeapp.features.production_details.generated.resources.month_february_short
import framezero.composeapp.features.production_details.generated.resources.month_january_short
import framezero.composeapp.features.production_details.generated.resources.month_july_short
import framezero.composeapp.features.production_details.generated.resources.month_june_short
import framezero.composeapp.features.production_details.generated.resources.month_march_short
import framezero.composeapp.features.production_details.generated.resources.month_may_short
import framezero.composeapp.features.production_details.generated.resources.month_november_short
import framezero.composeapp.features.production_details.generated.resources.month_october_short
import framezero.composeapp.features.production_details.generated.resources.month_september_short
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun phaseAccentColor(phase: ProductionPhase): Color =
  when (phase) {
    ProductionPhase.IDEA -> AppTheme.colorSystem.textMuted
    ProductionPhase.DEVELOPMENT -> AppTheme.colorSystem.developmentText
    ProductionPhase.FINANCING -> AppTheme.colorSystem.warningText
    ProductionPhase.PRE_PRODUCTION -> AppTheme.colorSystem.preProductionText
    ProductionPhase.PRODUCTION -> AppTheme.colorSystem.productionText
    ProductionPhase.POST_PRODUCTION -> AppTheme.colorSystem.postProductionText
    ProductionPhase.MARKETING -> AppTheme.colorSystem.accentText
    ProductionPhase.DISTRIBUTION -> AppTheme.colorSystem.distributionText
    ProductionPhase.RELEASE -> AppTheme.colorSystem.successText
    ProductionPhase.ARCHIVED -> AppTheme.colorSystem.textMuted
  }

internal fun ProductionPhase.displayLabel(): String =
  name.replace('_', ' ').lowercase()
    .replaceFirstChar { it.uppercase() }

internal fun Genre.displayLabel(): String =
  name.replace('_', ' ').lowercase()
    .replaceFirstChar { it.uppercase() }

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

internal fun formatBudget(cents: Long?): String {
  if (cents == null) return "—"
  val dollars = cents / 100
  return "$${
    dollars.toString().reversed().chunked(3)
      .joinToString(",").reversed()
  }"
}

@Suppress("MagicNumber")
internal fun parseHexColor(hex: String): Color? {
  val cleaned = hex.removePrefix("#")
  return runCatching {
    Color(("FF$cleaned").toLong(16))
  }.getOrNull()
}
