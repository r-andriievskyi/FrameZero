package com.frame.zero.feature.gallery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.frame.zero.feature.gallery.DesignSystemGalleryComponent
import com.frame.zero.feature.gallery.ui.components.ButtonsSection
import com.frame.zero.feature.gallery.ui.components.ColorsSection
import com.frame.zero.feature.gallery.ui.components.InputSection
import com.frame.zero.feature.gallery.ui.components.RadiusSection
import com.frame.zero.feature.gallery.ui.components.SpacingSection
import com.frame.zero.feature.gallery.ui.components.ToastSection
import com.frame.zero.feature.gallery.ui.components.TypographySection
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.TopToolbar
import com.frame.zero.shared.design_system.widgets.VerticalSpacer

@Composable
fun DesignSystemGalleryScreen(
  component: DesignSystemGalleryComponent,
  modifier: Modifier = Modifier
) {
  DesignSystemGalleryContent(onBack = component.onBack, modifier = modifier)
}

@Composable
private fun DesignSystemGalleryContent(
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val spacingSystem = AppTheme.spacingSystem
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AppTheme.colorSystem.background)
      .systemBarsPadding()
  ) {
    TopToolbar(title = "Design System", onBack = onBack)
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = spacingSystem.space16)
    ) {
      VerticalSpacer(spacingSystem.space16)
      ColorsSection()
      VerticalSpacer(spacingSystem.space24)
      TypographySection()
      VerticalSpacer(spacingSystem.space24)
      SpacingSection()
      VerticalSpacer(spacingSystem.space24)
      RadiusSection()
      VerticalSpacer(spacingSystem.space24)
      ButtonsSection()
      VerticalSpacer(spacingSystem.space24)
      InputSection()
      VerticalSpacer(spacingSystem.space24)
      ToastSection()
      VerticalSpacer(spacingSystem.space32)
    }
  }
}

@LightDarkPreview
@Composable
private fun DesignSystemGalleryContentPreview() {
  AppTheme {
    DesignSystemGalleryContent(onBack = {})
  }
}
