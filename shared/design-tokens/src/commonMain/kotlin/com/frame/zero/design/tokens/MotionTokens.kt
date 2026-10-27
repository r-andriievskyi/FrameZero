package com.frame.zero.design.tokens

/** Cubic Bézier easing control points (x1, y1, x2, y2), as in CSS `cubic-bezier()`. */
data class CubicBezier(
  val x1: Float,
  val y1: Float,
  val x2: Float,
  val y2: Float
) {
  companion object {
    val Linear = CubicBezier(0f, 0f, 1f, 1f)
  }
}

/** Durations in milliseconds. */
data class MotionTokens(
  val durationFast: Int,
  val durationMedium: Int,
  val durationSlow: Int,
  val durationLoop: Int,
  val easingStandard: CubicBezier,
  val easingEmphasized: CubicBezier,
  val easingDecelerate: CubicBezier,
  val easingAccelerate: CubicBezier,
  val easingLinear: CubicBezier
)
