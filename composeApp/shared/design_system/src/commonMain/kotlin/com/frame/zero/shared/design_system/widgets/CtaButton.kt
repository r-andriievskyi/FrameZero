package com.frame.zero.shared.design_system.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple

private val MinHeight = 48.dp

@Composable
fun CtaButton(
  text: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val colorSystem = AppTheme.colorSystem
  CtaButtonInternal(
    text = text,
    backgroundColor = colorSystem.accent,
    contentColor = colorSystem.textOnAccent,
    rippleColor = colorSystem.accentDim,
    border = null,
    modifier = modifier,
    onClick = onClick
  )
}

@Composable
fun OutlinedCtaButton(
  text: String,
  contentColor: Color,
  rippleColor: Color,
  modifier: Modifier = Modifier,
  borderColor: Color = contentColor,
  onClick: () -> Unit
) {
  CtaButtonInternal(
    text = text,
    backgroundColor = Color.Transparent,
    contentColor = contentColor,
    rippleColor = rippleColor,
    border = BorderStroke(AppTheme.borderSystem.hairline, borderColor),
    modifier = modifier,
    onClick = onClick
  )
}

@Composable
private fun CtaButtonInternal(
  text: String,
  backgroundColor: Color,
  contentColor: Color,
  rippleColor: Color,
  border: BorderStroke?,
  modifier: Modifier,
  onClick: () -> Unit
) {
  val shape = rememberRoundedCornerShape(AppTheme.radiusSystem.radius16)
  val spacingSystem = AppTheme.spacingSystem
  Box(
    modifier = modifier
      .heightIn(min = MinHeight)
      .clip(shape)
      .background(color = backgroundColor, shape = shape)
      .then(if (border != null) Modifier.border(border, shape) else Modifier)
      .clickableWithRipple(
        color = rippleColor,
        onClick = onClick
      ).padding(
        horizontal = spacingSystem.space8,
        vertical = spacingSystem.space8
      ),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      style = AppTheme.typographySystem.labelLarge,
      color = contentColor
    )
  }
}

@LightDarkPreview
@Composable
private fun CtaButtonPreview() {
  AppTheme {
    CtaButton(
      text = "Continue",
      onClick = {}
    )
  }
}

@LightDarkPreview
@Composable
private fun OutlinedCtaButtonPreview() {
  AppTheme {
    OutlinedCtaButton(
      text = "Sign out",
      contentColor = AppTheme.colorSystem.errorText,
      rippleColor = AppTheme.colorSystem.errorSurface,
      onClick = {}
    )
  }
}
