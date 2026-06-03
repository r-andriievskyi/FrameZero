package com.frame.zero.feature.auth.ui.signin.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.frame.zero.feature.auth.ui.components.LabeledField
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.SingleLineInputField
import framezero.composeapp.features.auth.generated.resources.Res
import framezero.composeapp.features.auth.generated.resources.field_password
import framezero.composeapp.features.auth.generated.resources.ic_eye
import framezero.composeapp.features.auth.generated.resources.ic_eye_off
import framezero.composeapp.features.auth.generated.resources.ic_lock
import framezero.composeapp.features.auth.generated.resources.placeholder_password
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SignInPasswordField(
  value: String,
  onValueChange: (String) -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier
) {
  val colorSystem = AppTheme.colorSystem
  var isPasswordVisible by remember { mutableStateOf(false) }
  LabeledField(
    label = stringResource(Res.string.field_password),
    modifier = modifier
  ) {
    SingleLineInputField(
      value = value,
      onValueChange = onValueChange,
      placeholder = stringResource(Res.string.placeholder_password),
      leadingContent = {
        Icon(
          painter = painterResource(Res.drawable.ic_lock),
          tint = colorSystem.textPrimary,
          contentDescription = null
        )
      },
      trailingContent = {
        Icon(
          modifier = Modifier.clickableWithRipple(
            color = colorSystem.textPrimary
          ) { isPasswordVisible = !isPasswordVisible },
          painter = painterResource(
            if (isPasswordVisible) Res.drawable.ic_eye_off else Res.drawable.ic_eye
          ),
          tint = colorSystem.textPrimary,
          contentDescription = null
        )
      },
      visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
      enabled = enabled,
      modifier = Modifier.fillMaxWidth()
    )
  }
}

@LightDarkPreview
@Composable
private fun SignInPasswordFieldPreview() {
  AppTheme {
    SignInPasswordField(value = "secret", onValueChange = {}, enabled = true)
  }
}
