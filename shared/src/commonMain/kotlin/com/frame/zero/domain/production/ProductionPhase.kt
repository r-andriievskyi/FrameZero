package com.frame.zero.domain.production

import kotlinx.serialization.Serializable

@Serializable
enum class ProductionPhase {
  IDEA,
  DEVELOPMENT,
  FINANCING,
  PRE_PRODUCTION,
  PRODUCTION,
  POST_PRODUCTION,
  MARKETING,
  DISTRIBUTION,
  RELEASE,
  ARCHIVED;

  fun isForwardFrom(other: ProductionPhase): Boolean = ordinal > other.ordinal
}
