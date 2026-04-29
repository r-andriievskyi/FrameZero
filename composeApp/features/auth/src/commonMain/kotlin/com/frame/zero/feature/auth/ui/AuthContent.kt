package com.frame.zero.feature.auth.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.CtaButton
import com.discovery.playground.shared.design_system.widgets.SingleLineInputField
import com.discovery.playground.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.feature.auth.AuthComponent
import com.frame.zero.feature.auth.AuthIntent
import com.frame.zero.feature.auth.AuthMode
import com.frame.zero.feature.auth.AuthState

@Composable
fun AuthContent(component: AuthComponent) {
  val state by component.state.collectAsState()
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }

  AuthContent(
    state = state,
    email = email,
    password = password,
    onEmailChange = { email = it },
    onPasswordChange = { password = it },
    onIntent = component::onIntent,
  )
}

@Composable
private fun AuthContent(
  state: AuthState,
  email: String,
  password: String,
  onEmailChange: (String) -> Unit,
  onPasswordChange: (String) -> Unit,
  onIntent: (AuthIntent) -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxSize()
      .background(AppTheme.colorSystem.background)
      .padding(horizontal = AppTheme.spacingSystem.space16)
      .systemBarsPadding(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "FrameZero",
      color = AppTheme.colorSystem.textPrimary,
      style = AppTheme.typographySystem.displayMedium
    )
    VerticalSpacer(AppTheme.spacingSystem.space24)
    AnimatedContent(state.mode) { mode ->
      when (mode) {
        AuthMode.Login -> {
          Column(modifier = Modifier.fillMaxSize()) {
            Text(
              modifier = Modifier.fillMaxWidth(),
              text = "Welcome back",
              color = AppTheme.colorSystem.textPrimary,
              style = AppTheme.typographySystem.displayMedium,
              textAlign = TextAlign.Center
            )
            VerticalSpacer(AppTheme.spacingSystem.space8)
            Text(
              modifier = Modifier.fillMaxWidth(),
              text = "Sign in to your production workspace",
              color = AppTheme.colorSystem.textPrimary,
              style = AppTheme.typographySystem.bodyLarge,
              textAlign = TextAlign.Center
            )
            VerticalSpacer(AppTheme.spacingSystem.space24)
            Text(
              text = "EMAIL",
              color = AppTheme.colorSystem.textPrimary,
              style = AppTheme.typographySystem.labelSmall
            )
            VerticalSpacer(AppTheme.spacingSystem.space8)
            SingleLineInputField(
              value = email,
              onValueChange = onEmailChange,
              placeholder = "you@studio.com",
              enabled = !state.isLoading,
              modifier = Modifier.fillMaxWidth(),
            )
            VerticalSpacer(AppTheme.spacingSystem.space16)
            Text(
              text = "PASSWORD",
              color = AppTheme.colorSystem.textPrimary,
              style = AppTheme.typographySystem.labelSmall
            )
            VerticalSpacer(AppTheme.spacingSystem.space8)
            SingleLineInputField(
              value = password,
              onValueChange = onPasswordChange,
              placeholder = "******",
              visualTransformation = PasswordVisualTransformation(),
              enabled = !state.isLoading,
              modifier = Modifier.fillMaxWidth(),
            )
            VerticalSpacer(AppTheme.spacingSystem.space8)
            Text(
              modifier = Modifier.fillMaxWidth(),
              text = "Forgot password?",
              color = AppTheme.colorSystem.textPrimary,
              style = AppTheme.typographySystem.bodySmall,
              textAlign = TextAlign.End
            )
            VerticalSpacer(AppTheme.spacingSystem.space24)
            CtaButton(
              text = "Sign in",
              onClick = {
                val intent =
                  when (state.mode) {
                    AuthMode.Login -> AuthIntent.Login(email.trim(), password)
                    AuthMode.Register -> AuthIntent.Register(email.trim(), password)
                  }
                onIntent(intent)
              },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        AuthMode.Register -> {

        }
      }
    }
    /*val tabIndex = if (state.mode == AuthMode.Login) 0 else 1
    PrimaryTabRow(selectedTabIndex = tabIndex, modifier = Modifier.fillMaxWidth()) {
      Tab(
        selected = tabIndex == 0,
        onClick = { if (state.mode != AuthMode.Login) onIntent(AuthIntent.SwitchMode) },
        text = { Text("Log in") },
      )
      Tab(
        selected = tabIndex == 1,
        onClick = { if (state.mode != AuthMode.Register) onIntent(AuthIntent.SwitchMode) },
        text = { Text("Register") },
      )
    }*/

    OutlinedTextField(
      value = email,
      onValueChange = onEmailChange,
      label = { Text("Email") },
      singleLine = true,
      enabled = !state.isLoading,
      modifier = Modifier.fillMaxWidth(),
    )

    OutlinedTextField(
      value = password,
      onValueChange = onPasswordChange,
      label = { Text("Password") },
      singleLine = true,
      enabled = !state.isLoading,
      visualTransformation = PasswordVisualTransformation(),
      modifier = Modifier.fillMaxWidth(),
    )

    state.error?.let { error ->
      Text(
        text = error,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
      )
    }
  }
}

@Preview
@Composable
private fun AuthContentLoginPreview() {
  AppTheme(darkTheme = true) {
    AuthContent(
      state = AuthState(mode = AuthMode.Login),
      email = "user@example.com",
      password = "secret",
      onEmailChange = {},
      onPasswordChange = {},
      onIntent = {},
    )
  }
}

@Preview
@Composable
private fun AuthContentRegisterPreview() {
  AppTheme {
    AuthContent(
      state = AuthState(mode = AuthMode.Login),
      email = "",
      password = "",
      onEmailChange = {},
      onPasswordChange = {},
      onIntent = {},
    )
  }
}
