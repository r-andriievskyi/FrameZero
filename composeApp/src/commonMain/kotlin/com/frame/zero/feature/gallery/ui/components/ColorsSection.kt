package com.frame.zero.feature.gallery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.shared.design_system.widgets.rememberRoundedCornerShape

// Swatch box is a visual element size (not spacing), so it is hoisted here per convention.
private val SwatchSize = 44.dp

@Composable
internal fun ColorsSection() {
  val colorSystem = AppTheme.colorSystem
  val swatches = listOf(
    "background" to colorSystem.background,
    "surfaceElevated" to colorSystem.surfaceElevated,
    "cardBackground" to colorSystem.cardBackground,
    "accent" to colorSystem.accent,
    "accentDim" to colorSystem.accentDim,
    "textPrimary" to colorSystem.textPrimary,
    "textSecondary" to colorSystem.textSecondary,
    "textMuted" to colorSystem.textMuted,
    "successText" to colorSystem.successText,
    "warningText" to colorSystem.warningText,
    "errorText" to colorSystem.errorText,
    "border" to colorSystem.border
  )
  GallerySection(title = "Colors") {
    Column {
      swatches.forEach { (name, color) ->
        ColorSwatch(name = name, color = color)
        VerticalSpacer(AppTheme.spacingSystem.space8)
      }
    }
  }
}

@Composable
private fun ColorSwatch(
  name: String,
  color: Color
) {
  val colorSystem = AppTheme.colorSystem
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(SwatchSize)
        .clip(rememberRoundedCornerShape(AppTheme.radiusSystem.radius8))
        .background(color)
        .border(
          AppTheme.borderSystem.hairline,
          colorSystem.border,
          rememberRoundedCornerShape(AppTheme.radiusSystem.radius8)
        )
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
private fun ColorsSectionPreview() {
  AppTheme {
    Box(modifier = Modifier.background(AppTheme.colorSystem.background).padding(AppTheme.spacingSystem.space16)) {
      ColorsSection()
    }
  }
}
