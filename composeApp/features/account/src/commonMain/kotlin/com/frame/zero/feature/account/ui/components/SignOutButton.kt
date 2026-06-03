package com.frame.zero.feature.account.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.OutlinedCtaButton
import framezero.composeapp.features.account.generated.resources.Res
import framezero.composeapp.features.account.generated.resources.sign_out
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SignOutButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  OutlinedCtaButton(
    text = stringResource(Res.string.sign_out),
    contentColor = AppTheme.colorSystem.errorText,
    rippleColor = AppTheme.colorSystem.errorSurface,
    modifier = modifier.fillMaxWidth(),
    onClick = onClick
  )
}

@LightDarkPreview
@Composable
private fun SignOutButtonPreview() {
  AppTheme {
    SignOutButton(onClick = {})
  }
}
