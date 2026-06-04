package com.frame.zero.shared.design_system.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
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
private val LoadingIndicatorSize = 36.dp

@Composable
fun CtaButton(
  text: String,
  modifier: Modifier = Modifier,
  loading: Boolean = false,
  onClick: () -> Unit
) {
  val colorSystem = AppTheme.colorSystem
  CtaButtonInternal(
    text = text,
    backgroundColor = colorSystem.accent,
    contentColor = colorSystem.textOnAccent,
    rippleColor = colorSystem.accentDim,
    border = null,
    loading = loading,
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
  loading: Boolean = false,
  onClick: () -> Unit
) {
  CtaButtonInternal(
    text = text,
    backgroundColor = Color.Transparent,
    contentColor = contentColor,
    rippleColor = rippleColor,
    border = BorderStroke(AppTheme.borderSystem.hairline, borderColor),
    loading = loading,
    modifier = modifier,
    onClick = onClick
  )
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun CtaButtonInternal(
  text: String,
  backgroundColor: Color,
  contentColor: Color,
  rippleColor: Color,
  border: BorderStroke?,
  loading: Boolean,
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
        enabled = !loading,
        onClick = onClick
      ).padding(
        horizontal = spacingSystem.space8,
        vertical = spacingSystem.space8
      ),
    contentAlignment = Alignment.Center
  ) {
    if (loading) {
      LoadingIndicator(
        modifier = Modifier.size(LoadingIndicatorSize),
        color = contentColor
      )
    } else {
      Text(
        text = text,
        style = AppTheme.typographySystem.labelLarge,
        color = contentColor
      )
    }
  }
}

@LightDarkPreview
@Composable
private fun CtaButtonPreview() {
  AppTheme {
    Column(
      modifier = Modifier.padding(AppTheme.spacingSystem.space16),
      verticalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space12)
    ) {
      CtaButton(
        modifier = Modifier.fillMaxWidth(),
        text = "Continue",
        onClick = {}
      )
      CtaButton(
        modifier = Modifier.fillMaxWidth(),
        text = "Loading…",
        loading = true,
        onClick = {}
      )
      OutlinedCtaButton(
        modifier = Modifier.fillMaxWidth(),
        text = "Sign out",
        contentColor = AppTheme.colorSystem.errorText,
        rippleColor = AppTheme.colorSystem.errorSurface,
        onClick = {}
      )
      OutlinedCtaButton(
        modifier = Modifier.fillMaxWidth(),
        text = "Loading…",
        contentColor = AppTheme.colorSystem.errorText,
        rippleColor = AppTheme.colorSystem.errorSurface,
        loading = true,
        onClick = {}
      )
    }
  }
}
