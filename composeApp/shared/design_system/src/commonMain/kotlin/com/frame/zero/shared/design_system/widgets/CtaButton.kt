package com.frame.zero.shared.design_system.widgets

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.disabled
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.style.borderSystem
import com.frame.zero.shared.design_system.style.colorSystem
import com.frame.zero.shared.design_system.style.radiusSystem

private val Height = 48.dp
private val LoadingIndicatorSize = 36.dp

@OptIn(ExperimentalFoundationStyleApi::class)
private val FilledCtaStyle: Style =
  Style {
    height(Height)
    shape(RoundedCornerShape(radiusSystem.radius16))
    background(colorSystem.accent)
    disabled { background(colorSystem.accentDim) }
  }

@OptIn(ExperimentalFoundationStyleApi::class)
private fun outlinedCtaStyle(borderColor: Color): Style =
  Style {
    height(Height)
    shape(RoundedCornerShape(radiusSystem.radius16))
    background(Color.Transparent)
    border(borderSystem.hairline, borderColor)
  }

@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun CtaButton(
  text: String,
  modifier: Modifier = Modifier,
  loading: Boolean = false,
  onClick: () -> Unit
) {
  CtaButtonInternal(
    text = text,
    contentColor = AppTheme.colorSystem.textOnAccent,
    rippleColor = AppTheme.colorSystem.accentDim,
    style = FilledCtaStyle,
    loading = loading,
    modifier = modifier,
    onClick = onClick
  )
}

@OptIn(ExperimentalFoundationStyleApi::class)
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
    contentColor = contentColor,
    rippleColor = rippleColor,
    style = outlinedCtaStyle(borderColor),
    loading = loading,
    modifier = modifier,
    onClick = onClick
  )
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationStyleApi::class)
private fun CtaButtonInternal(
  text: String,
  contentColor: Color,
  rippleColor: Color,
  style: Style,
  loading: Boolean,
  modifier: Modifier,
  onClick: () -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val styleState = rememberUpdatedStyleState(interactionSource) { it.isEnabled = !loading }
  val shape = rememberRoundedCornerShape(AppTheme.radiusSystem.radius16)
  Box(
    modifier = modifier
      .styleable(styleState, style)
      .clip(shape)
      .clickableWithRipple(
        color = rippleColor,
        enabled = !loading,
        role = Role.Button,
        interactionSource = interactionSource,
        onClick = onClick
      )
      .semantics(mergeDescendants = true) {},
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
