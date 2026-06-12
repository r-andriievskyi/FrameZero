package com.frame.zero.shared.design_system.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.generated.resources.Res
import com.frame.zero.shared.design_system.generated.resources.action_retry
import com.frame.zero.shared.design_system.generated.resources.error_generic_message
import org.jetbrains.compose.resources.stringResource

/**
 * Centered error message with an optional retry button, filling its parent. Use as
 * the error branch of a screen whose content failed to load. Defaults read from the
 * design system's string catalog; pass [message]/[retryLabel] to override per feature.
 *
 * @param onRetry when non-null, renders a retry [CtaButton] beneath the message.
 */
@Composable
fun FullScreenError(
  modifier: Modifier = Modifier,
  message: String = stringResource(Res.string.error_generic_message),
  onRetry: (() -> Unit)? = null,
  retryLabel: String = stringResource(Res.string.action_retry)
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(AppTheme.spacingSystem.space16),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = message,
      style = AppTheme.typographySystem.bodyLarge,
      color = AppTheme.colorSystem.textSecondary,
      textAlign = TextAlign.Center
    )
    if (onRetry != null) {
      VerticalSpacer(AppTheme.spacingSystem.space16)
      CtaButton(
        modifier = Modifier.fillMaxWidth(0.5f),
        text = retryLabel,
        onClick = onRetry
      )
    }
  }
}

@LightDarkPreview
@Composable
private fun FullScreenErrorPreview() {
  AppTheme {
    Column(modifier = Modifier.fillMaxSize().background(AppTheme.colorSystem.background)) {
      FullScreenError(onRetry = {})
    }
  }
}
