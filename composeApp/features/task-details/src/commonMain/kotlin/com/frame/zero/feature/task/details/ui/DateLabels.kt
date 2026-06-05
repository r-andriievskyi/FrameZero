package com.frame.zero.feature.task.details.ui

import com.frame.zero.core.format.formatMedium
import kotlinx.datetime.LocalDate

/** Renders a due date as a medium label, e.g. "Jun 5, 2026". */
internal fun LocalDate.toMediumDateLabel(): String = formatMedium()
