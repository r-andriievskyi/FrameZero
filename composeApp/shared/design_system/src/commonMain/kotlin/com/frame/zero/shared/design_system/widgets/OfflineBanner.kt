package com.frame.zero.shared.design_system.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview

@Composable
fun OfflineBanner(
  message: String,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(AppTheme.spacingSystem.space16),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = message,
      style = AppTheme.typographySystem.bodyLarge,
      color = AppTheme.colorSystem.warningText,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .background(AppTheme.colorSystem.warningSurface, RoundedCornerShape(AppTheme.radiusSystem.radius14))
        .padding(AppTheme.spacingSystem.space16)
    )
  }
}

@LightDarkPreview
@Composable
private fun OfflineBannerPreview() {
  AppTheme {
    OfflineBanner(message = "No internet connection.\nWe'll refresh automatically once you're back online.")
  }
}
