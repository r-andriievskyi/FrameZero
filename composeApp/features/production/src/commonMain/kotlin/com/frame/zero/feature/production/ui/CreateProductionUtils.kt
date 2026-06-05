package com.frame.zero.feature.production.ui

import com.frame.zero.core.format.formatMedium
import com.frame.zero.domain.production.Genre
import kotlinx.datetime.LocalDate

internal fun Genre.displayLabel(): String = name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

internal fun LocalDate.formatDisplay(): String = formatMedium()

internal fun parseDateInput(raw: String): LocalDate? {
  val parts = raw.split(".")
  if (parts.size == 3) {
    val day = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val year = parts[2].toIntOrNull() ?: return null
    return runCatching { LocalDate(year, month, day) }.getOrNull()
  }
  return runCatching { LocalDate.parse(raw) }.getOrNull()
}
