package com.frame.zero.feature.production.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.shared.design_system.AppTheme
import framezero.composeapp.features.production.generated.resources.Res
import framezero.composeapp.features.production.generated.resources.phase_archived
import framezero.composeapp.features.production.generated.resources.phase_development
import framezero.composeapp.features.production.generated.resources.phase_distribution
import framezero.composeapp.features.production.generated.resources.phase_financing
import framezero.composeapp.features.production.generated.resources.phase_idea
import framezero.composeapp.features.production.generated.resources.phase_marketing
import framezero.composeapp.features.production.generated.resources.phase_post_production
import framezero.composeapp.features.production.generated.resources.phase_pre_production
import framezero.composeapp.features.production.generated.resources.phase_production
import framezero.composeapp.features.production.generated.resources.phase_release
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

internal val SelectedBorderWidth = 2.dp

internal fun Genre.displayLabel(): String = name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

@Composable
internal fun ProductionPhase.label(): String =
  when (this) {
    ProductionPhase.IDEA -> stringResource(Res.string.phase_idea)
    ProductionPhase.DEVELOPMENT -> stringResource(Res.string.phase_development)
    ProductionPhase.FINANCING -> stringResource(Res.string.phase_financing)
    ProductionPhase.PRE_PRODUCTION -> stringResource(Res.string.phase_pre_production)
    ProductionPhase.PRODUCTION -> stringResource(Res.string.phase_production)
    ProductionPhase.POST_PRODUCTION -> stringResource(Res.string.phase_post_production)
    ProductionPhase.MARKETING -> stringResource(Res.string.phase_marketing)
    ProductionPhase.DISTRIBUTION -> stringResource(Res.string.phase_distribution)
    ProductionPhase.RELEASE -> stringResource(Res.string.phase_release)
    ProductionPhase.ARCHIVED -> stringResource(Res.string.phase_archived)
  }

@Composable
internal fun ProductionPhase.dotColor(): Color =
  when (this) {
    ProductionPhase.IDEA -> Color(0xFF9B9EA4)
    ProductionPhase.DEVELOPMENT -> Color(0xFF5BC0EB)
    ProductionPhase.FINANCING -> AppTheme.colorSystem.warningText
    ProductionPhase.PRE_PRODUCTION -> AppTheme.colorSystem.warningText
    ProductionPhase.PRODUCTION -> AppTheme.colorSystem.successText
    ProductionPhase.POST_PRODUCTION -> AppTheme.colorSystem.accent
    ProductionPhase.MARKETING -> Color(0xFF9B59B6)
    ProductionPhase.DISTRIBUTION -> AppTheme.colorSystem.successText
    ProductionPhase.RELEASE -> AppTheme.colorSystem.successText
    ProductionPhase.ARCHIVED -> AppTheme.colorSystem.textMuted
  }

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

internal fun formatBudget(cents: Long): String {
  val dollars = cents / 100
  return "$${dollars.toString().reversed().chunked(3).joinToString(",").reversed()}"
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
