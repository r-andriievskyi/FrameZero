package com.frame.zero.feature.account.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.rememberRoundedCornerShape

@Composable
internal fun SettingsSection(
  title: String,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  val colorSystem = AppTheme.colorSystem
  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = title,
      style = AppTheme.typographySystem.labelLarge,
      color = colorSystem.textMuted,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.padding(bottom = AppTheme.spacingSystem.space8)
    )
    val shape = rememberRoundedCornerShape(AppTheme.radiusSystem.radius16)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(shape)
        .background(colorSystem.cardBackground, shape)
        .border(AppTheme.borderSystem.hairline, colorSystem.border, shape)
    ) {
      content()
    }
  }
}

@LightDarkPreview
@Composable
private fun SettingsSectionPreview() {
  AppTheme {
    SettingsSection(title = "Account") {
    }
  }
}
