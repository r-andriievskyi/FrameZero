package com.frame.zero.feature.auth.ui.register.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import framezero.composeapp.features.auth.generated.resources.Res
import framezero.composeapp.features.auth.generated.resources.btn_sign_in
import framezero.composeapp.features.auth.generated.resources.register_have_account
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RegisterFooter(
  isLoading: Boolean,
  onSignInClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colorSystem = AppTheme.colorSystem
  val typographySystem = AppTheme.typographySystem
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Center
  ) {
    Text(
      text = stringResource(Res.string.register_have_account),
      color = colorSystem.textSecondary,
      style = typographySystem.bodyMedium
    )
    HorizontalSpacer(AppTheme.spacingSystem.space4)
    Text(
      modifier = Modifier.clickableWithRipple(
        color = colorSystem.accent,
        enabled = !isLoading,
        onClick = onSignInClick
      ),
      text = stringResource(Res.string.btn_sign_in),
      color = colorSystem.accent,
      style = typographySystem.bodyMedium
    )
  }
}

@LightDarkPreview
@Composable
private fun RegisterFooterPreview() {
  AppTheme { RegisterFooter(isLoading = false, onSignInClick = {}) }
}

