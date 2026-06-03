package com.frame.zero.feature.auth.ui.register.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.auth.generated.resources.Res
import framezero.composeapp.features.auth.generated.resources.register_subtitle
import framezero.composeapp.features.auth.generated.resources.register_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RegisterHeader(modifier: Modifier = Modifier) {
  val colorSystem = AppTheme.colorSystem
  val typographySystem = AppTheme.typographySystem
  Column(modifier = modifier) {
    Text(
      modifier = Modifier.fillMaxWidth(),
      text = stringResource(Res.string.register_title),
      color = colorSystem.textPrimary,
      style = typographySystem.displayMedium,
      textAlign = TextAlign.Center
    )
    VerticalSpacer(AppTheme.spacingSystem.space8)
    Text(
      modifier = Modifier.fillMaxWidth(),
      text = stringResource(Res.string.register_subtitle),
      color = colorSystem.textSecondary,
      style = typographySystem.bodyLarge,
      textAlign = TextAlign.Center
    )
  }
}

@LightDarkPreview
@Composable
private fun RegisterHeaderPreview() {
  AppTheme { RegisterHeader() }
}

