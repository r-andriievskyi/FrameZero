package com.frame.zero.common

import java.util.Base64
import java.util.UUID

data class PageCursor(
  val epochMillis: Long,
  val id: UUID
)

fun encodeCursor(
  epochMillis: Long,
  id: UUID
): String = Base64.getUrlEncoder().encodeToString("$epochMillis|$id".toByteArray(Charsets.UTF_8))

fun decodeCursor(cursor: String): PageCursor? =
  try {
    val decoded = String(Base64.getUrlDecoder().decode(cursor), Charsets.UTF_8)
    val idx = decoded.indexOf('|')
    val epochMillis = decoded.substring(0, idx).toLong()
    val id = UUID.fromString(decoded.substring(idx + 1))
    PageCursor(epochMillis, id)
  } catch (_: Exception) {
    null
  }
