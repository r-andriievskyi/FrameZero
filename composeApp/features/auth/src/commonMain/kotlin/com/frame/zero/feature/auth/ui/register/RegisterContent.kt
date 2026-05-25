package com.frame.zero.feature.auth.ui.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.widgets.CtaButton
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.SingleLineInputField
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.feature.auth.register.RegisterComponent
import com.frame.zero.feature.auth.register.RegisterIntent
import com.frame.zero.feature.auth.register.RegisterState
import framezero.composeapp.features.auth.generated.resources.Res
import framezero.composeapp.features.auth.generated.resources.app_name
import framezero.composeapp.features.auth.generated.resources.btn_create_account
import framezero.composeapp.features.auth.generated.resources.btn_sign_in
import framezero.composeapp.features.auth.generated.resources.field_email
import framezero.composeapp.features.auth.generated.resources.field_password
import framezero.composeapp.features.auth.generated.resources.ic_logo
import framezero.composeapp.features.auth.generated.resources.placeholder_email
import framezero.composeapp.features.auth.generated.resources.placeholder_password
import framezero.composeapp.features.auth.generated.resources.register_field_first_name
import framezero.composeapp.features.auth.generated.resources.register_field_last_name
import framezero.composeapp.features.auth.generated.resources.register_have_account
import framezero.composeapp.features.auth.generated.resources.register_placeholder_first_name
import framezero.composeapp.features.auth.generated.resources.register_placeholder_last_name
import framezero.composeapp.features.auth.generated.resources.register_subtitle
import framezero.composeapp.features.auth.generated.resources.register_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun RegisterContent(component: RegisterComponent) {
  val state by component.state.collectAsState()
  RegisterContent(
    state = state,
    onIntent = component::onIntent,
    onSignInClick = component.onNavigateToSignIn
  )
}

@Composable
private fun RegisterContent(
  state: RegisterState,
  onIntent: (RegisterIntent) -> Unit,
  onSignInClick: () -> Unit
) {
  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .background(AppTheme.colorSystem.background)
        .padding(horizontal = AppTheme.spacingSystem.space16)
        .systemBarsPadding()
  ) {
    VerticalSpacer(AppTheme.spacingSystem.space24)
    Row(verticalAlignment = Alignment.CenterVertically) {
      Image(
        painter = painterResource(Res.drawable.ic_logo),
        colorFilter = ColorFilter.tint(AppTheme.colorSystem.accent),
        contentDescription = null
      )
      HorizontalSpacer(AppTheme.spacingSystem.space8)
      Text(
        text = stringResource(Res.string.app_name),
        color = AppTheme.colorSystem.textPrimary,
        style = AppTheme.typographySystem.displayLarge
      )
    }
    VerticalSpacer(AppTheme.spacingSystem.space24)
    Text(
      modifier = Modifier.fillMaxWidth(),
      text = stringResource(Res.string.register_title),
      color = AppTheme.colorSystem.textPrimary,
      style = AppTheme.typographySystem.displayMedium,
      textAlign = TextAlign.Center
    )
    VerticalSpacer(AppTheme.spacingSystem.space8)
    Text(
      modifier = Modifier.fillMaxWidth(),
      text = stringResource(Res.string.register_subtitle),
      color = AppTheme.colorSystem.textSecondary,
      style = AppTheme.typographySystem.bodyLarge,
      textAlign = TextAlign.Center
    )
    VerticalSpacer(AppTheme.spacingSystem.space24)
    Text(
      text = stringResource(Res.string.register_field_first_name),
      color = AppTheme.colorSystem.textPrimary,
      style = AppTheme.typographySystem.labelSmall
    )
    VerticalSpacer(AppTheme.spacingSystem.space8)
    SingleLineInputField(
      value = state.firstName,
      onValueChange = { onIntent(RegisterIntent.FirstNameChanged(it)) },
      placeholder = stringResource(Res.string.register_placeholder_first_name),
      enabled = !state.isLoading,
      modifier = Modifier.fillMaxWidth()
    )
    VerticalSpacer(AppTheme.spacingSystem.space16)
    Text(
      text = stringResource(Res.string.register_field_last_name),
      color = AppTheme.colorSystem.textPrimary,
      style = AppTheme.typographySystem.labelSmall
    )
    VerticalSpacer(AppTheme.spacingSystem.space8)
    SingleLineInputField(
      value = state.lastName,
      onValueChange = { onIntent(RegisterIntent.LastNameChanged(it)) },
      placeholder = stringResource(Res.string.register_placeholder_last_name),
      enabled = !state.isLoading,
      modifier = Modifier.fillMaxWidth()
    )
    VerticalSpacer(AppTheme.spacingSystem.space16)
    Text(
      text = stringResource(Res.string.field_email),
      color = AppTheme.colorSystem.textPrimary,
      style = AppTheme.typographySystem.labelSmall
    )
    VerticalSpacer(AppTheme.spacingSystem.space8)
    SingleLineInputField(
      value = state.email,
      onValueChange = { onIntent(RegisterIntent.EmailChanged(it)) },
      placeholder = stringResource(Res.string.placeholder_email),
      enabled = !state.isLoading,
      modifier = Modifier.fillMaxWidth()
    )
    VerticalSpacer(AppTheme.spacingSystem.space16)
    Text(
      text = stringResource(Res.string.field_password),
      color = AppTheme.colorSystem.textPrimary,
      style = AppTheme.typographySystem.labelSmall
    )
    VerticalSpacer(AppTheme.spacingSystem.space8)
    SingleLineInputField(
      value = state.password,
      onValueChange = { onIntent(RegisterIntent.PasswordChanged(it)) },
      placeholder = stringResource(Res.string.placeholder_password),
      visualTransformation = PasswordVisualTransformation(),
      enabled = !state.isLoading,
      modifier = Modifier.fillMaxWidth()
    )
    VerticalSpacer(AppTheme.spacingSystem.space24)
    CtaButton(
      text = stringResource(Res.string.btn_create_account),
      onClick = { onIntent(RegisterIntent.Submit) },
      modifier = Modifier.fillMaxWidth()
    )
    state.error?.let { error ->
      VerticalSpacer(AppTheme.spacingSystem.space8)
      Text(
        text = error,
        color = AppTheme.colorSystem.errorText,
        style = AppTheme.typographySystem.bodySmall
      )
    }
    Spacer(modifier = Modifier.weight(1f))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
      Text(
        text = stringResource(Res.string.register_have_account),
        color = AppTheme.colorSystem.textSecondary,
        style = AppTheme.typographySystem.bodyMedium
      )
      HorizontalSpacer(AppTheme.spacingSystem.space4)
      Text(
        modifier = Modifier.clickable(enabled = !state.isLoading) { onSignInClick() },
        text = stringResource(Res.string.btn_sign_in),
        color = AppTheme.colorSystem.accent,
        style = AppTheme.typographySystem.bodyMedium
      )
    }
    VerticalSpacer(AppTheme.spacingSystem.space24)
  }
}

@Preview
@Composable
private fun RegisterContentPreview() {
  AppTheme { RegisterContent(state = RegisterState(), onIntent = {}, onSignInClick = {}) }
}
