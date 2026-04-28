package com.discovery.playground.shared.design_system.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.modifier.clickableWithRipple

@Composable
fun CtaButton(
  text: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  Box(
    modifier =
      modifier
        .clip(CircleShape)
        .background(color = AppTheme.colorSystem.accent, shape = CircleShape)
        .clickableWithRipple(
          color = AppTheme.colorSystem.primary,
          bounded = true,
          onClick = onClick,
        )
        .padding(
          horizontal = AppTheme.spacingSystem.space24,
          vertical = AppTheme.spacingSystem.space16,
        ),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = text,
      style = AppTheme.typographySystem.button,
      color = AppTheme.colorSystem.secondary,
    )
  }
}
