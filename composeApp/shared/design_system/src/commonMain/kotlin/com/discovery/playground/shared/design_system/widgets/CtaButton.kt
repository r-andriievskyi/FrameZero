package com.discovery.playground.shared.design_system.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.modifier.clickableWithRipple

private val MinHeight = 48.dp

@Composable
fun CtaButton(
  text: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius16)
  Box(
    modifier =
      modifier
        .heightIn(min = MinHeight)
        .clip(shape)
        .background(color = AppTheme.colorSystem.accent, shape = shape)
        .clickableWithRipple(
          color = AppTheme.colorSystem.accentDim,
          bounded = true,
          onClick = onClick
        ).padding(
          horizontal = AppTheme.spacingSystem.space8,
          vertical = AppTheme.spacingSystem.space8
        ),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      style = AppTheme.typographySystem.labelLarge,
      color = AppTheme.colorSystem.textOnAccent
    )
  }
}
