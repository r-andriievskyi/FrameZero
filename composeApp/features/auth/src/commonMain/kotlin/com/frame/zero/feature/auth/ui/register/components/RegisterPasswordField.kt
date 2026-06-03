package com.frame.zero.feature.auth.ui.register.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.frame.zero.feature.auth.ui.components.LabeledField
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.SingleLineInputField
import framezero.composeapp.features.auth.generated.resources.Res
import framezero.composeapp.features.auth.generated.resources.field_password
import framezero.composeapp.features.auth.generated.resources.placeholder_password
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RegisterPasswordField(
  value: String,
  onValueChange: (String) -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier
) {
  LabeledField(
    label = stringResource(Res.string.field_password),
    modifier = modifier
  ) {
    SingleLineInputField(
      value = value,
      onValueChange = onValueChange,
      placeholder = stringResource(Res.string.placeholder_password),
      visualTransformation = PasswordVisualTransformation(),
      enabled = enabled,
      modifier = Modifier.fillMaxWidth()
    )
  }
}

@LightDarkPreview
@Composable
private fun RegisterPasswordFieldPreview() {
  AppTheme {
    RegisterPasswordField(value = "secret", onValueChange = {}, enabled = true)
  }
}
