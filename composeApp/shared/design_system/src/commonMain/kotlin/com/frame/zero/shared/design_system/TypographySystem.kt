package com.frame.zero.shared.design_system

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle

@Immutable
data class TypographySystem(
  val displayLarge: TextStyle,
  val displayMedium: TextStyle,
  val titleLarge: TextStyle,
  val titleExtraLarge: TextStyle,
  val titleMedium: TextStyle,
  val titleSmall: TextStyle,
  val bodyLarge: TextStyle,
  val bodyMedium: TextStyle,
  val bodySmall: TextStyle,
  val labelLarge: TextStyle,
  val labelMedium: TextStyle,
  val labelSmall: TextStyle,
  val caption: TextStyle,
  val monoMedium: TextStyle,
  val monoSmall: TextStyle
)
