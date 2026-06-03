package com.frame.zero.feature.auth.ui.register.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.frame.zero.feature.auth.ui.components.LabeledField
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.SingleLineInputField
import framezero.composeapp.features.auth.generated.resources.Res
import framezero.composeapp.features.auth.generated.resources.register_field_last_name
import framezero.composeapp.features.auth.generated.resources.register_placeholder_last_name
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RegisterLastNameField(
  value: String,
  onValueChange: (String) -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier
) {
  LabeledField(
    label = stringResource(Res.string.register_field_last_name),
    modifier = modifier
  ) {
    SingleLineInputField(
      value = value,
      onValueChange = onValueChange,
      placeholder = stringResource(Res.string.register_placeholder_last_name),
      enabled = enabled,
      modifier = Modifier.fillMaxWidth()
    )
  }
}

@LightDarkPreview
@Composable
private fun RegisterLastNameFieldPreview() {
  AppTheme {
    RegisterLastNameField(value = "Doe", onValueChange = {}, enabled = true)
  }
}
