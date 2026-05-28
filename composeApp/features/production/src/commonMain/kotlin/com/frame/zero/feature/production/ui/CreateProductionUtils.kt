package com.frame.zero.feature.production.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
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
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

// ── Shared dimension constants ────────────────────────────────────────
internal val SelectedBorderWidth = 2.dp

// ── Extension utilities ───────────────────────────────────────────────

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

internal fun LocalDate.formatDisplay(): String {
  val monthNames = listOf(
    "Jan",
    "Feb",
    "Mar",
    "Apr",
    "May",
    "Jun",
    "Jul",
    "Aug",
    "Sep",
    "Oct",
    "Nov",
    "Dec"
  )
  return "${monthNames[month.ordinal]} $day, $year"
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
