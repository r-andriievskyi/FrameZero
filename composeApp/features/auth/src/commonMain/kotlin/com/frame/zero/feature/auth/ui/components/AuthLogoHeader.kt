package com.frame.zero.feature.auth.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import framezero.composeapp.features.auth.generated.resources.Res
import framezero.composeapp.features.auth.generated.resources.app_name
import framezero.composeapp.features.auth.generated.resources.ic_logo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AuthLogoHeader(modifier: Modifier = Modifier) {
  val colorSystem = AppTheme.colorSystem
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      painter = painterResource(Res.drawable.ic_logo),
      tint = colorSystem.accent,
      contentDescription = null
    )
    HorizontalSpacer(AppTheme.spacingSystem.space8)
    Text(
      text = stringResource(Res.string.app_name),
      color = colorSystem.textPrimary,
      style = AppTheme.typographySystem.displayLarge
    )
  }
}

@LightDarkPreview
@Composable
private fun AuthLogoHeaderPreview() {
  AppTheme { AuthLogoHeader() }
}
