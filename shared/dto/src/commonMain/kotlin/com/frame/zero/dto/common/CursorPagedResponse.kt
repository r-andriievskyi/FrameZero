package com.frame.zero.dto.common

import kotlinx.serialization.Serializable

@Serializable data class CursorPagedResponse<T>(
  val items: List<T>,
  val nextCursor: String? = null
)
