package com.frame.zero.repository.productions.local

import com.frame.zero.domain.production.ProductionPhase

internal const val ALL_FILTER_KEY = "ALL"

internal fun filterKeyFor(phase: ProductionPhase?): String = phase?.name ?: ALL_FILTER_KEY
