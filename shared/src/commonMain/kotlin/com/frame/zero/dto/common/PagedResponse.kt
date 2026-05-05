package com.frame.zero.dto.common

import kotlinx.serialization.Serializable

@Serializable data class PagedResponse<T>(
  val items: List<T>,
  val nextCursor: String? = null
)
