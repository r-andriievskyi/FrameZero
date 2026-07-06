package com.frame.zero.feature.gallery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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

// Swatch box is a visual element size (not spacing), so it is hoisted here per convention.
private val RadiusSwatchSize = 56.dp

@Composable
internal fun RadiusSection() {
  val radiusSystem = AppTheme.radiusSystem
  val tokens = listOf(
    "radius4" to radiusSystem.radius4,
    "radius8" to radiusSystem.radius8,
    "radius14" to radiusSystem.radius14,
    "radius16" to radiusSystem.radius16,
    "radiusMax" to radiusSystem.radiusMax
  )
  GallerySection(title = "Radius") {
    Column {
      tokens.forEach { (name, token) ->
        RadiusSwatch(name = name, radius = token)
        VerticalSpacer(AppTheme.spacingSystem.space8)
      }
    }
  }
}

@Composable
private fun RadiusSwatch(
  name: String,
  radius: Dp
) {
  val colorSystem = AppTheme.colorSystem
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(RadiusSwatchSize)
        .clip(RoundedCornerShape(radius))
        .background(colorSystem.accentSurface)
        .border(AppTheme.borderSystem.hairline, colorSystem.accent, RoundedCornerShape(radius))
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
private fun RadiusSectionPreview() {
  AppTheme {
    Box(modifier = Modifier.background(AppTheme.colorSystem.background).padding(AppTheme.spacingSystem.space16)) {
      RadiusSection()
    }
  }
}
