package com.frame.zero.feature.gallery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.shared.design_system.widgets.rememberRoundedCornerShape

// Marker box is a visual element size (not spacing), so it is hoisted here per convention.
private val SpacingMarkerSize = 20.dp

@Composable
internal fun SpacingSection() {
  val spacingSystem = AppTheme.spacingSystem
  val tokens = listOf(
    "space2" to spacingSystem.space2,
    "space4" to spacingSystem.space4,
    "space8" to spacingSystem.space8,
    "space12" to spacingSystem.space12,
    "space16" to spacingSystem.space16,
    "space24" to spacingSystem.space24,
    "space32" to spacingSystem.space32
  )
  GallerySection(title = "Spacing") {
    Column {
      tokens.forEach { (name, token) ->
        SpacingBar(name = name, token = token)
        VerticalSpacer(spacingSystem.space8)
      }
    }
  }
}

@Composable
private fun SpacingBar(
  name: String,
  token: Dp
) {
  val colorSystem = AppTheme.colorSystem
  Row(verticalAlignment = Alignment.CenterVertically) {
    // The token drives the gap between the two markers, illustrating the spacing amount.
    Box(
      modifier = Modifier
        .size(width = SpacingMarkerSize, height = SpacingMarkerSize)
        .clip(rememberRoundedCornerShape(AppTheme.radiusSystem.radius4))
        .background(colorSystem.accent)
    )
    HorizontalSpacer(token)
    Box(
      modifier = Modifier
        .size(width = SpacingMarkerSize, height = SpacingMarkerSize)
        .clip(rememberRoundedCornerShape(AppTheme.radiusSystem.radius4))
        .background(colorSystem.accentDim)
    )
    HorizontalSpacer(AppTheme.spacingSystem.space16)
    Text(
      text = name,
      style = AppTheme.typographySystem.bodyMedium,
      color = colorSystem.textPrimary
    )
  }
}

@LightDarkPreview
@Composable
private fun SpacingSectionPreview() {
  AppTheme {
    Box(modifier = Modifier.background(AppTheme.colorSystem.background).padding(AppTheme.spacingSystem.space16)) {
      SpacingSection()
    }
  }
}
