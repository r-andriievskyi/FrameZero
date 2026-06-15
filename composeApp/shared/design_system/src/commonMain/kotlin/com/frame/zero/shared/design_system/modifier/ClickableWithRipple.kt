package com.frame.zero.shared.design_system.modifier

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp

fun Modifier.clickableWithRipple(
  color: Color,
  bounded: Boolean = false,
  radius: Dp = Dp.Unspecified,
  enabled: Boolean = true,
  role: Role? = null,
  onClickLabel: String? = null,
  interactionSource: MutableInteractionSource? = null,
  onClick: () -> Unit
): Modifier =
  clickable(
    enabled = enabled,
    interactionSource = interactionSource,
    indication = ripple(color = color, bounded = bounded, radius = radius),
    role = role,
    onClickLabel = onClickLabel,
    onClick = onClick
  )
