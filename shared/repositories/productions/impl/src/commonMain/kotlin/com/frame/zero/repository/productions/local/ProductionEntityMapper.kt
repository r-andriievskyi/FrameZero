package com.frame.zero.repository.productions.local

import com.frame.zero.database.ProductionEntity
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.Production
import com.frame.zero.domain.production.ProductionPhase
import kotlin.time.Instant

internal fun ProductionEntity.toProduction(): Production =
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
