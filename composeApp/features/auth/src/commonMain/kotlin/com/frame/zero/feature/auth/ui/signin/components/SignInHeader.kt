package com.frame.zero.feature.auth.ui.signin.components

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
import framezero.composeapp.features.auth.generated.resources.signin_subtitle
import framezero.composeapp.features.auth.generated.resources.signin_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SignInHeader(modifier: Modifier = Modifier) {
  val colorSystem = AppTheme.colorSystem
  val typographySystem = AppTheme.typographySystem
  Column(modifier = modifier) {
    Text(
      modifier = Modifier.fillMaxWidth(),
      text = stringResource(Res.string.signin_title),
      color = colorSystem.textPrimary,
      style = typographySystem.displayMedium,
      textAlign = TextAlign.Center
    )
    VerticalSpacer(AppTheme.spacingSystem.space8)
    Text(
      modifier = Modifier.fillMaxWidth(),
      text = stringResource(Res.string.signin_subtitle),
      color = colorSystem.textSecondary,
      style = typographySystem.bodyLarge,
      textAlign = TextAlign.Center
    )
  }
}

@LightDarkPreview
@Composable
private fun SignInHeaderPreview() {
  AppTheme { SignInHeader() }
}

