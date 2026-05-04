package com.frame.zero.domain.production

import kotlinx.serialization.Serializable

@Serializable
enum class ProductionSort {
  DUE_DATE,
  RECENT;

  companion object {
    val DEFAULT: ProductionSort = DUE_DATE
  }
}
