package com.frame.zero.feature.gallery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.CtaButton
import com.frame.zero.shared.design_system.widgets.OutlinedCtaButton
import com.frame.zero.shared.design_system.widgets.VerticalSpacer

@Composable
internal fun ButtonsSection() {
  val spacingSystem = AppTheme.spacingSystem
  val colorSystem = AppTheme.colorSystem
  GallerySection(title = "Buttons") {
    Column {
      CtaButton(modifier = Modifier.fillMaxWidth(), text = "Primary CTA", onClick = {})
      VerticalSpacer(spacingSystem.space12)
      CtaButton(modifier = Modifier.fillMaxWidth(), text = "Loading", loading = true, onClick = {})
      VerticalSpacer(spacingSystem.space12)
      OutlinedCtaButton(
        modifier = Modifier.fillMaxWidth(),
        text = "Outlined CTA",
        contentColor = colorSystem.accent,
        rippleColor = colorSystem.accentDim,
        onClick = {}
      )
    }
  }
}

@LightDarkPreview
@Composable
private fun ButtonsSectionPreview() {
  AppTheme {
    Box(modifier = Modifier.background(AppTheme.colorSystem.background).padding(AppTheme.spacingSystem.space16)) {
      ButtonsSection()
    }
  }
}
