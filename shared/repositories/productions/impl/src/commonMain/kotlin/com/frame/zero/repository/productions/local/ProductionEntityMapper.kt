package com.frame.zero.repository.productions.local

import com.frame.zero.database.ProductionEntity
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.Production
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.dto.production.ProductionSummaryDto
import kotlin.time.Instant

internal fun ProductionSummaryDto.toEntity(pageOrder: Long): ProductionEntity =
  ProductionEntity(
    id = id,
    title = title,
    genre = genre.name,
    phase = phase.name,
    progressPercent = progressPercent,
    daysLeft = daysLeft,
    membersCount = membersCount,
    updatedAtEpochMs = updatedAt.toEpochMilliseconds(),
    pageOrder = pageOrder
  )

internal fun ProductionEntity.toDomain(): Production =
  Production(
    id = id,
    title = title,
    genre = Genre.valueOf(genre),
    phase = ProductionPhase.valueOf(phase),
    progressPercent = progressPercent,
    daysLeft = daysLeft,
    membersCount = membersCount,
    updatedAt = Instant.fromEpochMilliseconds(updatedAtEpochMs)
  )
