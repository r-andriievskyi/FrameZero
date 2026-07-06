package com.frame.zero.shared.design_system

import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Immutable

@Immutable
data class MotionSystem(
  val durationFast: Int,
  val durationMedium: Int,
  val durationSlow: Int,
  val durationLoop: Int,
  val easingStandard: Easing,
  val easingEmphasized: Easing,
  val easingDecelerate: Easing,
  val easingAccelerate: Easing,
  val easingLinear: Easing
)
