package com.frame.zero.domain.production

import kotlinx.serialization.Serializable

@Serializable
enum class Genre {
  DRAMA,
  THRILLER,
  SCI_FI,
  COMEDY,
  HORROR,
  DOCUMENTARY,
  ACTION,
  ANIMATION,
  OTHER
}
