package com.frame.zero.domain.production

import kotlinx.serialization.Serializable

@Serializable
enum class ProductionPhase {
  DEVELOPMENT,
  PRE_PRODUCTION,
  PRODUCTION,
  POST_PRODUCTION,
  DISTRIBUTION;

  fun isForwardFrom(other: ProductionPhase): Boolean = ordinal > other.ordinal
}
