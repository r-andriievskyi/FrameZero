package com.discovery.playground.shared.design_system

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp

@Immutable
data class RadiusSystem(
  val radius4: Dp,
  val radius8: Dp,
  val radius16: Dp,
  val radiusMax: Dp,
)
