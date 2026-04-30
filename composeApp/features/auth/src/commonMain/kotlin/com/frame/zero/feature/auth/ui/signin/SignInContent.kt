package com.frame.zero.feature.auth.ui.signin

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
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.CtaButton
import com.discovery.playground.shared.design_system.widgets.HorizontalSpacer
import com.discovery.playground.shared.design_system.widgets.SingleLineInputField
import com.discovery.playground.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.feature.auth.signin.SignInComponent
import com.frame.zero.feature.auth.signin.SignInIntent
import com.frame.zero.feature.auth.signin.SignInState
import framezero.composeapp.features.auth.generated.resources.Res
import framezero.composeapp.features.auth.generated.resources.ic_logo
import org.jetbrains.compose.resources.painterResource

@Composable
fun SignInContent(component: SignInComponent) {
  val state by component.state.collectAsState()
  SignInContent(
    state = state,
    onIntent = component::onIntent,
    onCreateAccountClick = component.onNavigateToRegister,
  )
}

@Composable
private fun SignInContent(
  state: SignInState,
  onIntent: (SignInIntent) -> Unit,
  onCreateAccountClick: () -> Unit,
) {
  Column(
    modifier =
      Modifier.fillMaxSize()
        .background(AppTheme.colorSystem.background)
        .padding(horizontal = AppTheme.spacingSystem.space16)
        .systemBarsPadding()
  ) {
    VerticalSpacer(AppTheme.spacingSystem.space24)
    Row(verticalAlignment = Alignment.CenterVertically) {
      Image(
        painter = painterResource(Res.drawable.ic_logo),
        colorFilter = ColorFilter.tint(AppTheme.colorSystem.accent),
        contentDescription = null,
      )
      HorizontalSpacer(AppTheme.spacingSystem.space8)
      Text(
        text = "FrameZero",
        color = AppTheme.colorSystem.textPrimary,
        style = AppTheme.typographySystem.displayLarge,
      )
    }
    VerticalSpacer(AppTheme.spacingSystem.space24)
    Text(
      modifier = Modifier.fillMaxWidth(),
      text = "Welcome back",
      color = AppTheme.colorSystem.textPrimary,
      style = AppTheme.typographySystem.displayMedium,
      textAlign = TextAlign.Center,
    )
    VerticalSpacer(AppTheme.spacingSystem.space8)
    Text(
      modifier = Modifier.fillMaxWidth(),
      text = "Sign in to your production workspace",
      color = AppTheme.colorSystem.textSecondary,
      style = AppTheme.typographySystem.bodyLarge,
      textAlign = TextAlign.Center,
    )
    VerticalSpacer(AppTheme.spacingSystem.space24)
    Text(
      text = "EMAIL",
      color = AppTheme.colorSystem.textPrimary,
      style = AppTheme.typographySystem.labelSmall,
    )
    VerticalSpacer(AppTheme.spacingSystem.space8)
    SingleLineInputField(
      value = state.email,
      onValueChange = { onIntent(SignInIntent.EmailChanged(it)) },
      placeholder = "you@studio.com",
      enabled = !state.isLoading,
      modifier = Modifier.fillMaxWidth(),
    )
    VerticalSpacer(AppTheme.spacingSystem.space16)
    Text(
      text = "PASSWORD",
      color = AppTheme.colorSystem.textPrimary,
      style = AppTheme.typographySystem.labelSmall,
    )
    VerticalSpacer(AppTheme.spacingSystem.space8)
    SingleLineInputField(
      value = state.password,
      onValueChange = { onIntent(SignInIntent.PasswordChanged(it)) },
      placeholder = "******",
      visualTransformation = PasswordVisualTransformation(),
      enabled = !state.isLoading,
      modifier = Modifier.fillMaxWidth(),
    )
    VerticalSpacer(AppTheme.spacingSystem.space8)
    Text(
      modifier = Modifier.fillMaxWidth(),
      text = "Forgot password?",
      color = AppTheme.colorSystem.accent,
      style = AppTheme.typographySystem.bodyMedium,
      textAlign = TextAlign.End,
    )
    VerticalSpacer(AppTheme.spacingSystem.space24)
    CtaButton(
      text = "Sign in",
      onClick = { onIntent(SignInIntent.Submit) },
      modifier = Modifier.fillMaxWidth(),
    )
    state.error?.let { error ->
      VerticalSpacer(AppTheme.spacingSystem.space8)
      Text(
        text = error,
        color = AppTheme.colorSystem.errorText,
        style = AppTheme.typographySystem.bodySmall,
      )
    }
    Spacer(modifier = Modifier.weight(1f))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
      Text(
        text = "New to FrameZero?",
        color = AppTheme.colorSystem.textSecondary,
        style = AppTheme.typographySystem.bodyMedium,
      )
      HorizontalSpacer(AppTheme.spacingSystem.space4)
      Text(
        modifier = Modifier.clickable(enabled = !state.isLoading) { onCreateAccountClick() },
        text = "Create account",
        color = AppTheme.colorSystem.accent,
        style = AppTheme.typographySystem.bodyMedium,
      )
    }
    VerticalSpacer(AppTheme.spacingSystem.space24)
  }
}

@Preview
@Composable
private fun SignInContentPreview() {
  AppTheme(darkTheme = true) {
    SignInContent(
      state = SignInState(email = "user@example.com", password = "secret"),
      onIntent = {},
      onCreateAccountClick = {},
    )
  }
}
