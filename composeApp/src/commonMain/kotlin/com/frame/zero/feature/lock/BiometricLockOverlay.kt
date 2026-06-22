package com.frame.zero.feature.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.frame.zero.core.security.BiometricPromptText
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.CtaButton
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.generated.resources.Res
import framezero.composeapp.generated.resources.ic_lock
import framezero.composeapp.generated.resources.lock_prompt_cancel
import framezero.composeapp.generated.resources.lock_prompt_subtitle
import framezero.composeapp.generated.resources.lock_prompt_title
import framezero.composeapp.generated.resources.lock_sign_out_button
import framezero.composeapp.generated.resources.lock_subtitle
import framezero.composeapp.generated.resources.lock_title
import framezero.composeapp.generated.resources.lock_unlock_button
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val LockIconContainerSize = 88.dp
private val LockIconSize = 40.dp
private val ContentMaxWidthPadding = 32.dp

@Composable
internal fun BiometricLockOverlay(
  onUnlock: (BiometricPromptText) -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colorSystem = AppTheme.colorSystem
  val typographySystem = AppTheme.typographySystem
  val spacingSystem = AppTheme.spacingSystem

  val promptTitle = stringResource(Res.string.lock_prompt_title)
  val promptSubtitle = stringResource(Res.string.lock_prompt_subtitle)
  val promptCancel = stringResource(Res.string.lock_prompt_cancel)
  val promptText = remember(promptTitle, promptSubtitle, promptCancel) {
    BiometricPromptText(title = promptTitle, subtitle = promptSubtitle, negativeButton = promptCancel)
  }

  // present the prompt automatically the moment the overlay appears (cold start / resume).
  LaunchedEffect(Unit) { onUnlock(promptText) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(colorSystem.background)
      .pointerInput(Unit) {
        // no op
      },
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = ContentMaxWidthPadding),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(LockIconContainerSize)
          .clip(RoundedCornerShape(AppTheme.radiusSystem.radius16))
          .background(colorSystem.accentSurface),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          painter = painterResource(Res.drawable.ic_lock),
          contentDescription = null,
          tint = colorSystem.accent,
          modifier = Modifier.size(LockIconSize)
        )
      }
      VerticalSpacer(spacingSystem.space24)
      Text(
        text = stringResource(Res.string.lock_title),
        style = typographySystem.titleMedium,
        color = colorSystem.textPrimary,
        textAlign = TextAlign.Center
      )
      VerticalSpacer(spacingSystem.space8)
      Text(
        text = stringResource(Res.string.lock_subtitle),
        style = typographySystem.bodyMedium,
        color = colorSystem.textMuted,
        textAlign = TextAlign.Center
      )
      VerticalSpacer(spacingSystem.space32)
      CtaButton(
        text = stringResource(Res.string.lock_unlock_button),
        modifier = Modifier.fillMaxWidth(),
        onClick = { onUnlock(promptText) }
      )
      VerticalSpacer(spacingSystem.space16)
      Text(
        text = stringResource(Res.string.lock_sign_out_button),
        style = typographySystem.labelLarge,
        color = colorSystem.textMuted,
        modifier = Modifier
          .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
          .clickableWithRipple(color = colorSystem.accentDim, onClick = onSignOut)
          .padding(horizontal = spacingSystem.space16, vertical = spacingSystem.space8)
      )
    }
  }
}

@LightDarkPreview
@Composable
private fun BiometricLockOverlayPreview() {
  AppTheme {
    BiometricLockOverlay(onUnlock = {}, onSignOut = {})
  }
}
