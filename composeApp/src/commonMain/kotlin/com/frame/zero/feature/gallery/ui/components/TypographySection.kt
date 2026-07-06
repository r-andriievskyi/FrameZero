package com.frame.zero.feature.gallery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.VerticalSpacer

@Composable
internal fun TypographySection() {
  val typographySystem = AppTheme.typographySystem
  val samples = listOf(
    "displayLarge" to typographySystem.displayLarge,
    "titleLarge" to typographySystem.titleLarge,
    "titleMedium" to typographySystem.titleMedium,
    "titleSmall" to typographySystem.titleSmall,
    "bodyLarge" to typographySystem.bodyLarge,
    "bodyMedium" to typographySystem.bodyMedium,
    "bodySmall" to typographySystem.bodySmall,
    "labelLarge" to typographySystem.labelLarge,
    "labelMedium" to typographySystem.labelMedium,
    "caption" to typographySystem.caption,
    "monoMedium" to typographySystem.monoMedium
  )
  GallerySection(title = "Typography") {
    Column {
      samples.forEach { (name, style) ->
        TypographySample(name = name, style = style)
        VerticalSpacer(AppTheme.spacingSystem.space8)
      }
    }
  }
}

@Composable
private fun TypographySample(
  name: String,
  style: TextStyle
) {
  Text(
    text = name,
    style = style,
    color = AppTheme.colorSystem.textPrimary
  )
}

@LightDarkPreview
@Composable
private fun TypographySectionPreview() {
  AppTheme {
    Box(modifier = Modifier.background(AppTheme.colorSystem.background).padding(AppTheme.spacingSystem.space16)) {
      TypographySection()
    }
  }
}
