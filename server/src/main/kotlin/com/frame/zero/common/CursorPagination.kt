package com.frame.zero.common

import java.util.Base64
import java.util.UUID

// sortKey is whatever Long the sort column encodes to — epoch millis for
// timestamp sorts, epoch days for due-date sorts.
data class PageCursor(
  val sortKey: Long,
  val id: UUID
)

fun encodeCursor(
  sortKey: Long,
  id: UUID
): String = Base64.getUrlEncoder().encodeToString("$sortKey|$id".toByteArray(Charsets.UTF_8))

fun decodeCursor(cursor: String): PageCursor? =
  try {
    val decoded = String(Base64.getUrlDecoder().decode(cursor), Charsets.UTF_8)
    val idx = decoded.indexOf('|')
    val sortKey = decoded.substring(0, idx).toLong()
    val id = UUID.fromString(decoded.substring(idx + 1))
    PageCursor(sortKey, id)
  } catch (_: Exception) {
    null
  }
