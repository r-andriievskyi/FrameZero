package com.frame.zero.feature.auth.ui.signin

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
import com.frame.zero.feature.auth.signin.SignInComponent
import com.frame.zero.feature.auth.signin.SignInIntent
import com.frame.zero.feature.auth.signin.SignInState
import com.frame.zero.feature.auth.ui.components.AuthLogoHeader
import com.frame.zero.feature.auth.ui.signin.components.SignInEmailField
import com.frame.zero.feature.auth.ui.signin.components.SignInFooter
import com.frame.zero.feature.auth.ui.signin.components.SignInHeader
import com.frame.zero.feature.auth.ui.signin.components.SignInPasswordField
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.CtaButton
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.shared.design_system.widgets.toast.ToastHost
import framezero.composeapp.features.auth.generated.resources.Res
import framezero.composeapp.features.auth.generated.resources.btn_sign_in
import org.jetbrains.compose.resources.stringResource

@Composable
fun SignInScreen(component: SignInComponent, modifier: Modifier = Modifier) {
  val state by component.state.collectAsStateWithLifecycle()
  Box(modifier = modifier.fillMaxSize()) {
    SignInContent(
      state = state,
      onIntent = component::onIntent,
      onCreateAccountClick = component.onNavigateToRegister
    )
    ToastHost(
      message = state.errorToast,
      onDismiss = { component.onIntent(SignInIntent.ToastDismissed) }
    )
  }
}

@Composable
private fun SignInContent(
  state: SignInState,
  onIntent: (SignInIntent) -> Unit,
  onCreateAccountClick: () -> Unit
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
    SignInHeader()
    VerticalSpacer(spacingSystem.space24)
    SignInEmailField(
      value = state.email,
      onValueChange = { onIntent(SignInIntent.EmailChanged(it)) },
      enabled = !state.isLoading
    )
    VerticalSpacer(spacingSystem.space16)
    SignInPasswordField(
      value = state.password,
      onValueChange = { onIntent(SignInIntent.PasswordChanged(it)) },
      enabled = !state.isLoading
    )
    VerticalSpacer(spacingSystem.space24)
    CtaButton(
      text = stringResource(Res.string.btn_sign_in),
      loading = state.isLoading,
      onClick = { onIntent(SignInIntent.Submit) },
      modifier = Modifier.fillMaxWidth()
    )
    state.error?.let { error ->
      VerticalSpacer(spacingSystem.space8)
      Text(
        text = error,
        color = AppTheme.colorSystem.errorText,
        style = AppTheme.typographySystem.bodySmall
      )
    }
    Spacer(modifier = Modifier.weight(1f))
    SignInFooter(
      isLoading = state.isLoading,
      onCreateAccountClick = onCreateAccountClick
    )
    VerticalSpacer(spacingSystem.space24)
  }
}

@LightDarkPreview
@Composable
private fun SignInContentPreview() {
  AppTheme {
    SignInContent(
      state = SignInState(email = "user@example.com", password = "secret"),
      onIntent = {},
      onCreateAccountClick = {}
    )
  }
}
