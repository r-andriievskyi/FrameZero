package com.frame.zero.feature.auth.ui.signin.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import com.frame.zero.feature.auth.ui.components.LabeledField
import com.frame.zero.feature.auth.ui.signin.SignInTestTags
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.SingleLineInputField
import framezero.composeapp.features.auth.generated.resources.Res
import framezero.composeapp.features.auth.generated.resources.field_email
import framezero.composeapp.features.auth.generated.resources.ic_mail
import framezero.composeapp.features.auth.generated.resources.placeholder_email
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SignInEmailField(
  value: String,
  onValueChange: (String) -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier
) {
  val colorSystem = AppTheme.colorSystem
  LabeledField(
    label = stringResource(Res.string.field_email),
    modifier = modifier
  ) {
    SingleLineInputField(
      value = value,
      onValueChange = onValueChange,
      placeholder = stringResource(Res.string.placeholder_email),
      leadingContent = {
        Image(
          painter = painterResource(Res.drawable.ic_mail),
          colorFilter = ColorFilter.tint(colorSystem.textPrimary),
          contentDescription = null
        )
      },
      enabled = enabled,
      modifier = Modifier.fillMaxWidth().testTag(SignInTestTags.EMAIL)
    )
  }
}

@LightDarkPreview
@Composable
private fun SignInEmailFieldPreview() {
  AppTheme {
    SignInEmailField(value = "user@example.com", onValueChange = {}, enabled = true)
  }
}
