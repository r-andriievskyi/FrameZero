package com.discovery.playground.shared.design_system

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class ColorSystem(
  val background: Color,
  val secondary: Color,
  val primary: Color,
  val accent: Color,
  val error: Color,
)
