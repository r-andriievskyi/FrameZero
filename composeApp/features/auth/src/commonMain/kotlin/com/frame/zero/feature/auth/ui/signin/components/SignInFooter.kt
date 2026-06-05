package com.frame.zero.feature.auth.ui.signin.components

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
import framezero.composeapp.features.auth.generated.resources.btn_create_account
import framezero.composeapp.features.auth.generated.resources.signin_new_user
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SignInFooter(
  isLoading: Boolean,
  onCreateAccountClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colorSystem = AppTheme.colorSystem
  val typographySystem = AppTheme.typographySystem
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Center
  ) {
    Text(
      text = stringResource(Res.string.signin_new_user),
      color = colorSystem.textSecondary,
      style = typographySystem.bodyMedium
    )
    HorizontalSpacer(AppTheme.spacingSystem.space4)
    Text(
      modifier = Modifier.clickableWithRipple(
        color = colorSystem.accent,
        enabled = !isLoading,
        onClick = onCreateAccountClick
      ),
      text = stringResource(Res.string.btn_create_account),
      color = colorSystem.accent,
      style = typographySystem.bodyMedium
    )
  }
}

@LightDarkPreview
@Composable
private fun SignInFooterPreview() {
  AppTheme { SignInFooter(isLoading = false, onCreateAccountClick = {}) }
}
