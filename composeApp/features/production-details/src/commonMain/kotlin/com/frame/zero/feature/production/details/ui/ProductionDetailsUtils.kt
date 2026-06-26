package com.frame.zero.feature.production.details.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase

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

@Suppress("MagicNumber")
internal fun parseHexColor(hex: String): Color? {
  val cleaned = hex.removePrefix("#")
  return runCatching {
    Color(("FF$cleaned").toLong(16))
  }.getOrNull()
}
