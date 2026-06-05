package com.frame.zero.feature.auth.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frame.zero.feature.auth.register.RegisterComponent
import com.frame.zero.feature.auth.register.RegisterIntent
import com.frame.zero.feature.auth.register.RegisterState
import com.frame.zero.feature.auth.ui.components.AuthLogoHeader
import com.frame.zero.feature.auth.ui.register.components.RegisterEmailField
import com.frame.zero.feature.auth.ui.register.components.RegisterFirstNameField
import com.frame.zero.feature.auth.ui.register.components.RegisterFooter
import com.frame.zero.feature.auth.ui.register.components.RegisterHeader
import com.frame.zero.feature.auth.ui.register.components.RegisterLastNameField
import com.frame.zero.feature.auth.ui.register.components.RegisterPasswordField
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.CtaButton
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.shared.design_system.widgets.toast.ToastHost
import com.frame.zero.ui.asString
import framezero.composeapp.features.auth.generated.resources.Res
import framezero.composeapp.features.auth.generated.resources.btn_create_account
import org.jetbrains.compose.resources.stringResource

@Composable
fun RegisterScreen(
  component: RegisterComponent,
  modifier: Modifier = Modifier
) {
  val state by component.state.collectAsStateWithLifecycle()
  Box(modifier = modifier.fillMaxSize()) {
    RegisterContent(
      state = state,
      onIntent = component::onIntent,
      onSignInClick = component.onNavigateToSignIn
    )
    ToastHost(
      message = state.errorToast?.asString(),
      onDismiss = { component.onIntent(RegisterIntent.ToastDismissed) }
    )
  }
}

@Composable
private fun RegisterContent(
  state: RegisterState,
  onIntent: (RegisterIntent) -> Unit,
  onSignInClick: () -> Unit
) {
  val spacingSystem = AppTheme.spacingSystem
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(AppTheme.colorSystem.background)
      .padding(horizontal = spacingSystem.space16)
      .systemBarsPadding()
  ) {
    VerticalSpacer(spacingSystem.space24)
    AuthLogoHeader()
    VerticalSpacer(spacingSystem.space24)
    RegisterHeader()
    VerticalSpacer(spacingSystem.space24)
    RegisterFirstNameField(
      value = state.firstName,
      onValueChange = { onIntent(RegisterIntent.FirstNameChanged(it)) },
      enabled = !state.isLoading
    )
    VerticalSpacer(spacingSystem.space16)
    RegisterLastNameField(
      value = state.lastName,
      onValueChange = { onIntent(RegisterIntent.LastNameChanged(it)) },
      enabled = !state.isLoading
    )
    VerticalSpacer(spacingSystem.space16)
    RegisterEmailField(
      value = state.email,
      onValueChange = { onIntent(RegisterIntent.EmailChanged(it)) },
      enabled = !state.isLoading
    )
    VerticalSpacer(spacingSystem.space16)
    RegisterPasswordField(
      value = state.password,
      onValueChange = { onIntent(RegisterIntent.PasswordChanged(it)) },
      enabled = !state.isLoading
    )
    VerticalSpacer(spacingSystem.space24)
    CtaButton(
      text = stringResource(Res.string.btn_create_account),
      loading = state.isLoading,
      onClick = { onIntent(RegisterIntent.Submit) },
      modifier = Modifier.fillMaxWidth()
    )
    state.error?.let { error ->
      VerticalSpacer(spacingSystem.space8)
      Text(
        text = error.asString(),
        color = AppTheme.colorSystem.errorText,
        style = AppTheme.typographySystem.bodySmall
      )
    }
    Spacer(modifier = Modifier.weight(1f))
    RegisterFooter(
      isLoading = state.isLoading,
      onSignInClick = onSignInClick
    )
    VerticalSpacer(spacingSystem.space24)
  }
}

@LightDarkPreview
@Composable
private fun RegisterContentPreview() {
  AppTheme { RegisterContent(state = RegisterState(), onIntent = {}, onSignInClick = {}) }
}
