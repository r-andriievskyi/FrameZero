package com.frame.zero.domain.production

import com.frame.zero.dto.production.ProductionDetailDto
import com.frame.zero.dto.production.ProductionSummaryDto

fun ProductionSummaryDto.toProduction(): Production =
  Production(
    id = id,
    title = title,
    genre = genre,
    phase = phase,
    progressPercent = progressPercent,
    daysLeft = daysLeft,
    membersCount = membersCount,
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
    updatedAt = updatedAt
  )
