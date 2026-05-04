package com.frame.zero.domain.production

import com.frame.zero.dto.production.AccentColorHint
import com.frame.zero.dto.production.ProductionSummaryDto
import kotlin.time.Instant

data class Production(
  val id: String,
  val title: String,
  val phase: ProductionPhase,
  val progressPercent: Int,
  val daysLeft: Int,
  val accentColorHint: AccentColorHint,
  val updatedAt: Instant,
)

fun ProductionSummaryDto.toProduction(): Production =
  Production(
    id = id,
    title = title,
    phase = phase,
    progressPercent = progressPercent,
    daysLeft = daysLeft,
    accentColorHint = accentColorHint,
    updatedAt = updatedAt,
  )
