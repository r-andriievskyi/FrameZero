package com.discovery.playground.shared.design_system.modifier

import androidx.compose.foundation.clickable
import androidx.compose.material3.ripple
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

fun Modifier.clickableWithRipple(
  color: Color,
  bounded: Boolean = false,
  radius: Dp = Dp.Unspecified,
  onClick: () -> Unit
): Modifier =
  clickable(
    interactionSource = null,
    indication = ripple(color = color, bounded = bounded, radius = radius),
    onClick = onClick
  )
