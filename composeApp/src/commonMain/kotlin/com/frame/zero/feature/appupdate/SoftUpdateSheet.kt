package com.frame.zero.feature.appupdate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.CtaButton
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.generated.resources.Res
import framezero.composeapp.generated.resources.update_action_button
import framezero.composeapp.generated.resources.update_later_button
import framezero.composeapp.generated.resources.update_soft_subtitle
import framezero.composeapp.generated.resources.update_soft_title
import org.jetbrains.compose.resources.stringResource

private const val ScrimAlpha = 0.45f
private val CardHorizontalPadding = 24.dp
private val CardVerticalPadding = 24.dp

/**
 * Dismissable bottom sheet shown when a newer build is available but the current one still works.
 * A dimmed scrim (blocking touches to the covered stack) with a bottom card: update now, or defer
 * for this process ([onDismiss]). The system back button stays active — the user can leave the
 * prompt up.
 *
 * @param message optional server-supplied copy; falls back to a localized default.
 */
@Composable
internal fun SoftUpdateSheet(
  message: String?,
  onUpdate: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colorSystem = AppTheme.colorSystem
  val typographySystem = AppTheme.typographySystem
  val spacingSystem = AppTheme.spacingSystem
  val cardShape = RoundedCornerShape(
    topStart = AppTheme.radiusSystem.radius16,
    topEnd = AppTheme.radiusSystem.radius16
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(colorSystem.textPrimary.copy(alpha = ScrimAlpha))
      .pointerInput(Unit) { /* swallow touches to the covered stack */ },
    contentAlignment = Alignment.BottomCenter
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(cardShape)
        .background(colorSystem.cardBackground)
        .padding(horizontal = CardHorizontalPadding, vertical = CardVerticalPadding),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = stringResource(Res.string.update_soft_title),
        style = typographySystem.titleMedium,
        color = colorSystem.textPrimary,
        textAlign = TextAlign.Center
      )
      VerticalSpacer(spacingSystem.space8)
      Text(
        text = message ?: stringResource(Res.string.update_soft_subtitle),
        style = typographySystem.bodyMedium,
        color = colorSystem.textMuted,
        textAlign = TextAlign.Center
      )
      VerticalSpacer(spacingSystem.space24)
      CtaButton(
        text = stringResource(Res.string.update_action_button),
        modifier = Modifier.fillMaxWidth(),
        onClick = onUpdate
      )
      VerticalSpacer(spacingSystem.space8)
      Text(
        text = stringResource(Res.string.update_later_button),
        style = typographySystem.labelLarge,
        color = colorSystem.textMuted,
        modifier = Modifier
          .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
          .clickableWithRipple(color = colorSystem.accentDim, onClick = onDismiss)
          .padding(horizontal = spacingSystem.space16, vertical = spacingSystem.space8)
      )
    }
  }
}

@LightDarkPreview
@Composable
private fun SoftUpdateSheetPreview() {
  AppTheme {
    SoftUpdateSheet(message = null, onUpdate = {}, onDismiss = {})
  }
}
