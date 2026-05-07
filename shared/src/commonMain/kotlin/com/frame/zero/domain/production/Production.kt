package com.frame.zero.domain.production

import com.frame.zero.dto.production.AccentColorHint
import com.frame.zero.dto.production.ProductionDetailDto
import com.frame.zero.dto.production.ProductionSummaryDto
import kotlin.time.Instant

data class Production(
  val id: String,
  val title: String,
  val genre: Genre,
  val phase: ProductionPhase,
  val progressPercent: Int,
  val daysLeft: Int,
  val membersCount: Int,
  val accentColorHint: AccentColorHint,
  val updatedAt: Instant
)

fun ProductionPhase.toAccentColorHint(): AccentColorHint =
  when (this) {
    ProductionPhase.DEVELOPMENT -> AccentColorHint.GREEN
    ProductionPhase.PRE_PRODUCTION -> AccentColorHint.ORANGE
    ProductionPhase.PRODUCTION -> AccentColorHint.ORANGE
    ProductionPhase.POST_PRODUCTION -> AccentColorHint.PURPLE
    ProductionPhase.DISTRIBUTION -> AccentColorHint.GREEN
  }

fun ProductionSummaryDto.toProduction(): Production =
  Production(
    id = id,
    title = title,
    genre = genre,
    phase = phase,
    progressPercent = progressPercent,
    daysLeft = daysLeft,
    membersCount = membersCount,
    accentColorHint = phase.toAccentColorHint(),
    updatedAt = updatedAt
  )

fun ProductionDetailDto.toProduction(): Production =
  Production(
    id = id,
    title = title,
    genre = genre,
    phase = phase,
    progressPercent = progressPercent,
    daysLeft = daysLeft,
    membersCount = membersCount,
    accentColorHint = phase.toAccentColorHint(),
    updatedAt = updatedAt
  )
