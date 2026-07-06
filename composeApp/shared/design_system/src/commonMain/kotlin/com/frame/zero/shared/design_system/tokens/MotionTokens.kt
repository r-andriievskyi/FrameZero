package com.frame.zero.shared.design_system.tokens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing

internal const val TokenDurationFast = 150
internal const val TokenDurationMedium = 250
internal const val TokenDurationSlow = 400
internal const val TokenDurationLoop = 900

internal val TokenEasingStandard: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
internal val TokenEasingEmphasized: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
internal val TokenEasingDecelerate: Easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
internal val TokenEasingAccelerate: Easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
internal val TokenEasingLinear: Easing = LinearEasing
