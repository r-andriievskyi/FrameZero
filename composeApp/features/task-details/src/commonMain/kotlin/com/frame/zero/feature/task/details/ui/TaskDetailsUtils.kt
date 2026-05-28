package com.frame.zero.feature.task.details.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme

internal val AvatarSize = 40.dp
internal val AttachmentIconSize = 40.dp
internal val ProgressBarHeight = 6.dp
internal val ProductionDotSize = 8.dp
internal val TagPaddingHorizontal = 10.dp
internal val TagPaddingVertical = 4.dp

@Composable
internal fun SectionLabel(
  text: String,
  modifier: Modifier = Modifier
) {
  Text(
    text = text,
    style = AppTheme.typographySystem.caption.copy(fontWeight = FontWeight.Bold),
    color = AppTheme.colorSystem.textMuted,
    modifier = modifier
  )
}

@Suppress("MagicNumber")
internal fun parseHexColor(hex: String): Color? {
  val cleaned = hex.removePrefix("#")
  return runCatching {
    Color(("FF$cleaned").toLong(16))
  }.getOrNull()
}

