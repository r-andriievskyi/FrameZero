package com.frame.zero.feature.home.ui

import com.frame.zero.core.format.formatShort
import kotlinx.datetime.LocalDate

/** Short due-date label, e.g. "Jun 5". */
internal fun LocalDate.toShortDueLabel(): String = formatShort()
