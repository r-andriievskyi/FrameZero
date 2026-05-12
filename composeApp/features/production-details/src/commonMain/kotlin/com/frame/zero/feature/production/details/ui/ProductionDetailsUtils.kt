package com.frame.zero.feature.production.details.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.discovery.playground.shared.design_system.AppTheme
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import kotlinx.datetime.LocalDate

@Composable
internal fun phaseAccentColor(phase: ProductionPhase): Color =
  when (phase) {
    ProductionPhase.IDEA -> AppTheme.colorSystem.textMuted
    ProductionPhase.DEVELOPMENT -> AppTheme.colorSystem.developmentText
    ProductionPhase.FINANCING -> AppTheme.colorSystem.warningText
    ProductionPhase.PRE_PRODUCTION ->
      AppTheme.colorSystem.preProductionText
    ProductionPhase.PRODUCTION -> AppTheme.colorSystem.productionText
    ProductionPhase.POST_PRODUCTION ->
      AppTheme.colorSystem.postProductionText
    ProductionPhase.MARKETING -> AppTheme.colorSystem.accentText
    ProductionPhase.DISTRIBUTION ->
      AppTheme.colorSystem.distributionText
    ProductionPhase.RELEASE -> AppTheme.colorSystem.successText
    ProductionPhase.ARCHIVED -> AppTheme.colorSystem.textMuted
  }

internal fun ProductionPhase.displayLabel(): String =
  name.replace('_', ' ').lowercase()
    .replaceFirstChar { it.uppercase() }

internal fun Genre.displayLabel(): String =
  name.replace('_', ' ').lowercase()
    .replaceFirstChar { it.uppercase() }

internal fun LocalDate.formatDisplay(): String {
  val monthNames = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
  )
  return "${monthNames[month.ordinal]} $day, $year"
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

