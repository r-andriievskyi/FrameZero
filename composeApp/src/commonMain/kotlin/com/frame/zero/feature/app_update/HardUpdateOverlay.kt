package com.frame.zero.feature.app_update

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.CtaButton
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.generated.resources.Res
import framezero.composeapp.generated.resources.update_hard_action_button
import framezero.composeapp.generated.resources.update_hard_caption
import framezero.composeapp.generated.resources.update_hard_subtitle
import framezero.composeapp.generated.resources.update_hard_title
import framezero.composeapp.generated.resources.update_hard_warning
import org.jetbrains.compose.resources.stringResource

private val ContentHorizontalPadding = 32.dp
private val IconContainerSize = 72.dp
private val IconBadgeSize = 24.dp

/**
 * Full-screen, non-dismissable gate shown when the running build is below the minimum supported
 * one. Mirrors [com.frame.zero.feature.lock.BiometricLockOverlay]: an opaque cover that swallows
 * touches to whatever it hides, with a single call to action. The system back button is disabled
 * by `RootComponent` while this is up.
 *
 * @param message optional server-supplied copy; falls back to a localized default.
 */
@Composable
internal fun HardUpdateOverlay(
  message: String?,
  onUpdate: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colorSystem = AppTheme.colorSystem
  val typographySystem = AppTheme.typographySystem
  val spacingSystem = AppTheme.spacingSystem

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(colorSystem.background)
      .pointerInput(Unit) { /* swallow touches to the covered stack */ },
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = ContentHorizontalPadding),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(modifier = Modifier.size(IconContainerSize)) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(AppTheme.radiusSystem.radius16))
            .background(colorSystem.accentSurface)
        )
        Box(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(IconBadgeSize)
            .clip(CircleShape)
            .background(colorSystem.warningSurface),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "!",
            style = typographySystem.labelSmall,
            color = colorSystem.warningText
          )
        }
      }
      VerticalSpacer(spacingSystem.space24)
      Text(
        text = stringResource(Res.string.update_hard_title),
        style = typographySystem.displayMedium,
        color = colorSystem.textPrimary,
        textAlign = TextAlign.Center
      )
      VerticalSpacer(spacingSystem.space8)
      Text(
        text = message ?: stringResource(Res.string.update_hard_subtitle),
        style = typographySystem.bodyMedium,
        color = colorSystem.textMuted,
        textAlign = TextAlign.Center
      )
      VerticalSpacer(spacingSystem.space24)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(AppTheme.radiusSystem.radius14))
          .background(colorSystem.warningSurface)
          .padding(spacingSystem.space16),
        horizontalArrangement = Arrangement.spacedBy(spacingSystem.space12)
      ) {
        Text(
          text = "!",
          style = typographySystem.labelLarge,
          color = colorSystem.warningText
        )
        Text(
          text = stringResource(Res.string.update_hard_warning),
          style = typographySystem.bodySmall,
          color = colorSystem.warningText
        )
      }
      VerticalSpacer(spacingSystem.space32)
      CtaButton(
        text = stringResource(Res.string.update_hard_action_button),
        modifier = Modifier.fillMaxWidth(),
        onClick = onUpdate
      )
      VerticalSpacer(spacingSystem.space8)
      Text(
        text = stringResource(Res.string.update_hard_caption),
        style = typographySystem.caption,
        color = colorSystem.textMuted,
        textAlign = TextAlign.Center
      )
    }
  }
}

@LightDarkPreview
@Composable
private fun HardUpdateOverlayPreview() {
  AppTheme {
    HardUpdateOverlay(message = null, onUpdate = {})
  }
}
