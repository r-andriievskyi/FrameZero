package com.discovery.playground.shared.design_system

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle

@Immutable
data class TypographySystem(
  val titleSection: TextStyle,
  val bodyStandard: TextStyle,
  val bodySmall: TextStyle,
  val label: TextStyle,
  val button: TextStyle,
)
